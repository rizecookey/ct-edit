package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.rizecookey.ctedit.util.ItemStackUtils;
import net.rizecookey.ctedit.mixin.accessor.ClientboundContainerSetContentPacketAccessor;
import net.rizecookey.ctedit.mixin.accessor.ClientboundContainerSetSlotPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

public abstract class OutgoingPacketModifierMixin {
    @Mixin(ServerGamePacketListenerImpl.class)
    public static abstract class ServerGamePacketListenerImplMixin {
        @Shadow public ServerPlayer player;

        @ModifyArg(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"), index = 0)
        public Packet<?> modifyPacket(Packet<?> packet) {
            if (packet instanceof ClientboundContainerSetSlotPacket) {
                ClientboundContainerSetSlotPacketAccessor accessor = (ClientboundContainerSetSlotPacketAccessor) packet;
                ItemStackUtils utils = new ItemStackUtils(accessor.getItemStack(), player);
                accessor.setItemStack(utils.modifyPacketStack().getItemStack());
            } else if (packet instanceof ClientboundContainerSetContentPacket) {
                ClientboundContainerSetContentPacketAccessor accessor = (ClientboundContainerSetContentPacketAccessor) packet;
                ItemStackUtils utils = new ItemStackUtils(null, player);
                List<ItemStack> items = accessor.getItems();
                for (int i = 0; i < items.size(); i++) {
                    utils.setItemStack(items.get(i));
                    items.set(i, utils.modifyPacketStack().getItemStack());
                }
            }
            return packet;
        }
    }
}
