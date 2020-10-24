package net.rizecookey.ctedit.world.item.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

public interface ServerSideEnchantment {
    default Component getDefaultFullname(int i) {
        Style style = Style.EMPTY.withItalic(false);
        MutableComponent mutableComponent = new TextComponent(getName()).withStyle(style);
        if (this.isCurse()) {
            mutableComponent.withStyle(ChatFormatting.RED);
        } else {
            mutableComponent.withStyle(ChatFormatting.GRAY);
        }

        if (i != 1 || this.getMaxLevel() != 1) {
            mutableComponent.append(" ").append(new TranslatableComponent("enchantment.level." + i).withStyle(Style.EMPTY.withItalic(false)));
        }

        return mutableComponent;
    }

    boolean isCurse();
    int getMaxLevel();
    String getName();
}
