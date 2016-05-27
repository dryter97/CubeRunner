package me.poutineqc.cuberunner.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemHeadManager extends ItemStackManager {

	private String playerName;

	public ItemHeadManager(String playerName) {
		super(Material.SKULL_ITEM);
		this.durability = 3;
		this.playerName = playerName;
	}

	public ItemHeadManager(ItemStack itemStack) {
		super(itemStack);

		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		this.playerName = meta.hasOwner() ? meta.getOwner() : null;
	}

	public ItemHeadManager() {
		super(Material.SKULL_ITEM);
		this.durability = 3;
	}

	@Override
	public ItemStack getItem() {
		ItemStack itemStack = super.getItem();
		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		if (playerName != null)
			meta.setOwner(playerName);

		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	public boolean isSame(ItemStack itemStack) {
		if (!super.isSame(itemStack))
			return false;

		if (durability != 3)
			return true;

		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		if (meta.hasOwner()) {
			if (playerName == null)
				return false;
			else if (!meta.getOwner().equalsIgnoreCase(playerName))
				return false;
		} else if (playerName != null)
			return false;

		return true;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return playerName;
	}
}