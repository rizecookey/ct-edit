package net.rizecookey.ctedit.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.*;

public class SlashingEnchantment extends Enchantment implements ServerSideEnchantment {
    public SlashingEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.AXE, new EquipmentSlot[] {EquipmentSlot.MAINHAND} );
    }

    public int getMinCost(int i) { return 5 + (i - 1) * 20; }

    public int getMaxCost(int i) { return this.getMinCost(i) + 20; }

    public int getMaxLevel() { return 3; }

    public float getChargedDamageBonus(int i, LivingEntity livingEntity) {
        return 2.0F * i;
    }

    public boolean checkCompatibility(Enchantment enchantment) {
        return !(enchantment instanceof CleavingEnchantment) && !(enchantment instanceof DamageEnchantment) &&  !(enchantment instanceof SweepingEdgeEnchantment) && super.checkCompatibility(enchantment);
    }

    public String getName() {
        return "Slashing";
    }
}
