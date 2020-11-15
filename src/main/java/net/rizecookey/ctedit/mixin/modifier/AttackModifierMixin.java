package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
