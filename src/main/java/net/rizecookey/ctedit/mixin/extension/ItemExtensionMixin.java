package net.rizecookey.ctedit.mixin.extension;

import net.minecraft.world.item.Item;
import net.rizecookey.ctedit.extension.ItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemExtensionMixin implements ItemExtension {
    Item.Properties properties;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void injectProperties(Item.Properties properties, CallbackInfo ci) {
        this.properties = properties;
    }

    public Item.Properties getProperties() {
        return this.properties;
    }
}
