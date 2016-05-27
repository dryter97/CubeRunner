package me.poutineqc.cuberunner;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.ConfigurationSection;

public enum CRStats {

	GAMES_PLAYED("games", "games", 0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getInt(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getInt(this.getNameFlatFile(), (int) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return (int) 0;
		}
	},
	TOTAL_SCORE("totalScore", "totalScore", 0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getInt(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getInt(this.getNameFlatFile(), (int) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return 0;
		}
	},
	KILLS("kills", "kills", 0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getInt(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getInt(this.getNameFlatFile(), (int) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return 0;
		}
	},
	MULTIPLAYER_WON("multiplayerWon", "multiplayerWon", 0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getInt(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getInt(this.getNameFlatFile(), (int) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return 0;
		}
	},
	TOTAL_DISTANCE("totalDistance", "totalDistance", 0.0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getDouble(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getDouble(this.getNameFlatFile(), (double) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return 0.0;
		}
	},
	AVERAGE_SCORE("averageDistancePerGame", "averageDistancePerGame", 0.0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getDouble(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getDouble(this.getNameFlatFile(), (double) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return 0.0;
		}
	},

	TIME_PLAYED("timePlayed", "timePlayed", 0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getInt(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getInt(this.getNameFlatFile(), (int) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return 0;
		}
	},
	MONEY("money", "money", 0.0) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getDouble(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getDouble(this.getNameFlatFile(), (double) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return 0.0;
		}
	},

	SURVIVE_5_MINUTES("survive5Minutes", "achievement.survive5Minutes", false) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getBoolean(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getBoolean(this.getNameFlatFile(), (boolean) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return false;
		}
	},
	REACH_HEIGHT_10("reachHeight10", "achievement.reachHeight10", false) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getBoolean(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getBoolean(this.getNameFlatFile(), (boolean) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return false;
		}
	},
	FILL_THE_ARENA("fillTheArena", "achievement.fillTheArena", false) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getBoolean(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getBoolean(this.getNameFlatFile(), (boolean) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return false;
		}
	},
	THE_ANSWER_TO_LIFE("theAnswerToLife", "achievement.theAnswerToLife", false) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getBoolean(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getBoolean(this.getNameFlatFile(), (boolean) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return false;
		}
	},
	THE_RAGE_QUIT("theRageQuit", "achievement.theRageQuit", false) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getBoolean(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getBoolean(this.getNameFlatFile(), (boolean) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return false;
		}
	},
	THE_KILLER_BUNNY("theKillerBunny", "achievement.theKillerBunny", false) {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getBoolean(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getBoolean(this.getNameFlatFile(), (boolean) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return false;
		}
	},
	NAME("name", "name", "default") {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return query.getString(this.getNameMySQL());
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return config.getString(this.getNameFlatFile(), (String) this.getDefaultValue());
		}

		@Override
		public Object getDefault() {
			return null;
		}
	},
	LANGUAGE("language", "language", "default") {
		@Override
		public Object getValue(ResultSet query) throws SQLException {
			return Language.getKeyLanguage(query.getString(this.getNameMySQL()));
		}

		@Override
		public Object getValue(ConfigurationSection config) {
			return Language.getKeyLanguage(config.getString(this.getNameFlatFile(), (String) this.getDefaultValue()));
		}

		@Override
		public Object getDefault() {
			return Language.getKeyLanguage("default");
		}
	};

	private final String nameMySQL;
	private final String nameFlatFile;
	private final Object defaultValue;

	public abstract Object getValue(ResultSet query) throws SQLException;

	public abstract Object getValue(ConfigurationSection config);

	public abstract Object getDefault();

	private CRStats(String nameMySQL, String nameFlatFile, Object defaultValue) {
		this.nameMySQL = nameMySQL;
		this.nameFlatFile = nameFlatFile;
		this.defaultValue = defaultValue;
	}

	public String getNameMySQL() {
		return nameMySQL;
	}

	public String getNameFlatFile() {
		return nameFlatFile;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
