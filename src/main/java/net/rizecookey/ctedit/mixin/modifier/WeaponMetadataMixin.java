package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.WeaponType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class WeaponMetadataMixin {
    @SuppressWarnings("ConstantConditions")
    @Mixin(WeaponType.class)
    public static abstract class WeaponTypeMixin {
        @Inject(method = "getReach", at = @At("RETURN"), cancellable = true)
        public void modifyReach(Tier tier, CallbackInfoReturnable<Float> cir) {
            if ((Object) this == WeaponType.AXE) {
                cir.setReturnValue(0.5F);
            }
        }

        @Inject(method = "getSpeed", at = @At("RETURN"), cancellable = true)
        public void modifySpeed(Tier tier, CallbackInfoReturnable<Float> cir) {
            if ((Object) this == WeaponType.SWORD) {
                cir.setReturnValue(0.0F);
            }
        }
    }
}
