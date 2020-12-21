package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class AttackModifierMixin {
    @Mixin(Player.class)
    public static abstract class PlayerMixin extends LivingEntity {
        protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
            super(entityType, level);
        }

        @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"))
        public void fixSprintDesync(Player player, boolean bl) {
        }
    }

    @Mixin(LivingEntity.class)
    public static abstract class LivingEntityMixin extends Entity {
        @Shadow public abstract ItemStack getMainHandItem();

        @Shadow public abstract float getAttackStrengthScale(float f);

        @Shadow public abstract boolean hasEffect(MobEffect mobEffect);

        @Shadow public abstract boolean onClimbable();

        public LivingEntityMixin(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Inject(method = "blockedByShield", at = @At("TAIL"))
        public void injectCritDisabling(LivingEntity livingEntity, CallbackInfo ci) {
            boolean isCrit = !this.onClimbable() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && this.fallDistance > 0.0F && !this.onGround;
            if (!(this.getMainHandItem().getItem() instanceof AxeItem) && this.getAttackStrengthScale(0.0F) >= 2.0F && isCrit) {
                livingEntity.disableShield(0.8F);
            }
        }
    }
}
