package xyz.mcfridays.games.hungergames.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import xyz.zeeraa.ezcore.EZCore;
import xyz.zeeraa.ezcore.callbacks.Callback;
import xyz.zeeraa.ezcore.log.EZLogger;
import xyz.zeeraa.ezcore.module.game.MapGame;
import xyz.zeeraa.ezcore.module.game.PlayerQuitEliminationAction;
import xyz.zeeraa.ezcore.teams.Team;
import xyz.zeeraa.ezcore.timers.BasicTimer;
import xyz.zeeraa.ezcore.timers.TickCallback;
import xyz.zeeraa.ezcore.utils.PlayerUtils;

public class Hungergames extends MapGame implements Listener {
	private boolean started;

	private final boolean randomStartLocation = false;
	private final int countdownTime = 20;

	private HashMap<UUID, Location> usedStartLocation;

	private boolean countdownOver;

	public Hungergames() {
		super();

		this.started = false;

		this.countdownOver = false;

		this.usedStartLocation = new HashMap<UUID, Location>();
	}

	@Override
	public String getName() {
		return "hungergames";
	}

	public String getDisplayName() {
		return "Hungergames";
	}

	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return PlayerQuitEliminationAction.DELAYED;
	}

	@Override
	public boolean eliminatePlayerOnDeath(Player player) {
		return true;
	}

	@Override
	public boolean isPVPEnabled() {
		return countdownOver;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		return countdownOver;
	}

	public boolean setCages(boolean state) {
		if (!hasActiveMap()) {
			return false;
		}

		for (Location location : getActiveMap().getStarterLocations()) {
			setStartCage(location, state);
		}

		return true;
	}

	public void setStartCage(Location location, boolean state) {
		Material material = state ? Material.BARRIER : Material.AIR;

		location.clone().add(1, 0, 0).getBlock().setType(material);
		location.clone().add(-1, 0, 0).getBlock().setType(material);
		location.clone().add(0, 0, 1).getBlock().setType(material);
		location.clone().add(0, 0, -1).getBlock().setType(material);

		location.clone().add(1, 1, 0).getBlock().setType(material);
		location.clone().add(-1, 1, 0).getBlock().setType(material);
		location.clone().add(0, 1, 1).getBlock().setType(material);
		location.clone().add(0, 1, -1).getBlock().setType(material);

		location.clone().add(0, 2, 0).getBlock().setType(material);
	}

	private void tpToSpectator(Player player) {
		player.setMaxHealth(20);
		player.setHealth(20);
		player.teleport(getActiveMap().getSpectatorLocation());
	}

	public void tpToArena(Player player) {
		if (hasActiveMap()) {
			for (int i = 0; i < getActiveMap().getStarterLocations().size(); i++) {
				Location location = getActiveMap().getStarterLocations().get(i);

				if (usedStartLocation.containsValue(location)) {
					continue;
				}

				this.tpToArena(player, location);
				return;
			}
			Random random = new Random();
			Location backupLocation = getActiveMap().getStarterLocations().get(random.nextInt(getActiveMap().getStarterLocations().size()));

			this.tpToArena(player, backupLocation);
		} else {
			System.err.println("tpToArena() called without map");
		}
	}

	/**
	 * Teleport a player to a provided start location
	 * 
	 * @param player   {@link Player} to teleport
	 * @param location {@link Location} to teleport the player to
	 */
	protected void tpToArena(Player player, Location location) {
		player.teleport(location.getWorld().getSpawnLocation());
		usedStartLocation.put(player.getUniqueId(), location);
		PlayerUtils.clearPlayerInventory(player);
		PlayerUtils.clearPotionEffects(player);
		PlayerUtils.resetPlayerXP(player);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.teleport(location);
		player.setGameMode(GameMode.SURVIVAL);
	}

	@Override
	public void onEnd() {

	}

	@Override
	public void onStart() {
		ArrayList<Player> toTeleport = new ArrayList<Player>();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (players.contains(player.getUniqueId())) {
				EZLogger.trace(player.getName() + " is in the player list");
				toTeleport.add(player);
			} else {
				tpToSpectator(player);
			}
		}

		if (randomStartLocation) {
			Collections.shuffle(getActiveMap().getStarterLocations());
			Collections.shuffle(toTeleport);
		} else {
			if (EZCore.getInstance().hasTeamManager()) {
				ArrayList<UUID> teamOrder = new ArrayList<UUID>();

				for (Team team : EZCore.getInstance().getTeamManager().getTeams()) {
					teamOrder.add(team.getTeamUuid());
				}

				Collections.shuffle(teamOrder);

				ArrayList<Player> toTeleportReal = new ArrayList<Player>();

				for (UUID uuid : teamOrder) {
					for (Player pt : toTeleport) {
						if (EZCore.getInstance().getTeamManager().getPlayerTeam(pt).getTeamUuid().equals(uuid)) {
							toTeleportReal.add(pt);
						}
					}
				}
				toTeleport = toTeleportReal;
			}
		}

		EZLogger.debug("Final toTeleport size: " + toTeleport.size());

		for (Location location : getActiveMap().getStarterLocations()) {
			setStartCage(location, true);
		}

		for (Player player : toTeleport) {
			tpToArena(player);
		}

		this.started = true;

		BasicTimer startTimer = new BasicTimer(countdownTime, 20L);
		startTimer.addFinishCallback(new Callback() {
			@Override
			public void execute() {
				countdownOver = true;
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "May the odds be ever in your favor");

				setCages(false);

				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 2F);
				}
			}
		});

		startTimer.addTickCallback(new TickCallback() {
			@Override
			public void execute(int timeLeft) {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 1.3F);
					if (EZCore.getInstance().getActionBar() != null) {
						EZCore.getInstance().getActionBar().sendMessage(player, ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in: " + ChatColor.AQUA + ChatColor.BOLD + timeLeft);
					}
				}

				if (EZCore.getInstance().getActionBar() == null) {
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in: " + ChatColor.AQUA + ChatColor.BOLD + timeLeft);
				} else {
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in: " + ChatColor.AQUA + ChatColor.BOLD + timeLeft);
				}
			}
		});

		startTimer.start();
	}

	@Override
	public void onPlayerRespawn(Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(EZCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				player.setGameMode(GameMode.SPECTATOR);
				player.setHealth(20);
				if (hasActiveMap()) {
					player.teleport(getActiveMap().getSpectatorLocation());
				}
			}
		}, 5L);
		
	}
}