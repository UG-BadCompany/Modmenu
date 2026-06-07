package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.settings.StringSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class SignDatabaseModule extends AbstractBlockScanModule {
    private final StringSetting keyword = addSetting(new StringSetting("Keyword", "Optional keyword used by command/database searches.", ""));

    public SignDatabaseModule() {
        super("Sign Database", "Automatically saves sign coordinates, dimension, timestamp, and visible sign text for keyword search.", 48.0D, 60.0D);
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state, String blockId, String dimension) {
        if (!blockIdContains(blockId, "sign")) return;
        BlockEntity entity = world.getBlockEntity(pos);
        String text = "<unloaded sign text>";
        if (entity instanceof SignBlockEntity sign) {
            text = Arrays.stream(sign.getText(true).getMessages(false)).map(Text::getString).collect(Collectors.joining(" | "));
        }
        if (keyword.get().isBlank() || text.toLowerCase().contains(keyword.get().toLowerCase())) database.recordSign(pos, dimension, text);
    }
}
