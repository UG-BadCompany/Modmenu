package com.badcompany.modmenu.module.modules.hunting;

import com.badcompany.modmenu.hunting.HuntDatabase;
import com.badcompany.modmenu.module.Category;
import com.badcompany.modmenu.module.Module;
import com.badcompany.modmenu.settings.BooleanSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class BookArchiveModule extends Module {
    private final HuntDatabase database = HuntDatabase.get();
    private final BooleanSetting includeWritableBooks = addSetting(new BooleanSetting("Writable books", "Also archive unsigned writable books encountered in inventory.", false));
    private int cooldown;

    public BookArchiveModule() {
        super("Book Archive", "Archives written books found in your inventory with title, item id, coordinates found, and searchable metadata.", Category.HUNTING);
    }

    @Override
    public void tick() {
        database.tickAutosave();
        if (client.player == null) return;
        if (cooldown-- > 0) return;
        cooldown = 100;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            String id = Registries.ITEM.getId(stack.getItem()).toString();
            if (id.equals("minecraft:written_book") || (includeWritableBooks.get() && id.equals("minecraft:writable_book"))) {
                String contents = stack.getName().getString() + " " + stack.toString();
                database.recordBook(client.player.getBlockPos(), HuntDatabase.dimension(), stack, contents);
            }
        }
    }
}
