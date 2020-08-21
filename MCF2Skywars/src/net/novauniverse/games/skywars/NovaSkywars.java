package net.novauniverse.games.skywars;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.novauniverse.games.skywars.game.Skywars;
import net.novauniverse.games.skywars.loottable.islandloottable.SkywarsLootLoader;
import net.novauniverse.games.skywars.mapmodule.SkywarsIslandSpecialLootTableMapModule;
import net.zeeraa.novacore.NovaCore;
import net.zeeraa.novacore.abstraction.events.VersionIndependantPlayerAchievementAwardedEvent;
import net.zeeraa.novacore.log.Log;
import net.zeeraa.novacore.module.ModuleManager;
import net.zeeraa.novacore.module.modules.compass.CompassTracker;
import net.zeeraa.novacore.module.modules.compass.event.CompassTrackingEvent;
import net.zeeraa.novacore.module.modules.game.GameManager;
import net.zeeraa.novacore.module.modules.game.map.mapmodule.MapModuleManager;
import net.zeeraa.novacore.module.modules.game.mapselector.selectors.guivoteselector.GUIMapVote;
import net.zeeraa.novacore.module.modules.gamelobby.GameLobby;

public class NovaSkywars extends JavaPlugin implements Listener {
	private static NovaSkywars instance;
	
	public static NovaSkywars getInstance() {
		return instance;
	}
	
	private Skywars game;

	private boolean allowReconnect;
	private int reconnectTime;

	public boolean isAllowReconnect() {
		return allowReconnect;
	}

	public int getReconnectTime() {
		return reconnectTime;
	}

	@Override
	public void onEnable() {
		NovaSkywars.instance = this;
		
		saveDefaultConfig();

		allowReconnect = getConfig().getBoolean("allow_reconnect");
		reconnectTime = getConfig().getInt("player_elimination_delay");

		// Create files and folders
		File mapFolder = new File(this.getDataFolder().getPath() + File.separator + "Maps");
		File worldFolder = new File(this.getDataFolder().getPath() + File.separator + "Worlds");
		File lootTableFolder = new File(this.getDataFolder().getPath() + File.separator + "LootTables");

		try {
			FileUtils.forceMkdir(getDataFolder());
			FileUtils.forceMkdir(mapFolder);
			FileUtils.forceMkdir(worldFolder);
			FileUtils.forceMkdir(lootTableFolder);
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.fatal("Skywars", "Failed to setup data directory");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// Enable required modules
		ModuleManager.enable(GameManager.class);
		ModuleManager.enable(GameLobby.class);
		ModuleManager.enable(CompassTracker.class);

		// Register map modules
		MapModuleManager.addMapModule("skywars.island_special_loot", SkywarsIslandSpecialLootTableMapModule.class);

		// Init game and maps
		this.game = new Skywars();

		GameManager.getInstance().loadGame(game);

		GUIMapVote mapSelector = new GUIMapVote();

		GameManager.getInstance().setMapSelector(mapSelector);

		// Register events
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(mapSelector, this);

		// Register loot loaders
		NovaCore.getInstance().getLootTableManager().addLoader(new SkywarsLootLoader());

		// Load loot tables
		NovaCore.getInstance().getLootTableManager().loadAll(lootTableFolder);

		// Read maps
		Log.info("Skywars", "Loading maps from " + mapFolder.getPath());
		GameManager.getInstance().getMapReader().loadAll(mapFolder, worldFolder);
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll((Plugin) this);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onCompassTracking(CompassTrackingEvent e) {
		boolean enabled = false;
		if (GameManager.getInstance().isEnabled()) {
			if (GameManager.getInstance().hasGame()) {
				if (GameManager.getInstance().getActiveGame().hasStarted()) {
					enabled = true;
				}
			}
		}
		e.setCancelled(!enabled);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVersionIndependantPlayerAchievementAwarded(VersionIndependantPlayerAchievementAwardedEvent e) {
		e.setCancelled(true);
	}
}