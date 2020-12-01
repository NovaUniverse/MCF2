package xyz.mcfridays.base.yborder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.language.LanguageManager;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.module.modules.scoreboard.NetherBoardScoreboard;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import xyz.mcfridays.base.MCF;

public class YBorder extends NovaModule implements Listener {
	private int yLimit;
	private Task decreaseTask;
	private Task damageTask;

	private List<Player> aboveLimit;

	private boolean color;

	private static final int SCOREBOARD_LINE = 6;

	@Override
	public String getName() {
		return "YBorder";
	}

	@Override
	public void onLoad() {
		yLimit = 255;

		decreaseTask = null;
		damageTask = null;

		aboveLimit = new ArrayList<Player>();

		color = false;
	}

	@Override
	public void onEnable() throws Exception {
		Task.tryStopTask(decreaseTask);
		Task.tryStopTask(damageTask);

		decreaseTask = new SimpleTask(MCF.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (yLimit > 0) {
					yLimit--;

				}

				color = !color;
				showLimit();
			}
		}, 20L, 20L);

		damageTask = new SimpleTask(MCF.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					if (GameManager.getInstance().isInGame(player)) {
						if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
							if (player.getLocation().getY() > yLimit) {
								if (player.getHealth() > 0) {
									player.damage(1);

									if (!aboveLimit.contains(player)) {
										aboveLimit.add(player);
										player.sendMessage(LanguageManager.getString(player, "mcf.yborder.above_limit.warning", "" + yLimit));
									}

									String message = LanguageManager.getString(player, "mcf.yborder.above_limit.info", (player.getLocation().getBlockY() - yLimit) + "");

									if (message.length() <= 40) {
										NovaCore.getInstance().getActionBar().sendMessage(player, message);
									}
								} else {
									if (aboveLimit.contains(player)) {
										aboveLimit.remove(player);
									}
								}
							}
						}
					}
				}
			}
		}, 20L, 20L);

		decreaseTask.start();
		damageTask.start();

		LanguageManager.broadcast("mcf.yborder.start");

		showLimit();
	}

	@Override
	public void onDisable() throws Exception {
		LanguageManager.broadcast("mcf.yborder.remove");
		Task.tryStopTask(decreaseTask);
		Task.tryStopTask(damageTask);
		NetherBoardScoreboard.getInstance().clearGlobalLine(SCOREBOARD_LINE);
	}

	private void showLimit() {
		NetherBoardScoreboard.getInstance().setGlobalLine(SCOREBOARD_LINE, (color ? ChatColor.RED : ChatColor.YELLOW) + TextUtils.ICON_WARNING + ChatColor.RED + " Height limit Y: " + ChatColor.AQUA + yLimit + " " + (color ? ChatColor.RED : ChatColor.YELLOW) + TextUtils.ICON_WARNING);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (aboveLimit.contains(e.getPlayer())) {
			aboveLimit.remove(e.getPlayer());
		}
	}
}