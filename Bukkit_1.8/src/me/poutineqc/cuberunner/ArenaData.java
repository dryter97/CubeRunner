package me.poutineqc.cuberunner;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaData {
	
	private File arenaFile;
	private FileConfiguration arenaData;

	public ArenaData(CubeRunner plugin) {
		arenaFile = new File(plugin.getDataFolder(), "arenaData.yml");
		if (!arenaFile.exists()) {
			try {
				arenaFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create arenaData.ylm.");
			}
		}
	}
	
	public void loadArenaData() {
		arenaData = YamlConfiguration.loadConfiguration(arenaFile);
	}

	public FileConfiguration getData() {
		return arenaData;
	}

	public void saveArenaData() {
		try {
			arenaData.save(arenaFile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save arenaData.yml!");
		}
	}

	public void reloadArenaData() {
		arenaData = YamlConfiguration.loadConfiguration(arenaFile);
	}
}
