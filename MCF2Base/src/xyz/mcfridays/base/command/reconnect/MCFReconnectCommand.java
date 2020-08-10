package xyz.mcfridays.base.command.reconnect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import xyz.mcfridays.base.crafting.database.MCFDB;
import xyz.zeeraa.novacore.command.AllowedSenders;
import xyz.zeeraa.novacore.command.NovaCommand;
import xyz.zeeraa.novacore.utils.BungeecordUtils;

public class MCFReconnectCommand extends NovaCommand {
	public MCFReconnectCommand() {
		super("reconnect");

		this.setAllowedSenders(AllowedSenders.PLAYERS);
		this.setPermission("mcf.command.reconnect");
		this.setPermissionDefaultValue(PermissionDefault.TRUE);
		this.setDescription("Reconnect to a game");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		String activeServer = MCFDB.getActiveServer();
		if (activeServer != null) {
			sender.sendMessage(ChatColor.GREEN + "Connecting to the server...");
			BungeecordUtils.sendToServer((Player) sender, activeServer);
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "There is no active game right now");
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		return new ArrayList<String>();
	}
}