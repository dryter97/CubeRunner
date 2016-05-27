package me.poutineqc.cuberunner.games;

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
import org.bukkit.inventory.ItemStack;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.PlayerData;
import me.poutineqc.cuberunner.tools.ItemStackManager;

public class ColorGUI implements Listener {

	private PlayerData playerData;

	public ColorGUI(CubeRunner plugin) {
		this.playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) event.getWhoClicked();
		Language local = playerData.getLanguageOfPlayer(player);

		if (!ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase(ChatColor
				.stripColor(ChatColor.translateAlternateColorCodes('&', local.guiColorName + " &0: &5CubeRunner"))))
			return;

		if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN)
			return;
		
		event.setCancelled(true);

		ItemStack item = event.getCurrentItem();

		if (item.getType() != Material.STAINED_CLAY && item.getType() != Material.WOOL)
			return;

		Arena arena = Arena
				.getArena(ChatColor.stripColor(event.getInventory().getItem(0).getItemMeta().getLore().get(0)));
		
		if (arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
			player.closeInventory();
			local.sendMsg(player, local.guiColorEditWhileActive);
			return;
		}
		
		int valueOfItem = item.getDurability();
		if (item.getType() == Material.STAINED_CLAY)
			valueOfItem += 16;

		if (item.getItemMeta().hasEnchants())
			arena.getColorManager()
					.setColorIndice(arena.getColorManager().getColorIndice() - (int) Math.pow(2, valueOfItem));
		else
			arena.getColorManager()
					.setColorIndice(arena.getColorManager().getColorIndice() + (int) Math.pow(2, valueOfItem));

		arena.resetArena(item);
		openColorGUI(player, arena);
	}

	public void openColorGUI(Player player, Arena arena) {
		Language local = playerData.getLanguageOfPlayer(player);

		Inventory inv = Bukkit.createInventory(null, 54,
				ChatColor.translateAlternateColorCodes('&', local.guiColorName + " &0: &5CubeRunner"));
		ItemStackManager icon;
		/***************************************************
		 * Instructions
		 ***************************************************/

		icon = new ItemStackManager(Material.BOOKSHELF, 4);
		icon.setTitle(local.guiInstrictions);
		for (String loreLine : local.guiColorInfo.split("\n"))
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
			case 18:
			case 27:
			case 36:
			case 45:
				icon.setPosition(i);
				icon.addToInventory(inv);
			}

		/***************************************************
		 * Blocks
		 ***************************************************/

		List<ItemStackManager> colorManager = arena.getColorManager().getAllBlocks();
		for (int i = 0; i < 32; i++) {
			ItemStackManager item = colorManager.get(i);
			item.setPosition((int) ((Math.floor(i / 8.0) * 9) + 19 + (i % 8)));
			item.addToInventory(inv);
		}

		/***************************************************
		 * ArenaNAme
		 ***************************************************/

		icon = new ItemStackManager(Material.PAPER);
		icon.setTitle("&eArena:");
		icon.addToLore("&f" + arena.getName());

		icon.setPosition(0);
		icon.addToInventory(inv);
		icon.setPosition(8);
		icon.addToInventory(inv);

		/***************************************************
		 * Display
		 ***************************************************/

		player.openInventory(inv);
	}
}
