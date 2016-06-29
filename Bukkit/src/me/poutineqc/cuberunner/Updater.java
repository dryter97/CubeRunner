package me.poutineqc.cuberunner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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

	private final CubeRunner plugin;
	private final int id;

	private boolean lastVersion = true;
	private String latestVersion = "";

	public Updater(final CubeRunner plugin) {
		this.plugin = plugin;

		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				checkForLastVersion(plugin);
			}
		}, 0, 72000L);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (Permissions.hasPermission(CRCommand.RELOAD.getPermission(), event.getPlayer(), false) && !lastVersion)
			notifyPlayer(event.getPlayer());
	}

	private void checkForLastVersion(final CubeRunner plugin) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					lastVersion = getInfoFromServer();
				} catch (IOException e) {
					plugin.getLogger().warning("Could not find the latest version available.");
					stop();
					return;
				}
				
				if (!lastVersion) {
					notifyConsole(plugin);
				}
			}
		}).start();
	}

	private boolean getInfoFromServer() throws IOException {

		URL oracle = new URL(spigotPage + "history");
		URLConnection urlConn = oracle.openConnection();
		urlConn.addRequestProperty("User-Agent", "Mozilla/4.76");
		InputStream is = urlConn.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is, 4 * 1024);
		BufferedReader in = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8));

		latestVersion = null;
		String inputLine;
		while ((inputLine = in.readLine()) != null && latestVersion == null)
			if (inputLine.matches("^.*?([1-9][0-9]?\\.[1-9][0-9]?.*)$") && inputLine.contains(plugin.getName()))
				latestVersion = inputLine.replaceAll(" ", "").replace(plugin.getName(), "").replaceAll("<[^>]*>", "");

		in.close();
		if (latestVersion == null) {
			throw new IOException("Could not find the version on the page.");
		}
		return latestVersion.equalsIgnoreCase(plugin.getDescription().getVersion());
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
