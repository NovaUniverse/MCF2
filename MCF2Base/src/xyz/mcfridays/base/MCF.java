package xyz.mcfridays.base;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import xyz.mcfridays.base.crafting.EnchantedGoldenAppleRecipe;
import xyz.mcfridays.base.crafting.database.MCFDB;
import xyz.mcfridays.base.deathmessage.MCFPlayerEliminationMessage;
import xyz.mcfridays.base.deathmessage.MCFTeamEliminationMessage;
import xyz.mcfridays.base.kills.KillListener;
import xyz.mcfridays.base.kills.MCFPlayerKillCache;
import xyz.mcfridays.base.leaderboard.MCFLeaderboard;
import xyz.mcfridays.base.listeners.EdibleHeads;
import xyz.mcfridays.base.listeners.NoEnderPearlDamage;
import xyz.mcfridays.base.listeners.PlayerHeadDrop;
import xyz.mcfridays.base.listeners.PlayerListener;
import xyz.mcfridays.base.listeners.ScoreListener;
import xyz.mcfridays.base.lobby.MCFLobby;
import xyz.mcfridays.base.lobby.duels.DuelsManager;
import xyz.mcfridays.base.lobby.duels.command.AcceptDuelCommand;
import xyz.mcfridays.base.lobby.duels.command.DuelCommand;
import xyz.mcfridays.base.lobby.npc.trait.MerchantTrait;
import xyz.mcfridays.base.misc.MCFPlayerNameCache;
import xyz.mcfridays.base.score.ScoreManager;
import xyz.mcfridays.base.scoreboard.MCFScoreboard;
import xyz.mcfridays.base.team.MCFTeam;
import xyz.mcfridays.base.team.MCFTeamManager;
import xyz.mcfridays.base.tracker.MCFCompassTraker;
import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.command.CommandRegistry;
import xyz.zeeraa.novacore.customcrafting.CustomCraftingManager;
import xyz.zeeraa.novacore.database.DBConnection;
import xyz.zeeraa.novacore.database.DBCredentials;
import xyz.zeeraa.novacore.log.Log;
import xyz.zeeraa.novacore.module.ModuleManager;
import xyz.zeeraa.novacore.module.modules.compass.CompassTracker;
import xyz.zeeraa.novacore.module.modules.game.GameManager;
import xyz.zeeraa.novacore.module.modules.game.events.GameEndEvent;
import xyz.zeeraa.novacore.module.modules.game.events.GameLoadedEvent;
import xyz.zeeraa.novacore.module.modules.game.events.PlayerWinEvent;
import xyz.zeeraa.novacore.module.modules.game.events.TeamWinEvent;
import xyz.zeeraa.novacore.module.modules.gamelobby.GameLobby;
import xyz.zeeraa.novacore.module.modules.gui.GUIManager;
import xyz.zeeraa.novacore.module.modules.scoreboard.NetherBoardScoreboard;
import xyz.zeeraa.novacore.teams.Team;
import xyz.zeeraa.novacore.utils.BungeecordUtils;

public class MCF extends JavaPlugin implements Listener {
	private static MCF instance;
	private static DBConnection dbConnection;
	private static String serverName;
	
	private ArrayList<Plugin> relatedPlugins;

	private File sqlFixFile;

	private MCFTeamManager teamManager;

	private ScoreListener scoreListener;

	private String lobbyServer;

	public static MCF getInstance() {
		return instance;
	}

	public static DBConnection getDBConnection() {
		return dbConnection;
	}

	public static String getServerName() {
		return serverName;
	}

	public File getSqlFixFile() {
		return sqlFixFile;
	}

	public MCFTeamManager getTeamManager() {
		return teamManager;
	}

	public String getLobbyServer() {
		return lobbyServer;
	}
	
	public ArrayList<Plugin> getRelatedPlugins() {
		return relatedPlugins;
	}

	public void setServerAsActive(boolean active) {
		MCFDB.setActiveServer(active ? getServerName() : null);
	}
	
	@Override
	public void onEnable() {
		saveDefaultConfig();

		MCF.instance = this;
		MCF.serverName = getConfig().getString("server_name");
		MCF.dbConnection = new DBConnection();

		this.relatedPlugins = new ArrayList<Plugin>();
		
		this.lobbyServer = getConfig().getString("lobby_server");

		DBCredentials dbCredentials = new DBCredentials(getConfig().getString("mysql.driver"), getConfig().getString("mysql.host"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"));

		try {
			MCF.dbConnection.connect(dbCredentials);
		} catch (ClassNotFoundException | SQLException e) {
			Log.fatal("MCF", "Failed to connect to the database");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		MCF.dbConnection.startKeepAliveTask();

		File gameLobbyFolder = new File(this.getDataFolder().getPath() + File.separator + "GameLobby");
		File worldFolder = new File(this.getDataFolder().getPath() + File.separator + "Worlds");

		sqlFixFile = new File(this.getDataFolder().getPath() + File.separator + "sql_fix.sql");

		try {
			FileUtils.forceMkdir(gameLobbyFolder);
			FileUtils.forceMkdir(worldFolder);

			FileUtils.touch(sqlFixFile);

			Log.info("MCF", "Reading lobby files from " + gameLobbyFolder.getPath());
			GameLobby.getInstance().getMapReader().loadAll(gameLobbyFolder, worldFolder);
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.fatal("Failed to setup data directory");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		int[] winScore;

		@SuppressWarnings("unchecked")
		List<Integer> winScoreList = (List<Integer>) getConfig().getList("win_score");

		winScore = new int[winScoreList.size()];
		for (int i = 0; i < winScoreList.size(); i++) {
			winScore[i] = winScoreList.get(i);
		}

		String winScoreString = "Win score: ";
		for (int i = 0; i < winScore.length; i++) {
			winScoreString += winScore[i] + (i < (winScore.length - 1) ? ", " : " ");
		}

		Log.info(winScoreString);

		if (ModuleManager.isDisabled(NetherBoardScoreboard.class)) {
			ModuleManager.enable(NetherBoardScoreboard.class);
		}

		ModuleManager.loadModule(ScoreManager.class, true);
		ModuleManager.loadModule(KillListener.class, true);
		ModuleManager.loadModule(MCFLeaderboard.class, true);
		ModuleManager.loadModule(MCFScoreboard.class, true);
		ModuleManager.loadModule(MCFPlayerNameCache.class, true);
		ModuleManager.loadModule(MCFPlayerKillCache.class, true);
		
		ModuleManager.loadModule(MCFLobby.class);

		teamManager = new MCFTeamManager();
		NovaCore.getInstance().setTeamManager(teamManager);

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(teamManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoEnderPearlDamage(), this);

		CustomCraftingManager.getInstance().addRecipe(EnchantedGoldenAppleRecipe.class);

		NetherBoardScoreboard.getInstance().setDefaultTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "MCF Season 2");
		NetherBoardScoreboard.getInstance().setLineCount(15);

		if (getConfig().getBoolean("enable_head_drops")) {
			Log.info("Listener PlayerHeadDrop registered");
			Bukkit.getPluginManager().registerEvents(new PlayerHeadDrop(), this);
		}

		if (getConfig().getBoolean("enable_edible_heads")) {
			Log.info("Listener EdibleHeads registered");
			Bukkit.getPluginManager().registerEvents(new EdibleHeads(), this);
		}

		GameManager.getInstance().setUseTeams(true); // TODO: Add a mode without teams

		// -=-=-= lobby =-=-=-
		if (getConfig().getBoolean("lobby_enabled")) {
			ModuleManager.enable(MCFLobby.class);

			Location lobbyLocation = new Location(Bukkit.getServer().getWorlds().get(0), getConfig().getDouble("spawn_x"), getConfig().getDouble("spawn_y"), getConfig().getDouble("spawn_z"), (float) getConfig().getDouble("spawn_yaw"), (float) getConfig().getDouble("spawn_pitch"));
			MCFLobby.getInstance().setLobbyLocation(lobbyLocation);

			MCFLobby.getInstance().setKOTLLocation(getConfig().getDouble("kotl_x"), getConfig().getDouble("kotl_z"), getConfig().getDouble("kotl_radius"));
			
			ConfigurationSection playerLeaderboard = getConfig().getConfigurationSection("lobby_player_leaderboard");
			ConfigurationSection teamLeaderboard = getConfig().getConfigurationSection("lobby_team_leaderboard");
			
			MCFLeaderboard.getInstance().setLines(8);
			
			MCFLeaderboard.getInstance().setPlayerHologramLocation(new Location(MCFLobby.getInstance().getWorld(), playerLeaderboard.getDouble("x"), playerLeaderboard.getDouble("y"), playerLeaderboard.getDouble("z")));
			MCFLeaderboard.getInstance().setTeamHologramLocation(new Location(MCFLobby.getInstance().getWorld(), teamLeaderboard.getDouble("x"), teamLeaderboard.getDouble("y"), teamLeaderboard.getDouble("z")));
			
			ModuleManager.require(GUIManager.class);
			
			CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MerchantTrait.class).withName("MerchantTrait"));
			
			ModuleManager.loadModule(DuelsManager.class, true);
			CommandRegistry.registerCommand(new AcceptDuelCommand());
			CommandRegistry.registerCommand(new DuelCommand());
			
		} else if (getConfig().getBoolean("game_enabled")) {
			scoreListener = new ScoreListener(getConfig().getBoolean("kill_score_enabled"), getConfig().getInt("kill_score"), getConfig().getBoolean("win_score_enabled"), winScore, getConfig().getBoolean("participation_score_enabled"), getConfig().getInt("participation_score"));

			Bukkit.getServer().getPluginManager().registerEvents(scoreListener, this);

			GameManager.getInstance().setPlayerEliminationMessage(new MCFPlayerEliminationMessage());
			GameManager.getInstance().setTeamEliminationMessage(new MCFTeamEliminationMessage());

			CompassTracker.getInstance().setCompassTrackerTarget(new MCFCompassTraker());
			CompassTracker.getInstance().setStrictMode(true);
		}
		
		NetherBoardScoreboard.getInstance().setGlobalLine(14, ChatColor.YELLOW + "http://mcfridays.xyz");
		
		relatedPlugins.add(this);
		relatedPlugins.add(NovaCore.getInstance());
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll((Plugin) this);

		try {
			if (MCF.dbConnection != null) {
				if (MCF.dbConnection.isConnected()) {
					MCF.dbConnection.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onGameLoaded(GameLoadedEvent e) {
		NetherBoardScoreboard.getInstance().setGlobalLine(0, ChatColor.YELLOW + "" + ChatColor.BOLD + e.getGame().getDisplayName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onGameEnd(GameEndEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.sendMessage(ChatColor.AQUA + "Sending you to the lobby in 10 seconds");
				}

				Bukkit.getScheduler().scheduleSyncDelayedTask(MCF.getInstance(), new Runnable() {
					@Override
					public void run() {
						for (Player player : Bukkit.getServer().getOnlinePlayers()) {
							Bukkit.getScheduler().runTaskLater(MCF.getInstance(), new Runnable() {
								@Override
								public void run() {
									BungeecordUtils.sendToServer(player, lobbyServer);
								}
							}, 4L);
						}

						Bukkit.getScheduler().scheduleSyncDelayedTask(MCF.getInstance(), new Runnable() {
							@Override
							public void run() {
								for (Player p : Bukkit.getServer().getOnlinePlayers()) {
									p.kickPlayer(ChatColor.AQUA + e.getGame().getDisplayName() + " server restarting, Please reconnect");
								}
								Bukkit.getServer().shutdown();
							}
						}, 40L);
					}
				}, 200L);
			}
		}, 100L);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerWin(PlayerWinEvent e) {
		ChatColor color = ChatColor.AQUA;

		Team team = teamManager.getPlayerTeam(e.getPlayer());

		if (team != null) {
			color = team.getTeamColor();
		}

		Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "GAME OVER> " + ChatColor.GOLD + ChatColor.BOLD + "Winning player: " + color + ChatColor.BOLD + e.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeamWin(TeamWinEvent e) {
		ChatColor color = e.getTeam().getTeamColor();

		Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "GAME OVER> " + ChatColor.GOLD + ChatColor.BOLD + "Winning team: " + color + ChatColor.BOLD + "Team " + ((MCFTeam) e.getTeam()).getTeamNumber());
	}
}