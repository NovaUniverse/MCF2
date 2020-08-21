package net.novauniverse.games.bingo.game;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.zeeraa.novacore.utils.ItemBuilder;

public class BingoItemGenerator {
	private static ArrayList<ItemStack> possibleItems = new ArrayList<ItemStack>();

	public static ArrayList<ItemStack> getPossibleItems() {
		return possibleItems;
	}

	public static ArrayList<ItemStack> generate() {
		return BingoItemGenerator.generate(9);
	}

	public static ArrayList<ItemStack> generate(int count) {
		if (possibleItems.size() == 0) {
			fillItems();
		}

		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		ArrayList<ItemStack> possibleItemsClone = new ArrayList<ItemStack>();

		for (ItemStack item : possibleItems) {
			possibleItemsClone.add(item.clone());
		}

		Collections.shuffle(possibleItemsClone);

		for (int i = 0; i < count; i++) {
			if (possibleItemsClone.size() > 0) {
				result.add(possibleItemsClone.remove(0));
			}
		}

		return result;
	}

	private static void fillItems() {
		possibleItems.add(new ItemBuilder(Material.COOKED_BEEF).build());
		possibleItems.add(new ItemBuilder(Material.WOOL).build());
		possibleItems.add(new ItemBuilder(Material.STONE).build());
		possibleItems.add(new ItemBuilder(Material.COBBLESTONE).build());
		possibleItems.add(new ItemBuilder(Material.DIAMOND).build());
		possibleItems.add(new ItemBuilder(Material.NETHERRACK).build());
		possibleItems.add(new ItemBuilder(Material.OBSIDIAN).build());
		possibleItems.add(new ItemBuilder(Material.PAPER).build());
		possibleItems.add(new ItemBuilder(Material.SUGAR_CANE).build());
		possibleItems.add(new ItemBuilder(Material.SUGAR).build());
		possibleItems.add(new ItemBuilder(Material.APPLE).build());
		possibleItems.add(new ItemBuilder(Material.BLAZE_ROD).build());
		possibleItems.add(new ItemBuilder(Material.DIAMOND_HOE).build());
		possibleItems.add(new ItemBuilder(Material.BUCKET).build());
		possibleItems.add(new ItemBuilder(Material.STRING).build());
		possibleItems.add(new ItemBuilder(Material.BONE).build());
		possibleItems.add(new ItemBuilder(Material.BREAD).build());
		possibleItems.add(new ItemBuilder(Material.SULPHUR).build());
		possibleItems.add(new ItemBuilder(Material.GLASS_BOTTLE).build());
		possibleItems.add(new ItemBuilder(Material.GOLD_INGOT).build());
		possibleItems.add(new ItemBuilder(Material.REDSTONE_TORCH_ON).build());
	}
}