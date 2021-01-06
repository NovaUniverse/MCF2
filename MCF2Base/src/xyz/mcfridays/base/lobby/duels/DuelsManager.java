package xyz.mcfridays.base.lobby.duels;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.rayzr522.jsonmessage.JSONMessage;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseWorld;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldUnloadOption;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;
import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.lobby.MCFLobby;

public class DuelsManager extends NovaModule implements Listener {
	private static DuelsManager instance;

	private List<DuelInstance> duelInstances;
	private HashMap<UUID, ItemStack[]> inventoryContent;
	private HashMap<UUID, Location> playerLocation;
	private HashMap<UUID, GameMode> playerGamemode;

	// The key is the invite id and the value is the UUID of the player that
	// requested a duel
	private HashMap<UUID, UUID> invites;

	private int taskId;

	public static DuelsManager getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "MCFLobbbyDuels";
	}

	@Override
	public void onLoad() {
		DuelsManager.instance = this;

		this.duelInstances = new ArrayList<DuelInstance>();
		this.inventoryContent = new HashMap<UUID, ItemStack[]>();
		this.playerLocation = new HashMap<UUID, Location>();
		this.playerGamemode = new HashMap<UUID, GameMode>();
		this.invites = new HashMap<UUID, UUID>();

		this.taskId = -1;
	}

	@Override
	public void onEnable() throws Exception {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (DuelInstance duelInstance : duelInstances) {
						for (Player player : duelInstance.getPlayers()) {
							if (duelInstance.getStage() == DuelStage.INGAME) {
								if (player.getGameMode() == GameMode.ADVENTURE) {
									if (player.getLocation().getY() <= 19) {
										Player otherPlayer = null;

										for (Player p : duelInstance.getPlayers()) {
											p.setGameMode(GameMode.SPECTATOR);

											if (p.getUniqueId() != player.getUniqueId()) {
												otherPlayer = p;
											}
										}

										duelInstance.getWorld().getWorld().strikeLightning(player.getLocation());
										duelInstance.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Player Eliminated> " + ChatColor.AQUA + ChatColor.BOLD + player.getName() + ChatColor.RED + "" + ChatColor.BOLD + " died");
										duelInstance.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Game Over> Winner: " + ChatColor.AQUA + ChatColor.BOLD + (otherPlayer != null ? otherPlayer.getName() : "null"));
										Bukkit.getServer().broadcastMessage(ChatColor.AQUA + (otherPlayer != null ? otherPlayer.getName() : "null") + ChatColor.GOLD + " won a duel against " + ChatColor.AQUA + player.getName());
										duelInstance.setStage(DuelStage.ENDED);
									}
								}
							}
						}
					}
				}
			}, 5L, 5L);
		}
	}

	@Override
	public void onDisable() throws Exception {
		int failsafe = 0;
		while (duelInstances.size() > 0) {
			killInstance(duelInstances.get(0));

			failsafe++;
			if (failsafe > 10000) {
				Log.error("Duels", "Canceling attempts to kill duels instances after 10000 tries. Manual cleanup might be needed");
				break;
			}
		}

		duelInstances.clear();
		inventoryContent.clear();
		playerLocation.clear();
		playerGamemode.clear();
		invites.clear();
	}

	public boolean isInDuel(Player player) {
		for (DuelInstance i : duelInstances) {
			if (i.getPlayers().contains(player)) {
				return true;
			}
		}

		return false;
	}

	public boolean duel(Player player1, Player player2) {
		if (player1.getUniqueId() == player2.getUniqueId()) {
			return false;
		}

		try {
			UUID uuid = UUID.randomUUID();

			String worldName = "duels_" + uuid.toString().replace("-", "");

			MultiverseWorld world = MultiverseManager.getInstance().createFromFile(new File(MCF.getInstance().getDataFolder().getPath() + File.separator + "duels_world"), worldName, WorldUnloadOption.DELETE);

			world.getWorld().setDifficulty(Difficulty.PEACEFUL);
			world.getWorld().setTime(1000L);
			world.getWorld().setStorm(false);
			world.getWorld().setThundering(false);

			List<Player> players = new ArrayList<Player>();

			players.add(player1);
			players.add(player2);

			Bukkit.getScheduler().scheduleSyncDelayedTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					inventoryContent.put(player1.getUniqueId(), player1.getInventory().getContents());
					inventoryContent.put(player2.getUniqueId(), player2.getInventory().getContents());

					PlayerUtils.clearPotionEffects(player1);
					PlayerUtils.clearPotionEffects(player2);

					PlayerUtils.clearPlayerInventory(player1);
					PlayerUtils.clearPlayerInventory(player2);
				}
			}, 5L);

			DuelInstance instance = new DuelInstance(uuid, world, players);

			duelInstances.add(instance);

			instance.setStage(DuelStage.COUNTDOWN);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.fatal("Failed to start duels due to an " + e.getClass().getName());

			player1.sendMessage(ChatColor.RED + "Failed to start duels due to an error.\n" + e.getClass().getName());
			player2.sendMessage(ChatColor.RED + "Failed to start duels due to an error.\n" + e.getClass().getName());
		}
		return false;
	}

	public void restorePlayer(Player player) {
		if (inventoryContent.containsKey(player.getUniqueId())) {
			PlayerUtils.clearPlayerInventory(player);
			player.getInventory().setContents(inventoryContent.get(player.getUniqueId()));
			inventoryContent.remove(player.getUniqueId());
		}

		if (playerLocation.containsKey(player.getUniqueId())) {
			player.teleport(playerLocation.get(player.getUniqueId()));
			playerLocation.remove(player.getUniqueId());
		}

		if (playerGamemode.containsKey(player.getUniqueId())) {
			player.setGameMode(playerGamemode.get(player.getUniqueId()));
			playerGamemode.remove(player.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = (Player) e.getPlayer();

		// TODO: End game if a player quits
		for (DuelInstance di : duelInstances) {
			if (di.getPlayers().contains(player)) {
				di.onPlayerQuit(player);
				break;
			}
		}

		if (inventoryContent.containsKey(player.getUniqueId())) {
			inventoryContent.remove(player.getUniqueId());
		}

		if (playerLocation.containsKey(player.getUniqueId())) {
			playerLocation.remove(player.getUniqueId());
		}

		if (playerGamemode.containsKey(player.getUniqueId())) {
			playerGamemode.remove(player.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			for (DuelInstance duelInstace : duelInstances) {
				if (duelInstace.getPlayers().contains(player)) {
					if (duelInstace.getStage() == DuelStage.INGAME) {
						e.setDamage(0);
					} else {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	public void killInstance(DuelInstance duelInstance) {
		if (duelInstances.contains(duelInstance)) {
			Log.trace("Killing duels instance " + duelInstance.getInstanceUUID().toString());

			for (Player player : duelInstance.getPlayers()) {
				if (player.isOnline()) {
					restorePlayer(player);
				}
			}

			for (Player player : duelInstance.getWorld().getWorld().getPlayers()) {
				if (MCFLobby.getInstance().getLobbyLocation() != null) {
					player.teleport(MCFLobby.getInstance().getLobbyLocation());
					player.setGameMode(GameMode.ADVENTURE);
				} else {
					player.kickPlayer("Unloading duels world. Please reconnect");
				}
			}

			duelInstance.getPlayers().clear();

			duelInstances.remove(duelInstance);

			MultiverseManager.getInstance().unload(duelInstance.getWorld());
		} else {
			Log.debug("Duels", "Tried to kill instance that was not found in instance list");
		}
	}

	public boolean createInvite(Player from, Player to) {
		if (from.getUniqueId() == to.getUniqueId()) {
			return false;
		}

		if (!from.isOnline() || !to.isOnline()) {
			return false;
		}

		UUID inviteUuid = UUID.randomUUID();

		invites.put(inviteUuid, from.getUniqueId());

		Log.debug(from.getName() + " invited " + to.getName() + " to a duel");
		JSONMessage.create(from.getName() + " has sent you a duel request. Click this message to accept").color(ChatColor.GREEN).runCommand("/acceptduel " + inviteUuid.toString()).send(to);

		Bukkit.getScheduler().scheduleSyncDelayedTask(MCF.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (invites.containsKey(inviteUuid)) {
					Log.trace("Duel request " + inviteUuid.toString() + " has expired");
					invites.remove(inviteUuid);
				}
			}
		}, 60 * 20); // 1 Minute

		return true;
	}

	/**
	 * Try to accept a duels request
	 * 
	 * @param player The {@link Player} that is trying to accept a request
	 * @param invite The invite id
	 * @return See values of {@link InviteResult}
	 */
	public InviteResult acceptInvite(Player player, String invite) {
		UUID inviteUuid;
		try {
			inviteUuid = UUID.fromString(invite);
		} catch (Exception e) {
			return InviteResult.INVALID;
		}

		if (invites.containsKey(inviteUuid)) {
			Player targetPlayer = Bukkit.getServer().getPlayer(invites.get(inviteUuid));

			if (targetPlayer != null) {
				if (isInDuel(targetPlayer)) {
					// Player is busy
					return InviteResult.PLAYER_BUSY;
				} else {
					if (duel(targetPlayer, player)) {
						invites.remove(inviteUuid);

						// Success
						return InviteResult.OK;
					}
					// Failed to start
					return InviteResult.START_FAILURE;
				}
			}
		}

		// Player is not online or the invite has expired
		return InviteResult.INVALID;
	}
}