package xyz.mcfridays.games.bingo.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.zeeraa.novacore.NovaCore;
import net.zeeraa.novacore.callbacks.Callback;
import net.zeeraa.novacore.module.modules.game.Game;
import net.zeeraa.novacore.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.module.modules.gui.holders.GUIReadOnlyHolder;
import net.zeeraa.novacore.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.module.modules.multiverse.MultiverseWorld;
import net.zeeraa.novacore.module.modules.multiverse.WorldOptions;
import net.zeeraa.novacore.module.modules.multiverse.WorldUnloadOption;
import net.zeeraa.novacore.tasks.SimpleTask;
import net.zeeraa.novacore.teams.Team;
import net.zeeraa.novacore.timers.BasicTimer;
import net.zeeraa.novacore.utils.ItemBuilder;
import net.zeeraa.novacore.utils.LocationUtils;
import net.zeeraa.novacore.utils.PlayerUtils;
import net.zeeraa.novacore.world.worldgenerator.worldpregenerator.WorldPreGenerator;
import xyz.mcfridays.games.bingo.MCFBingo;
import xyz.mcfridays.games.bingo.game.event.TeamCompleteGameEvent;
import xyz.mcfridays.games.bingo.game.event.TeamCompleteItemEvent;
import xyz.mcfridays.games.bingo.game.messages.TeamCompleteGameMessage;
import xyz.mcfridays.games.bingo.game.messages.TeamFailMessage;
import xyz.mcfridays.games.bingo.game.messages.TeamFindItemMessage;
import xyz.mcfridays.games.bingo.game.messages.defaults.DefaultTeamMessages;

public class Bingo extends Game implements Listener {
	private boolean started;
	private boolean ended;

	private int worldSizeChunks;

	private HashMap<UUID, Location> teamStartLocations;

	private ArrayList<ItemStack> targetItems;

	private HashMap<UUID, ArrayList<Integer>> teamCompletedItems;
	private HashMap<UUID, Inventory> teamMenu;

	private int finishedTeamCount;

	private WorldPreGenerator worldPreGenerator;

	private TeamFindItemMessage teamFindItemMessage;
	private TeamCompleteGameMessage teamCompleteGameMessage;
	private TeamFailMessage teamFailMessage;

	private BasicTimer gameTimer;

	private SimpleTask checkTask;

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
		return PlayerQuitEliminationAction.DELAYED;
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
	public boolean canStart() {
		return worldPreGenerator.isFinished();
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

		this.worldSizeChunks = 32;

		this.teamStartLocations = new HashMap<UUID, Location>();

		this.finishedTeamCount = 0;

		DefaultTeamMessages defaultTeamMessages = new DefaultTeamMessages();

		this.teamFindItemMessage = defaultTeamMessages;
		this.teamCompleteGameMessage = defaultTeamMessages;
		this.teamFailMessage = defaultTeamMessages;

		this.targetItems = BingoItemGenerator.generate();
		this.teamMenu = new HashMap<UUID, Inventory>();
		this.teamCompletedItems = new HashMap<UUID, ArrayList<Integer>>();

		this.checkTask = new SimpleTask(MCFBingo.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (world != null) {
					for (UUID uuid : players) {
						Player player = Bukkit.getPlayer(uuid);

						if (player != null) {
							if (player.isOnline()) {
								if (player.getGameMode() == GameMode.SPECTATOR) {
									continue;
								}

								for (ItemStack item : player.getInventory().getContents()) {
									if (item != null) {
										checkItem(item, player);
									}
								}
							}
						}
					}

					for (Player player : world.getPlayers()) {
						int x = (int) player.getLocation().getX();
						int z = (int) player.getLocation().getZ();

						if (x < 0) {
							x *= -1;
						}

						if (z < 0) {
							z *= -1;
						}

						if (x > (world.getWorldBorder().getSize() / 2) + 10 || z > (world.getWorldBorder().getSize() / 2) + 10) {
							player.teleport(world.getSpawnLocation());
						}
					}
				}
				
				if(players.size() == 0) {
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD+""+ChatColor.BOLD +"All teams has finished");
					endGame();
				}
			}
		}, 20L, 20L);

		this.gameTimer = new BasicTimer(2400, 20L);

		gameTimer.addFinishCallback(new Callback() {
			@Override
			public void execute() {
				ArrayList<UUID> teamsFailed = new ArrayList<UUID>();
				for (UUID uuid : players) {
					OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(uuid);
					if (p != null) {
						Team team = NovaCore.getInstance().getTeamManager().getPlayerTeam(p);

						if (!teamsFailed.contains(team.getTeamUuid())) {
							teamsFailed.add(team.getTeamUuid());

							teamFailMessage.showTeamFailMessage(team);
						}

						if (p.isOnline()) {
							Player player = p.getPlayer();

							player.playSound(player.getLocation(), Sound.WITHER_HURT, 1F, 1F);
						}
					}
				}
				if (!hasEnded()) {
					endGame();
				}
			}
		});

		MultiverseWorld multiverseWorld = MultiverseManager.getInstance().createWorld(new WorldOptions("bingo_world"));

		multiverseWorld.setSaveOnUnload(false);
		multiverseWorld.setUnloadOption(WorldUnloadOption.DELETE);

		this.worldPreGenerator = new WorldPreGenerator(multiverseWorld.getWorld(), worldSizeChunks + 10, 32, 1, new Callback() {
			@Override
			public void execute() {
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "The world has been loded");
			}
		});

		worldPreGenerator.start();

		this.world = multiverseWorld.getWorld();

		this.world.setDifficulty(Difficulty.EASY);

		this.world.setGameRuleValue("doFireTick", "false");
		this.world.setGameRuleValue("keepInventory", "true");
	}

	public BasicTimer getGameTimer() {
		return gameTimer;
	}

	@Override
	public void onStart() {
		if (started) {
			return;
		}
		started = true;

		for (UUID uuid : players) {
			Player player = Bukkit.getServer().getPlayer(uuid);

			if (player != null) {
				if (player.isOnline()) {
					tpToArena(player);
				}
			}
		}

		gameTimer.start();
		checkTask.start();
	}

	@Override
	public void onEnd() {
		if (ended) {
			return;
		}

		if (gameTimer != null) {
			gameTimer.cancel();
		}

		if (checkTask != null) {
			checkTask.stop();
		}

		if (worldPreGenerator != null) {
			if (!worldPreGenerator.isFinished()) {
				worldPreGenerator.stop();
			}
		}

		ended = true;
	}

	@Override
	public void onUnload() {
		if (gameTimer != null) {
			gameTimer.cancel();
		}

		if (checkTask != null) {
			checkTask.stop();
		}
	}

	public WorldPreGenerator getWorldPreGenerator() {
		return worldPreGenerator;
	}

	@Override
	public void tpToSpectator(Player player) {
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(world.getSpawnLocation());
	}

	public void tpToArena(Player player) {
		PlayerUtils.clearPlayerInventory(player);

		ItemStack item = new ItemBuilder(Material.WRITTEN_BOOK).setName(ChatColor.GOLD + "" + ChatColor.BOLD + "Bingo").addLore(ChatColor.GREEN + "Click to open bingo menu").addLore(ChatColor.GREEN + "or use the /bingo command").build();

		item = NBTEditor.set(item, 1, "bingo", "bingobook");

		player.getInventory().addItem(item);

		Team team = NovaCore.getInstance().getTeamManager().getPlayerTeam(player);
		if (team != null) {
			if (teamStartLocations.containsKey(team.getTeamUuid())) {
				tpToArenaLocation(player, teamStartLocations.get(team.getTeamUuid()));
				return;
			}
		}

		for (int i = 0; i < 10000; i++) {
			Location location = tryGetSpawnLocation();
			if (location == null) {
				continue;
			}

			if (team != null) {
				teamStartLocations.put(team.getTeamUuid(), location);
			}

			tpToArenaLocation(player, location);

			return;
		}

		player.sendMessage(ChatColor.RED + "Failed to teleport within 10000 attempts, Sending you to default world spawn");
		tpToArenaLocation(player, world.getSpawnLocation());
	}

	private void tpToArenaLocation(Player player, Location location) {
		if (!location.getChunk().isLoaded()) {
			location.getChunk().load();
		}

		player.teleport(new Location(world, LocationUtils.blockCenter(location.getBlockX()), location.getY() + 1, LocationUtils.blockCenter(location.getBlockZ())));
		player.setGameMode(GameMode.SURVIVAL);
		NovaCore.getInstance().getVersionIndependentUtils().resetEntityMaxHealth(player);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setFireTicks(0);
	}

	public Location tryGetSpawnLocation() {
		int max = (worldSizeChunks * 16) - 50;

		Random random = new Random();
		int x = max - random.nextInt(max * 2);
		int z = max - random.nextInt(max * 2);

		Location location = new Location(world, x, 256, z);

		for (int i = 256; i > 14; i++) {
			location.setY(location.getY() - 1);

			Block b = location.clone().add(0, -1, 0).getBlock();

			if (b.getType() != Material.AIR) {
				if (b.isLiquid()) {
					break;
				}

				if (b.getType().isSolid()) {
					return location;
				}
			}
		}

		return null;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		Team team = NovaCore.getInstance().getTeamManager().getPlayerTeam(e.getPlayer());

		if (team != null) {
			if (teamStartLocations.containsKey(team.getTeamUuid())) {
				Location location = teamStartLocations.get(team.getTeamUuid());
				tpToArenaLocation(player, location);
				return;
			}
		}

		tpToArenaLocation(player, world.getSpawnLocation());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getItem() != null) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (e.getItem().getType() == Material.COMPASS) {
					if (NBTEditor.contains(e.getItem(), "bingo", "bingobook")) {
						e.getPlayer().performCommand("bingo");
					}
				}
			}
		}
	}

	public void createInventory(UUID uuid) {
		if (!teamCompletedItems.containsKey(uuid)) {
			teamCompletedItems.put(uuid, new ArrayList<Integer>());
		}

		Inventory inventory = Bukkit.createInventory(new GUIReadOnlyHolder(), 9 * 3, "Bingo");

		ItemStack backgroundItem = new ItemBuilder(Material.STAINED_GLASS_PANE).setName(" ").build();
		for (int i = 0; i < inventory.getSize(); i++) {
			inventory.setItem(i, backgroundItem);
		}

		teamMenu.put(uuid, inventory);

		updateInventory(uuid);
	}

	public boolean updateInventory(UUID uuid) {
		if (!teamMenu.containsKey(uuid)) {
			return false;
		}

		Inventory inventory = teamMenu.get(uuid);

		for (int i = 0; i < 9; i++) {
			int row = (((i) / 3) % 3) + 1;

			int itemSlot = (3 * row) + i + ((row - 1) * 3);

			if (targetItems.size() > i) {
				ItemBuilder iconBuilder = new ItemBuilder(targetItems.get(i).clone());

				if (teamCompletedItems.get(uuid).contains(i)) {
					iconBuilder.addLore(ChatColor.AQUA + "Completed");
					iconBuilder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					iconBuilder.addEnchant(Enchantment.DURABILITY, 1, true);
				} else {
					iconBuilder.addLore(ChatColor.AQUA + "Pick one up to complete");
				}

				inventory.setItem(itemSlot, iconBuilder.build());
			}
		}

		return true;
	}

	public ArrayList<ItemStack> getTargetItems() {
		return targetItems;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		Player player = e.getPlayer();

		checkItem(e.getItem().getItemStack(), player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player player = (Player) e.getWhoClicked();

			checkItem(e.getCurrentItem(), player);
		}
	}

	public Inventory getTeamMenu(Team team) {
		return this.getTeamMenu(team.getTeamUuid());
	}

	public Inventory getTeamMenu(UUID uuid) {
		return teamMenu.get(uuid);
	}

	public boolean hasTeamMenu(Team team) {
		return this.hasTeamMenu(team.getTeamUuid());
	}

	public boolean hasTeamMenu(UUID uuid) {
		return teamMenu.containsKey(uuid);
	}

	private void checkItem(ItemStack item, Player player) {
		Team team = NovaCore.getInstance().getTeamManager().getPlayerTeam(player);

		if (team != null) {

			for (int i = 0; i < targetItems.size(); i++) {
				if (teamCompletedItems.get(team.getTeamUuid()).contains(i)) {
					// System.out.println(player.getName() + " has already completed " +
					// targetItems.get(i).getType() + " id: " + i);
					continue;
				}

				ItemStack targetItem = targetItems.get(i);

				if (item.getType() == targetItem.getType()) {
					// System.out.println(e.getItem().getItemStack().getData() + " and " +
					// targetItem.getData());
					if (item.getData().equals(targetItem.getData())) {
						teamCompletedItems.get(team.getTeamUuid()).add(i);
						updateInventory(team.getTeamUuid());

						int totalItems = teamCompletedItems.get(team.getTeamUuid()).size();

						teamFindItemMessage.showTeamFindItemMessage(team, totalItems);

						TeamCompleteItemEvent event = new TeamCompleteItemEvent(team, item.getType());
						Bukkit.getServer().getPluginManager().callEvent(event);

						if (teamCompletedItems.get(team.getTeamUuid()).size() >= 9) {
							for (UUID uuid : team.getMembers()) {
								Player p2 = Bukkit.getPlayer(uuid);
								if (p2 != null) {
									if (p2.isOnline()) {
										p2.setGameMode(GameMode.SPECTATOR);
										p2.playSound(p2.getLocation(), Sound.LEVEL_UP, 1F, 1F);
									}
								}

								if (players.contains(uuid)) {
									players.remove(uuid);
								}
							}
							finishedTeamCount++;

							TeamCompleteGameEvent event2 = new TeamCompleteGameEvent(team, finishedTeamCount);
							Bukkit.getServer().getPluginManager().callEvent(event2);

							teamCompleteGameMessage.showTeamCompleteGameMessage(team, finishedTeamCount);

							return;
						}
					}
				}
			}
		}
	}
}