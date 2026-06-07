package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.BadCompanyClient;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.ColorSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import com.badcompany.modmenu.settings.StringSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AdvancedSearchModule extends Module {
    private static final int MAX_RANGE = 48;
    private static final int DEFAULT_SCAN_BUDGET = 4096;
    private static final int MAX_CACHED_TARGETS = 1024;
    private static final String DEFAULT_BLOCKS = "minecraft:diamond_ore,minecraft:deepslate_diamond_ore,minecraft:ancient_debris";

    private final StringSetting enabledBlockList = addSetting(new StringSetting(
            "Enabled block list",
            "Comma separated block ids, optionally with state filters like minecraft:oak_log[axis=y].",
            DEFAULT_BLOCKS
    ));
    private final ColorSetting highlightColor = addSetting(new ColorSetting(
            "Highlight color",
            "ARGB color saved for cached search-target rendering.",
            0xFF55FFFF
    ));
    private final BooleanSetting showTracers = addSetting(new BooleanSetting(
            "Show tracers",
            "Keep matching targets available to tracer-capable render integrations.",
            false
    ));
    private final BooleanSetting showBoxes = addSetting(new BooleanSetting(
            "Show boxes",
            "Keep matching targets available for box highlighting integrations.",
            true
    ));
    private final NumberSetting rangeLimit = addSetting(new NumberSetting(
            "Range limit",
            "Maximum client-side search radius in blocks. Values are clamped for FPS safety.",
            24.0D,
            4.0D,
            MAX_RANGE
    ));

    private final List<CachedTarget> cachedTargets = new ArrayList<>();
    private final List<BlockPos> scanPositions = new ArrayList<>();
    private List<BlockFilter> filters = List.of();
    private String lastFilterText = "";
    private int lastRange = -1;
    private int cursor;
    private int rescanCooldownTicks;

    public AdvancedSearchModule() {
        super("Advanced Search", "Safely searches nearby loaded blocks using configurable block and state filters.", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        rebuildFiltersIfNeeded(true);
        rebuildScanPositionsIfNeeded(true);
        rescanCooldownTicks = 0;
        BadCompanyClient.LOGGER.info("Advanced Search enabled with {} block filter(s)", filters.size());
    }

    @Override
    protected void onDisable() {
        cachedTargets.clear();
        scanPositions.clear();
        cursor = 0;
    }

    @Override
    public void tick() {
        if (client.world == null || client.player == null) {
            cachedTargets.clear();
            return;
        }

        boolean filtersChanged = rebuildFiltersIfNeeded(false);
        boolean rangeChanged = rebuildScanPositionsIfNeeded(filtersChanged);
        if (filtersChanged || rangeChanged) {
            cachedTargets.clear();
            cursor = 0;
        }

        if (filters.isEmpty() || scanPositions.isEmpty()) {
            cachedTargets.clear();
            return;
        }

        if (rescanCooldownTicks > 0) {
            rescanCooldownTicks--;
            return;
        }

        scanBudgeted(client.world, client.player.getBlockPos());
    }

    public List<CachedTarget> cachedTargets() {
        return Collections.unmodifiableList(cachedTargets);
    }

    public int highlightColor() {
        return highlightColor.get();
    }

    public boolean showTracers() {
        return showTracers.get();
    }

    public boolean showBoxes() {
        return showBoxes.get();
    }

    private boolean rebuildFiltersIfNeeded(boolean force) {
        String filterText = enabledBlockList.get();
        if (!force && filterText.equals(lastFilterText)) return false;
        lastFilterText = filterText;
        filters = parseFilters(filterText);
        return true;
    }

    private boolean rebuildScanPositionsIfNeeded(boolean force) {
        int range = (int) Math.round(rangeLimit.get());
        range = Math.max(4, Math.min(MAX_RANGE, range));
        if (!force && range == lastRange) return false;
        lastRange = range;
        scanPositions.clear();
        int rangeSquared = range * range;
        for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
                for (int x = -range; x <= range; x++) {
                    int distSquared = x * x + y * y + z * z;
                    if (distSquared <= rangeSquared) {
                        scanPositions.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        scanPositions.sort((a, b) -> Integer.compare(distanceSquared(a), distanceSquared(b)));
        cursor = 0;
        return true;
    }

    private static int distanceSquared(BlockPos pos) {
        return pos.getX() * pos.getX() + pos.getY() * pos.getY() + pos.getZ() * pos.getZ();
    }

    private void scanBudgeted(ClientWorld world, BlockPos origin) {
        if (cursor == 0) cachedTargets.clear();

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int scanned = 0;
        while (scanned < DEFAULT_SCAN_BUDGET && cursor < scanPositions.size()) {
            BlockPos offset = scanPositions.get(cursor++);
            mutable.set(origin.getX() + offset.getX(), origin.getY() + offset.getY(), origin.getZ() + offset.getZ());
            BlockState state = world.getBlockState(mutable);
            if (matchesAny(state)) {
                cachedTargets.add(new CachedTarget(mutable.toImmutable(), Registries.BLOCK.getId(state.getBlock()).toString(), stateToString(state)));
                if (cachedTargets.size() >= MAX_CACHED_TARGETS) break;
            }
            scanned++;
        }

        if (cursor >= scanPositions.size() || cachedTargets.size() >= MAX_CACHED_TARGETS) {
            cursor = 0;
            rescanCooldownTicks = 10;
        }
    }

    private boolean matchesAny(BlockState state) {
        for (BlockFilter filter : filters) {
            if (filter.matches(state)) return true;
        }
        return false;
    }

    private static List<BlockFilter> parseFilters(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<BlockFilter> parsed = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String rawToken : text.split(",")) {
            String token = rawToken.trim();
            if (token.isEmpty() || !seen.add(token)) continue;
            try {
                parseFilter(token).ifPresent(parsed::add);
            } catch (RuntimeException ex) {
                BadCompanyClient.LOGGER.warn("Ignoring invalid Advanced Search block filter '{}'", token, ex);
            }
        }
        return List.copyOf(parsed);
    }

    private static Optional<BlockFilter> parseFilter(String token) {
        String idText = token;
        Map<String, String> stateFilters = Map.of();
        int stateStart = token.indexOf('[');
        if (stateStart >= 0 && token.endsWith("]")) {
            idText = token.substring(0, stateStart).trim();
            stateFilters = parseStateFilters(token.substring(stateStart + 1, token.length() - 1));
        }
        Identifier id = Identifier.of(idText);
        Optional<Block> block = Registries.BLOCK.getOptionalValue(id);
        final Map<String, String> finalStateFilters = stateFilters;
        return block.map(value -> new BlockFilter(value, finalStateFilters));
    }

    private static Map<String, String> parseStateFilters(String text) {
        if (text.isBlank()) return Map.of();
        Map<String, String> out = new HashMap<>();
        for (String rawPair : text.split(";|,")) {
            String[] pair = rawPair.trim().split("=", 2);
            if (pair.length == 2 && !pair[0].isBlank() && !pair[1].isBlank()) {
                out.put(pair[0].trim().toLowerCase(Locale.ROOT), pair[1].trim().toLowerCase(Locale.ROOT));
            }
        }
        return Map.copyOf(out);
    }

    private static String stateToString(BlockState state) {
        if (state.getProperties().isEmpty()) return "";
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (Property<?> property : state.getProperties()) {
            if (!first) out.append(',');
            first = false;
            out.append(property.getName()).append('=').append(valueName(state, property));
        }
        return out.append(']').toString();
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }

    public record CachedTarget(BlockPos pos, String blockId, String stateDescription) { }

    private record BlockFilter(Block block, Map<String, String> states) {
        boolean matches(BlockState state) {
            if (!state.isOf(block)) return false;
            if (states.isEmpty()) return true;
            for (Map.Entry<String, String> entry : states.entrySet()) {
                if (!matchesProperty(state, entry.getKey(), entry.getValue())) return false;
            }
            return true;
        }

        private static boolean matchesProperty(BlockState state, String name, String expected) {
            for (Property<?> property : state.getProperties()) {
                if (property.getName().equals(name)) {
                    return valueNameUnchecked(state, property).equals(expected);
                }
            }
            return false;
        }

        private static <T extends Comparable<T>> String valueNameUnchecked(BlockState state, Property<T> property) {
            return property.name(state.get(property)).toLowerCase(Locale.ROOT);
        }
    }
}
