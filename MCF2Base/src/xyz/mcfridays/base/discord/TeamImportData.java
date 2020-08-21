package xyz.mcfridays.base.discord;

import java.util.UUID;

public class TeamImportData {
	private UUID uuid;
	private String name;
	private int teamNumber;
	
	public TeamImportData(UUID uuid, String name, int teamNumber) {
		this.uuid = uuid;
		this.name = name;
		this.teamNumber = teamNumber;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public String getName() {
		return name;
	}
	
	public int getTeamNumber() {
		return teamNumber;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof UUID) {
			return ((UUID) obj).toString().equalsIgnoreCase(getUuid().toString());
		} else if(obj instanceof TeamImportData){
			return ((TeamImportData) obj).getUuid().toString().equalsIgnoreCase(getUuid().toString());
		}
		
		return false;
	}
}