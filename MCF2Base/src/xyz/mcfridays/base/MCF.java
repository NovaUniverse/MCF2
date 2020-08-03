package xyz.mcfridays.base;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Golem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.base.crafting.EnchantedGoldenAppleRecipe;
import xyz.mcfridays.base.deathmessage.MCFPlayerEliminationMessage;
import xyz.mcfridays.base.listeners.EdibleHeads;
import xyz.mcfridays.base.listeners.MCFScoreboardData;
import xyz.mcfridays.base.listeners.NoEnderPearlDamage;
import xyz.mcfridays.base.listeners.PlayerHeadDrop;
import xyz.mcfridays.base.listeners.PlayerListener;
import xyz.mcfridays.base.listeners.ScoreListener;
import xyz.mcfridays.base.score.ScoreManager;
import xyz.mcfridays.base.team.MCFTeamManager;
import xyz.mcfridays.base.tracker.MCFCompassTraker;
import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.customcrafting.CustomCraftingManager;
import xyz.zeeraa.novacore.database.DBConnection;
import xyz.zeeraa.novacore.database.DBCredentials;
import xyz.zeeraa.novacore.log.Log;
import xyz.zeeraa.novacore.module.ModuleManager;
import xyz.zeeraa.novacore.module.modules.compass.CompassTracker;
import xyz.zeeraa.novacore.module.modules.game.GameManager;
import xyz.zeeraa.novacore.module.modules.game.events.GameLoadedEvent;
import xyz.zeeraa.novacore.module.modules.gamelobby.GameLobby;
import xyz.zeeraa.novacore.module.modules.scoreboard.NetherBoardScoreboard;

public class MCF extends JavaPlugin implements Listener {
	private static MCF instance;
	private static DBConnection dbConnection;
	private static String serverName;

	private File sqlFixFile;

	private MCFTeamManager teamManager;

	private ScoreListener scoreListener;

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

	@Override
	public void onEnable() {
		saveDefaultConfig();

		MCF.instance = this;
		MCF.serverName = getConfig().getString("server_name");
		MCF.dbConnection = new DBConnection();

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

		scoreListener = new ScoreListener(getConfig().getBoolean("kill_score_enabled"), getConfig().getInt("kill_score"), getConfig().getBoolean("win_score_enabled"), winScore, getConfig().getBoolean("participation_score_enabled"), getConfig().getInt("participation_score"));

		if (ModuleManager.isDisabled(NetherBoardScoreboard.class)) {
			ModuleManager.enable(NetherBoardScoreboard.class);
		}

		ModuleManager.loadModule(ScoreManager.class, true);
		ModuleManager.loadModule(MCFScoreboardData.class, true);

		teamManager = new MCFTeamManager();
		NovaCore.getInstance().setTeamManager(teamManager);

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(teamManager, this);
		Bukkit.getServer().getPluginManager().registerEvents(scoreListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new NoEnderPearlDamage(), this);
		
		CustomCraftingManager.getInstance().addRecipe(EnchantedGoldenAppleRecipe.class);

		GameManager.getInstance().setPlayerEliminationMessage(new MCFPlayerEliminationMessage());

		NetherBoardScoreboard.getInstance().setDefaultTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "MCF Season 2");
		NetherBoardScoreboard.getInstance().setLineCount(15);

		CompassTracker.getInstance().setCompassTrackerTarget(new MCFCompassTraker());
		CompassTracker.getInstance().setStrictMode(true);

		if (getConfig().getBoolean("enable_head_drops")) {
			Log.info("Listener PlayerHeadDrop registered");
			Bukkit.getPluginManager().registerEvents(new PlayerHeadDrop(), this);
		}

		if (getConfig().getBoolean("enable_edible_heads")) {
			Log.info("Listener EdibleHeads registered");
			Bukkit.getPluginManager().registerEvents(new EdibleHeads(), this);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onGameLoaded(GameLoadedEvent e) {
		NetherBoardScoreboard.getInstance().setGlobalLine(0, ChatColor.YELLOW + "" + ChatColor.BOLD + e.getGame().getDisplayName());
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
}