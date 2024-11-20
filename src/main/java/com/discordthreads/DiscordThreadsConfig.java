package com.discordthreads;

import net.runelite.client.config.*;

@ConfigGroup("discordthreads")
public interface DiscordThreadsConfig extends Config {

	//===========================================================================================

	@ConfigSection(
			name = "Webhook",
			description = "Contains settings for the Discord Webhook",
			position = 1,
			closedByDefault = false
	)
	String webhook = "webhook";

	@ConfigItem(
			keyName = "webhook",
			name = "Webhook",
			description = "The Discord Webhook",
			section = webhook,
			position = 1
	)
	String webhook();

	@ConfigItem(
			keyName = "isThread",
			name = "Is Destination a Discord Thread?",
			description = "Please check this if the webhook is currently on a Discord Thread",
			section = webhook,
			position = 2
	)
	default boolean isThread() {
		return false;
	}

	@ConfigItem(
			keyName = "tags",
			name = "Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = webhook,
			position = 3
	)
	String tags();

	//===========================================================================================

	@ConfigSection(
			name = "Level-Up Notifications",
			description = "Contains settings for level-up notifications",
			position = 2,
			closedByDefault = true
	)
	String levelupNotifications = "levelupNotifications";

	@ConfigItem(
			keyName = "includeLevelUp",
			name = "Send Levelling Notifications",
			description = "Send messages when you level-up a skill.",
			section = levelupNotifications,
			position = 1
	)
	default boolean includeLevelUp() {
		return true;
	}

	@ConfigItem(
			keyName = "sendLevelUpScreenshot",
			name = "Include screenshot?",
			description = "Will include a screenshot with the level-up message if checked",
			section = levelupNotifications,
			position = 2
	)
	default boolean sendLevelUpScreenshot() {
		return true;
	}

	@ConfigItem(
			keyName = "levelupMessage",
			name = "Level-Up Message",
			description = "Message to send to Discord when you level-up",
			section = levelupNotifications,
			position = 3
	)
	default String levelMessage() { return "$name leveled $skill to $level"; }

	@ConfigItem(
			keyName = "minimumLevel",
			name = "Minimum level",
			description = "Levels under this will not be sent",
			section = levelupNotifications,
			position = 4
	)
	default int minimumLevel() {
		return 50;
	}

	@ConfigItem(
			keyName = "levelSkipping",
			name = "Send every X levels",
			description = "Will send a notification is the level is divisible by this number",
			section = levelupNotifications,
			position = 5
	)
	default int levelSkipping() {
		return 1;
	}

	@ConfigItem(
			keyName = "levelUpTags",
			name = "Level-Up Specific Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = levelupNotifications,
			position = 6
	)
	String levelUpTags();

	//===========================================================================================

	@ConfigSection(
			name = "Collection Log Notifications",
			description = "Contains settings for collection log notifications",
			position = 3,
			closedByDefault = true
	)
	String collectionLogsNotification = "collectionLogsNotification";

	@ConfigItem(
			keyName = "includeCollectionLog",
			name = "Send Collection Log Notifications",
			description = "Send messages when you get a Collection Log",
			section = collectionLogsNotification,
			position = 1
	)
	default boolean includeCollectionLog() {
		return true;
	}

	@ConfigItem(
			keyName = "sendCollectionLogScreenshot",
			name = "Include screenshot?",
			description = "Will include a screenshot with the collection log message if checked",
			section = collectionLogsNotification,
			position = 2
	)
	default boolean sendCollectionLogScreenshot() {
		return true;
	}

	@ConfigItem(
			keyName = "collectionLogMessage",
			name = "Collection Log Message",
			description = "Message to send to Discord when you get a new collection log",
			section = collectionLogsNotification,
			position = 3
	)
	default String collectionLogMessage() {
		return "$name just received a new collection log item: $entry";
	}

	@ConfigItem(
			keyName = "collectionLogTags",
			name = "Collection Log Specific Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = collectionLogsNotification,
			position = 4
	)
	String collectionLogTags();

	//===========================================================================================

	@ConfigSection(
			name = "Loot Notifications",
			description = "Contains settings for loot notifications",
			position = 4,
			closedByDefault = true
	)
	String lootNotification = "lootNotification";

	@ConfigItem(
			keyName = "includeLoot",
			name = "Send Loot Notifications",
			description = "Send messages when you get loot",
			section = lootNotification,
			position = 1
	)
	default boolean includeLoot() {
		return false;
	}

	@ConfigItem(
			keyName = "sendLootScreenshot",
			name = "Include screenshot?",
			description = "Will include a screenshot with the collection log message if checked",
			section = lootNotification,
			position = 2
	)
	default boolean sendLootScreenshot() {
		return true;
	}

	@ConfigItem(
			keyName = "minimumLootValue",
			name = "Minimum Loot value",
			description = "Minimum value of the loot to send notification",
			section = lootNotification,
			position = 3
	)
	default int minLootValue() {
		return 1000000;
	}

	@ConfigItem(
			keyName = "useHighAlch",
			name = "Use High Alch Value?",
			description = "If checked use High Alch value, if not GE value",
			section = lootNotification,
			position = 4
	)
	default boolean useHighAlch() {
		return false;
	}

	@ConfigItem(
			keyName = "lootMessage",
			name = "Loot Message",
			description = "Message to send to Discord when you get loot",
			section = lootNotification,
			position = 5
	)
	default String lootMessage() {
		return "$name just received $quantity $entry from $source valued at $price";
	}

	@ConfigItem(
			keyName = "lootTags",
			name = "Loot Specific Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = lootNotification,
			position = 6
	)
	String lootTags();

	//===========================================================================================

	@ConfigSection(
			name = "Combat Achievement Notifications",
			description = "The config for combat achievements",
			position = 5,
			closedByDefault = true
	)
	String combatAchievementNotification = "combatAchievementNotification";

	@ConfigItem(
			keyName = "includeCombatAchievements",
			name = "Send Combat Achievement Notifications",
			description = "Send messages when you get a combat achievement",
			section = combatAchievementNotification,
			position = 1
	)
	default boolean includeCombatAchievements() {
		return false;
	}

	@ConfigItem(
			keyName = "sendCombatAchievementScreenshot",
			name = "Include screenshot?",
			description = "Will include a screenshot with the combat achievement message if checked",
			section = combatAchievementNotification,
			position = 2
	)
	default boolean sendCombatAchievementsScreenshot() {
		return true;
	}

	@ConfigItem(
			keyName = "combatAchievementsMessage",
			name = "Combat Achievement Message",
			description = "Message to send to Discord when you get a combat achievement",
			section = combatAchievementNotification,
			position = 3
	)
	default String combatAchievementsMessage() {
		return "$name has just completed a combat achievement: $achievement";
	}

	@ConfigItem(
			keyName = "combatAchievementTags",
			name = "Combat Achievement Specific Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = combatAchievementNotification,
			position = 4
	)
	String combatAchievementTags();

	//===========================================================================================

	@ConfigSection(
			name = "Pet Notifications",
			description = "The config for pets",
			position = 6,
			closedByDefault = true
	)
	String petNotification = "petNotification";

	@ConfigItem(
			keyName = "includePets",
			name = "Send Pet Notifications",
			description = "Send messages when you get a pet",
			section = petNotification,
			position = 1
	)
	default boolean includePets() {
		return true;
	}

	@ConfigItem(
			keyName = "sendPetScreenshot",
			name = "Include screenshot?",
			description = "Will include a screenshot with the pet message if checked",
			section = petNotification,
			position = 2
	)
	default boolean sendPetScreenshot() {
		return true;
	}

	@ConfigItem(
			keyName = "petMessage",
			name = "Pet Message",
			description = "Message to send to Discord when you get a pet",
			section = petNotification,
			position = 3
	)
	default String petMessage() {
		return "$name has just received a pet!";
	}

	@ConfigItem(
			keyName = "petTags",
			name = "Pet Specific Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = petNotification,
			position = 4
	)
	String petTags();

	//===========================================================================================

	@ConfigSection(
			name = "Death Notifications",
			description = "The config for deaths",
			position = 7,
			closedByDefault = true
	)
	String deathNotification = "deathNotification";

	@ConfigItem(
			keyName = "includeDeaths",
			name = "Send Death Notifications",
			description = "Send messages when you die :(",
			section = deathNotification,
			position = 1
	)
	default boolean includeDeaths() {
		return false;
	}

	@ConfigItem(
			keyName = "sendDeathScreenshot",
			name = "Include screenshot?",
			description = "Will include a screenshot with the death message if checked",
			section = deathNotification,
			position = 2
	)
	default boolean sendDeathScreenshot() {
		return false;
	}

	@ConfigItem(
			keyName = "deathMessage",
			name = "Death Message",
			description = "Message to send to Discord when you die :(",
			section = deathNotification,
			position = 3
	)
	default String deathMessage() {
		return "$name has just died! :(";
	}

	@ConfigItem(
			keyName = "deathTags",
			name = "Death Specific Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = deathNotification,
			position = 4
	)
	String deathTags();

	//===========================================================================================

	@ConfigSection(
			name = "Manual Notifications",
			description = "The config for manual screenshots",
			position = 8,
			closedByDefault = true
	)
	String manualNotification = "manualNotification";

	@ConfigItem(
			keyName = "includeManual",
			name = "Send Manual Notifications",
			description = "Send messages when you enter a key",
			section = manualNotification,
			position = 1
	)
	default boolean includeManual() {
		return false;
	}

	@ConfigItem(
			keyName = "manualKeybind",
			name = "Screenshot Keybind",
			description = "Add keybind to manually take a screenshot and send a message of your rare drop",
			section = manualNotification,
			position = 2
	)
	default Keybind keybind() {
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "manualMessage",
			name = "Manual Message",
			description = "Message to send to Discord when you enter a key",
			section = manualNotification,
			position = 3
	)
	default String manualMessage() {
		return "$name has sent a screenshot";
	}

	@ConfigItem(
			keyName = "manualTags",
			name = "Manual Specific Tags",
			description = "Enter tag ID's e.g. 1111111111111,222222222222. To find the tag id inspect element discord in a browser, filter the tag in the thread and look at the payload: sort_order=desc&limit=25&tag=1111111111111&tag_setting",
			section = manualNotification,
			position = 4
	)
	String manualTags();

	//===========================================================================================
}