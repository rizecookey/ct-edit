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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.rizecookey.ctedit.mixin.accessor.WeaponTypeAccessor;
import net.rizecookey.ctedit.world.item.enchantment.ServerSideEnchantment;

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
        if (itemStack.getItem() instanceof TieredItem && !hasAttributeModifiers()) {
            List<Component> lore = getLore();
            lore.addAll(getModifierTooltip());
            setLore(lore);

            itemStack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                for (Map.Entry<Attribute, AttributeModifier> entry : itemStack.getItem().getDefaultAttributeModifiers(equipmentSlot).entries()) {
                    itemStack.addAttributeModifier(entry.getKey(), entry.getValue(), equipmentSlot);
                }
            }
        } else if (itemStack.getItem() instanceof ShieldItem) {
            List<Component> lore = getLore();
            lore.addAll(getShieldInfoLines());
            setLore(lore);

            itemStack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        }

        if (containsServerEnchantment() || containsStoredServerEnchantment()) {
            Map<Enchantment, Integer> enchantments = getEnchantments();
            enchantments.putAll(getStoredEnchantments());
            List<Component> lore = Lists.newArrayList();
            for (Enchantment enchantment : enchantments.keySet()) {
                if (enchantment instanceof ServerSideEnchantment) {
                    ServerSideEnchantment serverSideEnchantment = (ServerSideEnchantment) enchantment;
                    int level = enchantments.get(enchantment);
                    lore.add(serverSideEnchantment.getDefaultFullname(level));
                }
            }
            lore.addAll(getLore());
            setLore(lore);
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
                        if (attributeModifier.getId() == WeaponTypeAccessor.getBASE_ATTACK_DAMAGE_UUID()) {
                            d += player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
                            d += EnchantmentHelper.getDamageBonus(itemStack, null);
                            bl = true;
                        } else if (attributeModifier.getId() == WeaponTypeAccessor.getBASE_ATTACK_SPEED_UUID()) {
                            d += player.getAttribute(Attributes.ATTACK_SPEED).getBaseValue() - 1.5D;
                            bl = true;
                        } else if (attributeModifier.getId() == WeaponTypeAccessor.getBASE_ATTACK_REACH_UUID()) {
                            d += player.getAttribute(Attributes.ATTACK_REACH).getBaseValue();
                            bl = true;
                        } else if (attributeAttributeModifierEntry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                            d += player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue();
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

    public List<Component> getShieldInfoLines() {
        List<Component> lines = Lists.newArrayList();

        if (itemStack.getItem() instanceof ShieldItem) {
            lines.add(TextComponent.EMPTY);

            Style style = Style.EMPTY.withItalic(false);
            lines.add(new TextComponent("When blocking:").withStyle(style).withStyle(ChatFormatting.GRAY));

            double damageReduction = ShieldItem.getShieldBlockDamageValue(itemStack);
            double knockbackReduction = ShieldItem.getShieldKnockbackResistanceValue(itemStack);

            lines.add(new TextComponent("+" + ATTRIBUTE_MODIFIER_FORMAT.format(damageReduction * 100) + "% Damage Resistance").withStyle(style).withStyle(ChatFormatting.BLUE));
            lines.add(new TextComponent("").append(new TranslatableComponent("attribute.modifier.plus.0", ATTRIBUTE_MODIFIER_FORMAT.format(knockbackReduction * 10), new TranslatableComponent(Attributes.KNOCKBACK_RESISTANCE.getDescriptionId()))).withStyle(style).withStyle(ChatFormatting.BLUE));
            if (itemStack.getTagElement("BlockEntityTag") != null) lines.add(new TextComponent(" 200% Attack Charge required").withStyle(style).withStyle(ChatFormatting.RED));
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

    public Map<Enchantment, Integer> getEnchantments() {
        Map<Enchantment, Integer> enchantmentMap = Maps.newHashMap();
        ListTag enchantmentTags = itemStack.getEnchantmentTags();
        for (int i = 0; i < enchantmentTags.size(); i++) {
            CompoundTag enchantmentTag = enchantmentTags.getCompound(i);
            String id = enchantmentTag.getString("id");
            int level = enchantmentTag.getInt("lvl");
            Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(id));
            optionalEnchantment.ifPresent(enchantment -> enchantmentMap.put(enchantment, level));
        }

        return enchantmentMap;
    }

    public Map<Enchantment, Integer> getStoredEnchantments() {
        Map<Enchantment, Integer> enchantmentMap = Maps.newHashMap();
        if (itemStack.getItem() instanceof EnchantedBookItem) {
            ListTag enchantmentTags = EnchantedBookItem.getEnchantments(itemStack);
            for (int i = 0; i < enchantmentTags.size(); i++) {
                CompoundTag enchantmentTag = enchantmentTags.getCompound(i);
                String id = enchantmentTag.getString("id");
                int level = enchantmentTag.getInt("lvl");
                Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(id));
                optionalEnchantment.ifPresent(enchantment -> enchantmentMap.put(enchantment, level));
            }
        }

        return enchantmentMap;
    }

    public boolean containsServerEnchantment() {
        for (Enchantment enchantment : getEnchantments().keySet()) {
            if (enchantment instanceof ServerSideEnchantment) {
                return true;
            }
        }
        return false;
    }

    public boolean containsStoredServerEnchantment() {
        for (Enchantment enchantment : getStoredEnchantments().keySet()) {
            if (enchantment instanceof ServerSideEnchantment) {
                return true;
            }
        }
        return false;
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
        return (itemStack.getItem() instanceof TieredItem && !hasAttributeModifiers()) || itemStack.getItem() instanceof ShieldItem || containsServerEnchantment() || containsStoredServerEnchantment();
    }
}
