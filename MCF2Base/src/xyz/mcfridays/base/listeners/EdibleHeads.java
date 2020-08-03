package xyz.mcfridays.base.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EdibleHeads implements Listener {
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player p = e.getPlayer();
			if (p.getItemInHand() != null) {
				if (p.getItemInHand().getType() == Material.SKULL_ITEM) {
					MaterialData data = e.getItem().getData();

					if (data.getData() == 3) {
						if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {

							e.setCancelled(true);

							if (p.getItemInHand().getAmount() > 1) {
								p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
							} else {
								p.setItemInHand(new ItemStack(Material.AIR));
							}

							p.getLocation().getWorld().playSound(p.getLocation(), Sound.EAT, 1F, 1F);
							p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0));
						}
					}
				}
			}
		}
	}
}