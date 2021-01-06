package xyz.mcfridays.base.lobby;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import me.rayzr522.jsonmessage.JSONMessage;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.commons.utils.RandomGenerator;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.events.VersionIndependantPlayerAchievementAwardedEvent;
import net.zeeraa.novacore.spigot.command.CommandRegistry;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseWorld;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldUnloadOption;
import net.zeeraa.novacore.spigot.module.modules.scoreboard.NetherBoardScoreboard;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;
import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.command.reconnect.MCFCommandReconnect;
import xyz.mcfridays.base.crafting.database.MCFDB;
import xyz.mcfridays.base.score.ScoreManager;

public class MCFLobby extends NovaModule implements Listener {
	private static MCFLobby instance;

	private Location lobbyLocation;
	private Location kotlLocation;

	private double kotlRadius;

	private SimpleTask calmDownCageResetTimer;

	private MultiverseWorld multiverseWorld;

	private HashMap<UUID, Integer> theCalmDownCageCounter;
	
	private boolean gameRunningMessageSent;
	private SimpleTask gameRunningCheckTask;

	private SimpleTask loadScoreTask;
	private SimpleTask lobbyTask;
	
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
		this.lobbyLocation = null;
		this.multiverseWorld = null;
		this.gameRunningMessageSent = false;
		this.gameRunningCheckTask = null;
		this.loadScoreTask = null;
		this.lobbyTask = null;
	}

	@Override
	public void onEnable() throws Exception {
		multiverseWorld = MultiverseManager.getInstance().createFromFile(new File(MCF.getInstance().getDataFolder().getPath() + File.separator + "mcf_lobby"), WorldUnloadOption.DELETE);

		multiverseWorld.getWorld().setThundering(false);
		multiverseWorld.getWorld().setWeatherDuration(0);

		multiverseWorld.setLockWeather(true);

		NetherBoardScoreboard.getInstance().setGlobalLine(0, ChatColor.YELLOW + "" + ChatColor.BOLD + "Lobby");

		lobbyTask = new SimpleTask(MCF.getInstance(), new Runnable() {
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
		}, 5L);
		lobbyTask.start();

		loadScoreTask = new SimpleTask(MCF.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (NovaCore.getInstance().hasTeamManager()) {
					for (Team team : NovaCore.getInstance().getTeamManager().getTeams()) {
						for (UUID uuid : team.getMembers()) {
							// Load all players into score cache so that they get displayed in the leader
							// board
							ScoreManager.getInstance().getPlayerScore(uuid);
						}
					}
				}
			}
		}, 200L);
		loadScoreTask.start();
		
		
		theCalmDownCageCounter = new HashMap<UUID, Integer>();

		calmDownCageResetTimer = new SimpleTask(new Runnable() {
			@Override
			public void run() {
				theCalmDownCageCounter.clear();
			}
		}, 900L, 900L);
		//calmDownCageResetTimer.start();
		
		gameRunningCheckTask = new SimpleTask(MCF.getInstance(), new Runnable() {
			@Override
			public void run() {
				String activeServer = MCFDB.getActiveServer();
				if(activeServer == null) {
					if(gameRunningMessageSent) {
						gameRunningMessageSent = false;
					}
				} else {
					if(!gameRunningMessageSent) {
						gameRunningMessageSent = true;
						for(Player player : Bukkit.getServer().getOnlinePlayers()) {
							JSONMessage.create("A game is in progress!").color(ChatColor.GOLD).style(ChatColor.BOLD).send(player);
							JSONMessage.create("Use /reconnect or click ").color(ChatColor.GOLD).style(ChatColor.BOLD).then("[Here]").color(ChatColor.GREEN).tooltip("Click to reconnect").runCommand("/reconnect").style(ChatColor.BOLD).then(" to reconnect").color(ChatColor.GOLD).style(ChatColor.BOLD).send(player);
						}
					}
				}
			}
		}, 20L);
		gameRunningCheckTask.start();
		
		CommandRegistry.registerCommand(new MCFCommandReconnect());
	}

	@Override
	public void onDisable() {
		Task.tryStopTask(gameRunningCheckTask);
		Task.tryStopTask(calmDownCageResetTimer);
		Task.tryStopTask(loadScoreTask);
		Task.tryStopTask(lobbyTask);
		
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinMonitor(PlayerJoinEvent e) {
		if (MCFDB.getActiveServer() != null) {
			Player p = e.getPlayer();

			JSONMessage.create("A game is in progress!").color(ChatColor.GOLD).style(ChatColor.BOLD).send(p);
			JSONMessage.create("Use /reconnect or click ").color(ChatColor.GOLD).style(ChatColor.BOLD).then("[Here]").color(ChatColor.GREEN).tooltip("Click to reconnect").runCommand("/reconnect").style(ChatColor.BOLD).then(" to reconnect").color(ChatColor.GOLD).style(ChatColor.BOLD).send(p);
		}
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
				if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
					if (e.getClickedBlock().getState() instanceof Sign) {
						Sign sign = (Sign) e.getClickedBlock().getState();
						if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Free]") && ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("Fishing rod")) {
							Player p = e.getPlayer();

							if (!p.getInventory().contains(Material.FISHING_ROD)) {
								if (!theCalmDownCageCounter.containsKey(p.getUniqueId())) {
									//theCalmDownCageCounter.put(p.getUniqueId(), 1);
								} else {
									int newVal = theCalmDownCageCounter.get(p.getUniqueId()) + 1;
									//theCalmDownCageCounter.put(p.getUniqueId(), newVal);

									if (newVal > 5) {
										p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + (RandomGenerator.generate(0, 5) == 4 ? "You have committed crimes against skyrim and her people. what say you in your defense" : "Stop you have violated the law pay the court a fine or serve your sentance"));
										p.teleport(new Location(lobbyLocation.getWorld(), 28.5, 27, 46.5, -90, 0));
									}
								}

								p.getInventory().addItem(new ItemBuilder(Material.FISHING_ROD).setUnbreakable(true).build());
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent e) {
		if (e.getLine(0).equalsIgnoreCase("[free rod]")) {
			e.setLine(0, ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[Free]");
			e.setLine(1, ChatColor.BLUE + "Fishing rod");
			e.setLine(2, "");
			e.setLine(3, "");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		if (e.getMessage().equalsIgnoreCase("fus ro dah") || e.getMessage().equalsIgnoreCase("yeet")) {
			Player player = e.getPlayer();
			if (player.getUniqueId().toString().equalsIgnoreCase("8ec663e7-9a3d-4014-9bc6-a6915e629a56") || player.getUniqueId().toString().equalsIgnoreCase("83d56501-b4e8-4d87-989a-93c9a4a6a9f8") || player.getUniqueId().toString().equalsIgnoreCase("980dbf7d-0904-426f-9c02-d9af3c099fb2")) {
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1, 1);
				for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
					if (player2.getWorld() != player.getWorld()) {
						continue;
					}

					Vector toPlayer2 = player2.getLocation().toVector().subtract(player.getLocation().toVector());

					Vector direction = player.getLocation().getDirection();

					double dot = toPlayer2.normalize().dot(direction);

					if (player.getLocation().distance(player2.getLocation()) < 12) {
						if (dot > 0.90) {
							player2.setVelocity(direction.multiply(4 - (player.getLocation().distance(player2.getLocation()) / 4)));
						}
					}
				}
			}
		}
	}
}