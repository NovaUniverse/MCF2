package xyz.mcfridays.base.listeners;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import xyz.mcfridays.base.MCF;

public class PlayerListener implements Listener {
	private List<UUID> hideQuitMessage;

	public PlayerListener() {
		this.hideQuitMessage = new ArrayList<UUID>();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		boolean playerFound = false;
		boolean isFirstTime = false;

		try {
			String sql = "SELECT id, has_joined FROM players WHERE uuid = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setString(1, p.getUniqueId().toString());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				playerFound = true;

				isFirstTime = !rs.getBoolean("has_joined");
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			p.kickPlayer(ChatColor.DARK_RED + ee.getClass().getName() + "\n\n" + ee.getMessage());
			return;
		}

		if (!playerFound) {
			isFirstTime = true;
			try {
				String sql = "INSERT INTO players (uuid, username, has_joined) VALUES (?, ?, 1)";
				PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

				ps.setString(1, p.getUniqueId().toString());
				ps.setString(2, p.getName());

				ps.executeUpdate();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
				p.kickPlayer(ChatColor.DARK_RED + ee.getClass().getName() + "\n\n" + ee.getMessage());
				return;
			}
		}

		try {
			String sql = "UPDATE players SET username = ?, has_joined = 1 WHERE uuid = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setString(1, p.getName());
			ps.setString(2, p.getUniqueId().toString());

			ps.executeUpdate();

			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			p.kickPlayer(ChatColor.DARK_RED + ee.getClass().getName() + "\n\n" + ee.getMessage());
			return;
		}

		if (!playerFound || isFirstTime) {
			e.setJoinMessage(null);
			hideQuitMessage.add(p.getUniqueId());
			p.kickPlayer(ChatColor.AQUA + "You need to reconnect since this is the first time joining the MCF server");
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (hideQuitMessage.contains(p.getUniqueId())) {
			hideQuitMessage.remove(p.getUniqueId());

			e.setQuitMessage(null);
		}
	}
}