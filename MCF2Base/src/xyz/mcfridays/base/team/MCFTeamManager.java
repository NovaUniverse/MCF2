package xyz.mcfridays.base.team;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.zeeraa.novacore.log.Log;
import net.zeeraa.novacore.module.modules.scoreboard.NetherBoardScoreboard;
import net.zeeraa.novacore.teams.Team;
import net.zeeraa.novacore.teams.TeamManager;
import xyz.mcfridays.base.MCF;

public class MCFTeamManager extends TeamManager implements Listener {
	public static final int TEAM_COUNT = 12;
	private static MCFTeamManager instance;

	private HashMap<UUID, ChatColor> playerColorCache;

	public static MCFTeamManager getInstance() {
		return instance;
	}

	public MCFTeamManager() {
		MCFTeamManager.instance = this;

		playerColorCache = new HashMap<UUID, ChatColor>();

		for (int i = 0; i < TEAM_COUNT; i++) {
			MCFTeam team = new MCFTeam(i + 1, 0);

			this.teams.add(team);
		}

		updateTeams();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {

			@Override
			public void run() {
				updateTeams();

				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					Team team = getPlayerTeam(player);

					if (team == null) {
						if (playerColorCache.containsKey(player.getUniqueId())) {
							Log.trace("Removing team color for player " + player.getName());
							playerColorCache.remove(player.getUniqueId());
							NetherBoardScoreboard.getInstance().resetPlayerNameColor(player);
						}
					} else {
						if (playerColorCache.containsKey(player.getUniqueId())) {
							if (team.getTeamColor() != playerColorCache.get(player.getUniqueId())) {
								Log.trace("Changing team color for player " + player.getName());
								playerColorCache.put(player.getUniqueId(), team.getTeamColor());
								NetherBoardScoreboard.getInstance().setPlayerNameColor(player, team.getTeamColor());
							}
						} else {
							Log.trace("Setting team color for player " + player.getName());
							playerColorCache.put(player.getUniqueId(), team.getTeamColor());
							NetherBoardScoreboard.getInstance().setPlayerNameColor(player, team.getTeamColor());
						}
					}
				}
			}
		}, 100L, 100L);

		ArrayList<Integer> missingTeams = new ArrayList<Integer>();

		for (int i = 0; i < TEAM_COUNT; i++) {
			missingTeams.add((Integer) i + 1);
		}

		try {
			String sql = "SELECT team_number FROM teams";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int teamNumber = rs.getInt("team_number");
				if (missingTeams.contains((Integer) teamNumber)) {
					missingTeams.remove((Integer) teamNumber);
				}
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		for (Integer i : missingTeams) {
			try {
				String sql = "INSERT INTO teams (team_number) VALUES (?)";
				PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

				ps.setInt(1, i);

				ps.execute();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}

	private void updateTeams() {
		// Update players
		try {
			String sql = "SELECT uuid, team_number FROM players";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				int teamNumber = rs.getInt("team_number");

				for (Team team : teams) {
					if (teamNumber <= 0 || ((MCFTeam) team).getTeamNumber() != teamNumber) {
						if (team.getMembers().contains(uuid)) {
							team.getMembers().remove(uuid);
							Log.trace("MCFTeamManager", "Removing player with uuid " + uuid.toString() + " from team " + ((MCFTeam) team).getTeamNumber());
						}
					} else {
						if (teamNumber == ((MCFTeam) team).getTeamNumber()) {
							if (!team.getMembers().contains(uuid)) {
								team.getMembers().add(uuid);
								Log.trace("MCFTeamManager", "Adding player with uuid " + uuid.toString() + " to team " + ((MCFTeam) team).getTeamNumber());
							}
						}
					}
				}
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.warn("MCFTeamManager", "Failed to update teams");
			return;
		}

		// Update score
		try {
			String sql = "SELECT score, team_number FROM teams";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MCFTeam team = getTeam(rs.getInt("team_number"));

				if (team != null) {
					team.setScore(rs.getInt("score"));
				}
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.warn("MCFTeamManager", "Failed to update team score");
			return;
		}

		// Update player names
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			updatePlayerName(player);
		}
	}

	public MCFTeam getTeam(int teamNumber) {
		for (Team team : teams) {
			if (((MCFTeam) team).getTeamNumber() == teamNumber) {
				return (MCFTeam) team;
			}
		}

		return null;
	}

	public void updatePlayerName(Player player) {
		Team team = getPlayerTeam(player);

		String name = "MissingNo";

		ChatColor color = ChatColor.YELLOW;
		if (team == null) {
			name = color + "No team : " + ChatColor.RESET + player.getName();
		} else {
			if (((MCFTeam) team).getTeamNumber() >= 1) {
				color = team.getTeamColor();
				name = color + "Team " + ((MCFTeam) team).getTeamNumber() + " : " + ChatColor.RESET + player.getName();
			}
		}

		player.setDisplayName("| " + name);
		player.setPlayerListName(name);

		if (NetherBoardScoreboard.getInstance().isEnabled()) {
			String teamName = "";

			if (team == null) {
				teamName = ChatColor.YELLOW + "No team";
			} else {
				teamName = color + "Team " + ((MCFTeam) team).getTeamNumber();
			}

			NetherBoardScoreboard.getInstance().setPlayerLine(1, player, teamName);
		}
	}

	public boolean requireTeamToJoin(Player player) {
		return true;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		updatePlayerName(player);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (playerColorCache.containsKey(e.getPlayer().getUniqueId())) {
			playerColorCache.remove(e.getPlayer().getUniqueId());
		}
	}
}