package xyz.mcfridays.base.lobby.npc.trait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import me.rayzr522.jsonmessage.JSONMessage;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.zeeraa.novacore.spigot.module.modules.gui.GUIAction;
import net.zeeraa.novacore.spigot.module.modules.gui.callbacks.GUIClickCallback;
import net.zeeraa.novacore.spigot.module.modules.gui.holders.GUIHolder;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

@TraitName("MerchantTrait")
public class MerchantTrait extends Trait {
	public MerchantTrait() {
		super("MerchantTrait");
	}

	@Override
	public void onAttach() {
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[Debug]: " + ChatColor.GREEN + npc.getName() + " has been assigned MerchantTrait!");
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void click(NPCRightClickEvent e) {
		if (e.getNPC() == this.getNPC()) {
			Player p = e.getClicker();

			if (p != null) {
				GUIHolder holder = new GUIHolder();

				Inventory inventory = Bukkit.createInventory(holder, 9 * 6, "Merchant");
				ItemStack backgroundItem = new ItemBuilder(Material.STAINED_GLASS_PANE).setName(" ").build();

				for (int i = 0; i < inventory.getSize(); i++) {
					inventory.setItem(i, backgroundItem);
				}

				inventory.setItem(0, new ItemBuilder(Material.EMERALD).setName(ChatColor.GREEN + "Visit our webstore").addLore(ChatColor.WHITE + "Click to open our webstore").build());
				holder.addClickCallback(0, new GUIClickCallback() {

					@Override
					public GUIAction onClick(Inventory clickedInventory, Inventory inventory, HumanEntity entity, int clickedSlot, SlotType slotType, InventoryAction clickType) {
						if (entity instanceof Player) {
							entity.closeInventory();
							JSONMessage.create("Click this message to open our webstore").openURL("https://quarantinefridays.tebex.io/").color(ChatColor.GREEN).style(ChatColor.BOLD).send((Player) entity);
						}
						return GUIAction.CANCEL_INTERACTION;
					}
				});

				inventory.setItem(2, new ItemBuilder(Material.NAME_TAG).setName(ChatColor.GREEN + "Ranks").addLore(ChatColor.WHITE + "Click to open our webstore").addLore(ChatColor.WHITE + "and view all ranks.").addLore("").addLore(ChatColor.GREEN + "Ranks are paid monthly").build());
				holder.addClickCallback(2, new GUIClickCallback() {
					@Override
					public GUIAction onClick(Inventory clickedInventory, Inventory inventory, HumanEntity entity, int clickedSlot, SlotType slotType, InventoryAction clickType) {
						if (entity instanceof Player) {
							entity.closeInventory();
							JSONMessage.create("Click this message to open our webstore (Ranks)").openURL("https://quarantinefridays.tebex.io/category/ranks").color(ChatColor.GREEN).style(ChatColor.BOLD).send((Player) entity);
						}
						return GUIAction.CANCEL_INTERACTION;
					}
				});

				inventory.setItem(3, new ItemBuilder(Material.PRISMARINE_SHARD).setName(ChatColor.GREEN + "Gadgets").addLore(ChatColor.WHITE + "Click to open our webstore").addLore(ChatColor.WHITE + "and view all gadgets.").build());
				holder.addClickCallback(3, new GUIClickCallback() {
					@Override
					public GUIAction onClick(Inventory clickedInventory, Inventory inventory, HumanEntity entity, int clickedSlot, SlotType slotType, InventoryAction clickType) {
						if (entity instanceof Player) {
							entity.closeInventory();
							JSONMessage.create("Click this message to open our webstore (Gadgets)").openURL("https://quarantinefridays.tebex.io/category/gadgets").color(ChatColor.GREEN).style(ChatColor.BOLD).send((Player) entity);
						}
						return GUIAction.CANCEL_INTERACTION;
					}
				});

				inventory.setItem(4, new ItemBuilder(Material.TRIPWIRE_HOOK).setName(ChatColor.GREEN + "Treasure keys").addLore(ChatColor.WHITE + "Click to open our webstore").addLore(ChatColor.WHITE + "and view treasure keys.").build());
				holder.addClickCallback(4, new GUIClickCallback() {
					@Override
					public GUIAction onClick(Inventory clickedInventory, Inventory inventory, HumanEntity entity, int clickedSlot, SlotType slotType, InventoryAction clickType) {
						if (entity instanceof Player) {
							entity.closeInventory();
							JSONMessage.create("Click this message to open our webstore (Treasure keys)").openURL("https://quarantinefridays.tebex.io/category/treasure-keys").color(ChatColor.GREEN).style(ChatColor.BOLD).send((Player) entity);
						}
						return GUIAction.CANCEL_INTERACTION;
					}
				});

				inventory.setItem(5, new ItemBuilder(Material.EMERALD).addEnchant(Enchantment.DURABILITY, 1).addItemFlags(ItemFlag.HIDE_ENCHANTS).setName(ChatColor.GREEN + "Special offers").addLore(ChatColor.WHITE + "Click to open our webstore").addLore(ChatColor.WHITE + "and view all special offers.").addLore(" ").addLore(ChatColor.GREEN + "There is usually a free package every week").build());
				holder.addClickCallback(5, new GUIClickCallback() {
					@Override
					public GUIAction onClick(Inventory clickedInventory, Inventory inventory, HumanEntity entity, int clickedSlot, SlotType slotType, InventoryAction clickType) {
						if (entity instanceof Player) {
							entity.closeInventory();
							JSONMessage.create("Click this message to open our webstore (Special offers)").openURL("https://quarantinefridays.tebex.io/category/special-offers").color(ChatColor.GREEN).style(ChatColor.BOLD).send((Player) entity);
						}
						return GUIAction.CANCEL_INTERACTION;
					}
				});

				inventory.setItem(8, new ItemBuilder(Material.BARRIER).setName(ChatColor.RED + "Canceling rank subscriptions").addLore(ChatColor.WHITE + "Click here to receive info about").addLore(ChatColor.WHITE + "canceling rank subscriptions").build());
				holder.addClickCallback(8, new GUIClickCallback() {
					@Override
					public GUIAction onClick(Inventory clickedInventory, Inventory inventory, HumanEntity entity, int clickedSlot, SlotType slotType, InventoryAction clickType) {
						if (entity instanceof Player) {
							entity.closeInventory();
							JSONMessage.create("Click this message for info about canceling rank subscriptions").openURL("https://www.paypal.com/li/smarthelp/article/how-do-i-cancel-a-recurring-payment,-subscription,-or-automatic-billing-agreement-i-have-with-a-merchant-faq1067").color(ChatColor.GREEN).style(ChatColor.BOLD).send((Player) entity);
						}
						return GUIAction.CANCEL_INTERACTION;
					}
				});

				p.openInventory(inventory);
			}
		}
	}
}