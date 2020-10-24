package net.rizecookey.ctedit.mixin.accessor;

import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundUpdateAttributesPacket.class)
public interface ClientboundUpdateAttributesPacketAccessor {
    @Accessor int getEntityId();
}
