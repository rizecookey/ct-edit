package net.rizecookey.ctedit.mixin.modifier;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rizecookey.ctedit.extension.ServerPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class ShieldModifierMixin {
    @Mixin(LivingEntity.class)
    public static abstract class LivingEntityMixin extends Entity {
        public LivingEntityMixin(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Redirect(method = "blockedByShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(FDD)V"))
        public void cancelKnockback(LivingEntity livingEntity, float f, double d, double e) {
        }
    }

    @Mixin(ServerPlayer.class)
    public static abstract class ServerPlayerMixin extends Player {
        public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
            super(level, blockPos, f, gameProfile);
        }


        @Inject(method = "slotChanged", at = @At("HEAD"))
        public void updateShieldCooldown(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack, CallbackInfo ci) {
            if (abstractContainerMenu instanceof InventoryMenu) {
                ((ServerPlayerExtension) this).updateShieldCooldown(false);
            }
        }
    }

    @Mixin(ServerGamePacketListenerImpl.class)
    public static abstract class ServerGamePacketListenerImplMixin {
        @Shadow public ServerPlayer player;

        @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
        public void updateShieldCooldown(ServerboundSetCarriedItemPacket serverboundSetCarriedItemPacket, CallbackInfo ci) {
            ((ServerPlayerExtension) player).updateShieldCooldown(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Mixin(Player.class)
    public static abstract class PlayerMixin {
        @Inject(method = "resetAttackStrengthTicker", at = @At("TAIL"))
        public void updateShieldAfterAttack(CallbackInfo ci) {
            if ((Object) this instanceof ServerPlayer) {
                ((ServerPlayerExtension) this).updateShieldCooldown(true);
            }
        }
    }
}
