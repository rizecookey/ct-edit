package net.rizecookey.ctedit.mixin.accessor;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundContainerSetSlotPacket.class)
public interface ClientboundContainerSetSlotPacketAccessor {
    @Accessor void setItemStack(ItemStack itemStack);
    @Accessor ItemStack getItemStack();
}
