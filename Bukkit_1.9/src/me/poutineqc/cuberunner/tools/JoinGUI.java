package me.poutineqc.cuberunner.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.PlayerData;
import me.poutineqc.cuberunner.games.Arena;
import me.poutineqc.cuberunner.games.GameState;

public class JoinGUI implements Listener {

	private PlayerData playerData;

	public JoinGUI(CubeRunner plugin) {
		playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onPlayerInventoryClick(InventoryClickEvent event) {

		if (!(event.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) event.getWhoClicked();
		Language local = playerData.getLanguageOfPlayer(player);

		if (!ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase(ChatColor
				.stripColor(ChatColor.translateAlternateColorCodes('&', local.guiJoinName + " &0: &5CubeRunner"))))
			return;

		event.setCancelled(true);

		if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN)
			return;

		String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

		if (isEqualOnColorStrip(itemName, ChatColor.translateAlternateColorCodes('&', local.guiNextPage))
				|| isEqualOnColorStrip(itemName, ChatColor.translateAlternateColorCodes('&', local.guiPreviousPage))) {
			openJoinGui(player,
					Integer.parseInt(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getLore().get(0))));
			return;
		}

		Arena arena = Arena.getArena(ChatColor.stripColor(itemName));
		if (arena == null)
			return;

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			arena.displayInformation(player);
		} else {
			arena.addPlayer(player, true);
		}

		player.closeInventory();
	}

	private boolean isEqualOnColorStrip(String toCheck, String original) {
		return ChatColor.stripColor(toCheck)
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', original)));
	}

	public void openJoinGui(Player player, int page) {

		List<String> Arenas = new ArrayList<String>();
		for (Arena arena : Arena.getArenas())
			Arenas.add(arena.getName());
		java.util.Collections.sort(Arenas);

		for (int i = 0; i < (page - 1) * 36; i++) {
			Arenas.remove(0);
		}

		int size;
		if (Arenas.size() > 36)
			size = 54;
		else {
			size = (int) (Math.ceil((Arenas.size() + 18.0) / 9.0) * 9.0);
		}

		Language local = playerData.getLanguageOfPlayer(player);
		Inventory inv = Bukkit.createInventory(null, size,
				ChatColor.translateAlternateColorCodes('&', local.guiJoinName + " &0: &5CubeRunner"));
		ItemStackManager icon;

		/***************************************************
		 * Instructions
		 ***************************************************/

		icon = new ItemStackManager(Material.BOOKSHELF, 4);
		icon.setTitle(local.guiInstrictions);
		for (String loreLine : local.guiJoinInfo.split("\n"))
			icon.addToLore(loreLine);
		icon.addToInventory(inv);

		/***************************************************
		 * Glass Spacer
		 ***************************************************/

		icon = new ItemStackManager(Material.STAINED_GLASS_PANE);
		icon.setData((short) 10);
		icon.setTitle(" ");

		for (int i = 0; i < inv.getSize(); i++)
			switch (i) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
				icon.setPosition(i);
				icon.addToInventory(inv);
			}

		/***************************************************
		 * arenas
		 ***************************************************/

		icon = new ItemStackManager(Material.INK_SACK);
		int slot = 18;

		for (String arenaName : Arenas) {
			Arena arena = Arena.getArena(arenaName);
			icon.clearLore();

			if (arena.getGameState() == GameState.UNREADY) {
				icon.setData((short) 8);
				icon.setTitle(ChatColor.GOLD + arenaName);
				icon.addToLore(local.keyWordUnset);

			} else if (arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
				icon.setData((short) 12);
				icon.setTitle(ChatColor.GOLD + arenaName);
				icon.addToLore(local.guiJoinStarted);

			} else {
				icon.setData((short) 10);
				icon.setTitle(ChatColor.GOLD + arenaName);
				icon.addToLore(local.guiJoinReady);
			}

			icon.setPosition(slot++);
			icon.addToInventory(inv);

			/***************************************************
			 * NextPage
			 ***************************************************/

			if (slot == 54 && Arenas.size() > 36) {
				icon = new ItemStackManager(Material.ARROW, 8);
				icon.setTitle(local.guiNextPage);
				icon.addToLore(String.valueOf(page + 1));
				icon.addToInventory(inv);
				break;
			}
		}

		/***************************************************
		 * Previous Page
		 ***************************************************/

		if (page > 1) {
			icon = new ItemStackManager(Material.ARROW, 7);
			icon.setTitle(local.guiPreviousPage);
			icon.addToLore(String.valueOf(page - 1));
			inv = icon.addToInventory(inv);
		}

		player.openInventory(inv);
	}

}
