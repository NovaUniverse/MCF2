package xyz.mcfridays.base.bingo;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import xyz.mcfridays.base.MCF;
import xyz.mcfridays.games.bingo.MCFBingo;
import xyz.mcfridays.games.bingo.game.Bingo;
import xyz.zeeraa.novacore.module.NovaModule;

public class MCFBingoManger extends NovaModule implements Listener {
	private boolean worldGenerationShown;
	
	@Override
	public String getName() {
		return "MCFBingoManger";
	}

	@Override
	public void onLoad() {
		worldGenerationShown = false;
	}
	
	@Override
	public void onEnable() {
		//TODO: taskId
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
			@Override
			public void run() {
				Bingo bingo = MCFBingo.getInstance().getGame();
				
				if(!bingo.getWorldPreGenerator().isFinished()) {
					worldGenerationShown = true;
					
				}
			}
		}, 5L, 5L);
	}

	@Override
	public void onDisable() {
		
	}
}