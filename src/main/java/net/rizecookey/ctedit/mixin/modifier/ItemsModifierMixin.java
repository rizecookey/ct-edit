package net.rizecookey.ctedit.mixin.modifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.rizecookey.ctedit.constructor.AxeItemBuilder;
import net.rizecookey.ctedit.constructor.PickaxeItemBuilder;
import net.rizecookey.ctedit.extension.ItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ItemsModifierMixin {
    @Mixin(Items.class)
    public static abstract class ItemsMixin implements ItemExtension {
        @Shadow
        private static Item registerItem(ResourceLocation resourceLocation, Item item) {
            return null;
        }

        private static boolean inject = true;
        @Inject(method = "registerItem(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"), cancellable = true)
        private static void modifyAttackSpeed(ResourceLocation resourceLocation, Item item, CallbackInfoReturnable<Item> cir) {
            if (inject) {
                inject = false;
                Item newItem = item;
                if (item instanceof TieredItem) {
                    if (item instanceof SwordItem) {
                        newItem = new SwordItem(((SwordItem) item).getTier(), 3, -2.0F, ((ItemExtension) item).getProperties());
                    }
                    else if (item instanceof AxeItem) {
                        newItem = AxeItemBuilder.construct(((AxeItem) item).getTier(), 5.0F, -2.6F, ((ItemExtension) item).getProperties());
                    }
                    else if (item instanceof PickaxeItem) {
                        newItem = PickaxeItemBuilder.construct(((PickaxeItem) item).getTier(), 2, -2.4F, ((ItemExtension) item).getProperties());
                    }
                }
                cir.setReturnValue(registerItem(resourceLocation, newItem));
            }
            inject = true;
        }
    }
}
