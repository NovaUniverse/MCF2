package xyz.mcfridays.bungeecord;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.zeeraa.novacore.bungeecord.novaplugin.NovaPlugin;
import net.zeeraa.novacore.commons.database.DBConnection;
import net.zeeraa.novacore.commons.database.DBCredentials;
import net.zeeraa.novacore.commons.database.async.ExecuteUpdateAsyncCallback;
import net.zeeraa.novacore.commons.log.Log;

public class MCF2BungeecordPlugin extends NovaPlugin implements Listener {
	private static MCF2BungeecordPlugin instance;
	private static DBConnection dbConnection;

	public static MCF2BungeecordPlugin getInstance() {
		return instance;
	}

	public static DBConnection getDbConnection() {
		return dbConnection;
	}

	@Override
	public void onEnable() {
		MCF2BungeecordPlugin.instance = this;

		saveDefaultConfiguration();

		DBCredentials dbCredentials = new DBCredentials(getConfig().getString("mysql.driver"), getConfig().getString("mysql.host"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"));

		try {
			MCF2BungeecordPlugin.dbConnection.connect(dbCredentials);
			MCF2BungeecordPlugin.dbConnection.startKeepAliveTask();
		} catch (ClassNotFoundException | SQLException e) {
			Log.fatal("MCF2BungeecordPlugin", "Failed to connect to the database");
			e.printStackTrace();
			return;
		}

		ProxyServer.getInstance().getPluginManager().registerListener(this, this);
	}

	@Override
	public void onDisable() {
		ProxyServer.getInstance().getPluginManager().unregisterListeners((Plugin) this);
		ProxyServer.getInstance().getScheduler().cancel(this);

		if (MCF2BungeecordPlugin.dbConnection != null) {
			if (MCF2BungeecordPlugin.dbConnection.isKeepAliveTaskRunning()) {
				MCF2BungeecordPlugin.dbConnection.endKeepAliveTask();
			}

			try {
				if (MCF2BungeecordPlugin.dbConnection.isConnected()) {
					MCF2BungeecordPlugin.dbConnection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(ChatEvent e) {
		if (e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) e.getSender();

			String sql = "INSERT INTO `global_chat_log` (`id`, `uuid`, `username`, `timestamp`, `server_name`, `content`, `is_command`, `canceled`) VALUES (NULL, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";
			try {
				PreparedStatement ps = dbConnection.getConnection().prepareStatement(sql);

				ps.setString(1, player.getUniqueId().toString());
				ps.setString(2, player.getName());
				ps.setString(3, player.getServer().getInfo().getName());
				ps.setString(4, e.getMessage());

				ps.setBoolean(5, e.isCommand());
				ps.setBoolean(6, e.isCancelled());

				DBConnection.executeUpdateAsync(ps, new ExecuteUpdateAsyncCallback() {
					@Override
					public void onExecute(int result, Exception exception) {
						if (exception != null) {
							Log.error("Failed to log chat message. Cause: " + exception.getClass().getName() + " : " + exception.getMessage());
							exception.printStackTrace();
						}
					}
				});
			} catch (Exception ex) {
				Log.error("Failed to log chat message. Cause: " + ex.getClass().getName() + " : " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
}