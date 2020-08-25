package xyz.mcfridays.base.command.mcf.exportresult;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.command.AllowedSenders;
import net.zeeraa.novacore.command.NovaSubCommand;
import net.zeeraa.novacore.log.Log;
import xyz.mcfridays.base.dataexporter.MCFDataExporter;

public class MCFSubcommandMCFExportResult extends NovaSubCommand {
	public MCFSubcommandMCFExportResult() {
		super("exportresult");

		setDescription("Export player data to long term storage");
		setAllowedSenders(AllowedSenders.ALL);

		setPermission("mcf.command.mcf.exportresult");
		setPermissionDefaultValue(PermissionDefault.OP);
		setPermissionDescription("Access to the mcf exportresult command");

		setEmptyTabMode(true);

		addHelpSubCommand();
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Missing week number");
		} else {
			try {
				int weekNumber = Integer.parseInt(args[0]);
				if (args.length != 2) {
					sender.sendMessage(ChatColor.YELLOW + "Please confirm with /mcf exportresult " + weekNumber + " confirm\nNote: You cant cancel a data export and the week number cant be changed after the commit.\nYou can enable logging before exporting by using /novacore log set INFO");
					return true;
				} else if(args[1].equalsIgnoreCase("confirm")) {
					try {
						if (!MCFDataExporter.runExport(weekNumber)) {
							sender.sendMessage(ChatColor.RED + "Export failed. Check if the week number is already used");
						} else {
							sender.sendMessage(ChatColor.GREEN + "Export complete. Result at http://mcfridays.xyz/results/?week=" + weekNumber);
						}
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "Data export filed due to an exception. See the console for more info. " + e.getClass().getName() + ". " + e.getMessage());
						Log.error("MCF Data export failed. See the console for more info");
						e.printStackTrace();
					}
				}

				
			} catch (NumberFormatException nfe) {
				sender.sendMessage(ChatColor.RED + "Invalid week number");
			}
		}

		return false;
	}
}