package com.badcompany.modmenu.hunting;

import com.badcompany.modmenu.BadCompanyClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/** Persistent local intelligence store used by every Hunting module. */
public final class HuntDatabase {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HuntDatabase INSTANCE = new HuntDatabase();
    private static final int SAVE_INTERVAL_TICKS = 200;

    private final Path directory = FabricLoader.getInstance().getConfigDir().resolve("badcompany").resolve("hunting");
    private final Path databaseFile = directory.resolve("hunt-database.json");
    private final Set<String> seenKeys = new HashSet<>();
    private Data data = new Data();
    private int dirtyTicks = -1;

    private HuntDatabase() { loadSafely(); }
    public static HuntDatabase get() { return INSTANCE; }
    public synchronized Data data() { return data; }
    public synchronized Path databaseFile() { return databaseFile; }

    public synchronized void tickAutosave() {
        if (dirtyTicks < 0) return;
        dirtyTicks++;
        if (dirtyTicks >= SAVE_INTERVAL_TICKS) saveSafely();
    }

    public synchronized void loadSafely() {
        try {
            Files.createDirectories(directory);
            if (Files.exists(databaseFile)) {
                try (Reader reader = Files.newBufferedReader(databaseFile)) {
                    data = sanitize(GSON.fromJson(reader, Data.class));
                }
            } else {
                data = new Data();
            }
            rebuildSeenKeys();
            dirtyTicks = -1;
        } catch (IOException | RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Unable to load hunt database from {}; starting empty", databaseFile, ex);
            data = new Data();
            seenKeys.clear();
        }
    }

    public synchronized void saveSafely() {
        try {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(databaseFile)) { GSON.toJson(data, writer); }
            dirtyTicks = -1;
        } catch (IOException | RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Unable to save hunt database to {}", databaseFile, ex);
        }
    }

    public synchronized Path exportSafely(String name) {
        Path target = directory.resolve(name == null || name.isBlank() ? "hunt-export.json" : name);
        try {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(target)) { GSON.toJson(data, writer); }
            return target;
        } catch (IOException | RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Unable to export hunt database to {}", target, ex);
            return databaseFile;
        }
    }

    public synchronized void importSafely(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            merge(sanitize(GSON.fromJson(reader, Data.class)));
            markDirty();
        } catch (IOException | RuntimeException ex) {
            BadCompanyClient.LOGGER.warn("Unable to import hunt database from {}", source, ex);
        }
    }

    public synchronized List<String> search(String query, int limit) {
        String normalized = String.valueOf(query).toLowerCase(Locale.ROOT).trim();
        if (normalized.startsWith("near:")) {
            String[] parts = normalized.substring("near:".length()).split(",");
            if (parts.length >= 2) {
                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double z = Double.parseDouble(parts[1].trim());
                    double radius = parts.length >= 3 ? Double.parseDouble(parts[2].trim()) : 2048.0D;
                    double radiusSquared = radius * radius;
                    return allRows().filter(row -> rowDistanceSquared(row, x, z) <= radiusSquared).limit(Math.max(1, limit)).toList();
                } catch (NumberFormatException ignored) { }
            }
        }
        if (normalized.startsWith("dimension:nether")) normalized = "dimension:minecraft:the_nether";
        if (normalized.startsWith("dimension:overworld")) normalized = "dimension:minecraft:overworld";
        String finalNormalized = normalized;
        return allRows().filter(row -> row.toLowerCase(Locale.ROOT).contains(finalNormalized)).limit(Math.max(1, limit)).toList();
    }

    public void recordTrail(BlockPos pos, String dimension, String blockId) { add("trail:" + key(pos, dimension, blockId), data.trails, new TrailRecord(pos, dimension, now(), blockId)); }
    public void recordStash(BlockPos pos, String dimension, int score, String label) { add("stash:" + key(pos, dimension, label), data.stashes, new StashRecord(pos, dimension, now(), score, label, Math.min(100, score))); }
    public void recordBase(BlockPos pos, String dimension, int confidence, int radius, String reason) { add("base:" + key(pos, dimension, reason), data.bases, new BaseRecord(pos, dimension, now(), confidence, radius, reason)); }
    public void recordPlayer(String name, UUID uuid, Vec3d pos, String dimension, double directionDegrees, double speed) { add("player:" + uuid + ':' + ((int) pos.x / 16) + ':' + ((int) pos.z / 16) + ':' + (System.currentTimeMillis() / 30000L), data.players, new PlayerRecord(name, uuid == null ? "unknown" : uuid.toString(), pos, dimension, now(), directionDegrees, speed)); }
    public void recordPortal(BlockPos pos, String dimension, double highwayDistance) { add("portal:" + key(pos, dimension, "portal"), data.portals, new PortalRecord(pos, dimension, now(), highwayDistance)); }
    public void recordSign(BlockPos pos, String dimension, String text) { add("sign:" + key(pos, dimension, text), data.signs, new SignRecord(pos, dimension, now(), text)); }
    public void recordBook(BlockPos pos, String dimension, ItemStack stack, String contents) { String item = Registries.ITEM.getId(stack.getItem()).toString(); add("book:" + key(pos, dimension, stack.getName().getString() + contents), data.books, new BookRecord(pos, dimension, now(), stack.getName().getString(), item, contents)); }
    public void recordChunkChange(String dimension, int chunkX, int chunkZ, String changeType, BlockPos pos, String blockId) { add("change:" + dimension + ':' + chunkX + ':' + chunkZ + ':' + changeType + ':' + blockId + ':' + pos.toShortString(), data.chunkChanges, new ChunkChangeRecord(dimension, chunkX, chunkZ, now(), changeType, pos, blockId)); }
    public void recordExpedition(BlockPos pos, String dimension, String reason, boolean important) { add("expedition:" + key(pos, dimension, reason), data.expeditions, new ExpeditionRecord(pos, dimension, now(), reason, false, important, "")); }
    public void recordRegion(String dimension, int regionX, int regionZ, int portals, int players, int trails, int storage, int baseProbability) { add("region:" + dimension + ':' + regionX + ':' + regionZ + ':' + (System.currentTimeMillis() / 300000L), data.regions, new RegionRecord(dimension, regionX, regionZ, now(), portals, players, trails, storage, baseProbability)); }
    public void recordNote(BlockPos pos, String dimension, String note) { add("note:" + key(pos, dimension, note), data.notes, new NoteRecord(pos, dimension, now(), note)); }
    public void recordFlightPoint(Vec3d pos, String dimension, double speed, int rocketsUsed) { add("flight:" + dimension + ':' + ((int) pos.x / 16) + ':' + ((int) pos.z / 16) + ':' + (System.currentTimeMillis() / 10000L), data.flightPoints, new FlightPointRecord(pos, dimension, now(), speed, rocketsUsed)); }
    public void recordEvidence(BlockPos pos, String dimension, String eventType, String details) { add("evidence:" + key(pos, dimension, eventType + details), data.evidence, new EvidenceRecord(pos, dimension, now(), eventType, details)); }
    public void incrementStatistic(String name, double amount) { synchronized (this) { StatisticRecord record = data.statistics.stream().filter(stat -> stat.name.equals(name)).findFirst().orElse(null); if (record == null) data.statistics.add(new StatisticRecord(name, amount)); else record.value += amount; markDirty(); } }

    public synchronized void recordChunkSnapshot(String dimension, int chunkX, int chunkZ, String hash) {
        String key = dimension + ':' + chunkX + ':' + chunkZ;
        ChunkSnapshot existing = data.chunkSnapshots.stream().filter(snapshot -> snapshot.key.equals(key)).findFirst().orElse(null);
        if (existing == null) { data.chunkSnapshots.add(new ChunkSnapshot(key, dimension, chunkX, chunkZ, now(), hash)); markDirty(); }
        else if (!existing.hash.equals(hash)) { existing.timestamp = now(); existing.hash = hash; markDirty(); }
    }

    public synchronized String chunkHash(String dimension, int chunkX, int chunkZ) {
        String key = dimension + ':' + chunkX + ':' + chunkZ;
        return data.chunkSnapshots.stream().filter(snapshot -> snapshot.key.equals(key)).map(snapshot -> snapshot.hash).findFirst().orElse(null);
    }

    public synchronized DashboardSummary dashboardSummary() {
        int confidence = data.bases.stream().mapToInt(BaseRecord::confidence).max().orElse(0);
        String prediction = data.bases.stream().max(Comparator.comparingInt(BaseRecord::confidence)).map(base -> base.reason() + " @ " + base.x() + "," + base.z()).orElse("No correlated base prediction yet");
        return new DashboardSummary(data.expeditions.size(), data.players.size(), data.portals.size(), data.trails.size(), data.stashes.size(), data.bases.size(), data.signs.size(), data.books.size(), data.chunkChanges.size(), confidence, prediction);
    }

    private synchronized <T> void add(String key, List<T> list, T value) { if (!seenKeys.add(key)) return; list.add(value); markDirty(); }

    private void merge(Data other) {
        other.players.forEach(row -> add("merge:player:" + row.timestamp + row.uuid + row.x + row.z, data.players, row));
        other.portals.forEach(row -> add("merge:portal:" + row.timestamp + row.x + row.z, data.portals, row));
        other.signs.forEach(row -> add("merge:sign:" + row.timestamp + row.text + row.x + row.z, data.signs, row));
        other.books.forEach(row -> add("merge:book:" + row.timestamp + row.title + row.x + row.z, data.books, row));
        other.trails.forEach(row -> add("merge:trail:" + row.timestamp + row.blockId + row.x + row.z, data.trails, row));
        other.stashes.forEach(row -> add("merge:stash:" + row.timestamp + row.label + row.x + row.z, data.stashes, row));
        other.bases.forEach(row -> add("merge:base:" + row.timestamp + row.reason + row.x + row.z, data.bases, row));
        other.chunkChanges.forEach(row -> add("merge:change:" + row.timestamp + row.changeType + row.x + row.z, data.chunkChanges, row));
        other.chunkSnapshots.forEach(row -> add("merge:snapshot:" + row.key, data.chunkSnapshots, row));
        other.expeditions.forEach(row -> add("merge:expedition:" + row.timestamp + row.reason + row.x + row.z, data.expeditions, row));
        other.regions.forEach(row -> add("merge:region:" + row.timestamp + row.dimension + row.regionX + row.regionZ, data.regions, row));
        other.notes.forEach(row -> add("merge:note:" + row.timestamp + row.note + row.x + row.z, data.notes, row));
        other.flightPoints.forEach(row -> add("merge:flight:" + row.timestamp + row.x + row.z, data.flightPoints, row));
        other.evidence.forEach(row -> add("merge:evidence:" + row.timestamp + row.eventType + row.x + row.z, data.evidence, row));
        for (StatisticRecord stat : other.statistics) incrementStatistic(stat.name, stat.value);
    }

    private Stream<String> allRows() {
        return Stream.of(
                data.players.stream().map(row -> "player:" + row.name + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.portals.stream().map(row -> "portal:true dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.signs.stream().map(row -> "sign:" + row.text + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.books.stream().map(row -> "book:" + row.title + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z + " " + row.contents),
                data.trails.stream().map(row -> "trail:" + row.blockId + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.stashes.stream().map(row -> "stash:" + row.label + " confidence:" + row.confidence + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.bases.stream().map(row -> "base:" + row.reason + " confidence:" + row.confidence + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.chunkChanges.stream().map(row -> "chunk_change:" + row.changeType + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.expeditions.stream().map(row -> "expedition:" + row.reason + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.notes.stream().map(row -> "note:" + row.note + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z),
                data.evidence.stream().map(row -> "evidence:" + row.eventType + " " + row.details + " dimension:" + row.dimension + " pos:" + row.x + ',' + row.y + ',' + row.z)
        ).flatMap(Function.identity()).sorted(Comparator.naturalOrder());
    }

    private static double rowDistanceSquared(String row, double x, double z) {
        int posIndex = row.indexOf("pos:");
        if (posIndex < 0) return Double.MAX_VALUE;
        String[] xyz = row.substring(posIndex + 4).split("[ ,]");
        if (xyz.length < 3) return Double.MAX_VALUE;
        try { double dx = Double.parseDouble(xyz[0]) - x; double dz = Double.parseDouble(xyz[2]) - z; return dx * dx + dz * dz; }
        catch (NumberFormatException ex) { return Double.MAX_VALUE; }
    }

    private void rebuildSeenKeys() { seenKeys.clear(); data.trails.forEach(row -> seenKeys.add("loaded:trail:" + row.timestamp + row.x + row.y + row.z)); }
    private void markDirty() { dirtyTicks = Math.max(0, dirtyTicks); }
    private static String now() { return Instant.now().toString(); }
    private static String key(BlockPos pos, String dimension, String extra) { return dimension + ':' + pos.toShortString() + ':' + extra; }
    private static Data sanitize(Data loaded) { return loaded == null ? new Data() : loaded.sanitize(); }

    public static String dimension() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world == null ? "unknown" : client.world.getRegistryKey().getValue().toString();
    }

    public static final class Data {
        public List<PlayerRecord> players = new ArrayList<>();
        public List<PortalRecord> portals = new ArrayList<>();
        public List<SignRecord> signs = new ArrayList<>();
        public List<BookRecord> books = new ArrayList<>();
        public List<TrailRecord> trails = new ArrayList<>();
        public List<StashRecord> stashes = new ArrayList<>();
        public List<BaseRecord> bases = new ArrayList<>();
        public List<ChunkChangeRecord> chunkChanges = new ArrayList<>();
        public List<ChunkSnapshot> chunkSnapshots = new ArrayList<>();
        public List<ExpeditionRecord> expeditions = new ArrayList<>();
        public List<RegionRecord> regions = new ArrayList<>();
        public List<NoteRecord> notes = new ArrayList<>();
        public List<FlightPointRecord> flightPoints = new ArrayList<>();
        public List<EvidenceRecord> evidence = new ArrayList<>();
        public List<StatisticRecord> statistics = new ArrayList<>();
        Data sanitize() {
            if (players == null) players = new ArrayList<>(); if (portals == null) portals = new ArrayList<>(); if (signs == null) signs = new ArrayList<>(); if (books == null) books = new ArrayList<>();
            if (trails == null) trails = new ArrayList<>(); if (stashes == null) stashes = new ArrayList<>(); if (bases == null) bases = new ArrayList<>(); if (chunkChanges == null) chunkChanges = new ArrayList<>(); if (chunkSnapshots == null) chunkSnapshots = new ArrayList<>();
            if (expeditions == null) expeditions = new ArrayList<>(); if (regions == null) regions = new ArrayList<>(); if (notes == null) notes = new ArrayList<>(); if (flightPoints == null) flightPoints = new ArrayList<>(); if (evidence == null) evidence = new ArrayList<>(); if (statistics == null) statistics = new ArrayList<>(); return this;
        }
    }

    public record DashboardSummary(int activeInvestigations, int players, int portals, int trails, int stashes, int bases, int signs, int books, int chunkChanges, int topConfidence, String topPrediction) { }
    public record TrailRecord(int x, int y, int z, String dimension, String timestamp, String blockId) { TrailRecord(BlockPos p, String d, String t, String b) { this(p.getX(), p.getY(), p.getZ(), d, t, b); } }
    public record StashRecord(int x, int y, int z, String dimension, String timestamp, int score, String label, int confidence) { StashRecord(BlockPos p, String d, String t, int s, String l, int c) { this(p.getX(), p.getY(), p.getZ(), d, t, s, l, c); } }
    public record BaseRecord(int x, int y, int z, String dimension, String timestamp, int confidence, int radius, String reason) { BaseRecord(BlockPos p, String d, String t, int c, int r, String reason) { this(p.getX(), p.getY(), p.getZ(), d, t, c, r, reason); } }
    public record PlayerRecord(String name, String uuid, double x, double y, double z, String dimension, String timestamp, double directionDegrees, double speed) { PlayerRecord(String n, String u, Vec3d p, String d, String t, double dir, double s) { this(n, u, p.x, p.y, p.z, d, t, dir, s); } }
    public record PortalRecord(int x, int y, int z, String dimension, String timestamp, double distanceFromNearestHighway) { PortalRecord(BlockPos p, String d, String t, double h) { this(p.getX(), p.getY(), p.getZ(), d, t, h); } }
    public record SignRecord(int x, int y, int z, String dimension, String timestamp, String text) { SignRecord(BlockPos p, String d, String t, String text) { this(p.getX(), p.getY(), p.getZ(), d, t, text); } }
    public record BookRecord(int x, int y, int z, String dimension, String timestamp, String title, String itemId, String contents) { BookRecord(BlockPos p, String d, String t, String title, String item, String c) { this(p.getX(), p.getY(), p.getZ(), d, t, title, item, c); } }
    public record ChunkChangeRecord(String dimension, int chunkX, int chunkZ, String timestamp, String changeType, int x, int y, int z, String blockId) { ChunkChangeRecord(String d, int cx, int cz, String t, String c, BlockPos p, String b) { this(d, cx, cz, t, c, p.getX(), p.getY(), p.getZ(), b); } }
    public record ExpeditionRecord(int x, int y, int z, String dimension, String timestamp, String reason, boolean complete, boolean important, String notes) { ExpeditionRecord(BlockPos p, String d, String t, String reason, boolean complete, boolean important, String notes) { this(p.getX(), p.getY(), p.getZ(), d, t, reason, complete, important, notes); } }
    public record RegionRecord(String dimension, int regionX, int regionZ, String timestamp, int portalCount, int playerCount, int trailCount, int storageCount, int baseProbability) { }
    public record NoteRecord(int x, int y, int z, String dimension, String timestamp, String note) { NoteRecord(BlockPos p, String d, String t, String note) { this(p.getX(), p.getY(), p.getZ(), d, t, note); } }
    public record FlightPointRecord(double x, double y, double z, String dimension, String timestamp, double speed, int rocketsUsed) { FlightPointRecord(Vec3d p, String d, String t, double s, int r) { this(p.x, p.y, p.z, d, t, s, r); } }
    public record EvidenceRecord(int x, int y, int z, String dimension, String timestamp, String eventType, String details) { EvidenceRecord(BlockPos p, String d, String t, String eventType, String details) { this(p.getX(), p.getY(), p.getZ(), d, t, eventType, details); } }
    public static final class StatisticRecord { public String name; public double value; StatisticRecord(String name, double value) { this.name = name; this.value = value; } }
    public static final class ChunkSnapshot { public String key; public String dimension; public int chunkX; public int chunkZ; public String timestamp; public String hash; ChunkSnapshot(String key, String dimension, int chunkX, int chunkZ, String timestamp, String hash) { this.key = key; this.dimension = dimension; this.chunkX = chunkX; this.chunkZ = chunkZ; this.timestamp = timestamp; this.hash = hash; } }
}
