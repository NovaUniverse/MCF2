package xyz.mcfridays.base.command.database.status;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaSubCommand;
import xyz.mcfridays.base.MCF;

public class DatabaseCommandSubCommandReconnect extends NovaSubCommand {
	public DatabaseCommandSubCommandReconnect() {
		super("reconnect");

		setDescription("Reconnect the database");
		setUseage("/database reconnect");
		setAllowedSenders(AllowedSenders.ALL);

		setPermission("mcf.command.database.reconnect");
		setPermissionDefaultValue(PermissionDefault.OP);
		setDescription("Access to the databse reconnect command");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		try {
			if (MCF.getDBConnection().reconnect()) {
				sender.sendMessage(ChatColor.GREEN + "Success");
				return true;
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Failed");
			}
		} catch (Exception e) {
			sender.sendMessage(ChatColor.DARK_RED + "Reconnect failed. Cause: " + e.getClass().getName() + " : " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
}