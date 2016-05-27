package me.poutineqc.cuberunner;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.game.User;
import me.poutineqc.cuberunner.utils.MinecraftConfiguration;

public class Language {
	private static HashMap<String, Language> languages;
	
	private HashMap<Messages, String> messages;
	private String langageName;
	private String fileName;

	static {
		languages = new HashMap<String, Language>();
	}
	
	public Language(CubeRunner plugin, String fileName) {
		this.fileName = fileName;
		MinecraftConfiguration languageFile = new MinecraftConfiguration("LanguageFiles", fileName, true);
		
		langageName = languageFile.get().getString("languageName", null);
		if (langageName == null)
			return;
		
		messages = new HashMap<Messages, String>();
		
		for (Messages message : Messages.values())
			messages.put(message, languageFile.get().getString(message.key, message.defaultMessage));

		languages.put(fileName, this);
	}

	public String getFileName() {
		return fileName;
	}

	public String getLanguageName() {
		return langageName;
	}

	public String get(Messages message) {
		return messages.get(message);
	}

	public void sendMsg(Player player, String message) {
		if (CubeRunner.get().getConfiguration().prefixInFrontOfEveryMessages)
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', get(Messages.PREFIX_SHORT) + " " + message));
		else
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public void sendMsg(User user, String message) {
		sendMsg(user.getPlayer(), message);
	}

	public static Language getKeyLanguage(String fileName) {
		for (Entry<String, Language> local : languages.entrySet()) {
			if (local.getKey().equalsIgnoreCase(fileName))
				return local.getValue();
		}
		return getDefault();
	}

	public static Language getLanguage(String languageName) {
		for (Entry<String, Language> local : languages.entrySet())
			if (local.getValue().langageName.equalsIgnoreCase(languageName))
				return local.getValue();

		return null;
	}

	public static Language getDefault() {
		for (Entry<String, Language> local : languages.entrySet()) {
			if (local.getKey().equalsIgnoreCase(CubeRunner.get().getConfiguration().language)) {
				return local.getValue();
			}
		}
		
		return languages.get("en");
	}

	public static HashMap<String, Language> getLanguages() {
		return languages;
	}

	public static void clear() {
		languages.clear();
	}

	public enum Messages {
		PREFIX_SHORT("prefixShort", "&7[&5CR&7]"),
		PREFIX_LONG("prefixLong", "&8[&5CubeRunner&8]"),
		
		DEVELOPPER("developper", "&5Developped by: &7%developper%"),
		VERSION("version", "&5Version: &7%version%"),
		DESCRIPTION("description", "&dType &5/%command% help &d for the list of commands."),
		
		ERROR_PERMISSION("errorPermission", "&4You don't have the permission to do this"),
		ERROR_COMMAND("errorCommand", "&cCommand not found. Type &8/%cmd% help &cfor help."),
		ERROR_TELEPORT("errorTeleport", "&cYou can't teleport away while you are in game. Do &7/cr quit &cto leave it."),
		ERROR_MISSING_ARENA("errorMissingArena", "&cYou need to choose an arena."),
		ERROR_ALREADY_IN_GAME("errorAlreadyInGame", "&cYou are already in a CubeRunner game."),
		ERROR_NOT_IN_GAME("errorNotInGame", "&cYou are not currently in a CubeRunner game."),
		
		INFO_TIP("infoTip", "&8[&7Tip&8] &7You may also do &8/%cmd% list &7and right click an arena to display it's information."),
		
		JOIN_UNREADY("joinUnready", "&cThe arena you are trying to join is not available."),
		JOIN_ACTIVE("joinActive", 	"&cThere is already an active game in the arena you are trying to join."),
		JOIN_SPECTATOR("joinSpectator", "&dJoining as spectator..."),
		JOIN_PLAYER("joinPlayer", "&aYou have successfully joined the arena &2%arena%&a."),
		JOIN_OTHERS("joinOthers", "&f%player% &djoined your CubeRunner's arena."),
		JOIN_GUI_TITLE("joinGuiTitle", "&2Arena List"),
		JOIN_GUI_INFO("joinGuiInfo", "&eClick the name of the arena you wish to go to."),
		JOIN_GUI_TIP_SPECTATE("joinGuiTipStarted", "&eClick to Spectate"),
		JOIN_GUI_TIP_JOIN("joinGuiTipJoin", "&aReady : &eClick to Join"),
		
		QUIT_PLAYER("quitPlayer", "&aYou have left the CubeRunner's arena &2%arena%&a."),
		QUIT_OTHERS("quitOthers", "&f%player% &dleft your CubeRunner's arena."),

		START_NOT_READY("startNotReady", "&cThe game is not ready to start at the moment"),
		START_MINIMUM("startMinimum", "&cThere must be at least %amount% players to start the game."),
		START_MAXIMUM("startMaximum", "&cThere must be at most %amount% players to start the game."),
		START_BROADCAST("startBroadcast", "&dA new game of &5CubeRunner &dhas been initiated. You may join with the command &5/cr join %arena%&d."),
		START_STOP("startStop", "&cThe countdown has been stopped. Not enough players in the game anymore."),
		
		END_CRUSH_PLAYER("endCrushPlayer", "&dYou have been crushed with a score of &5%score%&d. Game over!"),
		END_CRUSH_OTHERS("endCrushOthers", "&f%player% &dhas been crushed with a score of &5%score%&d."),
		END_HIDE_PLAYER("endHidePlayer", "&dYou have been kicked out of the game for hiding under a block."),
		END_HIDE_OTHERS("endHideOthers", "&f%player% &dhave been kicked out of the game for hiding under a block."),
		END_TELEPORT("endTeleport", "&dGame is over. Teleporting back in 5 seconds..."),
		END_BEST("endBest", "&6Congratulation to &f%player% &6who got the new best score of &4%score% &6in the arena &5%arena%"),
		END_REWARD("endReward", "&dYou gain %amount%%currency% for surviving %amount2% seconds."),
		END_BROADCAST_MULTIPLAYER("endBroadcastMultiplayer", "&6Congradulation to &f%player% &6who won a multiplayer game of CubeRunner in the arena &5%arena% &6with a score of &4%score%&6!"),
		END_BROADCAST_SINGLEPLAYER("endBroadcastSingleplayer", "&f%player% &6finished a game of CubeRunner in the arena &5%arena% &6with a score of &4%score%&6!"),

		STATS_GUI_TITLE("statsGuiTitle", "&6Stats"),
		STATS_INFO_AVERAGE_SCORE("statsInfoAverageScore", "&bAverage Score Per Game"),
		STATS_INFO_DISTANCE_RAN("statsInfoDistanceRan", "&bTotal Distance Ran"),
		STATS_INFO_GAMES("statsInfoGames", "&bGames played"),
		STATS_INFO_TOTAL_SCORE("statsInfoTotalScore", "&bTotal Score"),
		STATS_INFO_KILLS("statsInfoKills", "&bPlayers Killed"),
		STATS_INFO_MULTIPLAYER_WON("statsInfoMultiplayerWon", "&bMultiplayer games won"),
		STATS_INFO_TIME_PLAYED("statsInfoTimePlayed", "&dTime Played"),
		STATS_INFO_MONEY("statsInfoMoney", "&dMoney Gained"),
		STATS_CHALLENGES_TITLE("statsChallengesTitle", "&bChallenges"),

		ACHIEVEMENT_BROADCAST("achievementBroadcast", "&f%player% &6just completed the achievement &d%achievementName%&6!"),
		ACHIEVEMENT_REWARD("achievementReward", "&dYou gain %amount%%currency% for your achievement."),
		ACHIEVEMENT_GAMES("achievementAmountGame", "&dPlay %amount% game(s)"),
		ACHIEVEMENT_TOTAL_SCORE("achievementTotalScore", "&dReach total score of %amount%"),
		ACHIEVEMENT_KILLS("achievementKills", "&dKill %amount% player(s)"),
		ACHIEVEMENT_MULTIPLAYER_WON("achievementMultiplayerWon", "&dWin %amount% multiplayer game(s)"),
		ACHIEVEMENT_SURVIVE_5_MINUTES("achievementSurvive5Minutes", "&dSurvive 5 minutes"),
		ACHIEVEMENT_REACH_HEIGHT_10("achievementReachHeight10", "&dReach height 10"),
		ACHIEVEMENT_FILL_THE_ARENA("achievementFillTheArena", "&dFill the arena's floor"),
		ACHIEVEMENT_ANSWER_TO_LIFE("achievementAnswerToLife", "&dThe answer to life the universe and everything"),
		ACHIEVEMENT_RAGE_QUIT("achievementRageQuit", "&dThe Rage Quit"),
		ACHIEVEMENT_KILLER_BUNNY("achievementKillerBunny", "&dThe Killer Bunny"),
		
		EDIT_CREATE_NONAME("editCreateNoname", "&cYou must choose a name for your new arena."),
		EDIT_NEW_SUCCESS("editNewSuccess", "&aAn arena named &2%arena% &ahas been successfully created."),
		EDIT_NEW_EXISTS("editNewExists", "&cAn arena named &4%arena% &calready exists."),
		EDIT_NEW_LONG_NAME("editNewLongName", "&cTheArena's name can't be more than 12 letters long."),
		EDIT_ERROR_ARENA("editErrorArena", "&cAn arena named &4%arena% &cdoes not exists."),
		EDIT_REGION_WORLDEDIT("editRegionWorldedit", "&cYou must first choose a World Edit region."),
		EDIT_REGION("editRegion", "&aZone successfully set for the arena &2%arena%&a."),
		EDIT_STARTPOINT("editStartPoint", "&aStart Point successfully set for the arena &2%arena%&a."),
		EDIT_LOBBY("editLobby", "&aLobby successfully set for the arena &2%arena%&a."),
		EDIT_PLAYERS_MISSING("editPlayersMissing", "&cYou must provide a quantity to change the amount of players."),
		EDIT_PLAYERS_ERROR("editPlayersError", "&cThe amount of player you entered is not valid. &7[&8%error%&7]"),
		EDIT_PLAYERS_NAN("editPlayersNaN", "Not a number"),
		EDIT_PLAYERS_MINIMUM_1("editPlayersMinimum1", "Minimum lower than 1"),
		EDIT_PLAYERS_MINIMUM_MAXIMUM("editPlayersMinimumMaximum", "Minimum higher than maximum"),
		EDIT_PLAYERS_MAXIMUM_MINIMUM("editPlayersMaximumMinimum", "Maximum lower than minimum"),
		EDIT_PLAYERS_MAXIMUM_10("editPlayersMaximum10", "Maximum higher than 10"),
		EDIT_PLAYERS("editPlayers", "&aAmount of player successfully edited for the arena &2%arena%&a."),
		EDIT_COLOR_GUI_TITLE("editColorGuiTitle", "&4Color Picker"),
		EDIT_COLOR_GUI_INFO("editColorGuiInfo", "&eThe enchanted blocks are\n&ethe curently selected ones.\n&eClick a block to\n&eenable or disable it."),
		EDIT_COLOR_ERROR("editColorError", "&cYou can't edit the colors while a game is active."),
		EDIT_DELETE("editDelete", "&cArena deleted."),
		
		LANGUAGE_LIST("languageList", "Available languages"),
		LANGUAGE_NOT_FOUND("languageNotFound", "&cLanguage not found. &8/%cmd% language &cfor a list of available languages"),
		LANGUAGE_CHANGED("languageChanged", "&aLanguage successfully set to %language%"),

		HELP_ERROR_PERMISSION("helpErrorPermission", "&cYou do not have any permissions in this category."),
		HELP_DESCRIPTION_ALL("helpDescriptionAll", "&7All Commands"),
		HELP_DESCRIPTION_GENERALL("helpDescriptionGeneral", "&7General player commands"),
		HELP_DESCRIPTION_GAME("helpDescriptionGame", "&7Commands to simply play the game"),
		HELP_DESCRIPTION_ARENA("helpDescriptionArena", "&7Commands to setup the arenas"),
		HELP_DESCRIPTION_ADMIN("helpDescriptionAdmin", "&7Admin maintenance commands"),

		SIGN_PERM_0("signPerm0", "&cYou don't have"),
		SIGN_PERM_1("signPerm1", "&cthe permissions"),
		SIGN_PERM_2("signPerm2", "&cto create a DaC"),
		SIGN_PERM_3("signPerm3", "&csign, &4Sorry..."),
		SIGN_VALID_1("signValid1", "&cNone valid"),
		SIGN_VALID_2("signValid2", "&csign parameters"),
		SIGN_VALID_3("signValid3", "&cTry again"),
		
		KEYWORD_SIGN_JOIN("keyWordSignJoin", "&aJoin Arena"),
		KEYWORD_SIGN_QUIT("keyWordSignQuit", "&cQuit Arena"),
		KEYWORD_SIGN_START("keyWordSignStart", "&9Start Game"),
		KEYWORD_SIGN_STATS("keyWordSignStats", "&6Stats"),
		KEYWORD_SIGN_PLAY("keyWordSignPlay", "&aPlay"),
		KEYWORD_SIGN_TOP("keyWordSignTop", "&bBest Score"),
		
		KEYWORD_GUI_INSTRUCTIONS("keyWordGuiInstructions", "&6Instructions"),
		KEYWORD_GUI_PAGE("keyWordGuiPage", "&ePage %number%"),

		KEYWORD_GAMESTATE_UNSET("keyWordGameStateUnset", "&7Arena Unset"),
		KEYWORD_GAMESTATE_READY("keyWordGameStatReady", "&aReady"),
		KEYWORD_GAMESTATE_STARTUP("keyWordGameStatStartup", "&9Startup"),
		KEYWORD_GAMESTATE_ACTIVE("keyWordGameStatActive", "&cActive"),
		
		KEYWORD_GENERAL_HOURS("keyWordGeneralHours", "hours"),
		KEYWORD_GENERAL_MINUTES("keyWordGeneralMinutes", "minutes"),
		KEYWORD_GENERAL_SECONDS("keyWordGeneralSeconds", "seconds"),
		KEYWORD_GENERAL_DISTANCE("keyWordGeneralDistance", "km"),
		KEYWORD_GENERAL_BY("keyWordGeneralBy", "by"),

		KEYWORD_HELP("keyWordHelp", "Help"),
		KEYWORD_HELP_CATEGORY("keyWordHelpCategory", "Category"),
		KEYWORD_HELP_PAGE("keyWordHelpPage", "Page"),
		
		KEYWORD_INFO("keyWordInfo", "Information"),
		KEYWORD_INFO_ADVANCED("keyWordInfoAdvanced", "Advanced Information"),
		KEYWORD_INFO_MINIMUM("keyWordInfoMinimum", "Minimum"),
		KEYWORD_INFO_MAXIMUM("keyWordInfoMaximum", "Maximum"),
		KEYWORD_INFO_CURRENT("keyWordInfoCurrent", "Current"),
		KEYWORD_INFO_AMOUNT_OF_PLAYER("keyWordInfoAmountPlayer", "amount of players"),
		KEYWORD_INFO_GAME_STATE("keyWordInfoGameState", "game state"),
		KEYWORD_INFO_WORLD("keyWordInfoWorld", "World"),
		KEYWORD_INFO_LOBBY("keyWordInfoLobby", "Lobby"),
		KEYWORD_INFO_START_POINT("keyWordInfoStartPoint", "Start point"),
		KEYWORD_INFO_ZONE_COORDINATE("keyWordInfoZoneMinPoint", " zone coordinate"),
		KEYWORD_INFO_BEST_SCORE("keyWordInfoBestScore", "Best Score"),
		
		KEYWORD_STATS_TOP10("keyWordStatsTop10", "Top 10"),
		KEYWORD_STATS_PROGRESSION("keyWordStatsProgression", "Progression"),
		KEYWORD_STATS_COMPLETED("keyWordStatsCompleted", "Completed"),
		KEYWORD_STATS_NOT_COMPLETED("keyWordStatsNotCompleted", "Not Completed"),
		KEYWORD_STATS_REWARD("keyWordStatsReward", "Reward"),
		
		KEYWORD_SCOREBOARD_PLAYERS("keyWordScoreboardPlayers", "Players"),
		KEYWORD_SCOREBOARD_SCORE("keyWordScoreboardScore", "Score"),
		
		ADMIN_RELOAD("adminReload", "&aPlugin CubeRunner has been successfully reloaded."),
		
		COMMAND_HELP("cmdDescriptionHelp", "&7You already know how to do that don't you? ;)"),
		COMMAND_LANGUAGE("cmdDescriptionLanguage", "&7Change your own language for CubeRunner's plugin."),
		COMMAND_STATS("cmdDescriptionStats", "&7Opens your CubeRunner's stats."),
		COMMAND_LIST("cmdDescriptionList", "&7Opens a list of all the arenas."),
		COMMAND_INFO("cmdDescriptionInfo", "&7Display every information from the selected arena."),
		COMMAND_JOIN("cmdDescriptionJoin", "&7Join the specified game or open a GUI to choose."),
		COMMAND_QUIT("cmdDescriptionQuit", "&7Leave your current game."),
		COMMAND_START("cmdDescriptionStart", "&7Initiate the starting countdown."),
		COMMAND_NEW("cmdDescriptionNew", "&7Creates a new CubeRunner arena."),
		COMMAND_DELETE("cmdDescriptionDelete", "&7Delete the specified arena."),
		COMMAND_SETZONE("cmdDescriptionSetzone", "&7Set the delimitation of the selected arena with a World Edit region."),
		COMMAND_SETLOBBY("cmdDescriptionSetlobby", "&7Set the lobby of the selected arena."),
		COMMAND_SETSTARTPOINT("cmdDescriptionStartpoint", "&7Set the start point of the selected arena."),
		COMMAND_SETMINPLAYER("cmdDescriptionSetminplayer", "&7Set the minimum amount of player for an arena."),
		COMMAND_SETMAXPLAYER("cmdDescriptionSetmaxplayer", "&7Set the maximum amount of player for an arena."),
		COMMAND_SETCOLOR("cmdDescriptionSetcolor", "&7Set the block colors for the arena"),
		COMMAND_RELOAD("cmdDescriptionReload", "&7Reload the config, the player data and the language files.");
		
		private String key;
		private String defaultMessage;
		
		private Messages(String key, String defaultMessage) {
			this.key = key;
			this.defaultMessage = defaultMessage;
		}

		public String key() {
			return key;
		}
	}
}
