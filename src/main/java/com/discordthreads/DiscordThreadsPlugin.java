package com.discordthreads;

import com.google.common.collect.ImmutableList;
import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.UsernameChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;
import okhttp3.*;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
@PluginDescriptor(
	name = "DiscordThreads"
)
public class DiscordThreadsPlugin extends Plugin {

	private ArrayList<Skill> currentSkills;
	private ArrayList<Integer> lootArray;
	private boolean notificationStarted = false;

	private static final ImmutableList<String> PET_MESSAGES = ImmutableList.of("You have a funny feeling like you're being followed", "You feel something weird sneaking into your backpack", "You have a funny feeling like you would have been followed");

	@Inject
	private DrawManager drawManager;

	@Inject
	private Client client;

	@Inject
	private DiscordThreadsConfig config;

	@Inject
	private OkHttpClient okHttpClient;

    @Inject
	private ItemManager itemManager;

	@Inject
	private KeyManager keyManager;

	//==============================================================================================================================

	@Provides
	DiscordThreadsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(DiscordThreadsConfig.class);
	}

	@Override
	protected void startUp() throws Exception {
		currentSkills = new ArrayList<Skill>();
		lootArray = new ArrayList<Integer>();

		keyManager.registerKeyListener(hotkeyListener);
	}

	@Override
	protected void shutDown() throws Exception {
		keyManager.unregisterKeyListener(hotkeyListener);
	}

	@Subscribe
	public void onUsernameChanged(UsernameChanged usernameChanged) {
		resetState();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState().equals(GameState.LOGIN_SCREEN)) {
			resetState();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if(!lootArray.isEmpty()) {
			lootArray.clear();
		}
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived npcLootReceived) {
		if(config.includeLoot()) {
			for(ItemStack itemStack : npcLootReceived.getItems()) {
				ItemComposition itemComposition = itemManager.getItemComposition(itemStack.getId());
				int itemValue = itemComposition.getPrice();
				if(config.useHighAlch()) {
					itemValue = itemComposition.getHaPrice();
				}

				if((itemValue*itemStack.getQuantity()) >= config.minLootValue()) {
					if(!lootArray.contains(itemComposition.getId())) {
						sendLootMessage(npcLootReceived.getNpc().getName(), itemComposition, itemStack.getQuantity(), itemValue*itemStack.getQuantity());
						lootArray.add(itemComposition.getId());
					}
				}
			}
		}
	}

	@Subscribe
	public void onPlayerLootReceived(PlayerLootReceived playerLootReceived) {
		if(config.includeLoot()) {
			for(ItemStack itemStack : playerLootReceived.getItems()) {
				ItemComposition itemComposition = itemManager.getItemComposition(itemStack.getId());
				int itemValue = itemComposition.getPrice();
				if(config.useHighAlch()) {
					itemValue = itemComposition.getHaPrice();
				}

				if((itemValue*itemStack.getQuantity()) >= config.minLootValue()) {
					if(!lootArray.contains(itemComposition.getId())) {
						sendLootMessage(playerLootReceived.getPlayer().getName(), itemComposition, itemStack.getQuantity(), itemValue*itemStack.getQuantity());
						lootArray.add(itemComposition.getId());
					}
				}
			}
		}
	}

	@Subscribe
	public void onLootReceived(LootReceived lootReceived) {
		if(config.includeLoot()) {
			for(ItemStack itemStack : lootReceived.getItems()) {
				ItemComposition itemComposition = itemManager.getItemComposition(itemStack.getId());
				int itemValue = itemComposition.getPrice();
				if(config.useHighAlch()) {
					itemValue = itemComposition.getHaPrice();
				}

				if((itemValue*itemStack.getQuantity()) >= config.minLootValue()) {
					if(!lootArray.contains(itemComposition.getId())) {
						sendLootMessage(lootReceived.getName(), itemComposition, itemStack.getQuantity(), itemValue * itemStack.getQuantity());
						lootArray.add(itemComposition.getId());
					}
				}
			}
		}
	}

	@Subscribe
	public void onStatChanged(net.runelite.api.events.StatChanged statChanged) {
		if (config.includeLevelUp()) {
			String skillName = statChanged.getSkill().getName();
			Skill skill = getSkill(skillName);
			int level = statChanged.getLevel();
			Integer previousLevel = null;
			if (skill != null) {
				previousLevel = skill.getSkillLevel();
			}

			if (previousLevel != null && previousLevel == 0) {
				if (previousLevel != level) {
					skill.setSkillLevel(level);

					for (int levelIterator = previousLevel + 1; levelIterator <= level; levelIterator++) {
						if (levelIterator >= config.minimumLevel() && (levelIterator % config.levelSkipping() == 0 || levelIterator == 99)) {
							sendLevelUpMessage(skill);
							return;
						}
					}
				}
			} else {
				currentSkills.add(new Skill(skillName, level));
			}
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired) {
		switch (scriptPreFired.getScriptId()) {
			case ScriptID.NOTIFICATION_START:
				notificationStarted = true;
				break;
			case ScriptID.NOTIFICATION_DELAY:
				if (!notificationStarted) {
					return;
				}

				String topText = client.getVarcStrValue(VarClientStr.NOTIFICATION_TOP_TEXT);
				String bottomText = client.getVarcStrValue(VarClientStr.NOTIFICATION_BOTTOM_TEXT);

				if (topText.equalsIgnoreCase("Collection log") && config.includeCollectionLog()) {
					String entry = Text.removeTags(bottomText).substring("New item:".length());
					sendCollectionLogMessage(entry);
				}

				if (topText.equalsIgnoreCase("Combat Task Completed!") && config.includeCombatAchievements() && client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENTS_POPUP) == 0) {
					String[] s = bottomText.split("<.*?>");
					String task = s[1].replaceAll("[:?]", "");
					sendCombatAchievementMessage(task);
				}

				notificationStarted = false;
				break;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() == ChatMessageType.GAMEMESSAGE) {
			String chatMessage = event.getMessage();
			if (config.includePets() && PET_MESSAGES.stream().anyMatch(chatMessage::contains)) {
				sendPetMessage();
			}
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath) {
		if (config.includeDeaths()) {
			Actor actor = actorDeath.getActor();
			if (actor instanceof Player) {
				Player player = (Player) actor;
				if (player == client.getLocalPlayer()) {
					sendDeathMessage();
				}
			}
		}
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.keybind()) {
		@Override
		public void hotkeyPressed() {
			if(config.includeManual()) {
				sendManualMessage();
			}
		}
	};

	//==============================================================================================================================

	private void sendLevelUpMessage(Skill skill) {
		String localName = client.getLocalPlayer().getName();
		String levelUpString = config.levelMessage().replaceAll("\\$name", "**" + localName + "**").replaceAll("\\$skill", "**" + skill.getSkillName() + "**").replaceAll("\\$level", "**" + skill.getSkillLevel() + "**");
		String levelUpStringNoBold = levelUpString.replaceAll("\\*\\*", "");

		DiscordThreadsWebhookBody discordWebhookBody = new DiscordThreadsWebhookBody();

		if(config.isThread()) {
			discordWebhookBody.setContent(levelUpString);
			discordWebhookBody.setThreadName(levelUpStringNoBold);

			this.addTags(discordWebhookBody, config.tags());

			this.addTags(discordWebhookBody, config.levelUpTags());
		} else {
			discordWebhookBody.setContent(levelUpString);
		}

		sendWebhook(discordWebhookBody, config.sendLevelUpScreenshot());
	}

	private void sendCollectionLogMessage(String entry) {
		String localName = client.getLocalPlayer().getName();

		String collectionLogMessageString = config.collectionLogMessage().replaceAll("\\$name", "**" + localName + "**").replaceAll("\\$entry", "**" + entry + "**");
		String collectionLogMessageStringNotBold = collectionLogMessageString.replaceAll("\\*\\*", "");

		DiscordThreadsWebhookBody discordWebhookBody = new DiscordThreadsWebhookBody();

		if (config.isThread()) {
			discordWebhookBody.setContent(collectionLogMessageString);
			discordWebhookBody.setThreadName(collectionLogMessageStringNotBold);

			this.addTags(discordWebhookBody, config.tags());

			this.addTags(discordWebhookBody, config.collectionLogTags());
		} else {
			discordWebhookBody.setContent(collectionLogMessageString);
		}

		sendWebhook(discordWebhookBody, config.sendCollectionLogScreenshot());
	}

	private void sendLootMessage(String source, ItemComposition item, int quantity, int price) {
		String localName = client.getLocalPlayer().getName();

		String lootMessage = config.lootMessage();
		String itemName = item.getName();
		if(quantity == 1) {
			lootMessage = lootMessage.replaceAll(" \\$quantity", "");
		} else {
			itemName = itemName + "'s";
		}

		String lootMessageString = lootMessage.replaceAll("\\$name", "**" + localName + "**").replaceAll("\\$quantity", quantity + "").replaceAll("\\$entry", "**" + itemName + "**").replaceAll("\\$source", "**" + source + "**").replaceAll("\\$price", price + "");
		String lootMessageStringNotBold = lootMessageString.replaceAll("\\*\\*", "");

		DiscordThreadsWebhookBody discordWebhookBody = new DiscordThreadsWebhookBody();

		if (config.isThread()) {
			discordWebhookBody.setContent(lootMessageString);
			discordWebhookBody.setThreadName(lootMessageStringNotBold);

			this.addTags(discordWebhookBody, config.tags());

			this.addTags(discordWebhookBody, config.lootTags());
		} else {
			discordWebhookBody.setContent(lootMessageString);
		}

		sendWebhook(discordWebhookBody, config.sendLootScreenshot());
	}

	private void sendCombatAchievementMessage(String task) {
		String localName = client.getLocalPlayer().getName();

		String combatAchievementMessageString = config.combatAchievementsMessage().replaceAll("\\$name", "**" + localName + "**").replaceAll("\\$achievement", "**" + task + "**");
		String combatAchievementMessageStringNotBold = combatAchievementMessageString.replaceAll("\\*\\*", "");

		DiscordThreadsWebhookBody discordWebhookBody = new DiscordThreadsWebhookBody();

		if (config.isThread()) {
			discordWebhookBody.setContent(combatAchievementMessageString);
			discordWebhookBody.setThreadName(combatAchievementMessageStringNotBold);

			this.addTags(discordWebhookBody, config.tags());

			this.addTags(discordWebhookBody, config.combatAchievementTags());
		} else {
			discordWebhookBody.setContent(combatAchievementMessageString);
		}

		sendWebhook(discordWebhookBody, config.sendCombatAchievementsScreenshot());
	}

	private void sendPetMessage() {
		String localName = client.getLocalPlayer().getName();

		String petMessageString = config.petMessage().replaceAll("\\$name", "**" + localName + "**");
		String petMessageStringNotBold = petMessageString.replaceAll("\\*\\*", "");

		DiscordThreadsWebhookBody discordWebhookBody = new DiscordThreadsWebhookBody();

		if (config.isThread()) {
			discordWebhookBody.setContent(petMessageString);
			discordWebhookBody.setThreadName(petMessageStringNotBold);

			this.addTags(discordWebhookBody, config.tags());

			this.addTags(discordWebhookBody, config.petTags());
		} else {
			discordWebhookBody.setContent(petMessageString);
		}

		sendWebhook(discordWebhookBody, config.sendPetScreenshot());
	}

	private void sendDeathMessage() {
		String localName = client.getLocalPlayer().getName();

		String deathMessageString = config.deathMessage().replaceAll("\\$name", "**" + localName + "**");
		String deathMessageStringNotBold = deathMessageString.replaceAll("\\*\\*", "");

		DiscordThreadsWebhookBody discordWebhookBody = new DiscordThreadsWebhookBody();

		if (config.isThread()) {
			discordWebhookBody.setContent(deathMessageString);
			discordWebhookBody.setThreadName(deathMessageStringNotBold);

			this.addTags(discordWebhookBody, config.tags());

			this.addTags(discordWebhookBody, config.deathTags());
		} else {
			discordWebhookBody.setContent(deathMessageString);
		}

		sendWebhook(discordWebhookBody, config.sendDeathScreenshot());
	}

	private void sendManualMessage() {
		String localName = client.getLocalPlayer().getName();

		String manualMessageString = config.manualMessage().replaceAll("\\$name", "**" + localName + "**");
		String manualMessageStringNotBold = manualMessageString.replaceAll("\\*\\*", "");

		DiscordThreadsWebhookBody discordWebhookBody = new DiscordThreadsWebhookBody();

		if (config.isThread()) {
			discordWebhookBody.setContent(manualMessageString);
			discordWebhookBody.setThreadName(manualMessageStringNotBold);

			this.addTags(discordWebhookBody, config.tags());

			this.addTags(discordWebhookBody, config.manualTags());
		} else {
			discordWebhookBody.setContent(manualMessageString);
		}

		sendWebhook(discordWebhookBody, true);
	}

	//================================================================================================================================

	private void sendWebhook(DiscordThreadsWebhookBody discordWebhookBody, boolean sendScreenshot) {
		String webhook = config.webhook();
		if (webhook != null) {
			List<String> webhookUrls = Arrays.asList(webhook.split("\n")).stream().filter(u -> u.length() > 0).map(u -> u.trim()).collect(Collectors.toList());

			for (String webhookUrl : webhookUrls) {
				HttpUrl url = HttpUrl.parse(webhookUrl);

				MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("payload_json", GSON.toJson(discordWebhookBody));

				if (sendScreenshot) {
					sendWebhookWithScreenshot(url, requestBodyBuilder);
				} else {
					sendWebhookRequest(url, requestBodyBuilder);
				}
			}
		}
	}

	private void sendWebhookWithScreenshot(HttpUrl url, MultipartBody.Builder requestBodyBuilder) {
		drawManager.requestNextFrameListener(image -> {
			BufferedImage bufferedImage = (BufferedImage) image;
			byte[] imageBytes;
			try {
				imageBytes = convertImageToByteArray(bufferedImage);
			} catch (IOException e) {
				log.warn("Error converting image", e);
				return;
			}

			requestBodyBuilder.addFormDataPart("file", "image.png", RequestBody.create(MediaType.parse("image/png"), imageBytes));

			sendWebhookRequest(url, requestBodyBuilder);
		});
	}

	private void sendWebhookRequest(HttpUrl url, MultipartBody.Builder requestBodyBuilder) {
		RequestBody requestBody = requestBodyBuilder.build();

		Request request = new Request.Builder().url(url).post(requestBody).build();
		sendRequest(request);
	}

	private void sendRequest(Request request) {
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				log.debug("Error submitting webhook", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				response.close();
			}
		});
	}

	private static byte[] convertImageToByteArray(BufferedImage bufferedImage) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	//==================================================================================================================

	private Skill getSkill(String skillName) {
		for (Skill skill : currentSkills) {
			if(skill.getSkillName() == skillName) {
				return skill;
			}
		}
		return null;
	}

	private void addTags(DiscordThreadsWebhookBody body, String tags) {
		if(tags != null) {
			String[] tagArraySpecific = tags.split(",");
			for (String tag : tagArraySpecific) {
				if(tag.length() > 1) {
					body.addAppliedTag(tag);
				}
			}
		}
	}

	private void resetState() {
		currentSkills.clear();
		lootArray.clear();
	}
}
