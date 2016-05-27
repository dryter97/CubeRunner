package me.poutineqc.cuberunner.commands.inventories;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.cuberunner.CRPlayer;
import me.poutineqc.cuberunner.CRStats;
import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.AchievementManager.Challenge;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.commands.InventoryItem;
import me.poutineqc.cuberunner.utils.ItemStackManager;
import me.poutineqc.cuberunner.utils.Utils;

public class CRInventoryChallenge extends CRInventory {

	public CRInventoryChallenge(CRPlayer crPlayer) {
		super(crPlayer);

		Language local = crPlayer.getLanguage();
		this.title = local.get(Messages.STATS_CHALLENGES_TITLE);
		this.amountOfRows = 3;
		createInventory();

		try {
			fillInventory();
		} catch (PlayerStatsException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fillInventory() throws PlayerStatsException {
		Language local = crPlayer.getLanguage();
		InventoryItem icon;
		int location;

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
		 * Stats
		 ***************************************************/

		NumberFormat format2 = new DecimalFormat("#0.00");
		NumberFormat format3 = new DecimalFormat("#0.000");
		
		icon = new InventoryItem(new ItemStackManager(Material.PAPER), 4);
		icon.getItem().setDisplayName(
				ChatColor.translateAlternateColorCodes('&', local.get(Messages.STATS_GUI_TITLE) + " : CubeRunner"));

		icon.getItem().addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.getItem().addToLore(local.get(Messages.STATS_INFO_AVERAGE_SCORE) + " : &e"
				+ format2.format(crPlayer.getDouble(CRStats.AVERAGE_SCORE)));
		icon.getItem()
				.addToLore(local.get(Messages.STATS_INFO_DISTANCE_RAN) + " : &e"
						+ format3.format(crPlayer.getDouble(CRStats.TOTAL_DISTANCE) / 1000) + " " + ChatColor.GREEN
						+ local.get(Messages.KEYWORD_GENERAL_DISTANCE));
		icon.getItem().addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.getItem().addToLore(
				local.get(Messages.STATS_INFO_GAMES) + " : &e" + String.valueOf(crPlayer.getInt(CRStats.GAMES_PLAYED)));
		icon.getItem().addToLore(local.get(Messages.STATS_INFO_TOTAL_SCORE) + " : &e"
				+ String.valueOf(crPlayer.getInt(CRStats.TOTAL_SCORE)));
		icon.getItem().addToLore(
				local.get(Messages.STATS_INFO_KILLS) + " : &e" + String.valueOf(crPlayer.getInt(CRStats.KILLS)));
		icon.getItem().addToLore(local.get(Messages.STATS_INFO_MULTIPLAYER_WON) + " : &e"
				+ String.valueOf(crPlayer.getInt(CRStats.MULTIPLAYER_WON)));
		icon.getItem().addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.getItem().addToLore(ChatColor.LIGHT_PURPLE + Utils.strip(local.get(Messages.STATS_INFO_TIME_PLAYED)) + ": "
				+ getTimePLayed(local, crPlayer.getInt(CRStats.TIME_PLAYED)));
		if (CubeRunner.get().isEconomyEnabled())
			icon.getItem()
					.addToLore(ChatColor.LIGHT_PURPLE + Utils.strip(local.get(Messages.STATS_INFO_MONEY)) + ": &e"
							+ format2.format(crPlayer.getDouble(CRStats.MONEY)) + ChatColor.GREEN
							+ CubeRunner.get().getEconomy().currencyNamePlural());

		icon.addToInventory(inventory);

		/***************************************************
		 * Challenges
		 ***************************************************/

		location = 19;
		for (Entry<Challenge, Double> challenge : CubeRunner.get().getAchievementManager().getChallenges().entrySet()) {
			if (location == 22)
				location++;
			
			icon = new InventoryItem(new ItemStackManager(Material.INK_SACK));
			boolean done = crPlayer.hasChallenge(challenge.getKey().getCrStats());
			icon.getItem().setData(done ? (short) 10 : (short) 8);
			icon.getItem().setDisplayName(
					(done ? (ChatColor.GREEN + "") : (ChatColor.RED + "")) + Utils.strip(local.get(challenge.getKey().getMessage())));
			icon.getItem().addToLore(ChatColor.YELLOW + "----------------------------");
			icon.getItem()
					.addToLore(ChatColor.AQUA + Utils.strip(local.get(Messages.KEYWORD_STATS_PROGRESSION)) + ": "
							+ (done ? (ChatColor.GREEN + Utils.strip(local.get(Messages.KEYWORD_STATS_COMPLETED)))
									: (ChatColor.RED + Utils.strip(local.get(Messages.KEYWORD_STATS_NOT_COMPLETED)))));
			if (!done && CubeRunner.get().isEconomyEnabled() && CubeRunner.get().getConfiguration().achievementsRewards)
				icon.getItem().addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
								local.get(Messages.KEYWORD_STATS_REWARD) + ": "))
						+ ChatColor.YELLOW + String.valueOf(challenge.getValue())
						+ CubeRunner.get().getEconomy().currencyNamePlural());

			icon.setPosition(location++);
			icon.addToInventory(inventory);
		}

		/***************************************************
		 * Arrow
		 ***************************************************/

		icon = new InventoryItem(new ItemStackManager(Material.ARROW));

		icon.getItem().setDisplayName(local.get(Messages.STATS_GUI_TITLE));

		icon.setPosition(8);
		icon.addToInventory(inventory);

		/***************************************************
		 * Display
		 ***************************************************/

		openInventory();

	}

	private String getTimePLayed(Language local, int timePlayed) {
		long hours = 0;

		timePlayed /= 60000;
		while (timePlayed > 60) {
			timePlayed -= 60;
			hours++;
		}

		return ChatColor.YELLOW + String.valueOf(hours) + ChatColor.GREEN + " "
				+ Utils.strip(local.get(Messages.KEYWORD_GENERAL_HOURS)) + ChatColor.YELLOW + " "
				+ String.valueOf(timePlayed) + ChatColor.GREEN + " "
				+ Utils.strip(local.get(Messages.KEYWORD_GENERAL_MINUTES));
	}

	@Override
	public void update(ItemStack itemStack, InventoryAction action) {
		Language local = crPlayer.getLanguage();

		if (Utils.isEqualOnColorStrip(itemStack.getItemMeta().getDisplayName(), local.get(Messages.STATS_GUI_TITLE))) {
			new CRInventoryStats(crPlayer);
		}
	}

}
