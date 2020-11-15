package net.rizecookey.ctedit.constructor;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class PickaxeItemBuilder extends PickaxeItem {
    private PickaxeItemBuilder(Tier tier, int i, float f, Properties properties) {
        super(tier, i, f, properties);
    }

    public static PickaxeItem construct(Tier tier, int i, float f, Properties properties) {
        return new PickaxeItemBuilder(tier, i, f, properties);
    }
}
