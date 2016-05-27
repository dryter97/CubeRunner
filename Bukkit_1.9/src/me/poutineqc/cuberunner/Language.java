package me.poutineqc.cuberunner;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.commands.CubeRunnerCommand;
import me.poutineqc.cuberunner.games.User;
import me.poutineqc.cuberunner.tools.CaseInsensitiveMap;

public class Language {

	private static CubeRunner plugin;
	private static Configuration config;

	private static File langFolder;

	private File languageFile;
	private FileConfiguration languageData;

	private static HashMap<String, Language> languages = new HashMap<>();
	private CaseInsensitiveMap commandDescriptions;

	public String languageName;
	public String prefixShort;
	public String prefixLong;
	public String developper;
	public String version;
	public String description;

	public String errorPermission;
	public String errorCommand;
	public String errorTeleport;
	public String helpNoPermission;
	public String toolInfoMissingName;
	public String toolInfoTip;

	public String playerAlreadyInGame;
	public String playerNotInGame;
	public String playerJoinUnready;
	public String playerJoinActive;
	public String playerJoinSpectator;
	public String playerJoinSuccess;
	public String playerJoinOthers;
	public String playerQuitSuccess;
	public String playerQuitOthers;

	public String gameNotReady;
	public String gameStartLessMin;
	public String gameStartLessMax;
	public String gameCountdownStarted;
	public String gameCountdownStopped;
	public String gameCrushedPlayer;
	public String gameCrushedOthers;

	public String endingTeleport;
	public String endingBest;

	public String missingArenaName;
	public String arenaCreated;
	public String arenaAlreadyExist;
	public String arenaNotExist;
	public String arenaNotFound;
	public String reloadSuccess;
	public String missingEditArgument;
	public String missingWorldEditRegion;
	public String arenaSetArena;
	public String arenaSetStartPoint;
	public String arenaSetLobby;
	public String arenaAmountPlayerMissingArgument;
	public String arenaAmountPlayerInvalidArgument;
	public String arenaAmountPlayerNotANumber;
	public String arenaAmountPlayerMinEqualZero;
	public String arenaAmountPlayerMinBiggerMax;
	public String arenaAmountPlayerMaxBiggerMin;
	public String arenaAmountPlayerSuccess;
	public String arenaDeleted;

	public String guiInstrictions;
	public String guiNextPage;
	public String guiPreviousPage;

	public String guiJoinName;
	public String guiJoinInfo;
	public String guiJoinStarted;
	public String guiJoinReady;
	public String guiStatsName;
	public String guiStatsAverageDistancePerGame;
	public String guiStatsTotalDistance;
	public String guiStatsGamePlayed;
	public String guiStatsTotalPoints;
	public String guiStatsKills;
	public String guiStatsMultiplayer;
	public String guiStatsTimePlayed;
	public String guiStatsMoney;
	public String guiChallengeName;
	public String guiColorName;
	public String guiColorInfo;

	public String keyWordHelp;
	public String keyWordCategory;
	public String keyWordPage;
	public String keyWordHours;
	public String keyWordMinutes;
	public String keyWordSeconds;
	public String keyWordDistance;
	public String keyWordInformation;
	public String keyWordAdvanced;
	public String keyWordMinimum;
	public String keyWordMaximum;
	public String keyWordCurrent;
	public String keyWordAmountPlayer;
	public String keyWordGameState;
	public String keyWordUnset;
	public String keyWordReady;
	public String keyWordStartup;
	public String keyWordActive;
	public String keyWordWorld;
	public String keyWordLobby;
	public String keyWordStartPoint;
	public String keyWordZone;
	public String keyWordTop10;
	public String keyWordBestScore;
	public String keyWordBy;
	public String keyWordProgression;
	public String keyWordCompleted;
	public String keyWordNotCompleted;
	public String keyWordReward;

	public String helpDescriptionAll;
	public String helpDescriptionGeneral;
	public String helpDescriptionGame;
	public String helpDescriptionArena;
	public String helpDescriptionAdmin;
	
	public String signNoPermission0;
	public String signNoPermission1;
	public String signNoPermission2;
	public String signNoPermission3;
	public String signNotValid1;
	public String signNotValid2;
	public String signNotValid3;
	public String signStartGame;
	public String signOpenStats;
	public String signQuit;
	public String signJoin;
	public String signPlay;
	
	public String achievementCongrats;
	public String achievementAmountGame;
	public String achievementTotalScore;
	public String achievementKills;
	public String achievementMultiplayerWon;
	public String achievementSurvive5Minutes;
	public String achievementReachHeight10;
	public String achievementFillTheArena;
	public String achievementTheAnswerToLife;
	public String achievementTheRageQuit;
	public String achievementTheKillerBunny;
	public String achievementMoneyReward;
	public String achievementMoneyGame;
	
	public String languageList;
	public String languageNotFound;
	public String languageChangeSuccess;

	public Language(CubeRunner plugin) {
		Language.plugin = plugin;
		config = plugin.getConfiguration();

		langFolder = new File(plugin.getDataFolder(), "LanguageFiles");
		if (!langFolder.exists())
			langFolder.mkdir();
	}

	public Language(String fileName, boolean forceFileOverwrite) {
		languageFile = new File(langFolder.getPath(), fileName + ".yml");
		if (forceFileOverwrite) {
			languageFile.delete();
			plugin.saveResource("LanguageFiles/" + fileName + ".yml", false);
		}

		if (!languageFile.exists()) {
			InputStream local = plugin.getResource("LanguageFiles/" + fileName + ".yml");
			if (local != null) {
				plugin.saveResource("LanguageFiles/" + fileName + ".yml", false);
			} else
				plugin.getLogger().info("Could not find " + fileName + ".yml");
		}

		languages.put(fileName, this);
		loadLanguage();
	}

	private void loadLanguage() {
		languageData = YamlConfiguration.loadConfiguration(languageFile);

		languageName = languageData.getString("languageName");
		prefixShort = languageData.getString("prefixShort", "&7[&5CR&7] ");
		prefixLong = languageData.getString("prefixLong", "&8[&5CubeRunner&8] ");
		developper = languageData.getString("developper", "&5Developped by: &7%developper%");
		version = languageData.getString("version", "&5Version: &7%version%");
		description = languageData.getString("description", "&dType &5/%command% help &d for the list of commands.");

		errorPermission = languageData.getString("missingPermission", "&4You don't have the permission to do this");
		errorCommand = languageData.getString("commandError", "&cCommand or arena not found.  Type &8/%cmd% help &cfor help.");
		errorTeleport = languageData.getString("errorTeleport", "&cYou can't teleport away while you are in game. Do &7/cr quit &cto leave it.");
		helpNoPermission = languageData.getString("helpNoPermission", "&cYou do not have any permissions in this category.");
		toolInfoMissingName = languageData.getString("toolInfoMissingName", "&cYou need to choose an arena.");
		toolInfoTip = languageData.getString("toolInfoTip", "&8[&7Tip&8] &7You may also do &8/%cmd% list &7and right click an arena to display it's information.");

		playerAlreadyInGame = languageData.getString("playerAlreadyInGame", "&cYou are already in a CubeRunner game.");
		playerNotInGame = languageData.getString("playerNotInGame", "&cYou are not currently in a CubeRunner game.");
		playerJoinUnready = languageData.getString("playerJoinUnready", "&cThe arena you are trying to join is not available.");
		playerJoinActive = languageData.getString("playerJoinActive", "&cThere is already an active game in the arena you are trying to join.");
		playerJoinSpectator = languageData.getString("playerJoinSpectator", "&dJoining as spectator...");
		playerJoinSuccess = languageData.getString("playerJoinSuccess", "&aYou have successfully joined the arena &2%arena%&a.");
		playerJoinOthers = languageData.getString("playerJoinOthers", "&f%player% &djoined your CubeRunner's arena.");
		playerQuitSuccess = languageData.getString("playerQuitSuccess", "&aYou have left the CubeRunner's arena &2%arena%&a.");
		playerQuitOthers = languageData.getString("playerQuitOthers", "&f%player% &dleft your CubeRunner's arena.");

		gameNotReady = languageData.getString("gameNotReady", "&cThe game is not ready to start at the moment");
		gameStartLessMin = languageData.getString("arenaStartLessMin", "&cThere must be at least %amount% players to start the game.");
		gameStartLessMax = languageData.getString("arenaStartLessMax", "&cThere must be at most %amount% players to start the game.");
		gameCountdownStarted = languageData.getString("gameCountdownStarted", "&dA new game of &5CubeRunner &dhas been initiated. You may join with the command &5/cr join %arena%&d.");
		gameCountdownStopped = languageData.getString("gameCountdownStopped", "&cThe countdown has been stopped. Not enough players in the game anymore.");
		gameCrushedPlayer = languageData.getString("gameCrushedPlayer", "&dYou have been crushed with a score of &5%score%&d. Game over!");
		gameCrushedOthers = languageData.getString("gameCrushedOthers", "&f%player% &dhas been crushed with a score of &5%score%&d.");
		endingTeleport = languageData.getString("gameEndingTeleport", "&dGame is over. Teleporting back in 5 seconds...");
		endingBest = languageData.getString("gameEndingBest", "&6Congratulation to &f%player% &6who got the new best score of &4%score% &6in the arena &5%arena%");

		missingArenaName = languageData.getString("missingArenaName", "&cYou must choose a name for your new arena.");
		arenaCreated = languageData.getString("arenaCreated", "&aAn arena named &2%arena% &ahas been successfully created.");
		arenaAlreadyExist = languageData.getString("arenaAlreadyExist", "&cAn arena named &4%arena% &calready exists.");
		arenaNotExist = languageData.getString("arenaNotExist", "&cAn arena named &4%arena% &cdoes not exists.");
		arenaNotFound = languageData.getString("arenaNotFound", "&cAn arena named &4%arena% &cwas not found.");
		reloadSuccess = languageData.getString("reloadSuccess", "&aPlugin CubeRunner has been successfully reloaded.");
		missingEditArgument = languageData.getString("missingEditArgument", "&cYou must choose what to do with this arena. Type &8/%cmd% help arena &cfor help.");
		missingWorldEditRegion = languageData.getString("missingWorldEditRegion", "&cYou must first choose a World Edit region.");
		arenaSetArena = languageData.getString("arenaSetArena", "&aZone successfully set for the arena &2%arena%&a.");
		arenaSetStartPoint = languageData.getString("arenaSetStartPoint", "&aStart Point successfully set for the arena &2%arena%&a.");
		arenaSetLobby = languageData.getString("arenaSetLobby", "&aLobby successfully set for the arena &2%arena%&a.");
		arenaAmountPlayerMissingArgument = languageData.getString("arenaAmountPlayerMissingArgument", "&cYou must provide a quantity to change the amount of players.");
		arenaAmountPlayerInvalidArgument = languageData.getString("arenaAmountPlayerInvalidArgument", "&cThe amount of player you entered is not valid. &7[&8%error%&7]");
		arenaAmountPlayerNotANumber = languageData.getString("arenaAmountPlayerNotANumber", "Not a number");
		arenaAmountPlayerMinEqualZero = languageData.getString("arenaAmountPlayerMinEqualZero", "Minimum lower than 1");
		arenaAmountPlayerMinBiggerMax = languageData.getString("arenaAmountPlayerMinBiggerMax", "Minimum higher than maximum");
		arenaAmountPlayerMaxBiggerMin = languageData.getString("arenaAmountPlayerMaxBiggerMin", "Maximum lower than minimum");
		arenaAmountPlayerSuccess = languageData.getString("arenaAmountPlayerSuccess", "&aAmount of player successfully edited for the arena &2%arena%&a.");
		arenaDeleted = languageData.getString("arenaDeleted", "&cArena deleted.");
		
		guiInstrictions = languageData.getString("guiInstrictions", "&6Instructions");
		guiNextPage = languageData.getString("guiNextPage", "&2Next Page");
		guiPreviousPage = languageData.getString("guiPreviousPage", "&2Previous Page");
		guiJoinName = languageData.getString("guiJoinName", "&2Arena List");
		guiJoinInfo = languageData.getString("guiJoinInfo", "&eClick the name of the arena you wish to go to.");
		guiJoinStarted = languageData.getString("guiJoinStarted", "&cActive : &eClick to Spectate");
		guiJoinReady = languageData.getString("guiJoinReady", "&aReady : &eClick to Join");
		guiStatsName = languageData.getString("guiStatsName", "&6Stats");
		guiStatsAverageDistancePerGame = languageData.getString("guiStatsAverageDistancePerGame", "&bAverage Score Per Game");
		guiStatsTotalDistance = languageData.getString("guiStatsTotalDistance", "&bTotal Distance Ran");
		guiStatsGamePlayed = languageData.getString("guiStatsGamePlayed", "&bGames played");
		guiStatsTotalPoints = languageData.getString("guiStatsTotalPoints", "&bTotal Score");
		guiStatsKills = languageData.getString("guiStatsKills", "&bPlayers Killed");
		guiStatsMultiplayer = languageData.getString("guiStatsMultiplayer", "&bMultiplayer games won");
		guiStatsTimePlayed = languageData.getString("guiStatsTimePlayed", "&dTime Played");
		guiStatsMoney = languageData.getString("guiStatsMoney", "&dMoney Gained");
		guiChallengeName = languageData.getString("guiChallengeName", "&bChallenges");
		guiColorName = languageData.getString("guiColorName", "&4Color Picker");
		guiColorInfo = languageData.getString("guiColorInfo", "&eThe enchanted blocks are\n&ethe curently selected ones.\n&eClick a block to\n&eenable or disable it.");

		commandDescriptions = new CaseInsensitiveMap();
		for (CubeRunnerCommand cmd : CubeRunnerCommand.getCommands()) {
			commandDescriptions.put(cmd.getDescription(),
					languageData.getString(cmd.getDescription(), "&cOops, an Error has occured!"));
		}

		keyWordHelp = languageData.getString("keyWordHelp", "Help");
		keyWordCategory = languageData.getString("keyWordCategory", "Category");
		keyWordPage = languageData.getString("keyWordPage", "Page");
		keyWordHours = languageData.getString("keyWordHours", "hours");
		keyWordMinutes = languageData.getString("keyWordMinutes", "minutes");
		keyWordSeconds = languageData.getString("keyWordSeconds", "seconds");
		keyWordDistance = languageData.getString("keyWordDistance", "km");
		keyWordAdvanced = languageData.getString("keyWordAdvanced", "Advanced Information");
		keyWordInformation = languageData.getString("keyWordInformation", "Information");
		keyWordMinimum = languageData.getString("keyWordMinimum", "Minimum");
		keyWordMaximum = languageData.getString("keyWordMaximum", "Maximum");
		keyWordCurrent = languageData.getString("keyWordCurrent", "Current");
		keyWordAmountPlayer = languageData.getString("keyWordAmountPlayer", "amount of players");
		keyWordGameState = languageData.getString("keyWordGameState", "game state");
		keyWordUnset = languageData.getString("keyWordUnset", "&7Arena Unset");
		keyWordReady = languageData.getString("keyWordReady", "&aReady");
		keyWordStartup = languageData.getString("keyWordStartup", "&9Startup");
		keyWordActive = languageData.getString("keyWordActive", "&cActive");
		keyWordWorld = languageData.getString("keyWordWorld", "World");
		keyWordLobby = languageData.getString("keyWordLobby", "Lobby");
		keyWordStartPoint = languageData.getString("keyWordStartPoint", "Start point");
		keyWordZone = languageData.getString("keyWordZoneMinPoint", " zone coordinate");
		keyWordTop10 = languageData.getString("top10", "Top 10");
		keyWordBestScore = languageData.getString("keyWordBestScore", "Best Score");
		keyWordBy = languageData.getString("keyWordBy", "by");
		keyWordProgression = languageData.getString("keyWordProgression", "Progression");
		keyWordCompleted = languageData.getString("keyWordCompleted", "Completed");
		keyWordNotCompleted = languageData.getString("keyWordNotCompleted", "Not Completed");
		keyWordReward = languageData.getString("keyWordReward", "Reward");

		helpDescriptionAll = languageData.getString("helpDescriptionAll", "&7All Commands");
		helpDescriptionGeneral = languageData.getString("helpDescriptionGeneral", "&7General player commands");
		helpDescriptionGame = languageData.getString("helpDescriptionGame", "&7Commands to simply play the game");
		helpDescriptionArena = languageData.getString("helpDescriptionArena", "&7Commands to setup the arenas");
		helpDescriptionAdmin = languageData.getString("helpDescriptionAdmin", "&7Admin maintenance commands");

		signNoPermission0 = languageData.getString("signNoPermission0", "&cYou don't have");
		signNoPermission1 = languageData.getString("signNoPermission1", "&cthe permissions");
		signNoPermission2 = languageData.getString("signNoPermission2", "&cto create a DaC");
		signNoPermission3 = languageData.getString("signNoPermission3", "&csign, &4Sorry...");
		signNotValid1 = languageData.getString("signNotValid1", "&cNone valid");
		signNotValid2 = languageData.getString("signNotValid2", "&csign parameters");
		signNotValid3 = languageData.getString("signNotValid3", "&cTry again");
		signJoin = languageData.getString("signJoin", "&aJoin Arena");
		signQuit = languageData.getString("signQuit", "&cQuit Arena");
		signStartGame = languageData.getString("signStartGame", "&9Start Game");
		signOpenStats = languageData.getString("signOpenStats", "&6Stats");
		signPlay = languageData.getString("signPlay", "&aPlay");
		
		achievementCongrats = languageData.getString("achievementCongrats", "&f%player% &6just completed the achievement &d%achievementName%&6!");
		achievementAmountGame = languageData.getString("achievementAmountGame", "&dPlay %amount% game(s)");
		achievementTotalScore = languageData.getString("achievementTotalScore", "&dReach total score of %amount%");
		achievementKills = languageData.getString("achievementKills", "&dKill %amount% player(s)");
		achievementMultiplayerWon = languageData.getString("achievementMultiplayerWon", "&dWin %amount% multiplayer game(s)");
		achievementSurvive5Minutes = languageData.getString("achievementSurvive5Minutes", "&dSurvive 5 minutes");
		achievementReachHeight10 = languageData.getString("reachHeight10", "&dReach height 10");
		achievementFillTheArena = languageData.getString("fillTheArena", "&dFill the arena's floor");
		achievementTheAnswerToLife = languageData.getString("theAnswerToLife", "&dThe answer to life the universe and everything");
		achievementTheRageQuit = languageData.getString("achievementTheRageQuit", "&dThe Rage Quit");
		achievementTheKillerBunny = languageData.getString("achievementTheKillerBunny", "&dThe Killer Bunny");
		achievementMoneyReward = languageData.getString("achievementMoneyReward", "&dYou gain %amount%%currency% for your achievement.");
		achievementMoneyGame = languageData.getString("achievementMoneyGame", "&dYou gain %amount%%currency% for surviving %amount2% seconds.");
		
		languageList = languageData.getString("languageList", "Available languages");
		languageNotFound = languageData.getString("languageNotFound", "&cLanguage not found. &8/%cmd% language &cfor a list of available languages");
		languageChangeSuccess = languageData.getString("languageChangeSuccess", "&aLanguage successfully set to %language%");
	}

	public CaseInsensitiveMap getCommandsDescription() {
		return commandDescriptions;
	}

	public void sendMsg(Player player, String message) {
		if (config.prefixInFrontOfEveryMessages)
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefixShort + message));
		else
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public void sendMsg(User user, String message) {
		sendMsg(user.getPlayer(), message);
	}

	public static HashMap<String, Language> getLanguages() {
		return languages;
	}

	public static void clearLanguages() {
		languages.clear();
	}
	
	public static Entry<String, Language> getLanguage(String languageName) {
		for (Entry<String, Language> local : languages.entrySet())
			if (local.getValue().languageName.equalsIgnoreCase(languageName))
				return local;
		
		return null;
	}
}
