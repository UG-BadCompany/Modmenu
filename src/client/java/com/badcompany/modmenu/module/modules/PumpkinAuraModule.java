package com.badcompany.modmenu.module.modules;

import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import com.badcompany.modmenu.settings.ColorSetting;
import com.badcompany.modmenu.settings.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class PumpkinAuraModule extends Module {
    private final NumberSetting placeRange = addSetting(new NumberSetting(
            "Place range",
            "Maximum range for nearby legal pumpkin placements.",
            5.0D,
            1.0D,
            6.0D
    ));
    private final NumberSetting targetRange = addSetting(new NumberSetting(
            "Target range",
            "Maximum range for player targets considered by the safe placement helper.",
            12.0D,
            4.0D,
            16.0D
    ));
    private final NumberSetting placeDelay = addSetting(new NumberSetting(
            "Place delay",
            "Minimum delay between vanilla placement attempts, in ticks.",
            8.0D,
            2.0D,
            40.0D
    ));
    private final BooleanSetting autoSwitch = addSetting(new BooleanSetting(
            "Auto switch",
            "Switch to a hotbar pumpkin before using vanilla interaction when possible.",
            false
    ));
    private final BooleanSetting requireUseKey = addSetting(new BooleanSetting(
            "Require use key",
            "Only place while the vanilla use key is held, preventing unattended automation.",
            true
    ));
    private final BooleanSetting bedwarsMode = addSetting(new BooleanSetting(
            "Bedwars mode",
            "Allow wool supports instead of obsidian or bedrock.",
            false
    ));
    private final BooleanSetting notifyChat = addSetting(new BooleanSetting(
            "Chat notices",
            "Report safe PumpkinAura placement attempts in local chat.",
            false
    ));
    private final ColorSetting renderColor = addSetting(new ColorSetting(
            "Render color",
            "Saved highlight color for safe placement previews.",
            0xFFFFAA00
    ));

    private int cooldownTicks;
    private BlockPos lastCandidate;

    public PumpkinAuraModule() {
        super("PumpkinAura", "Safely places held pumpkins on nearby legal supports using normal vanilla interaction.", Category.WORLD);
    }

    @Override
    protected void onDisable() {
        cooldownTicks = 0;
        lastCandidate = null;
    }

    @Override
    public void tick() {
        if (client.world == null || client.player == null || client.interactionManager == null) {
            lastCandidate = null;
            return;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }
        Optional<BlockPos> candidate = findCandidate();
        lastCandidate = candidate.orElse(null);
        if (candidate.isEmpty()) return;
        if (requireUseKey.get() && !client.options.useKey.isPressed()) return;

        Hand hand = findPumpkinHand();
        if (hand == null && autoSwitch.get()) {
            switchToPumpkin();
            hand = findPumpkinHand();
        }
        if (hand == null) return;

        BlockPos base = candidate.get();
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(base), Direction.UP, base, false);
        ActionResult result = client.interactionManager.interactBlock(client.player, hand, hit);
        if (result.isAccepted()) {
            client.player.swingHand(hand);
            cooldownTicks = (int) Math.round(placeDelay.get());
            if (notifyChat.get()) {
                client.player.sendMessage(Text.literal("[PumpkinAura] Placed via vanilla interaction at " + base.getX() + ", " + (base.getY() + 1) + ", " + base.getZ()), false);
            }
        }
    }

    public BlockPos lastCandidate() {
        return lastCandidate;
    }

    public int renderColor() {
        return renderColor.get();
    }

    private Optional<BlockPos> findCandidate() {
        List<AbstractClientPlayerEntity> targets = client.world.getPlayers().stream()
                .filter(player -> player != client.player)
                .filter(Entity::isAlive)
                .filter(player -> player.distanceTo(client.player) <= targetRange.get())
                .sorted(Comparator.comparingDouble(player -> player.squaredDistanceTo(client.player)))
                .toList();
        for (AbstractClientPlayerEntity target : targets) {
            Optional<BlockPos> candidate = positionsAround(target.getBlockPos(), (int) Math.round(placeRange.get())).stream()
                    .filter(this::canPlacePumpkinOn)
                    .min(Comparator.comparingInt(pos -> pos.getManhattanDistance(target.getBlockPos())));
            if (candidate.isPresent()) return candidate;
        }
        return Optional.empty();
    }

    private List<BlockPos> positionsAround(BlockPos center, int range) {
        return BlockPos.stream(center.add(-range, -range, -range), center.add(range, range, range))
                .filter(pos -> squaredDistance(client.player.getBlockPos(), pos) <= placeRange.get() * placeRange.get())
                .map(BlockPos::toImmutable)
                .toList();
    }

    private static double squaredDistance(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dy = first.getY() - second.getY();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private boolean canPlacePumpkinOn(BlockPos base) {
        BlockPos place = base.up();
        BlockPos headroom = base.up(2);
        boolean validSupport = bedwarsMode.get()
                ? client.world.getBlockState(base).isOf(Blocks.WHITE_WOOL)
                : client.world.getBlockState(base).isOf(Blocks.OBSIDIAN) || client.world.getBlockState(base).isOf(Blocks.BEDROCK);
        if (!validSupport) return false;
        if (!client.world.getBlockState(place).isAir() || !client.world.getBlockState(headroom).isAir()) return false;
        Box occupied = new Box(place).union(new Box(headroom));
        return client.world.getOtherEntities(null, occupied).isEmpty();
    }

    private Hand findPumpkinHand() {
        if (client.player.getMainHandStack().isOf(Items.CARVED_PUMPKIN) || client.player.getMainHandStack().isOf(Items.PUMPKIN)) return Hand.MAIN_HAND;
        if (client.player.getOffHandStack().isOf(Items.CARVED_PUMPKIN) || client.player.getOffHandStack().isOf(Items.PUMPKIN)) return Hand.OFF_HAND;
        return null;
    }

    private void switchToPumpkin() {
        for (int slot = 0; slot < 9; slot++) {
            if (client.player.getInventory().getStack(slot).isOf(Items.CARVED_PUMPKIN) || client.player.getInventory().getStack(slot).isOf(Items.PUMPKIN)) {
                client.player.getInventory().setSelectedSlot(slot);
                return;
            }
        }
    }
}
