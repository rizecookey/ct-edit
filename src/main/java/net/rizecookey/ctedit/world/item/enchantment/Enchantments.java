package net.rizecookey.ctedit.world.item.enchantment;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class Enchantments {
    public static Enchantment SLASHING;

    public static void registerAll() {
        //SLASHING = Registry.register(Registry.ENCHANTMENT, new ResourceLocation("ctedit", "slashing"), new SlashingEnchantment());
    }
}
