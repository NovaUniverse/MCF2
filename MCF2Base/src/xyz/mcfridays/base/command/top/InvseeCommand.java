package xyz.mcfridays.base.command.top;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaCommand;

public class InvseeCommand extends NovaCommand {
	public InvseeCommand() {
		super("invsee");
		setDescription("Access the inventory of a player");
		setPermission("mcf.command.invsee");
		setPermissionDefaultValue(PermissionDefault.OP);
		setAllowedSenders(AllowedSenders.PLAYERS);
		
		addHelpSubCommand();
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		Player player = (Player) sender;

		if (args.length == 1) {
			Player target = Bukkit.getServer().getPlayer(args[0]);

			if (target != null) {
				if (target.isOnline()) {
					player.openInventory(target.getInventory());
					return true;
				} else {
					player.sendMessage(ChatColor.RED + "That player is not online");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Could not find player named " + args[0]);
			}
		} else {
			player.sendMessage(ChatColor.RED + "Use /invsee <Player>");
		}
		return false;
	}
}