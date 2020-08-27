package xyz.mcfridays.base.misc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.module.NovaModule;
import xyz.mcfridays.base.MCF;

public class MCFPlayerNameCache extends NovaModule implements Listener {
	private static MCFPlayerNameCache instance;

	private HashMap<UUID, String> cache;
	private int taskId;

	public static MCFPlayerNameCache getInstance() {
		return instance;
	}
	
	@Override
	public String getName() {
		return "MCFPlayerNameCache";
	}

	@Override
	public void onLoad() {
		MCFPlayerNameCache.instance = this;
		this.cache = new HashMap<UUID, String>();
		this.taskId = -1;
	}

	@Override
	public void onEnable() throws Exception {
		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCF.getInstance(), new Runnable() {
				@Override
				public void run() {
					updateCache();
				}
			}, 36000L, 36000L); // 30 minutes
		}
		updateCache();
	}
	
	@Override
	public void onDisable() throws Exception {
		if(taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
		
		clearCache();
	}
	
	public String getPlayerName(UUID uuid) {
		String name = null;
		
		if(cache.containsKey(uuid)) {
			name = cache.get(uuid);
		} else {
			Log.trace("Fetching player name from database");
			try {
				String sql = "SELECT username FROM players WHERE uuid = ?";
				PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

				ps.setString(1, uuid.toString());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					name = rs.getString("username");
					cache.put(uuid, name);
				}

				rs.close();
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return name;
	}
	
	public void updateCache() {
		Log.trace("Updating player name cache");
		cache.clear();
		for(Player player: Bukkit.getServer().getOnlinePlayers()) {
			cache.put(player.getUniqueId(), player.getName());
		}
	}

	public void clearCache() {
		Log.trace("Clearing player name cache");
		cache.clear();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Log.trace("Caching player name for " + player.getName());
		cache.put(player.getUniqueId(), player.getName());
	}
}