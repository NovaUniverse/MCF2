package xyz.mcfridays.base.discord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import xyz.mcfridays.base.MCF;

public class DiscordTeamManager {
	public static List<TeamImportData> getTeamData() throws SQLException {
		List<TeamImportData> result = new ArrayList<TeamImportData>();

		String sql = "SELECT minecraft_username, minecraft_uuid, team_number FROM users";
		PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			result.add(new TeamImportData(UUID.fromString(rs.getString("minecraft_uuid")), rs.getString("minecraft_username"), rs.getInt("team_number")));
		}

		rs.close();
		ps.close();

		return result;
	}

	public static void updateTeamDatabase() throws SQLException {
		String sql = "UPDATE players SET team_number = -1";

		PreparedStatement ps1 = MCF.getDBConnection().getConnection().prepareStatement(sql);
		ps1.executeUpdate();
		ps1.close();

		List<TeamImportData> teams = getTeamData();

		for (TeamImportData teamImportData : teams) {
			boolean playerFound = false;

			sql = "SELECT id FROM players WHERE uuid = ?";
			PreparedStatement ps2 = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps2.setString(1, teamImportData.getUuid().toString());

			ResultSet rs2 = ps2.executeQuery();
			if (rs2.next()) {
				playerFound = true;
			}

			rs2.close();
			ps2.close();

			if (playerFound) {
				sql = "UPDATE players SET team_number = ? WHERE uuid = ?";

				PreparedStatement ps3 = MCF.getDBConnection().getConnection().prepareStatement(sql);

				ps3.setInt(1, teamImportData.getTeamNumber());
				ps3.setString(2, teamImportData.getUuid().toString().toString());

				ps3.executeUpdate();
				ps3.close();
			} else {
				sql = "INSERT INTO players (uuid, username, team_number, has_joined) VALUES (?, ?, ?, 0)";
				PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

				ps.setString(1, teamImportData.getUuid().toString());
				ps.setString(2, teamImportData.getName());
				ps.setInt(3, teamImportData.getTeamNumber());

				ps.executeUpdate();

				ps.close();
			}
		}
	}
}