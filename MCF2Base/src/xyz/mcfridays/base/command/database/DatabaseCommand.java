package xyz.mcfridays.base.command.database;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaCommand;
import xyz.mcfridays.base.command.database.status.DatabaseCommandSubCommandStatus;

public class DatabaseCommand extends NovaCommand {
	public DatabaseCommand() {
		super("database");

		setAliases(generateAliasList("db", "dbc", "mysql"));

		setPermission("mcf.command.database");
		setPermissionDefaultValue(PermissionDefault.OP);

		setDescription("Database command");

		setAllowedSenders(AllowedSenders.ALL);

		addSubCommand(new DatabaseCommandSubCommandStatus());
		
		addHelpSubCommand();
		
		this.setFilterAutocomplete(true);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Use: " + ChatColor.AQUA + "/database help" + ChatColor.GOLD + " for more commands");
		return true;
	}
}