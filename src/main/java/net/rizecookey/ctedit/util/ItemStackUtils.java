package net.rizecookey.ctedit.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.rizecookey.ctedit.mixin.accessor.ItemAccessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class ItemStackUtils {
    public static final String PACKET_MOD_DATA = "prePacketMod";
    public static final String MODIFIED = "packetModified";

    ItemStack itemStack;
    ServerPlayer player;

    public ItemStackUtils(ItemStack itemStack) {
        this(itemStack, null);
    }

    public ItemStackUtils(ItemStack itemStack, ServerPlayer player) {
        this.itemStack = itemStack != null ? itemStack.copy() : null;
        this.player = player;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ItemStackUtils setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack != null ? itemStack.copy() : null;
        return this;
    }

    public ItemStackUtils setPlayer(ServerPlayer player) {
        this.player = player;
        return this;
    }

    public ItemStackUtils modifyPacketStack() {
        if (isModified() || !shouldBeModified()) return this;
        CompoundTag stackTag = itemStack.getOrCreateTag();
        CompoundTag previousStackTag = stackTag.copy();
        if ((itemStack.getItem() instanceof TieredItem || itemStack.getItem() instanceof TridentItem) && !hasAttributeModifiers()) {
            List<Component> lore = getLore();
            lore.addAll(getModifierTooltip());
            setLore(lore);

            itemStack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                for (Map.Entry<Attribute, AttributeModifier> entry : itemStack.getItem().getDefaultAttributeModifiers(equipmentSlot).entries()) {
                    itemStack.addAttributeModifier(entry.getKey(), entry.getValue(), equipmentSlot);
                }
            }
        }

        stackTag.put(PACKET_MOD_DATA, previousStackTag);
        stackTag.putBoolean(MODIFIED, true);
        return this;
    }

    public ItemStackUtils unmodifyPacketStack() {
        if (!isModified()) return this;
        CompoundTag stackTag = itemStack.getTag();
        assert stackTag != null;
        CompoundTag prevData = stackTag.contains(PACKET_MOD_DATA) ? stackTag.copy().getCompound(PACKET_MOD_DATA) : null;
        if (prevData != null) {
            itemStack.setTag(prevData);
        }
        stackTag.remove(PACKET_MOD_DATA);
        stackTag.remove(MODIFIED);
        return this;
    }

    // Mostly edited Mojang code
    public List<Component> getModifierTooltip() {
        List<Component> lines = Lists.newArrayList();
        EquipmentSlot[] slots = EquipmentSlot.values();
        Style nonItalic = Style.EMPTY.withItalic(false);
        for (EquipmentSlot equipmentSlot : slots) {
            Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(equipmentSlot);
            if (!multimap.isEmpty()) {
                lines.add(TextComponent.EMPTY);
                lines.add((new TranslatableComponent("item.modifiers." + equipmentSlot.getName())).withStyle(nonItalic).withStyle(ChatFormatting.GRAY));

                for (Map.Entry<Attribute, AttributeModifier> attributeAttributeModifierEntry : multimap.entries()) {
                    AttributeModifier attributeModifier = attributeAttributeModifierEntry.getValue();
                    double d = attributeModifier.getAmount();
                    boolean bl = false;
                    if (player != null) {
                        if (attributeModifier.getId() == ItemAccessor.BASE_ATTACK_DAMAGE_UUID()) {
                            d += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                            d += EnchantmentHelper.getDamageBonus(itemStack, MobType.UNDEFINED);
                            bl = true;
                        } else if (attributeModifier.getId() == ItemAccessor.BASE_ATTACK_SPEED_UUID()) {
                            d += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                            bl = true;
                        }
                    }

                    double g;
                    if (attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        if (attributeAttributeModifierEntry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                            g = d * 10.0D;
                        } else {
                            g = d;
                        }
                    } else {
                        g = d * 100.0D;
                    }

                    if (bl) {
                        lines.add((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(g), new TranslatableComponent(attributeAttributeModifierEntry.getKey().getDescriptionId()))).withStyle(nonItalic).withStyle(ChatFormatting.DARK_GREEN));
                    } else if (d > 0.0D) {
                        lines.add((new TranslatableComponent("attribute.modifier.plus." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(g), new TranslatableComponent(attributeAttributeModifierEntry.getKey().getDescriptionId()))).withStyle(nonItalic).withStyle(ChatFormatting.BLUE));
                    } else if (d < 0.0D) {
                        g *= -1.0D;
                        lines.add((new TranslatableComponent("attribute.modifier.take." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(g), new TranslatableComponent(attributeAttributeModifierEntry.getKey().getDescriptionId()))).withStyle(nonItalic).withStyle(ChatFormatting.RED));
                    }
                }
            }
        }
        return lines;
    }

    public ListTag componentToListTag(List<Component> components) {
        ListTag listTag = new ListTag();
        components.forEach((component -> listTag.add(StringTag.valueOf(Component.Serializer.toJson(component)))));
        return listTag;
    }

    public List<Component> listTagToComponent(ListTag listTag) {
        List<Component> components = Lists.newArrayList();
        for (int i = 0; i < listTag.size(); i++) {
            components.add(Component.Serializer.fromJson(listTag.getString(i)));
        }
        return components;
    }

    public boolean isModified() {
        if (!itemStack.hasTag()) return false;
        assert itemStack.getTag() != null;
        return itemStack.getTag().contains(MODIFIED) && itemStack.getTag().getBoolean(MODIFIED);
    }

    public void setLore(List<Component> components) {
        CompoundTag stackTag = itemStack.getOrCreateTag();
        CompoundTag displayTag = stackTag.getCompound("display");
        displayTag.put("Lore", componentToListTag(components));
        stackTag.put("display", displayTag);
    }

    public List<Component> getLore() {
        return listTagToComponent(itemStack.getOrCreateTag().getCompound("display").getList("Lore", 8));
    }

    public boolean hasAttributeModifiers() {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (itemStack.getAttributeModifiers(equipmentSlot) != itemStack.getItem().getDefaultAttributeModifiers(equipmentSlot)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldBeModified() {
        return ((itemStack.getItem() instanceof TieredItem || itemStack.getItem() instanceof TridentItem) && !hasAttributeModifiers()) || itemStack.getItem() instanceof ShieldItem;
    }
}
