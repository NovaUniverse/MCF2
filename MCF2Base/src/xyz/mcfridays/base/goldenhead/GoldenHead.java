package xyz.mcfridays.base.goldenhead;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.zeeraa.novacore.spigot.module.NovaModule;
import xyz.mcfridays.base.utils.SkullUtils;

public class GoldenHead extends NovaModule implements Listener {
	@Override
	public String getName() {
		return "GoldenHead";
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() throws Exception {
		ShapedRecipe recipe = new ShapedRecipe(getItem());

		MaterialData skull = new MaterialData(Material.SKULL_ITEM);

		skull.setData((byte) 3);

		recipe.shape("AAA", "ABA", "AAA");
		recipe.setIngredient('A', Material.GOLD_INGOT);
		recipe.setIngredient('B', skull);
		
		Bukkit.getServer().addRecipe(recipe);
	}

	public ItemStack getItem() {
		ItemStack stack = SkullUtils.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjkzN2UxYzQ1YmI4ZGEyOWIyYzU2NGRkOWE3ZGE3ODBkZDJmZTU0NDY4YTVkZmI0MTEzYjRmZjY1OGYwNDNlMSJ9fX0=");

		ItemMeta meta = stack.getItemMeta();

		meta.setDisplayName(ChatColor.GOLD + "Golden head");

		stack.setItemMeta(meta);
		
		stack = NBTEditor.set(stack, 1, "mcf", "goldenhead");

		return stack;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (NBTEditor.contains(e.getItemInHand(), "mcf", "goldenhead")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getItem() != null) {
			if (NBTEditor.contains(e.getItem(), "mcf", "goldenhead")) {
				if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					e.setCancelled(true);
					Player p = e.getPlayer();
					if (e.getItem().getAmount() > 1) {
						e.getItem().setAmount(e.getItem().getAmount() - 1);
					} else {
						if (p.getItemInHand().getAmount() == 1) {
							p.setItemInHand(null);
						} else {
							boolean removed = false;
							for (int i = 0; i < p.getInventory().getSize(); i++) {
								ItemStack item = p.getInventory().getItem(i);
								if (item != null) {
									if (item.getType() != Material.AIR) {
										if (NBTEditor.contains(item, "mcf", "goldenhead")) {
											if (item.getAmount() > 1) {
												item.setAmount(item.getAmount() - 1);
												removed = true;
												break;
											} else {
												p.getInventory().setItem(i, null);
												removed = true;
												break;
											}
										}
									}
								}
							}

							if (!removed) {
								return;
							}
						}
					}
					p.getWorld().playSound(p.getLocation(), Sound.EAT, 1F, 1F);

					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 2));
					p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60 * 20, 1));
				}
			}
		}
	}
}