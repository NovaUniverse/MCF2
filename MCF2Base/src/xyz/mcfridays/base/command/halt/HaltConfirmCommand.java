package xyz.mcfridays.base.command.halt;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.log.LogLevel;
import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaSubCommand;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseWorld;
import net.zeeraa.novacore.spigot.module.modules.multiverse.PlayerUnloadOption;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldUnloadOption;
import xyz.mcfridays.base.MCF;

public class HaltConfirmCommand extends NovaSubCommand {

	public HaltConfirmCommand() {
		super("confirm");
		setAllowedSenders(AllowedSenders.ALL);
		setPermissionDefaultValue(PermissionDefault.FALSE);
		setPermission("mcf.command.halt");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(sender instanceof Player) {
			Log.subscribedPlayers.put(((Player) sender).getUniqueId(), LogLevel.TRACE);
		}
		
		Log.fatal("Server Halt", "Server halt triggered by " + sender.getName());
		
		Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Halting server activity");
		
		
		
		if(MultiverseManager.getInstance().isEnabled()) {
			Log.info("Server Halt", "Preventing multiverse kick");
			
			for(MultiverseWorld world : MultiverseManager.getInstance().getWorlds().values()) {
				world.setUnloadOption(WorldUnloadOption.KEEP);
				world.setPlayerUnloadOptions(PlayerUnloadOption.DO_NOTHING);
			}
		}
		
		Log.info("Server Halt", "Killing database");
		try {
			MCF.getDBConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.warn("Server Halt", "Exception caught while closing database " + e.getClass().getName() + " " + e.getMessage());
		}
		
		Log.info("Server Halt", "Ending tasks");
		Bukkit.getScheduler().cancelAllTasks();
		
		Log.info("Server Halt", "Unregistering handlers");
		HandlerList.unregisterAll();
		
		Log.info("Server Halt", "Disabling all plugins");
		Bukkit.getServer().getPluginManager().disablePlugins();
		
		sender.sendMessage(ChatColor.DARK_RED + "All plugin activity halted");
		
		return true;
	}
}