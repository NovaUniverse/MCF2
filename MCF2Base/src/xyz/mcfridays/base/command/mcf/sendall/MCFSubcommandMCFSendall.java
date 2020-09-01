package xyz.mcfridays.base.command.mcf.sendall;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaSubCommand;
import net.zeeraa.novacore.spigot.utils.BungeecordUtils;

public class MCFSubcommandMCFSendall extends NovaSubCommand {
	public MCFSubcommandMCFSendall() {
		super("sendall");

		setDescription("Send all players to a server");
		setAllowedSenders(AllowedSenders.ALL);

		setPermission("mcf.command.mcf.sendall");
		setPermissionDefaultValue(PermissionDefault.OP);
		setPermissionDescription("Access to the mcf sendall command");

		addHelpSubCommand();
		
		this.setFilterAutocomplete(true);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length == 0) {
			String server = args[0];
			sender.sendMessage(ChatColor.GOLD + "Trying to send all player to " + ChatColor.AQUA + server);

			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				BungeecordUtils.sendToServer(player, server);
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Missing argument: server name");
		}
		return false;
	}
}