package net.rizecookey.ctedit.mixin.update;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.rizecookey.ctedit.mixin.accessor.ClientboundUpdateAttributesPacketAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class UpdateItemInfoMixin {
    @Mixin(ServerGamePacketListenerImpl.class)
    public static abstract class ServerGamePacketListenerImplMixin {
        @Shadow public ServerPlayer player;

        @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"))
        public void injectInventoryUpdate(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener, CallbackInfo ci) {
            if (packet instanceof ClientboundUpdateAttributesPacket) {
                if (((ClientboundUpdateAttributesPacketAccessor) packet).getEntityId() == player.getId()) {
                    player.connection.send(new ClientboundContainerSetContentPacket(player.inventoryMenu.containerId, player.inventoryMenu.getItems()));
                }
            }
        }
    }
}
