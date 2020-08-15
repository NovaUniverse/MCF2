package xyz.mcfridays.games.hungergames;

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

import net.zeeraa.novacore.NovaCore;
import net.zeeraa.novacore.abstraction.events.VersionIndependantPlayerAchievementAwardedEvent;
import net.zeeraa.novacore.log.Log;
import net.zeeraa.novacore.module.ModuleManager;
import net.zeeraa.novacore.module.modules.compass.CompassTracker;
import net.zeeraa.novacore.module.modules.compass.event.CompassTrackingEvent;
import net.zeeraa.novacore.module.modules.game.GameManager;
import net.zeeraa.novacore.module.modules.game.mapselector.selectors.guivoteselector.GUIMapVote;
import net.zeeraa.novacore.module.modules.gamelobby.GameLobby;
import xyz.mcfridays.games.hungergames.game.Hungergames;

public class MCFHungergames extends JavaPlugin implements Listener {
	private static MCFHungergames instance;
	
	public static MCFHungergames getInstance() {
		return instance;
	}
	
	private Hungergames game;

	@Override
	public void onEnable() {
		MCFHungergames.instance = this;
		
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
			Log.fatal("Failed to setup data directory");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		ModuleManager.enable(GameManager.class);
		ModuleManager.enable(GameLobby.class);
		ModuleManager.enable(CompassTracker.class);

		this.game = new Hungergames();

		GameManager.getInstance().loadGame(game);

		GUIMapVote mapSelector = new GUIMapVote();

		GameManager.getInstance().setMapSelector(mapSelector);

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(mapSelector, this);

		Log.info("Hungergames", "Loading maps from " + mapFolder.getPath());
		GameManager.getInstance().getMapReader().loadAll(mapFolder, worldFolder);

		NovaCore.getInstance().getLootTableManager().loadAll(lootTableFolder);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
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