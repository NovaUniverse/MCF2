package xyz.mcfridays.base.command.reconnect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaCommand;
import net.zeeraa.novacore.spigot.language.LanguageManager;
import net.zeeraa.novacore.spigot.utils.BungeecordUtils;
import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.crafting.database.MCFDB;

public class MCFCommandReconnect extends NovaCommand {
	public MCFCommandReconnect() {
		super("reconnect", MCF.getInstance());

		this.setAllowedSenders(AllowedSenders.PLAYERS);
		this.setPermission("mcf.command.reconnect");
		this.setPermissionDefaultValue(PermissionDefault.TRUE);
		this.setDescription("Reconnect to a game");
		
		this.setFilterAutocomplete(true);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		String activeServer = MCFDB.getActiveServer();
		if (activeServer != null) {
			sender.sendMessage(LanguageManager.getString(sender, "mcf.command.reconnect.connecting"));
			BungeecordUtils.sendToServer((Player) sender, activeServer);
			return true;
		} else {
			sender.sendMessage(LanguageManager.getString(sender, "mcf.command.reconnect.no_game"));
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		return new ArrayList<String>();
	}
}