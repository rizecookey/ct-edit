package net.rizecookey.ctedit;

import net.fabricmc.api.ModInitializer;
import net.rizecookey.ctedit.world.item.enchantment.Enchantments;

public class Mod implements ModInitializer {
	@Override
	public void onInitialize() {
		Enchantments.registerAll();
	}
}
