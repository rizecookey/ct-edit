package net.rizecookey.ctedit.mixin.accessor;

import net.minecraft.world.item.WeaponType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(WeaponType.class)
public interface WeaponTypeAccessor {
    @Accessor public static UUID getBASE_ATTACK_DAMAGE_UUID() { return null; }
    @Accessor public static UUID getBASE_ATTACK_REACH_UUID() { return null; }
    @Accessor static UUID getBASE_ATTACK_SPEED_UUID() { return null; }
}
