package net.alshanex.alshanexspells.item;

import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.alshanex.alshanexspells.item.custom.MeraMeraItem;
import net.alshanex.alshanexspells.item.custom.HieHieItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AlshanexSpellsMod.MODID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    public static final DeferredItem<Item> MERAMERA = ITEMS.register("meramerafruit", MeraMeraItem::new);
    public static final DeferredItem<Item> HIEHIE = ITEMS.register("hiehiefruit", HieHieItem::new);
}
