package xyz.mcfridays.base.kills;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import xyz.mcfridays.base.crafting.database.MCFDB;
import xyz.zeeraa.novacore.module.NovaModule;
import xyz.zeeraa.novacore.module.modules.game.events.PlayerEliminatedEvent;
import xyz.zeeraa.novacore.utils.ProjectileUtils;

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
				MCFDB.addKill(killerPlayer);
				MCFPlayerKillCache.getInstance().invalidate(killerPlayer);
			}
		}
	}
}