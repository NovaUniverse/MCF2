package xyz.mcfridays.base.command.database.status;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaSubCommand;
import xyz.mcfridays.base.MCF;

public class DatabaseCommandSubCommandStatus extends NovaSubCommand {

	public DatabaseCommandSubCommandStatus() {
		super("status");
		setDescription("Show database status");
		setAllowedSenders(AllowedSenders.ALL);
		setPermission("mcf.command.database.status");
		setPermissionDefaultValue(PermissionDefault.OP);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		try {
			boolean connected = MCF.getDBConnection().isConnected();
			boolean working = MCF.getDBConnection().testQuery();
			sender.sendMessage(ChatColor.GOLD + "===== Database status =====");
			sender.sendMessage(ChatColor.GOLD + "Connected: " + (connected ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
			sender.sendMessage(ChatColor.GOLD + "Test query: " + (working ? ChatColor.GREEN + "Ok" : ChatColor.RED + "Failure"));
			sender.sendMessage(ChatColor.GOLD + "===========================");
			return true;
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + e.getClass().getName() + " " + e.getMessage() + "\n" + e.getStackTrace());
		}
		return false;
	}
}