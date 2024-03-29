package xyz.mcfridays.base.kills;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.modules.game.events.PlayerEliminatedEvent;
import net.zeeraa.novacore.spigot.utils.ProjectileUtils;
import xyz.mcfridays.base.crafting.database.MCFDB;

public class KillListener extends NovaModule implements Listener {
	@Override
	public String getName() {
		return "MCFKillListener";
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEliminated(PlayerEliminatedEvent e) {

		if (e.getPlayer().isOnline()) {
			Entity killer = e.getKiller();

			Player killerPlayer = null;

			if (ProjectileUtils.isProjectile(killer)) {
				Entity shooter = ProjectileUtils.getProjectileShooterEntity(killer);

				if (shooter != null) {
					if (shooter instanceof Player) {
						killerPlayer = (Player) shooter;
					}
				}
			} else if (killer instanceof Player) {
				killerPlayer = (Player) killer;
			}

			if (killerPlayer != null) {
				killerPlayer.setLevel(killerPlayer.getLevel() + 1);
				MCFDB.addKill(killerPlayer);
				MCFPlayerKillCache.getInstance().invalidate(killerPlayer);
			}
		}
	}
}