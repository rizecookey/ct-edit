package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class AttackModifierMixin {
    @SuppressWarnings("ConstantConditions")
    @Mixin(Player.class)
    public static abstract class PlayerMixin extends LivingEntity {
        protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
            super(entityType, level);
        }

        @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
        public void cancelKnockbackReduction(Entity entity, Vec3 vec3) {
            if (entity != this) {
                entity.setDeltaMovement(vec3);
            }
            else if ((Object) this instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer) (Object) this;
                player.connection.send(new ClientboundSetEntityMotionPacket(this));
            }
        }
    }

    @Mixin(LivingEntity.class)
    public static abstract class LivingEntityMixin extends Entity {
        public LivingEntityMixin(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Redirect(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.PUTFIELD))
        public void changeInvulnerabilityFrames(LivingEntity livingEntity, int value, DamageSource damageSource, float f) {
            int invulTime = 10;
            if (damageSource.getEntity() instanceof Player) {
                Player damageSourcePlayer = (Player) damageSource.getEntity();
                invulTime = (int) Mth.clamp(damageSourcePlayer.getCurrentItemAttackStrengthDelay() * 0.8, 8.0, 20.0);
            }
            else if (damageSource.isProjectile()) {
                invulTime = -10;
            }

            this.invulnerableTime = invulTime + 10;
        }

        @Unique float cache_f;
        @Inject(method = "knockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V", shift = At.Shift.BEFORE))
        public void grabKnockbackArgs(float f, double d, double e, CallbackInfo ci) {
            this.cache_f = f;
        }

        @ModifyArg(method = "knockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V"), index = 1)
        public double modifyY(double x, double y, double z) {
            Vec3 vec3 = this.getDeltaMovement();
            float f = this.cache_f;
            return this.onGround ? Math.min(0.4D, (double)f * 0.75D) : Math.min(0.4D, vec3.y + (double)f * 0.5D);
        }
    }
}
