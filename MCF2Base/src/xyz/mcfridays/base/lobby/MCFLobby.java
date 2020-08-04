package xyz.mcfridays.base.lobby;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.score.ScoreManager;
import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.abstraction.events.VersionIndependantPlayerAchievementAwardedEvent;
import xyz.zeeraa.novacore.log.Log;
import xyz.zeeraa.novacore.module.NovaModule;
import xyz.zeeraa.novacore.module.modules.multiverse.MultiverseManager;
import xyz.zeeraa.novacore.module.modules.multiverse.MultiverseWorld;
import xyz.zeeraa.novacore.module.modules.multiverse.WorldUnloadOption;
import xyz.zeeraa.novacore.module.modules.scoreboard.NetherBoardScoreboard;
import xyz.zeeraa.novacore.teams.Team;
import xyz.zeeraa.novacore.utils.PlayerUtils;

public class MCFLobby extends NovaModule implements Listener {
	private static MCFLobby instance;

	private Location lobbyLocation;
	private Location kotlLocation;

	private double kotlRadius;

	private int taskId1;
	private int taskId2;

	private MultiverseWorld multiverseWorld;

	public static MCFLobby getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "MCFLobby";
	}

	@Override
	public void onLoad() {
		MCFLobby.instance = this;
		this.addDependency(NetherBoardScoreboard.class);
		this.addDependency(MultiverseManager.class);
		this.taskId1 = -1;
		this.taskId2 = -1;
		this.lobbyLocation = null;
		this.multiverseWorld = null;
	}

	@Override
	public void onEnable() throws Exception {
		multiverseWorld = MultiverseManager.getInstance().createFromFile(new File(MCF.getInstance().getDataFolder().getPath() + File.separator + "mcf_lobby"), WorldUnloadOption.DELETE);

		NetherBoardScoreboard.getInstance().setGlobalLine(0, ChatColor.YELLOW + "" + ChatColor.BOLD + "Lobby");
		if (taskId1 == -1) {
			taskId1 = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (Player player : lobbyLocation.getWorld().getPlayers()) {
						player.setFoodLevel(20);
						if (player.getLocation().getY() < -3) {
							player.teleport(lobbyLocation);
							player.setFallDistance(0);
						}
					}
				}
			}, 5L, 5L);
		}

		if (taskId2 == -1) {
			taskId2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (NovaCore.getInstance().hasTeamManager()) {
						for (Team team : NovaCore.getInstance().getTeamManager().getTeams()) {
							for(UUID uuid : team.getMembers()) {
								// Load all players into score cache so that they get displayed in the leader board
								ScoreManager.getInstance().getPlayerScore(uuid);
							}
						}
					}
				}
			}, 200L, 200L);
		}
	}

	@Override
	public void onDisable() {
		if (taskId1 != -1) {
			Bukkit.getScheduler().cancelTask(taskId1);
			taskId1 = -1;
		}

		if (taskId2 != -1) {
			Bukkit.getScheduler().cancelTask(taskId2);
			taskId2 = -1;
		}

		MultiverseManager.getInstance().unload(multiverseWorld);
		multiverseWorld = null;
	}

	public Location getLobbyLocation() {
		return lobbyLocation;
	}

	public void setLobbyLocation(Location lobbyLocation) {
		this.lobbyLocation = lobbyLocation;
		lobbyLocation.setWorld(multiverseWorld.getWorld());
	}

	public World getWorld() {
		return multiverseWorld.getWorld();
	}

	public void setKOTLLocation(double x, double z, double radius) {
		this.kotlRadius = radius;
		this.kotlLocation = new Location(multiverseWorld.getWorld(), x, 0, z);
	}

	private boolean isInKOTLArena(Entity entity) {
		if (kotlLocation != null) {
			if (entity.getWorld() == kotlLocation.getWorld()) {
				Location kotlCheck = kotlLocation.clone();

				kotlCheck.setY(entity.getLocation().getY());

				return kotlCheck.distance(entity.getLocation()) <= kotlRadius;
			}
		}

		return false;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		PlayerUtils.clearPlayerInventory(p);
		PlayerUtils.clearPotionEffects(p);
		PlayerUtils.resetPlayerXP(p);
		if (lobbyLocation != null) {
			p.teleport(lobbyLocation);
		}
		p.setFallDistance(0);
		p.setGameMode(GameMode.ADVENTURE);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVersionIndependantPlayerAchievementAwarded(VersionIndependantPlayerAchievementAwardedEvent e) {
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (lobbyLocation != null) {
				if (e.getEntity().getWorld() == lobbyLocation.getWorld()) {
					if (isInKOTLArena(e.getEntity())) {
						e.setDamage(0);
						e.setCancelled(false);
						Log.trace("KOTL", "Allow damage event for player " + e.getEntity().getName() + " due to being inside the KTOL arena");
					} else {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		if (player.getWorld() == lobbyLocation.getWorld()) {
			if (player.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		if (player.getWorld() == lobbyLocation.getWorld()) {
			if (player.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}
}