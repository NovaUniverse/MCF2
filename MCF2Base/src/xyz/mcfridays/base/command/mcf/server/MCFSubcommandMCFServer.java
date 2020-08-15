package xyz.mcfridays.base.command.mcf.server;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.command.AllowedSenders;
import net.zeeraa.novacore.command.NovaSubCommand;
import net.zeeraa.novacore.utils.BungeecordUtils;

public class MCFSubcommandMCFServer extends NovaSubCommand {
	public MCFSubcommandMCFServer() {
		super("server");

		setDescription("Go to a server");
		setAllowedSenders(AllowedSenders.PLAYERS);

		setPermission("mcf.command.mcf.server");
		setPermissionDefaultValue(PermissionDefault.OP);
		setPermissionDescription("Access to the mcf server command");

		addHelpSubCommand();
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length == 0) {
			String server = args[0];
			sender.sendMessage(ChatColor.GOLD + "Trying to send you to " + ChatColor.AQUA + server);

			BungeecordUtils.sendToServer((Player) sender, server);
		} else {
			sender.sendMessage(ChatColor.RED + "Missing argument: server name");
		}
		return false;
	}
}