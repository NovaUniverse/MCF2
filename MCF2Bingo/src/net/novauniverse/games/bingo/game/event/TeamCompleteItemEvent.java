package net.novauniverse.games.bingo.game.event;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.zeeraa.novacore.teams.Team;

public class TeamCompleteItemEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	private Team team;
	private Material material;

	public TeamCompleteItemEvent(Team team, Material material) {
		this.team = team;
		this.material = material;
	}

	public Team getTeam() {
		return team;
	}

	public Material getMaterial() {
		return material;
	}
}
