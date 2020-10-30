package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

public abstract class AttackModifierMixin {
    @SuppressWarnings("ConstantConditions")
    @Mixin(Player.class)
    public static abstract class PlayerMixin extends LivingEntity {
        protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
            super(entityType, level);
        }

        @Unique boolean cache_bl3;
        @Inject(method = "attack", slice = @Slice(
                from = @At(value = "JUMP", ordinal = 10)
        ), at = @At(value = "JUMP", ordinal = 11, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
        public void catchLocals(Entity entity, CallbackInfo ci, float g, float h, float i, boolean bl, boolean bl2, int j, boolean bl3) {
            this.cache_bl3 = bl3;
        }

        @ModifyVariable(method = "attack", slice = @Slice(
                from = @At(value = "JUMP", ordinal = 10)
        ), at = @At(value = "JUMP", ordinal = 11), index = 3)
        public float changeChargedCrits(float previous) {
            return cache_bl3 ? previous * 1.25F / 1.5F : previous;
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
}
