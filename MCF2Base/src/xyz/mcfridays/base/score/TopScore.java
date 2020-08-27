package xyz.mcfridays.base.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.teams.Team;
import xyz.mcfridays.base.team.MCFTeam;

public class TopScore {
	public static ArrayList<PlayerScoreData> getPlayerTopScore(int maxEntries) {
		ArrayList<PlayerScoreData> result = new ArrayList<PlayerScoreData>();

		for (UUID uuid : ScoreManager.getInstance().getPlayerScoreCache().keySet()) {
			if (NovaCore.getInstance().getTeamManager().getPlayerTeam(uuid) == null) {
				continue;
			}

			PlayerScoreData scoreData = new PlayerScoreData(uuid, ScoreManager.getInstance().getPlayerScore(uuid));
			result.add(scoreData);
		}

		Collections.sort(result);

		while (result.size() > maxEntries) {
			result.remove(result.size() - 1);
		}

		return result;
	}

	public static ArrayList<TeamScoreData> getTeamTopScore(int maxEntries) {
		ArrayList<TeamScoreData> result = new ArrayList<TeamScoreData>();

		if (NovaCore.getInstance().hasTeamManager()) {
			for (Team team : NovaCore.getInstance().getTeamManager().getTeams()) {
				TeamScoreData scoreData = new TeamScoreData((MCFTeam) team);
				result.add(scoreData);
			}

			Collections.sort(result);
		}

		while (result.size() > maxEntries) {
			result.remove(result.size() - 1);
		}

		return result;
	}
}