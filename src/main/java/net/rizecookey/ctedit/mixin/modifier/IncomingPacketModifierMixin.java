package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import net.rizecookey.ctedit.util.ItemStackUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class IncomingPacketModifierMixin {
    @Mixin(ServerboundContainerClickPacket.class)
    public abstract static class ServerboundContainerClickPacketMixin {
        @Shadow private ItemStack itemStack;

        @Inject(method = "read", at = @At("TAIL"))
        public void modifyItem(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
            ItemStackUtils utils = new ItemStackUtils(this.itemStack);
            this.itemStack = utils.unmodifyPacketStack().getItemStack();
        }
    }

    @Mixin(ServerboundSetCreativeModeSlotPacket.class)
    public abstract static class ServerboundSetCreativeModeSlotPacketMixin {
        @Shadow private ItemStack itemStack;

        @Inject(method = "read", at = @At("TAIL"))
        public void modifyItem(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
            ItemStackUtils utils = new ItemStackUtils(this.itemStack);
            this.itemStack = utils.unmodifyPacketStack().getItemStack();
        }
    }
}
