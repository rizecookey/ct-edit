package net.rizecookey.ctedit.mixin.feature.enchantment;

import net.minecraft.core.Registry;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.rizecookey.ctedit.world.item.enchantment.SlashingEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

public abstract class SlashingEnchantmentMixin {
    @Mixin(Player.class)
    public abstract static class PlayerMixin extends LivingEntity {
        @Shadow public abstract float getAttackStrengthScale(float f);

        protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
            super(entityType, level);
        }

        @ModifyVariable(method = "attack", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)F"), index = 1, ordinal = 0)
        private float addSlashingDamage(float g, Entity entity) {
            ListTag enchantments = this.getMainHandItem().getEnchantmentTags();
            for (int i = 0; i < enchantments.size(); i++) {
                String id = enchantments.getCompound(i).getString("id");
                Optional<Enchantment> enchantmentOpt = Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(id));
                if (enchantmentOpt.isPresent()
                        && enchantmentOpt.get() instanceof SlashingEnchantment
                        && this.getAttackStrengthScale(0.0F) >= 2.0F
                        && entity instanceof LivingEntity) {
                    g += ((SlashingEnchantment) enchantmentOpt.get()).getChargedDamageBonus(enchantments.getCompound(i).getInt("lvl"), (LivingEntity) entity);
                }
            }
            return g;
        }
    }
}
