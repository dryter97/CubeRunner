package me.poutineqc.cuberunner.commands.inventories;

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
import me.poutineqc.cuberunner.utils.ItemStackManager;

public class CRInventoryColor extends CRInventory {

	private Arena arena;

	public CRInventoryColor(CRPlayer crPlayer, Arena arena) {
		super(crPlayer);

		this.arena = arena;
		Language local = crPlayer.getLanguage();
		this.title = local.get(Messages.EDIT_COLOR_GUI_TITLE);
		this.amountOfRows = 6;
		createInventory();

		fillInventory();
	}

	@Override
	public void fillInventory() {
		Language local = crPlayer.getLanguage();
		InventoryItem icon;
		
		/***************************************************
		 * Instructions
		 ***************************************************/

		icon = new InventoryItem(new ItemStackManager(Material.BOOKSHELF), 4);
		icon.getItem().setDisplayName(local.get(Messages.KEYWORD_GUI_INSTRUCTIONS));
		for (String loreLine : local.get(Messages.EDIT_COLOR_GUI_INFO).split("\n"))
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
			case 18:
			case 27:
			case 36:
			case 45:
				icon.setPosition(i);
				icon.addToInventory(inventory);
			}

		/***************************************************
		 * Blocks
		 ***************************************************/

		List<ItemStackManager> colorManager = arena.getColorManager().getAllBlocks();
		for (int i = 0; i < 32; i++) {
			icon = new InventoryItem(colorManager.get(i));
			icon.setPosition((int) ((Math.floor(i / 8.0) * 9) + 19 + (i % 8)));
			icon.addToInventory(inventory);
		}

		/***************************************************
		 * Display
		 ***************************************************/

		openInventory();
	}

	@Override
	public void update(ItemStack itemStack, InventoryAction action) {
		if (itemStack.getType() != Material.STAINED_CLAY && itemStack.getType() != Material.WOOL)
			return;
		
		if (arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
			crPlayer.getPlayer().closeInventory();
			crPlayer.setCurrentInventory(null);
			crPlayer.getLanguage().sendMsg(crPlayer.getPlayer(), crPlayer.getLanguage().get(Messages.EDIT_COLOR_ERROR));
			return;
		}
		
		int valueOfItem = itemStack.getDurability();
		if (itemStack.getType() == Material.STAINED_CLAY)
			valueOfItem += 16;

		if (itemStack.getItemMeta().hasEnchants()) {
			arena.getColorManager()
					.setColorIndice(arena.getColorManager().getColorIndice() - (int) Math.pow(2, valueOfItem));
		} else {
			arena.getColorManager()
					.setColorIndice(arena.getColorManager().getColorIndice() + (int) Math.pow(2, valueOfItem));
		}

		arena.resetArena(itemStack);
		new CRInventoryColor(crPlayer, arena);
	}

}
