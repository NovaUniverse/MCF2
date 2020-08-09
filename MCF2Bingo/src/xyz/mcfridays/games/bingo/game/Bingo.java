package xyz.mcfridays.games.bingo.game;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;

import xyz.zeeraa.novacore.module.modules.game.Game;
import xyz.zeeraa.novacore.module.modules.game.elimination.PlayerQuitEliminationAction;

public class Bingo extends Game implements Listener {
	private boolean started;
	private boolean ended;
	
	// ###### Default functions ######
	@Override
	public String getName() {
		return "bingo";
	}

	@Override
	public String getDisplayName() {
		return "Bingo";
	}
	
	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return PlayerQuitEliminationAction.NONE;
	}

	@Override
	public boolean eliminatePlayerOnDeath(Player player) {
		return false;
	}

	@Override
	public boolean isPVPEnabled() {
		return false;
	}

	@Override
	public boolean autoEndGame() {
		return false;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		if (target instanceof Player) {
			if (attacker instanceof Player) {
				return false;
			}

			if (attacker instanceof Projectile) {
				if (((Projectile) attacker).getShooter() instanceof Player) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}
	
	// ##############################
	
	
	
	@Override
	public void onLoad() {
		this.started = false;
		this.ended = false;
	}

	@Override
	public void onStart() {
		if(started) {
			return;
		}
		started = true;
	}

	@Override
	public void onEnd() {
		if(ended) {
			return;
		}
		ended = true;
	}
}