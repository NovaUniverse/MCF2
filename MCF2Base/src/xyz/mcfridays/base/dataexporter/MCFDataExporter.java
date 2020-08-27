package xyz.mcfridays.base.dataexporter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import net.zeeraa.novacore.commons.log.Log;
import xyz.mcfridays.base.MCF;
import xyz.mcfridays.base.dataexporter.data.PlayerResult;
import xyz.mcfridays.base.dataexporter.data.TeamResult;

public class MCFDataExporter {
	public static boolean runExport(int weekNumber) throws Exception {
		try {
			if (weekExists(weekNumber)) {
				Log.error("MCFDataExport", "Error: week " + weekNumber + " already exists in the database");
			} else {
				MCF.getDBConnection().getConnection().setAutoCommit(false);

				ArrayList<TeamResult> teamResult = fetchTeamResult();
				System.out.println(teamResult.size() + " team entries found");

				ArrayList<PlayerResult> playerResult = fetchPlayerResult();
				System.out.println(playerResult.size() + " player entries found");

				Log.info("MCFDataExport", "Saving as week " + weekNumber);
				addWeek(weekNumber);

				Log.info("MCFDataExport", "Adding week " + weekNumber + " to week list");

				for (TeamResult tr : teamResult) {
					Log.info("MCFDataExport", "Saving team " + tr.getTeamId() + " with a score of " + tr.getScore());
					saveTeamResult(tr, weekNumber);
				}

				for (PlayerResult pr : playerResult) {
					Log.info("MCFDataExport", "Saving player " + pr.getUuid() + " with a score of " + pr.getScore() + " and " + pr.getKills() + " kills");
					savePlayerResult(pr, weekNumber);
				}

				Log.info("MCFDataExport", "Commiting changes...");
				MCF.getDBConnection().getConnection().commit();
				Log.info("MCFDataExport", "Data export complete");
				return true;

			}
		} catch (Exception e) {
			System.err.println("An error occurred during export. trying to rollback");
			MCF.getDBConnection().getConnection().rollback();
			MCF.getDBConnection().getConnection().setAutoCommit(true);
			throw e;
		}
		return false;
	}

	private static boolean weekExists(int week) throws SQLException {
		boolean result = false;
		String sql = "SELECT id FROM mcf_results.weeks WHERE week = ?";
		PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

		ps.setInt(1, week);

		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			result = true;
		}

		rs.close();
		ps.close();

		return result;
	}

	private static void addWeek(int week) throws SQLException {
		String sql = "INSERT INTO mcf_results.weeks (week) VALUES (?)";
		PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

		ps.setInt(1, week);

		ps.executeUpdate();

		ps.close();
	}

	private static void saveTeamResult(TeamResult result, int week) throws SQLException {
		String sql = "INSERT INTO mcf_results.team_result (week, team_number, score) VALUES (?, ?, ?)";
		PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

		ps.setInt(1, week);
		ps.setInt(2, result.getTeamId());
		ps.setInt(3, result.getScore());

		ps.executeUpdate();

		ps.close();
	}

	private static void savePlayerResult(PlayerResult result, int week) throws SQLException {
		String sql = "INSERT INTO mcf_results.player_result (week, uuid, team_number, score, kills) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

		ps.setInt(1, week);
		ps.setString(2, result.getUuid().toString());
		ps.setInt(3, result.getTeamId());
		ps.setInt(4, result.getScore());
		ps.setInt(5, result.getKills());

		ps.executeUpdate();

		ps.close();
	}

	private static ArrayList<TeamResult> fetchTeamResult() throws SQLException {
		ArrayList<TeamResult> result = new ArrayList<TeamResult>();
		String sql = "SELECT * FROM mcf_mcf.teams";
		PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			result.add(new TeamResult(rs.getInt("team_number"), rs.getInt("score")));
		}

		rs.close();
		ps.close();

		return result;
	}

	private static ArrayList<PlayerResult> fetchPlayerResult() throws SQLException {
		ArrayList<PlayerResult> result = new ArrayList<PlayerResult>();
		String sql = "SELECT * FROM mcf_mcf.players WHERE team_number > -1";
		PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			result.add(new PlayerResult(UUID.fromString(rs.getString("uuid")), rs.getInt("team_number"), rs.getInt("score"), rs.getInt("kills")));
		}

		rs.close();
		ps.close();

		return result;
	}
}