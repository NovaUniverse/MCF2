package xyz.mcfridays.base.command.mcf.importdiscordteams;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaSubCommand;
import xyz.mcfridays.base.discord.DiscordTeamManager;

public class MCFSubcommandMCFImportDiscordTeams extends NovaSubCommand {
	public MCFSubcommandMCFImportDiscordTeams() {
		super("importdiscordteams");

		setDescription("Import player teams from discord");
		setAllowedSenders(AllowedSenders.ALL);

		setPermission("mcf.command.mcf.importdiscordteams");
		setPermissionDefaultValue(PermissionDefault.OP);
		setPermissionDescription("Access to the mcf import discord teams command");

		setEmptyTabMode(true);

		addHelpSubCommand();
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		try {
			DiscordTeamManager.updateTeamDatabase();
			sender.sendMessage(ChatColor.GREEN + "Success");
			return true;
		} catch (Exception e) {
			sender.sendMessage(ChatColor.DARK_RED + "Failed to import teams. Reason: "+e.getClass().getName() + " " + e.getMessage());
			Log.fatal("Failed to import teams. Reason: "+e.getClass().getName() + " " + e.getMessage());
		}
		return false;
	}
}