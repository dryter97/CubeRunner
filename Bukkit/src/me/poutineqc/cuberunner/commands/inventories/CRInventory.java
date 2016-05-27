package me.poutineqc.cuberunner.commands.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.cuberunner.CRPlayer;
import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.Language.Messages;

public abstract class CRInventory {
		protected Inventory inventory;
		protected CRPlayer crPlayer;
		protected int amountOfRows;
		protected String title;

		public CRInventory(CRPlayer crPlayer) {
			this.crPlayer = crPlayer;
		}

		public abstract void fillInventory() throws PlayerStatsException;

		public abstract void update(ItemStack itemStack, InventoryAction action);

		public static boolean areEqualOnColorStrip(String itemA, String itemB) {
			return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', itemA))
					.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', itemB)));
		}

		protected void createInventory() {
			inventory = Bukkit.createInventory(crPlayer.getPlayer(), amountOfRows * 9, getFullTitle());
		}

		protected String getFullTitle() {
			return ChatColor.translateAlternateColorCodes('&', crPlayer.getLanguage().get(Messages.PREFIX_LONG) + " " + title);
		}
		
		protected void openInventory() {
			crPlayer.getPlayer().openInventory(inventory);
			crPlayer.setCurrentInventory(this);
		}
		
		protected void closeInventory() {
			crPlayer.getPlayer().closeInventory();
			crPlayer.setCurrentInventory(null);
		}
}
