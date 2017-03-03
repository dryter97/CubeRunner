package me.poutineqc.cuberunner.commands.inventories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.cuberunner.CRPlayer;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.commands.InventoryItem;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.game.GameState;
import me.poutineqc.cuberunner.utils.ItemBannerManager;
import me.poutineqc.cuberunner.utils.ItemBannerManager.CustomPattern;
import me.poutineqc.cuberunner.utils.ItemStackManager;
import me.poutineqc.cuberunner.utils.Utils;

public class CRInventoryJoin extends CRInventory {

	private int page;

	public CRInventoryJoin(CRPlayer crPlayer, int page) {
		super(crPlayer);

		Language local = crPlayer.getLanguage();
		this.title = local.get(Messages.JOIN_GUI_TITLE);
		this.page = page;
		this.amountOfRows = 6;
		createInventory();

		fillInventory();
	}

	@Override
	public void fillInventory() {

		List<String> arenas = new ArrayList<String>();
		for (Arena arena : Arena.getArenas())
			arenas.add(arena.getName());
		java.util.Collections.sort(arenas);

		Language local = crPlayer.getLanguage();
		InventoryItem icon;

		/***************************************************
		 * Instructions
		 ***************************************************/

		icon = new InventoryItem(new ItemStackManager(Material.BOOKSHELF), 4);
		icon.getItem().setDisplayName(local.get(Messages.KEYWORD_GUI_INSTRUCTIONS));
		for (String loreLine : local.get(Messages.JOIN_GUI_INFO).split("\n"))
			icon.getItem().addToLore(loreLine);
		icon.addToInventory(inventory);

		/***************************************************
		 * Glass Spacer
		 ***************************************************/

		icon = new InventoryItem(new ItemStackManager(Material.STAINED_GLASS_PANE));
		icon.getItem().setData((short) 10);
		icon.getItem().setDisplayName(ChatColor.RED + "");

		for (int i = 0; i < inventory.getSize(); i++)
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
				icon.addToInventory(inventory);
			}

		/***************************************************
		 * arenas
		 ***************************************************/

		icon = new InventoryItem(new ItemStackManager(Material.INK_SACK));
		int slot = 18;

		for (int i = ((page - 1) * 27); arenas.size() > i && slot < 45; i++) {
			Arena arena = Arena.getArena(arenas.get(i));
			icon.getItem().clearLore();

			if (arena.getGameState() == GameState.UNREADY) {
				icon.getItem().setData((short) 8);
				icon.getItem().setDisplayName(ChatColor.GOLD + arenas.get(i));
				icon.getItem().addToLore(local.get(Messages.KEYWORD_GAMESTATE_UNSET));

			} else if (arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
				icon.getItem().setData((short) 12);
				icon.getItem().setDisplayName(ChatColor.GOLD + arenas.get(i));
				icon.getItem().addToLore(local.get(Messages.KEYWORD_GAMESTATE_ACTIVE));

			} else {
				icon.getItem().setData((short) 10);
				icon.getItem().setDisplayName(ChatColor.GOLD + arenas.get(i));
				icon.getItem().addToLore(local.get(Messages.KEYWORD_GAMESTATE_READY));
				icon.getItem().addToLore(ChatColor.YELLOW + local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS) + " : "
						+ String.valueOf(arena.getAmountOfPlayerInGame()) + "/" + arena.getMaxPlayer());
			}

			icon.setPosition(slot++);
			icon.addToInventory(inventory);
		}

		/***************************************************
		 * NextPage
		 ***************************************************/

		if (arenas.size() - ((page - 1) * 27) > 27) {
			icon = new InventoryItem(new ItemBannerManager(CustomPattern.ARROW_RIGHT), 50);
			icon.getItem()
					.setDisplayName(local.get(Messages.KEYWORD_GUI_PAGE).replace("%number%", String.valueOf(page + 1)));
			icon.addToInventory(inventory);
		}

		/***************************************************
		 * Current
		 ***************************************************/

		icon = new InventoryItem(new ItemBannerManager(CustomPattern.ARROW_ACTUAL), 49);
		icon.getItem().setDisplayName(local.get(Messages.KEYWORD_GUI_PAGE).replace("%number%", String.valueOf(page)));
		icon.addToInventory(inventory);

		/***************************************************
		 * Previous Page
		 ***************************************************/

		if (page > 1) {
			icon = new InventoryItem(new ItemBannerManager(CustomPattern.ARROW_LEFT), 48);
			icon.getItem()
					.setDisplayName(local.get(Messages.KEYWORD_GUI_PAGE).replace("%number%", String.valueOf(page - 1)));
			icon.addToInventory(inventory);
		}

		/***********************************************
		 * Final Procedure
		 */

		openInventory();

	}

	@Override
	public void update(ItemStack itemStack, InventoryAction action) {

		Language local = crPlayer.getLanguage();
		String itemName = itemStack.getItemMeta().getDisplayName();

		if (Utils.isEqualOnColorStrip(itemName,
				local.get(Messages.KEYWORD_GUI_PAGE).replace("%number%", String.valueOf(page + 1)))) {
			new CRInventoryJoin(crPlayer, page + 1);
			return;
		}

		if (Utils.isEqualOnColorStrip(itemName,
				local.get(Messages.KEYWORD_GUI_PAGE).replace("%number%", String.valueOf(page - 1)))) {
			new CRInventoryJoin(crPlayer, page - 1);
			return;
		}

		Arena arena = Arena.getArena(ChatColor.stripColor(itemName));
		if (arena == null)
			return;

		if (action == InventoryAction.PICKUP_HALF) {
			arena.displayInformation(crPlayer.getPlayer());
		} else {
			arena.addPlayer(crPlayer.getPlayer(), true);
		}

		crPlayer.getPlayer().closeInventory();

	}

}
