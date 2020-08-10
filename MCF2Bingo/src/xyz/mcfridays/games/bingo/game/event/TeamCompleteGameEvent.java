package xyz.mcfridays.games.bingo.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import xyz.zeeraa.novacore.teams.Team;

public class TeamCompleteGameEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	private Team team;
	private int placement;

	public TeamCompleteGameEvent(Team team, int placement) {
		this.team = team;
		this.placement = placement;
	}

	public Team getTeam() {
		return team;
	}

	public int getPlacement() {
		return placement;
	}
}