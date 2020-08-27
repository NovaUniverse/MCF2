package xyz.mcfridays.linkserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.zeeraa.novacore.commons.database.DBConnection;
import net.zeeraa.novacore.commons.database.DBCredentials;
import net.zeeraa.novacore.commons.log.Log;

public class MCFLinkServer extends JavaPlugin implements Listener {
	private DBConnection dbc;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		dbc = new DBConnection();

		DBCredentials dbCredentials = new DBCredentials(getConfig().getString("mysql.driver"), getConfig().getString("mysql.host"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"));

		try {
			dbc.connect(dbCredentials);
		} catch (ClassNotFoundException | SQLException e) {
			Log.fatal("MCFLinkServer", "Failed to connect to the database");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);

		if (dbc != null) {
			try {
				if (dbc.isConnected()) {
					dbc.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void kick(Player p, Exception e) {
		kick(p, ChatColor.RED + "An internal server error occurred\nPlease send a screenshot of this message to an admin\n\n" + e.getClass().getName() + " : " + e.getMessage());
	}

	public void kick(Player p, String message) {
		p.kickPlayer(ChatColor.AQUA + "-=-=-=-= MCF Discord =-=-=-=-\n\n" + message);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);

		Log.info(e.getPlayer().getName() + " is connecting");

		Player p = e.getPlayer();

		boolean playerFound = false;

		boolean isLinked = false;

		String linkCode = "";

		String message = "";

		try {
			String sql = "SELECT link_code, discord_id FROM users WHERE minecraft_uuid = ?";
			PreparedStatement ps = dbc.getConnection().prepareStatement(sql);

			ps.setString(1, p.getUniqueId().toString());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				playerFound = true;

				linkCode = rs.getString("link_code");

				if (rs.getString("discord_id") != null) {
					isLinked = true;
				}
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			kick(p, ee);
			return;
		}

		if (!playerFound) {
			linkCode = generateId();
			try {
				String sql = "INSERT INTO `users` (`id`, `minecraft_uuid`, `minecraft_username`, `link_code`, `discord_id`, `discord_tag`, `team_number`, `link_date`, `created`) VALUES (NULL, ?, ?, ?, NULL, NULL, '-1', NULL, CURRENT_TIMESTAMP)";
				PreparedStatement ps = dbc.getConnection().prepareStatement(sql);

				ps.setString(1, p.getUniqueId().toString());
				ps.setString(2, e.getPlayer().getName());
				ps.setString(3, linkCode);

				ps.executeUpdate();

				ps.close();
			} catch (Exception ee) {
				ee.printStackTrace();
				kick(p, ee);
				return;
			}
		}

		if (isLinked) {
			message = ChatColor.RED + "You have already linked your account";
			Log.info(e.getPlayer().getName() + " has already linked");
		} else {
			message = ChatColor.GREEN + "Send this message to MC FRIDAYS bot DM's. " + ChatColor.GOLD + " !link " + linkCode;
			Log.info(e.getPlayer().getName() + " received the code");
		}

		kick(p, message);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);
	}

	private String generateId() {
		return UUID.randomUUID().toString().substring(0, 8);
	}
}