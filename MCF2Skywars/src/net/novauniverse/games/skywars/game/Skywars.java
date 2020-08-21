package net.novauniverse.games.skywars.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.meta.FireworkMeta;

import net.novauniverse.games.skywars.NovaSkywars;
import net.zeeraa.novacore.NovaCore;
import net.zeeraa.novacore.callbacks.Callback;
import net.zeeraa.novacore.log.Log;
import net.zeeraa.novacore.module.modules.game.MapGame;
import net.zeeraa.novacore.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.teams.Team;
import net.zeeraa.novacore.timers.BasicTimer;
import net.zeeraa.novacore.timers.TickCallback;
import net.zeeraa.novacore.utils.PlayerUtils;
import net.zeeraa.novacore.utils.RandomGenerator;

public class Skywars extends MapGame implements Listener {
	private boolean started;
	private boolean ended;

	private boolean noFallEnabled;

	private final int countdownTime = 20;

	private ArrayList<Location> placedBlocks;
	private ArrayList<Location> teamStartLocation;

	private boolean countdownOver;

	public Skywars() {
		this.started = false;
		this.ended = false;

		this.noFallEnabled = false;
		this.placedBlocks = new ArrayList<Location>();
		this.teamStartLocation = new ArrayList<Location>();
	}

	@Override
	public String getName() {
		return "skywars";
	}

	@Override
	public String getDisplayName() {
		return "Skywars";
	}

	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return NovaSkywars.getInstance().isAllowReconnect() ? PlayerQuitEliminationAction.DELAYED : PlayerQuitEliminationAction.INSTANT;
	}
	
	@Override
	public int getPlayerEliminationDelay() {
		return NovaSkywars.getInstance().getReconnectTime();
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
	public boolean autoEndGame() {
		return true;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		return countdownOver;
	}

	@Override
	public void onStart() {
		if (started) {
			return;
		}
		started = true;

		getActiveMap().getWorld().setGameRuleValue("doMobSpawning", "false");
		getActiveMap().getWorld().setDifficulty(Difficulty.PEACEFUL);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(NovaSkywars.getInstance(), new Runnable() {
			@Override
			public void run() {
				getActiveMap().getWorld().setDifficulty(Difficulty.NORMAL);
			}
		}, 200L);

		setCages(true);

		Collections.shuffle(getActiveMap().getStarterLocations());
		Log.debug("Start location count: " + getActiveMap().getStarterLocations().size());
		for (int i = 0; i < 12; i++) {
			teamStartLocation.add(getActiveMap().getStarterLocations().get(i));
		}

		ArrayList<Player> toTeleport = new ArrayList<Player>();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (players.contains(player.getUniqueId())) {
				toTeleport.add(player);
			} else {
				tpToSpectator(player);
			}
		}

		for (Player p : toTeleport) {
			try {
				this.tpToArena(p);
			} catch (Exception e) {
				p.sendMessage(ChatColor.DARK_RED + "Teleport failed: " + e.getClass().getName() + ". Please contact an admin");
			}
		}

		this.started = true;

		BasicTimer startTimer = new BasicTimer(countdownTime, 20L);
		startTimer.addFinishCallback(new Callback() {
			@Override
			public void execute() {
				countdownOver = true;

				setCages(false);

				noFallEnabled = true;
				Bukkit.getScheduler().scheduleSyncDelayedTask(NovaSkywars.getInstance(), new Runnable() {
					@Override
					public void run() {
						noFallEnabled = false;
					}
				}, 5 * 20);

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
					if (NovaCore.getInstance().getActionBar() != null) {
						NovaCore.getInstance().getActionBar().sendMessage(player, ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in: " + ChatColor.AQUA + ChatColor.BOLD + timeLeft);
					}
				}

				if (NovaCore.getInstance().getActionBar() == null) {
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in: " + ChatColor.AQUA + ChatColor.BOLD + timeLeft);
				} else {
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starting in: " + ChatColor.AQUA + ChatColor.BOLD + timeLeft);
				}
			}
		});

		startTimer.start();
	}

	@Override
	public void onEnd() {
		if (ended) {
			return;
		}

		ended = true;

		for (Location location : getActiveMap().getStarterLocations()) {
			Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
			FireworkMeta fwm = fw.getFireworkMeta();

			fwm.setPower(2);
			fwm.addEffect(RandomGenerator.randomFireworkEffect());

			fw.setFireworkMeta(fwm);
		}

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			PlayerUtils.clearPlayerInventory(p);
			PlayerUtils.resetPlayerXP(p);
			p.setGameMode(GameMode.SPECTATOR);
			p.playSound(p.getLocation(), Sound.WITHER_DEATH, 1F, 1F);
		}
	}

	public void setCages(boolean state) {
		if (hasActiveMap()) {
			for (Location location : getActiveMap().getStarterLocations()) {
				setStartCage(location, state);
			}
		}
	}

	public void setStartCage(Location location, boolean state) {
		Material material = state ? Material.BARRIER : Material.AIR;

		for (int x = -2; x < 3; x++) {
			for (int y = 0; y < 5; y++) {
				for (int z = -2; z < 3; z++) {
					location.clone().add(x, y - 1, z).getBlock().setType(material);
				}
			}
		}

		if (state == true) {
			for (int x = -1; x < 2; x++) {
				for (int y = 0; y < 3; y++) {
					for (int z = -1; z < 2; z++) {
						location.clone().add(x, y, z).getBlock().setType(Material.AIR);
					}
				}
			}
		}
	}

	private HashMap<UUID, Integer> teamNumbers = new HashMap<UUID, Integer>();
	
	public void tpToArena(Player player) {
		if (hasActiveMap()) {
			Team team =  NovaCore.getInstance().getTeamManager().getPlayerTeam(player);

			if (team != null) {
				Log.trace("Teleporting " + player.getName() + " to their starting location");
				try {
					int teamNumber = 0;
					
					if(teamNumbers.containsKey(team.getTeamUuid())) {
						teamNumber = teamNumbers.get(team.getTeamUuid());
					} else {
						teamNumber = teamNumbers.size() + 1;
						
						teamNumbers.put(team.getTeamUuid(), teamNumber);
					}
					
					Location location = teamStartLocation.get(teamNumber - 1);
					tpToArena(player, location);
				} catch (Exception e) {
					e.printStackTrace();
					Log.error("tpToArena() failed for player: " + player.getName());
					player.sendMessage(ChatColor.RED + "Tp failure ERR:EXCEPTION");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Tp failure ERR:TEAM_NULL");
			}
		} else {
			Log.error("tpToArena() called without map");
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
		PlayerUtils.clearPlayerInventory(player);
		PlayerUtils.clearPotionEffects(player);
		PlayerUtils.resetPlayerXP(player);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.teleport(location);
		player.setGameMode(GameMode.SURVIVAL);
	}

	@Override
	public void tpToSpectator(Player player) {
		if (hasActiveMap()) {
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.clearPotionEffects(player);
			PlayerUtils.resetPlayerXP(player);
			player.setGameMode(GameMode.SPECTATOR);
			player.setHealth(player.getMaxHealth());
			player.teleport(getActiveMap().getSpectatorLocation());
		} else {
			System.err.println("tpToSpectator() called without map");
		}
	}

	@Override
	public void onPlayerRespawn(Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(NovaCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				tpToSpectator(player);
			}
		}, 5L);

	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getCause() == DamageCause.FALL) {
				if (noFallEnabled) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

		if (players.contains(p.getUniqueId())) {

			if (p.getGameMode() == GameMode.CREATIVE) {
				// Allow
				return;
			}

			if (e.getBlock().getType() == Material.CHEST || e.getBlock().getType() == Material.TRAPPED_CHEST || e.getBlock().getType() == Material.ENDER_CHEST) {
				// Deny
				p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are not allowed to place that block");
				e.setCancelled(true);
				return;
			}

			// Allow
			if (!placedBlocks.contains(e.getBlock().getLocation())) {
				placedBlocks.add(e.getBlock().getLocation());
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();

		if (players.contains(p.getUniqueId())) {
			if (e.getBlock().getType() == Material.CHEST || e.getBlock().getType() == Material.TRAPPED_CHEST || e.getBlock().getType() == Material.ENDER_CHEST) {
				// Deny
				p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are not allowed to break that block");
				e.setCancelled(true);
				return;
			}
		}
	}
}