package me.poutineqc.cuberunner.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.poutineqc.cuberunner.CubeRunner;

public class MinecraftConfiguration {

	File folder;
	File file;
	FileConfiguration config;

	public MinecraftConfiguration(String folderPath, String fileName, boolean buildIn) {
		getFile(folderPath, fileName, buildIn);

		if (!file.exists()) {

			return;
		}

		this.config = YamlConfiguration.loadConfiguration(file);
	}

	public void getFile(String folderPath, String fileName, boolean buildIn) {
		folder = CubeRunner.get().getDataFolder();
		if (folderPath != null)
			getFolder(folderPath);

		file = new File(folder, fileName + ".yml");

		if (!file.exists()) {
			if (!buildIn) {
				try {
					file.createNewFile();
					return;
				} catch (IOException e) {
					Bukkit.getServer().getLogger().severe("Could not create playerData.ylm.");
					Bukkit.getServer().getLogger().severe("Review your minecraft server's permissions to write and edit files in it's plugin directory");
					Bukkit.getServer().getLogger().severe("Disabling CubeRunner...");
					CubeRunner.get().getPluginLoader().disablePlugin(CubeRunner.get());
				}
			} else {
				folderPath = (folderPath == null) ? "" : (folderPath + "/");
				InputStream local = CubeRunner.get().getResource(folderPath + fileName + ".yml");
				if (local != null) {
					CubeRunner.get().saveResource(folderPath + fileName + ".yml", false);
				} else {
					CubeRunner.get().getLogger().severe("Could not find " + fileName + ".yml");
					CubeRunner.get().getLogger().severe("Contact the developper as fast as possible, this should not happend.");
					Bukkit.getServer().getLogger().severe("Disabling CubeRunner...");
					CubeRunner.get().getPluginLoader().disablePlugin(CubeRunner.get());
				}
			}
		}
	}

	private void getFolder(String folderPath) {
		String[] folderNames = folderPath.split("/");
		for (String names : folderNames) {
			folder = new File(folder, names);
			if (!folder.exists())
				folder.mkdir();
		}
	}

	public boolean hasFile() {
		return file != null;
	}

	public void save() {
		try {
			config.save(file);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(
					ChatColor.RED + "Could not save " + ((file == null) ? "the file." : file.getName() + ".yml."));
		}
	}

	public File getFile() {
		return file;
	}

	public FileConfiguration get() {
		return config;
	}
}
