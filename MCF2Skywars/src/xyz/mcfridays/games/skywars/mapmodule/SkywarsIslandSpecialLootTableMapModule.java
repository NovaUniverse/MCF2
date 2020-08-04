package xyz.mcfridays.games.skywars.mapmodule;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.json.JSONObject;

import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.log.Log;
import xyz.zeeraa.novacore.loottable.LootTable;
import xyz.zeeraa.novacore.module.modules.chestloot.ChestType;
import xyz.zeeraa.novacore.module.modules.chestloot.events.ChestFillEvent;
import xyz.zeeraa.novacore.module.modules.game.GameManager;
import xyz.zeeraa.novacore.module.modules.game.MapGame;
import xyz.zeeraa.novacore.module.modules.game.map.mapmodule.MapModule;

public class SkywarsIslandSpecialLootTableMapModule extends MapModule implements Listener {
	private int islandRadius;
	private LootTable islandLootTable;

	public SkywarsIslandSpecialLootTableMapModule(JSONObject json) {
		super(json);

		this.islandRadius = json.getInt("island_radius");
		this.islandLootTable = NovaCore.getInstance().getLootTableManager().getLootTable(json.getString("loot_table"));

		if (islandLootTable == null) {
			Log.fatal("Could not find loot table named " + json.getString("loot_table"));
		}

	}

	public int getIslandRadius() {
		return islandRadius;
	}

	public LootTable getIslandLootTable() {
		return islandLootTable;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onChestFill(ChestFillEvent e) {
		if (e.getChestType() == ChestType.CHEST) {
			Location location = e.getLocation();

			MapGame game = (MapGame) GameManager.getInstance().getActiveGame();

			if (game.getActiveMap().getWorld() == location.getWorld()) {
				for (Location spawnLocation : game.getActiveMap().getStarterLocations()) {
					Location l2 = spawnLocation.clone();

					l2.setY(location.getY());

					if (location.distance(l2) <= islandRadius) {
						Log.trace("Replacing loot table for chest on island with " + getIslandLootTable().getName());
						e.setLootTable(getIslandLootTable());
						break;
					}
				}
			}
		}
	}
}