package justfatlard.kragle;

import justfatlard.pandorical.api.ItemRegistration;
import justfatlard.pandorical.api.PandoricalApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Kragle.
 *
 * <p>A bottle of glue. Put it on any block and that block is yours: nobody else breaks it, and
 * no explosion takes it either. Sneak and use it again to take the glue off something you glued.
 *
 * <p>There is no recipe, and there is not going to be one. A block nobody can break is not a
 * thing to be farmed; it is a thing an operator hands somebody, one bottle at a time, for a
 * reason. It sits in a creative tab and comes out of {@code /give}, and that is the whole supply.
 *
 * <p>Server-side, through Pandorical: a vanilla client sees the bottle and the blocks it will
 * not break, and needs nothing installed to see either.
 */
public class Main implements ModInitializer {
	public static final String MOD_ID = "kragle";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier KRAGLE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "kragle");
	public static final ResourceKey<Item> KRAGLE_KEY = ResourceKey.create(Registries.ITEM, KRAGLE_ID);

	// Vanilla's stack of 64, and whatever stackz makes of that where it is installed. Scarcity is
	// in the supply - no recipe, handed out on purpose - and in a bottle doing one block, not in
	// making somebody who was given ten spend ten slots carrying them.
	public static final Item KRAGLE = new Item(new Item.Properties()
		.setId(KRAGLE_KEY)
		.rarity(Rarity.EPIC));

	@Override
	public void onInitialize() {
		if (PandoricalApi.isAvailable()) {
			PandoricalApi.content().registerItem(MOD_ID + ":kragle", new ItemRegistration()
				.model(MOD_ID + ":item/kragle"));
			PandoricalApi.content().registerModAssets(MOD_ID);
		}

		Registry.register(BuiltInRegistries.ITEM, KRAGLE_ID, KRAGLE);

		ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "kragle"));
		CreativeModeTab tab = FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.kragle"))
			.icon(() -> new ItemStack(KRAGLE))
			.displayItems((context, entries) -> entries.accept(new ItemStack(KRAGLE)))
			.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, tab);

		Kragle.register();
		KragleGuard.register();

		LOGGER.info("[{}] Loaded", MOD_ID);
	}
}
