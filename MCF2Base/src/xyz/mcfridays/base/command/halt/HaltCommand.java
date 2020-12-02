package xyz.mcfridays.base.command.halt;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.command.NovaCommand;
import xyz.mcfridays.base.MCF;

public class HaltCommand extends NovaCommand {
	public HaltCommand() {
		super("halt", MCF.getInstance());

		setDescription("Cancel all server activity");
		setAliases(generateAliasList("haltactivity", "emergencystop"));
		addHelpSubCommand();
		setEmptyTabMode(true);

		addSubCommand(new HaltConfirmCommand());
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + TextUtils.ICON_WARNING + " Warning! " + TextUtils.ICON_WARNING + "\nThis command will halt all plugin activity on the server!\nThis should only be done in emergencies or while testing.\nTo confirm halt use " + ChatColor.AQUA + "/halt confirm");
		return false;
	}
}