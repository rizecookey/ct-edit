package net.rizecookey.ctedit.constructor;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;

public class AxeItemBuilder extends AxeItem {
    private AxeItemBuilder(Tier tier, float f, float g, Properties properties) {
        super(tier, f, g, properties);
    }

    public static AxeItem construct(Tier tier, float f, float g, Properties properties) {
        return new AxeItemBuilder(tier, f, g, properties);
    }
}
