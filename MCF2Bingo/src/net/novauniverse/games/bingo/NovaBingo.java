package net.novauniverse.games.bingo;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.novauniverse.games.bingo.command.BingoCommand;
import net.novauniverse.games.bingo.game.Bingo;
import net.zeeraa.novacore.command.CommandRegistry;
import net.zeeraa.novacore.module.ModuleManager;
import net.zeeraa.novacore.module.modules.game.GameManager;
import net.zeeraa.novacore.module.modules.gamelobby.GameLobby;

public class NovaBingo extends JavaPlugin implements Listener {
	// --- Global instance ---
	private static NovaBingo instance;

	public static NovaBingo getInstance() {
		return instance;
	}

	private boolean allowReconnect;
	private int reconnectTime;

	public boolean isAllowReconnect() {
		return allowReconnect;
	}

	public int getReconnectTime() {
		return reconnectTime;
	}

	// --- Begin plugin code ---
	private Bingo game;

	public Bingo getGame() {
		return game;
	}

	@Override
	public void onEnable() {
		NovaBingo.instance = this;

		saveDefaultConfig();

		allowReconnect = getConfig().getBoolean("allow_reconnect");
		reconnectTime = getConfig().getInt("player_elimination_delay");

		// Enable required modules
		ModuleManager.enable(GameManager.class);
		ModuleManager.enable(GameLobby.class);

		// Init game and maps
		this.game = new Bingo();

		GameManager.getInstance().loadGame(game);

		// Register events
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		CommandRegistry.registerCommand(new BingoCommand());
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll((Plugin) this);
	}
}