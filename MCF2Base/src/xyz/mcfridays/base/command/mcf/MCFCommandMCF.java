package xyz.mcfridays.base.command.mcf;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import xyz.mcfridays.base.command.mcf.sendall.MCFSubcommandMCFSendall;
import xyz.mcfridays.base.command.mcf.server.MCFSubcommandMCFServer;
import xyz.zeeraa.novacore.command.AllowedSenders;
import xyz.zeeraa.novacore.command.NovaCommand;

public class MCFCommandMCF extends NovaCommand {
	public MCFCommandMCF() {
		super("mcf");

		setAllowedSenders(AllowedSenders.ALL);
		setDescription("Main command for MCF");
		
		setPermission("mcf.command.mcf");
		setPermissionDefaultValue(PermissionDefault.OP);
		setPermissionDescription("Access to the mcf command");
		
		setEmptyTabMode(true);
		
		addSubCommand(new MCFSubcommandMCFSendall());
		addSubCommand(new MCFSubcommandMCFServer());
		
		addHelpSubCommand();
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Use "+ChatColor.AQUA+"/mcf help"+ChatColor.GOLD+" for help");
		return true;
	}

}