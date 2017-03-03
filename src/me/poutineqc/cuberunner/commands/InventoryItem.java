package me.poutineqc.cuberunner.commands;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.cuberunner.utils.ItemBannerManager;
import me.poutineqc.cuberunner.utils.ItemHeadManager;
import me.poutineqc.cuberunner.utils.ItemStackManager;

public class InventoryItem {
	private int position;
	private ItemStackManager item;
	
	public InventoryItem(ItemStackManager item, int position) {
		this.item = item;
		this.position = position;
	}
	
	public InventoryItem(ItemStackManager item) {
		this.item = item;
	}
	
	public InventoryItem(ItemStack itemStack) {
		switch (itemStack.getType()) {
		case SKULL_ITEM:
			this.item = new ItemHeadManager(itemStack);
			break;
		case BANNER:
			this.item = new ItemBannerManager(itemStack);
			break;
		default:
			this.item = new ItemStackManager(itemStack);
		}
	}

	public InventoryItem(Material material) {
		switch (material) {
		case SKULL_ITEM:
			this.item = new ItemHeadManager();
			break;
		case BANNER:
			this.item = new ItemBannerManager();
			break;
		default:
			this.item = new ItemStackManager(material);
		}
	}

	public InventoryItem(Material material, int position) {
		this(material);
		this.position = position;
	}

	public ItemStackManager getItem() {
		return item;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public void addToInventory(Inventory inventory) {
		inventory.setItem(position, item.getItem());
	}
}