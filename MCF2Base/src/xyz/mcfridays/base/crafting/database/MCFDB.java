package xyz.mcfridays.base.crafting.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import xyz.mcfridays.base.MCF;

public class MCFDB {
	public static String getData(String key) {
		String result = null;
		try {
			String sql = "SELECT data_value FROM mcf_data WHERE data_key = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setString(1, key);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString("data_value");
			}

			rs.close();
			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		return result;
	}

	public static boolean setData(String key, String value) {
		try {
			String sql = "UPDATE mcf_data SET data_value = ? WHERE data_key = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setString(1, value);
			ps.setString(2, key);

			ps.executeUpdate();

			ps.close();
		} catch (Exception ee) {
			ee.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean setActiveServer(String server) {
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Changing active server to " + server);
		return setData("active_server", server);
	}

	public static String getActiveServer() {
		return getData("active_server");
	}

	public static boolean addKill(OfflinePlayer player) {
		return addKill(player.getUniqueId());
	}

	public static boolean addKill(UUID uuid) {
		try {
			String sql = "UPDATE players SET kills = kills + 1 WHERE uuid = ?";
			PreparedStatement ps = MCF.getDBConnection().getConnection().prepareStatement(sql);

			ps.setString(1, uuid.toString());

			int count = ps.executeUpdate();

			ps.close();

			if (count == 0) {
				return false;
			}

			return true;
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return false;
	}
}