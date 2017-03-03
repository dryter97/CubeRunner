package me.poutineqc.cuberunner.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackManager {

	protected Material material;
	protected int amount = 1;
	protected short durability = 0;

	protected String name;
	protected List<String> lore = new ArrayList<String>();
	protected Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();

	public ItemStackManager(Material material) {
		this.material = material;
	}

	public ItemStackManager(ItemStack itemStack) {
		this.material = itemStack.getType();
		this.amount = itemStack.getAmount();
		this.durability = itemStack.getDurability();

		ItemMeta meta = itemStack.getItemMeta();
		this.name = meta.hasDisplayName() ? meta.getDisplayName() : null;
		this.enchantments = meta.hasEnchants() ? meta.getEnchants() : new HashMap<Enchantment, Integer>();
		this.lore = meta.hasLore() ? meta.getLore() : null;
	}

	public ItemStack getItem() {
		ItemStack itemStack = new ItemStack(material, amount, durability);

		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		for (Entry<Enchantment, Integer> enchantment : enchantments.entrySet())
			meta.addEnchant(enchantment.getKey(), enchantment.getValue(), true);

		itemStack.setItemMeta(meta);
		return itemStack;
	}

	public boolean isSame(ItemStack itemStack) {
		if (material != itemStack.getType())
			return false;

		if (durability != itemStack.getDurability())
			return false;

		if (itemStack.getItemMeta().hasDisplayName()) {
			if (name == null)
				return false;
			
			if (!Utils.isEqualOnColorStrip(itemStack.getItemMeta().getDisplayName(), name))
				return false;
			
		} else if (name != null)
			return false;

		if (itemStack.getItemMeta().hasEnchants()) {
			for (Entry<Enchantment, Integer> enchantment : itemStack.getItemMeta().getEnchants().entrySet())
				if (!hasEnchantement(enchantment.getKey(), enchantment.getValue()))
					return false;
		} else if (enchantments.size() > 0)
			return false;

		return true;
	}

	public void setData(short durability) {
		this.durability = durability;
	}

	public void setDisplayName(String displayName) {
		this.name = ChatColor.translateAlternateColorCodes('&', displayName);
	}

	public void addToLore(String loreLine) {
		lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public void clearLore() {
		lore = new ArrayList<String>();
	}

	public void addEnchantement(Enchantment enchantment, int level) {
		enchantments.put(enchantment, level);
	}

	public void setEnchantements(Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
	}

	public void clearEnchantements() {
		enchantments.clear();
	}

	public int getMaxStackSize() {
		return material.getMaxStackSize();
	}

	public Material getMaterial() {
		return material;
	}

	public short getDurability() {
		return durability;
	}

	public boolean hasEnchantement(Enchantment enchantement) {
		for (Entry<Enchantment, Integer> entry : enchantments.entrySet())
			if (entry.getKey() == enchantement)
				return true;

		return false;
	}

	public boolean hasEnchantement(Enchantment enchantement, int value) {
		for (Entry<Enchantment, Integer> entry : enchantments.entrySet())
			if (entry.getKey() == enchantement)
				if (entry.getValue() == value)
					return true;
				else
					return false;

		return false;
	}

	public String getDisplayName() {
		return name;
	}

	public List<String> getLore() {
		return lore;
	}
}
