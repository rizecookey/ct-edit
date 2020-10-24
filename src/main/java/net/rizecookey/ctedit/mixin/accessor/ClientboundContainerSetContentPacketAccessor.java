package net.rizecookey.ctedit.mixin.accessor;

import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundContainerSetContentPacket.class)
public interface ClientboundContainerSetContentPacketAccessor {
    @Accessor void setItems(List<ItemStack> items);
    @Accessor List<ItemStack> getItems();
}
