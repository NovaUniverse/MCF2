package net.novauniverse.games.bingo.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import net.md_5.bungee.api.ChatColor;
import net.novauniverse.games.bingo.NovaBingo;
import net.zeeraa.novacore.NovaCore;
import net.zeeraa.novacore.command.AllowedSenders;
import net.zeeraa.novacore.command.NovaCommand;
import net.zeeraa.novacore.module.modules.game.GameManager;
import net.zeeraa.novacore.teams.Team;

public class BingoCommand extends NovaCommand {
	public BingoCommand() {
		super("bingo");
		this.setDescription("Show bingo items");
		this.setAllowedSenders(AllowedSenders.PLAYERS);
		this.setPermission("bingo.commands.bingo");
		this.setPermissionDefaultValue(PermissionDefault.TRUE);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		Player player = (Player) sender;

		if (GameManager.getInstance().hasGame()) {
			if (GameManager.getInstance().getActiveGame().hasStarted()) {
				if (NovaBingo.getInstance().getGame().getPlayers().contains(player.getUniqueId())) {
					Team team = NovaCore.getInstance().getTeamManager().getPlayerTeam(player);

					if (team != null) {
						if (!NovaBingo.getInstance().getGame().hasTeamMenu(team)) {
							NovaBingo.getInstance().getGame().createInventory(team.getTeamUuid());
						}
						player.openInventory(NovaBingo.getInstance().getGame().getTeamMenu(team));
					} else {
						player.sendMessage(ChatColor.RED + "ERR:TEAM_NULL");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You are not playing");
				}
			} else {
				player.sendMessage(ChatColor.RED + "No game in progress");
			}
		} else {
			player.sendMessage(ChatColor.RED + "No game has been loaded");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		return new ArrayList<String>();
	}
}