package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

public class HungerRegenModifierMixin {
    @Mixin(FoodData.class)
    public static abstract class FoodDataMixin {
        @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F", ordinal = 0), index = 1)
        public float modifyMaxHeal(float previous) {
            return 2.0F;
        }
    }
}
