package net.rizecookey.ctedit.mixin.accessor;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Enchantments.class)
public interface EnchantmentsAccessor {
    @Invoker
    static Enchantment invokeRegister(String string, Enchantment enchantment) {
        return null;
    }
}
