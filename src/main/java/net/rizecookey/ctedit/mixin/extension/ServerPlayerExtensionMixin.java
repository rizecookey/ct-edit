package net.rizecookey.ctedit.mixin.extension;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.rizecookey.ctedit.extension.ServerPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerExtensionMixin extends Player implements ServerPlayerExtension {
    public ServerPlayerExtensionMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    public void updateShieldCooldown(boolean force) {
        boolean enableCooldown = false;
        int cooldown = 0;
        for (ItemStack itemStack : this.getHandSlots()) {
            if (itemStack.getItem() instanceof ShieldItem) {
                if (itemStack.getTagElement("BlockEntityTag") != null && this.getAttackStrengthScale(0.0F) < 2.0F) {
                    enableCooldown = force || !this.getCooldowns().isOnCooldown(Items.SHIELD);
                    cooldown = this.attackStrengthTicker;
                    break;
                }
                else if (this.isCrouching() && this.isBlocking() && this.hasEnabledShieldOnCrouch()) {
                    enableCooldown = true;
                    cooldown += 4;
                    break;
                }
            }
        }
        if (enableCooldown) {
            this.getCooldowns().addCooldown(Items.SHIELD, cooldown);
            if (this.getCooldowns().isOnCooldown(Items.SHIELD)) {
                this.stopUsingItem();
            }
        }
    }
}
