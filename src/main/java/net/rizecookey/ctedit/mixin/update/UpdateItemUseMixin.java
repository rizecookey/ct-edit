package net.rizecookey.ctedit.mixin.update;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class UpdateItemUseMixin {
    @SuppressWarnings("ConstantConditions")
    @Mixin(LivingEntity.class)
    public static abstract class LivingEntityMixin extends Entity {
        @Shadow protected abstract void setLivingEntityFlag(int i, boolean bl);

        public LivingEntityMixin(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Inject(method = "stopUsingItem", at = @At("HEAD"))
        public void extendForPlayer(CallbackInfo ci) {
            if (!this.level.isClientSide && (Object) this instanceof ServerPlayer) setLivingEntityFlag(1, true);
        }
    }
}
