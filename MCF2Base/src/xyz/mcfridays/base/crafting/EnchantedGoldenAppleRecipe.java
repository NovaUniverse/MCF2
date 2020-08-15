package xyz.mcfridays.base.crafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import net.zeeraa.novacore.customcrafting.CustomRecipe;

public class EnchantedGoldenAppleRecipe extends CustomRecipe {

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1));

		recipe.shape("AAA", "ABA", "AAA");
		recipe.setIngredient('A', Material.GOLD_BLOCK);
		recipe.setIngredient('B', Material.APPLE);

		return recipe;
	}

	@Override
	public String getName() {
		return "Enchanted golden apple";
	}
}