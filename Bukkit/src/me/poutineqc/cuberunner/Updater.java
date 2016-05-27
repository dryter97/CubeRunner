package me.poutineqc.cuberunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.poutineqc.cuberunner.commands.CRCommand;
import me.poutineqc.cuberunner.utils.Permissions;

public final class Updater implements Listener {

	private static final String spigotPage = "https://www.spigotmc.org/resources/cuberunner.19715/";
	private static final String versionPage = "http://www.poutineqc.ca/pluginVersion.txt";

	private final CubeRunner plugin;
	private final int id;
	
	private boolean lastVersion;
	private String latestVersion;

	public Updater(final CubeRunner plugin) {
		this.plugin = plugin;

		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				checkForLastVersion(plugin);
				if (!lastVersion) {
					notifyConsole(plugin);
				}
			}
		}, 0, 72000L);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (Permissions.hasPermission(CRCommand.RELOAD.getPermission(), event.getPlayer(), false) && !lastVersion)
			notifyPlayer(event.getPlayer());
	}

	private void checkForLastVersion(CubeRunner plugin) {
		boolean next = false;
		
		try {
			lastVersion = getInfoFromServer();
		} catch (IOException e) {
			plugin.getLogger().warning("Could not find the latest version available.");
		}

		lastVersion = (next) ? latestVersion.equalsIgnoreCase(plugin.getDescription().getVersion()) : true;
	}
	
	private boolean getInfoFromServer() throws IOException {
		boolean next = false;
		
		URL url = new URL(versionPage);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		while ((latestVersion = in.readLine()) != null && !next) {
			if (latestVersion.equalsIgnoreCase(CubeRunner.name))
				next = true;
		}

		in.close();
		return next;
	}

	private void notifyConsole(CubeRunner plugin) {
		Logger logger = plugin.getLogger();
		logger.info("----------------------------");
		logger.info("CubeRunner Updater");
		logger.info("");
		logger.info("An update for CubeRunner has been found!");
		logger.info("CubeRunner " + latestVersion);
		logger.info("You are running " + plugin.getDescription().getVersion());
		logger.info("");
		logger.info("Download at https://www.spigotmc.org/resources/cuberunner.19715/");
		logger.info("----------------------------");
	}

	private void notifyPlayer(Player player) {
		Language local = plugin.getCRPlayer(player).getLanguage();
		local.sendMsg(player,
				String.format("&5A new CubeRunner version is available &d(v%1$s)&5.%n&5Get it now : &d%2$s",
						latestVersion, spigotPage));
	}

	public void stop() {
		Bukkit.getScheduler().cancelTask(id);
	}

}
