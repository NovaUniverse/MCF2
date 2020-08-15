package xyz.mcfridays.games.bingo;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.zeeraa.novacore.command.CommandRegistry;
import net.zeeraa.novacore.module.ModuleManager;
import net.zeeraa.novacore.module.modules.game.GameManager;
import net.zeeraa.novacore.module.modules.gamelobby.GameLobby;
import xyz.mcfridays.games.bingo.command.BingoCommand;
import xyz.mcfridays.games.bingo.game.Bingo;

public class MCFBingo extends JavaPlugin implements Listener {
	// --- Global instance ---
	private static MCFBingo instance;
	
	public static MCFBingo getInstance() {
		return instance;
	}
	
	// --- Begin plugin code ---
	private Bingo game;
	
	public Bingo getGame() {
		return game;
	}
	
	@Override
	public void onEnable() {
		MCFBingo.instance = this;

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