package me.poutineqc.cuberunner.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import me.poutineqc.cuberunner.ArenaData;
import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.games.Arena;

public class ColorManager {

	private long colorIndice;
	private List<ItemStackManager> allBlocks;
	private List<ItemStackManager> onlyChoosenBlocks;
	private MySQL mysql;
	private Configuration config;
	private Arena arena;
	private ArenaData arenaData;

	public ColorManager(Long colorIndice, CubeRunner plugin, Arena arena) {
		this.colorIndice = colorIndice;
		this.mysql = plugin.getMySQL();
		this.arenaData = plugin.getArenaData();
		this.config = plugin.getConfiguration();
		this.arena = arena;
		updateLists();
	}

	public void setColorIndice(long colorIndice) {
		this.colorIndice = colorIndice;
		updateLists();
		
		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET colorIndice=" + colorIndice + " WHERE name='"
					+ arena.getName() + "';");
		} else {
			arenaData.getData().set("arenas." + arena.getName() + ".colorIndice", colorIndice);
			arenaData.saveArenaData();
		}
	}

	public void updateLists() {
		allBlocks = new ArrayList<ItemStackManager>();
		onlyChoosenBlocks = new ArrayList<ItemStackManager>();
		long tempColorIndice = colorIndice;
		
		for (int i = 31; i >= 0; i--) {
			ItemStackManager icon;
			if (i >= 16)
				icon = new ItemStackManager(Material.STAINED_CLAY);
			else
				icon = new ItemStackManager(Material.WOOL);

			icon.setData((short) (i % 16));

			int value = (int) Math.pow(2, i);
			if (value <= tempColorIndice) {
				icon.addEnchantement(Enchantment.DURABILITY, 1);
				tempColorIndice -= value;
				onlyChoosenBlocks.add(0, icon);
			}

			allBlocks.add(0, icon);
		}
		
		if (onlyChoosenBlocks.size() == 0)
			onlyChoosenBlocks = allBlocks;
	}

	public ItemStackManager getRandomAvailableBlock() {
		return onlyChoosenBlocks.get((int) Math.floor(Math.random() * onlyChoosenBlocks.size()));
	}
	
	public List<ItemStackManager> getAllBlocks() {
		return allBlocks;
	}
	
	public List<ItemStackManager> getOnlyChoosenBlocks() {
		return onlyChoosenBlocks;
	}

	public long getColorIndice() {
		return colorIndice;
	}
	
}
