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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;

import xyz.mcfridays.base.MCF;
import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.callbacks.Callback;
import xyz.zeeraa.novacore.log.Log;
import xyz.zeeraa.novacore.module.modules.game.MapGame;
import xyz.zeeraa.novacore.module.modules.game.elimination.PlayerQuitEliminationAction;
import xyz.zeeraa.novacore.teams.Team;
import xyz.zeeraa.novacore.timers.BasicTimer;
import xyz.zeeraa.novacore.timers.TickCallback;
import xyz.zeeraa.novacore.utils.PlayerUtils;
import xyz.zeeraa.novacore.utils.RandomGenerator;

public class Hungergames extends MapGame implements Listener {
	private boolean started;
	private boolean ended;

	private final boolean randomStartLocation = false;
	private final int countdownTime = 20;

	private HashMap<UUID, Location> usedStartLocation;
	
	private boolean countdownOver;

	public Hungergames() {
		super();

		this.started = false;
		this.ended = false;

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

	public void tpToSpectator(Player player) {
		NovaCore.getInstance().getVersionIndependentUtils().resetEntityMaxHealth(player);
		player.setHealth(20);
		player.setGameMode(GameMode.SPECTATOR);
		if (hasActiveMap()) {
			player.teleport(getActiveMap().getSpectatorLocation());
		}
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
		try {
			MCF.getInstance().setServerAsActive(false);
		} catch (Exception ex) {
			ex.printStackTrace();
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

	@Override
	public void onStart() {
		ArrayList<Player> toTeleport = new ArrayList<Player>();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (players.contains(player.getUniqueId())) {
				Log.trace(player.getName() + " is in the player list");
				toTeleport.add(player);
			} else {
				tpToSpectator(player);
			}
		}

		if (randomStartLocation) {
			Collections.shuffle(getActiveMap().getStarterLocations());
			Collections.shuffle(toTeleport);
		} else {
			if (NovaCore.getInstance().hasTeamManager()) {
				ArrayList<UUID> teamOrder = new ArrayList<UUID>();

				for (Team team : NovaCore.getInstance().getTeamManager().getTeams()) {
					teamOrder.add(team.getTeamUuid());
				}

				Collections.shuffle(teamOrder);

				ArrayList<Player> toTeleportReal = new ArrayList<Player>();

				for (UUID uuid : teamOrder) {
					for (Player pt : toTeleport) {
						if (NovaCore.getInstance().getTeamManager().getPlayerTeam(pt).getTeamUuid().equals(uuid)) {
							toTeleportReal.add(pt);
						}
					}
				}
				toTeleport = toTeleportReal;
			}
		}

		Log.debug("Final toTeleport size: " + toTeleport.size());

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
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "May the odds be ever in your favour");

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
	public void onPlayerRespawn(Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(NovaCore.getInstance(), new Runnable() {
			@Override
			public void run() {
				tpToSpectator(player);
			}
		}, 5L);

	}
}