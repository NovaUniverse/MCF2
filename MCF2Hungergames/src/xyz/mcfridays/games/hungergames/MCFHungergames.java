package xyz.mcfridays.games.hungergames;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.mcfridays.games.hungergames.game.Hungergames;
import xyz.zeeraa.ezcore.EZCore;
import xyz.zeeraa.ezcore.log.EZLogger;
import xyz.zeeraa.ezcore.module.ModuleManager;
import xyz.zeeraa.ezcore.module.compass.CompassTracker;
import xyz.zeeraa.ezcore.module.game.GameManager;
import xyz.zeeraa.ezcore.module.game.mapselector.selectors.guivoteselector.GUIMapVote;
import xyz.zeeraa.ezcore.module.gamelobby.GameLobby;

public class MCFHungergames extends JavaPlugin {
	private Hungergames game;

	@Override
	public void onEnable() {
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
			EZLogger.fatal("Failed to setup data directory");
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

		Bukkit.getServer().getPluginManager().registerEvents(mapSelector, this);

		EZLogger.info("Hungergames", "Loading maps from " + mapFolder.getPath());
		GameManager.getInstance().getMapReader().loadAll(mapFolder, worldFolder);
		
		EZCore.getInstance().getLootTableManager().loadAll(lootTableFolder);
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
}