package xyz.mcfridays.base.command.top;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaCommand;
import net.zeeraa.novacore.spigot.language.LanguageManager;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;
import xyz.mcfridays.base.MCF;

public class TopCommand extends NovaCommand {
	private Map<UUID, Integer> cooldownList;

	public TopCommand() {
		super("top", MCF.getInstance());
		setDescription("Teleport to the top of the world");
		setPermission("mcf.command.top");
		setPermissionDefaultValue(PermissionDefault.TRUE);
		setAllowedSenders(AllowedSenders.PLAYERS);

		setEmptyTabMode(true);

		this.setFilterAutocomplete(true);

		cooldownList = new HashMap<UUID, Integer>();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (UUID uuid : cooldownList.keySet()) {
					if (cooldownList.get(uuid) <= 1) {
						cooldownList.remove(uuid);

						Player player = Bukkit.getServer().getPlayer(uuid);
						if (player != null) {
							if (player.isOnline()) {
								player.sendMessage(LanguageManager.getString(player, "mcf.command.top.can_use_again"));
							}
						}
						continue;
					}

					cooldownList.put(uuid, cooldownList.get(uuid) - 1);
				}
			}
		}, 20L, 20L);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		Player p = (Player) sender;

		if (p.getLocation().getWorld().getEnvironment() == Environment.NETHER) {
			p.sendMessage(LanguageManager.getString(p, "mcf.command.top.nether"));
			return false;
		}

		if (cooldownList.containsKey(p.getUniqueId())) {
			// p.sendMessage(ChatColor.RED + "Please wait " +
			// cooldownList.get(p.getUniqueId()) + " seconds before using this command
			// again");
			p.sendMessage(LanguageManager.getString(p, "mcf.command.top.wait", cooldownList.get(p.getUniqueId())));
			return false;
		}

		if (p.getGameMode() == GameMode.SPECTATOR) {
			p.sendMessage(LanguageManager.getString(p, "mcf.command.top.spectator"));
			return false;
		}

		if (!ModuleManager.isEnabled(GameManager.class)) {
			p.sendMessage(LanguageManager.getString(p, "mcf.command.top.disabled.world"));
			return false;
		} else {
			if (GameManager.getInstance().hasGame()) {
				if (!GameManager.getInstance().getActiveGame().getWorld().getUID().toString().equalsIgnoreCase(p.getWorld().getUID().toString())) {
					p.sendMessage(LanguageManager.getString(p, "mcf.command.top.disabled.world"));
					return false;
				}
			} else {
				p.sendMessage(LanguageManager.getString(p, "mcf.command.top.disabled.world"));
				return false;
			}
		}

		if (!MCF.getInstance().isTopEnabled()) {
			p.sendMessage(LanguageManager.getString(p, "mcf.command.top.disabled"));
			return false;
		}

		Location location = p.getLocation().clone();

		location.setY(256);

		for (int i = 255; i > 1; i--) {
			location.setY(i);

			if (location.getBlock() != null) {
				if (location.getBlock().getType() != Material.AIR) {
					location.add(0, 1, 0);
					p.teleport(location);
					cooldownList.put(p.getUniqueId(), 60);
					return true;
				}
			}
		}

		p.sendMessage(LanguageManager.getString(p, "mcf.command.top.failed"));

		return false;
	}
}