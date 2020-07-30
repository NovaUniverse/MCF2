package xyz.mcfridays.base;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.base.deathmessage.MCFPlayerEliminationMessage;
import xyz.zeeraa.ezcore.database.DBConnection;
import xyz.zeeraa.ezcore.database.DBCredentials;
import xyz.zeeraa.ezcore.log.EZLogger;
import xyz.zeeraa.ezcore.module.ModuleManager;
import xyz.zeeraa.ezcore.module.game.GameManager;
import xyz.zeeraa.ezcore.module.game.events.GameLoadedEvent;
import xyz.zeeraa.ezcore.module.gamelobby.GameLobby;
import xyz.zeeraa.ezcore.module.scoreboard.EZScoreboard;

public class MCF extends JavaPlugin implements Listener {
	private static MCF instance;
	private static DBConnection dbConnection;
	private static String serverName;

	public static MCF getInstance() {
		return instance;
	}

	public static DBConnection getDBConnection() {
		return dbConnection;
	}

	public static String getServerName() {
		return serverName;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();

		MCF.instance = this;
		MCF.serverName = getConfig().getString("server_name");
		MCF.dbConnection = new DBConnection();

		DBCredentials dbCredentials = new DBCredentials(getConfig().getString("mysql.driver"), getConfig().getString("mysql.host"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"));

		try {
			// Success check is not required since the only way this can fail is if the
			// connection is already stared
			MCF.dbConnection.connect(dbCredentials);
		} catch (ClassNotFoundException | SQLException e) {
			EZLogger.fatal("MCF", "Failed to connect to the database");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		MCF.dbConnection.startKeepAliveTask();

		File gameLobbyFolder = new File(this.getDataFolder().getPath() + File.separator + "GameLobby");
		File worldFolder = new File(this.getDataFolder().getPath() + File.separator + "Worlds");

		try {
			FileUtils.forceMkdir(gameLobbyFolder);
			FileUtils.forceMkdir(worldFolder);

			EZLogger.info("MCF", "Reading lobby files from " + gameLobbyFolder.getPath());
			GameLobby.getInstance().getMapReader().loadAll(gameLobbyFolder, worldFolder);
		} catch (IOException e1) {
			e1.printStackTrace();
			EZLogger.fatal("Failed to setup data directory");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if (ModuleManager.isDisabled(EZScoreboard.class)) {
			ModuleManager.enable(EZScoreboard.class);
		}

		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		GameManager.getInstance().setPlayerEliminationMessage(new MCFPlayerEliminationMessage());
		
		EZScoreboard.getInstance().setDefaultTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "MCF Season 2");
		EZScoreboard.getInstance().setLineCount(15);

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onGameLoaded(GameLoadedEvent e) {
		EZScoreboard.getInstance().setGlobalLine(0, ChatColor.YELLOW + "" + ChatColor.BOLD + e.getGame().getDisplayName());
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