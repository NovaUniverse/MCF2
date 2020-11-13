package xyz.mcfridays.base.command.mcf;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaCommand;
import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.command.mcf.exportresult.MCFSubcommandMCFExportResult;
import xyz.mcfridays.base.command.mcf.importdiscordteams.MCFSubcommandMCFImportDiscordTeams;
import xyz.mcfridays.base.command.mcf.sendall.MCFSubcommandMCFSendall;
import xyz.mcfridays.base.command.mcf.server.MCFSubcommandMCFServer;

public class MCFCommandMCF extends NovaCommand {
	public MCFCommandMCF() {
		super("mcf", MCF.getInstance());

		setAllowedSenders(AllowedSenders.ALL);
		setDescription("Main command for MCF");
		
		setPermission("mcf.command.mcf");
		setPermissionDefaultValue(PermissionDefault.OP);
		setPermissionDescription("Access to the mcf command");
		
		setEmptyTabMode(true);
		
		addSubCommand(new MCFSubcommandMCFSendall());
		addSubCommand(new MCFSubcommandMCFServer());
		addSubCommand(new MCFSubcommandMCFImportDiscordTeams());
		addSubCommand(new MCFSubcommandMCFExportResult());
		
		addHelpSubCommand();
		
		this.setFilterAutocomplete(true);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Use "+ChatColor.AQUA+"/mcf help"+ChatColor.GOLD+" for help");
		return true;
	}
}