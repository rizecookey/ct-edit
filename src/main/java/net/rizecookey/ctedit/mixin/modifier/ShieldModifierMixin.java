package net.rizecookey.ctedit.mixin.modifier;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.rizecookey.ctedit.extension.ServerPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class ShieldModifierMixin {
    @Mixin(ShieldItem.class)
    public static abstract class ShieldItemMixin {
        /**
         * @author RizeCookey
         * @reason to change shield damage blocking percentage
         * new values in percentage
         */
        @Overwrite
        public static float getShieldBlockDamageValue(ItemStack itemStack) {
            return itemStack.getTagElement("BlockEntityTag") != null ? 1.0F : (2.0F / 3.0F);
        }

        /**
         * @author RizeCookey
         * @reason to change shield knockback resistance
         */
        @Overwrite
        public static float getShieldKnockbackResistanceValue(ItemStack itemStack) {
            return itemStack.getTagElement("BlockEntityTag") != null ? 0.6F : 0.3F;
        }
    }

    @Mixin(LivingEntity.class)
    public static abstract class LivingEntityMixin extends Entity {
        public LivingEntityMixin(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Shadow public abstract ItemStack getBlockingItem();

        @Shadow public abstract void knockback(float f, double d, double e);

        @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F", ordinal = 0))
        public float modifyBlockedDamage(float a, float b, DamageSource damageSource, float f) {
            return b * ShieldItem.getShieldBlockDamageValue(this.getBlockingItem());
        }

        @Redirect(method = "blockedByShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(FDD)V"))
        public void cancelKnockback(LivingEntity livingEntity, float f, double d, double e) {
            this.hasImpulse = true;
            Vec3 vec3 = this.getDeltaMovement();
            Vec3 vec32 = (new Vec3(d, 0.0D, e)).normalize().scale(f);
            this.setDeltaMovement(vec3.x / 2.0D - vec32.x, vec3.y, vec3.z / 2.0D - vec32.z);
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
        @Unique float cache_blockingValue = 0.0F;

        @ModifyVariable(method = "hurtCurrentlyUsedShield", at = @At(value = "JUMP", ordinal = 2), index = 1)
        public float alwaysDamageShield(float f) {
            cache_blockingValue = f;
            return f > 0.0F ? Math.max(3.0F, f) : 0.0F;
        }

        @ModifyVariable(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(F)I"), index = 1)
        public float restoreBlockingValue(float f) {
            return cache_blockingValue;
        }


        @Inject(method = "resetAttackStrengthTicker", at = @At("TAIL"))
        public void updateShieldAfterAttack(boolean bl, CallbackInfo ci) {
            if ((Object) this instanceof ServerPlayer) {
                ((ServerPlayerExtension) this).updateShieldCooldown(true);
            }
        }
    }
}
