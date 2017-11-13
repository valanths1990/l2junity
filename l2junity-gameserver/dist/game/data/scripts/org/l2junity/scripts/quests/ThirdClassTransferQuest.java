/*
 * Copyright (C) 2004-2016 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.scripts.quests;

import java.util.EnumMap;

import org.l2junity.commons.util.ArrayUtil;
import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.base.ClassId;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.ListenerRegisterType;
import org.l2junity.gameserver.model.events.annotations.RegisterEvent;
import org.l2junity.gameserver.model.events.annotations.RegisterType;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerLogin;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerPressTutorialMark;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.TutorialShowHtml;
import org.l2junity.gameserver.network.client.send.TutorialShowQuestionMark;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Abstract class for all Third Class Transfer quests.
 * @author St3eT, Gladicek
 */
public abstract class ThirdClassTransferQuest extends Quest
{
	// NPCs
	private static final int QUARTERMASTER = 33407;
	private static final int VANGUARD_MEMBER = 33165;
	private static final int[] VANGUARDS =
	{
		33166,
		33167,
		33168,
		33169,
	};
	// Items
	private static final int SOLDIER_TAG_HUMAN = 17748; // Vanguard Soldier's Dog Tags
	private static final int SOLDIER_TAG_ELF = 17749; // Vanguard Soldier's Dog Tags
	private static final int SOLDIER_TAG_DARK_ELF = 17750; // Vanguard Soldier's Dog Tags
	private static final int SOLDIER_TAG_ORC = 17751; // Vanguard Soldier's Dog Tags
	private static final int SOLDIER_TAG_DWARF = 17752; // Vanguard Soldier's Dog Tags
	private static final int SOLDIER_TAG_KAMAEL = 17753; // Vanguard Soldier's Dog Tags
	private static final int SOULSHOTS = 1467;
	private static final int SPIRITSHOTS = 3952;
	private static final int BLESSED_SCROLL_OF_RESURRECTION = 33518;
	private static final int PAULINA_S_EQUIPMENT_SET = 46852;
	// Skills
	private static final SkillHolder SHOW_SKILL = new SkillHolder(5103, 1);
	// Misc
	private final int _minLevel;
	private final Race _race;
	
	private static final EnumMap<Race, Integer> RACE_ITEMS = new EnumMap<>(Race.class);
	static
	{
		RACE_ITEMS.put(Race.HUMAN, SOLDIER_TAG_HUMAN);
		RACE_ITEMS.put(Race.ELF, SOLDIER_TAG_ELF);
		RACE_ITEMS.put(Race.DARK_ELF, SOLDIER_TAG_DARK_ELF);
		RACE_ITEMS.put(Race.ORC, SOLDIER_TAG_ORC);
		RACE_ITEMS.put(Race.DWARF, SOLDIER_TAG_DWARF);
		RACE_ITEMS.put(Race.KAMAEL, SOLDIER_TAG_KAMAEL);
	}
	
	public ThirdClassTransferQuest(int questId, int minLevel, Race race)
	{
		super(questId);
		addTalkId(QUARTERMASTER, VANGUARD_MEMBER);
		addTalkId(VANGUARDS);
		//@formatter:off
		registerQuestItems(SOLDIER_TAG_HUMAN, SOLDIER_TAG_ELF, SOLDIER_TAG_DARK_ELF, SOLDIER_TAG_ORC, SOLDIER_TAG_DWARF, SOLDIER_TAG_KAMAEL,
			17484, // Cry of Destiny - Gladiator
			17485, // Cry of Destiny - Warlord
			17486, // Cry of Destiny - Paladin
			17487, // Cry of Destiny - Dark Avanger
			17488, // Cry of Destiny - Treasure Hunter
			17489, // Cry of Destiny - Hawkeye
			17490, // Cry of Destiny - Sorcerer
			17491, // Cry of Destiny - Necromancer
			17492, // Cry of Destiny - Warlock
			17493, // Cry of Destiny - Bishop
			17494, // Cry of Destiny - Prophet
			17495, // Cry of Destiny - Temple Knight
			17496, // Cry of Destiny - Swordsinger
			17497, // Cry of Destiny - Plains Walker
			17498, // Cry of Destiny - Silver Ranger
			17499, // Cry of Destiny - Spellsinger
			17500, // Cry of Destiny - Elemental Summoner
			17501, // Cry of Destiny - Elder
			17502, // Cry of Destiny - Shillien Knight
			17503, // Cry of Destiny - Bladecancer
			17504, // Cry of Destiny - Abyss Walker
			17505, // Cry of Destiny - Phantom Ranger
			17506, // Cry of Destiny - Spellhower
			17507, // Cry of Destiny - Phantom Summoner
			17508, // Cry of Destiny - Shillen Elder
			17509, // Cry of Destiny - Destroyer
			17510, // Cry of Destiny - Tyrant
			17511, // Cry of Destiny - Overlord
			17512, // Cry of Destiny - Warcryer
			17513, // Cry of Destiny - Bounty Hunter
			17514, // Cry of Destiny - Warsmith
			17515, // Cry of Destiny - Berserker
			17516, // Cry of Destiny - Soulbreaker (male)
			17516, // Cry of Destiny - Soulbreaker (female)
			17517 // Cry of Destiny - Arbalester
		);
		//@formatter:on
		_minLevel = minLevel;
		_race = race;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33407-02.html":
			{
				if (st.isCond(1))
				{
					st.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "33407-05.html":
			{
				if (st.isCond(3))
				{
					st.setCond(4, true);
					st.unset("vanguard");
					takeItems(player, RACE_ITEMS.get(player.getRace()), 1);
					htmltext = event;
				}
				break;
			}
			case "33165-02.html":
			{
				if (st.isCond(4))
				{
					st.setCond(5, true);
					htmltext = event;
				}
				break;
			}
			case "collectTag":
			{
				if (st.isCond(2))
				{
					final int bit = 1 << (VANGUARDS[0] - npc.getId());
					final int vanguard = st.getInt("vanguard");
					
					if ((vanguard & bit) != bit)
					{
						st.set("vanguard", vanguard | bit);
						giveItems(player, RACE_ITEMS.get(player.getRace()), 1);
						
						if (getQuestItemsCount(player, RACE_ITEMS.get(player.getRace())) == 4)
						{
							st.setCond(3, true);
							htmltext = "vanguard-04.html";
						}
						else
						{
							htmltext = "vanguard-02.html";
						}
					}
					else
					{
						htmltext = "vanguard-03.html";
					}
				}
				break;
			}
			case "nextClassInfo":
			{
				if ((st.getInt("STARTED_CLASS") != player.getClassId().getId()) && (player.getLevel() >= _minLevel))
				{
					htmltext = npc.getId() + "-10.html";
					break;
				}
				
				final ClassId newClassId = player.getClassId().getNextClassIds().stream().findFirst().orElse(null);
				if (newClassId != null)
				{
					htmltext = "class_preview_" + newClassId.toString().toLowerCase() + ".html";
				}
				break;
			}
			case "classTransfer":
			{
				if ((st.getInt("STARTED_CLASS") != player.getClassId().getId()) && (player.getLevel() >= _minLevel))
				{
					htmltext = npc.getId() + "-10.html";
					break;
				}
				
				final ClassId newClassId = player.getClassId().getNextClassIds().stream().findFirst().orElse(null);
				if (newClassId != null)
				{
					final ClassId currentClassId = player.getClassId();
					
					if (!newClassId.childOf(currentClassId))
					{
						break;
					}
					
					addSkillCastDesire(npc, player, SHOW_SKILL.getSkill(), 23);
					player.sendPacket(SystemMessageId.CONGRATULATIONS_YOU_VE_COMPLETED_YOUR_THIRD_CLASS_TRANSFER_QUEST);
					player.broadcastSocialAction(3);
					player.setClassId(newClassId.getId());
					if (player.isSubClassActive())
					{
						player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
					}
					else
					{
						player.setBaseClass(player.getActiveClass());
					}
					player.broadcastUserInfo();
					player.sendSkillList();
					giveItems(player, SOULSHOTS, 8000);
					giveItems(player, SPIRITSHOTS, 8000);
					giveItems(player, BLESSED_SCROLL_OF_RESURRECTION, 3);
					giveItems(player, PAULINA_S_EQUIPMENT_SET, 1);
					addExp(player, 42_000_000);
					st.exitQuest(true, true);
					htmltext = npc.getId() + "-09.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player, boolean isSimulated)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		if (st.getState() == State.STARTED)
		{
			switch (npc.getId())
			{
				case QUARTERMASTER:
				{
					switch (st.getCond())
					{
						case 1:
						{
							htmltext = "33407-01.html";
							break;
						}
						case 2:
						{
							htmltext = "33407-03.html";
							break;
						}
						case 3:
						{
							htmltext = "33407-04.html";
							break;
						}
						case 4:
						case 5:
						case 6:
						case 7:
						case 8:
						case 9:
						case 10:
						case 11:
						case 12:
						{
							htmltext = "33407-05.html";
							break;
						}
					}
					break;
				}
				case VANGUARD_MEMBER:
				{
					switch (st.getCond())
					{
						case 4:
						case 5:
						case 6:
						case 7:
						case 8:
						case 9:
						case 10:
						case 11:
						case 12:
						{
							htmltext = "33165-01.html";
							break;
						}
					}
					break;
				}
				default:
				{
					if (st.isCond(2) && ArrayUtil.contains(VANGUARDS, npc.getId()))
					{
						htmltext = "vanguard-01.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerPressTutorialMark(OnPlayerPressTutorialMark event)
	{
		if (event.getQuestId() == getId())
		{
			final PlayerInstance player = event.getActiveChar();
			player.sendPacket(new TutorialShowHtml(getHtm(player.getHtmlPrefix(), "popupInvite.html")));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final PlayerInstance player = event.getActiveChar();
		final int oldLevel = event.getOldLevel();
		final int newLevel = event.getNewLevel();
		
		if ((oldLevel < newLevel) && (newLevel == _minLevel) && (player.getRace() == _race) && (player.isInCategory(CategoryType.THIRD_CLASS_GROUP)))
		{
			player.sendPacket(new TutorialShowQuestionMark(getId(), 1));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		final PlayerInstance player = event.getActiveChar();
		
		if ((player.getLevel() >= _minLevel) && (player.getRace() == _race) && (player.isInCategory(CategoryType.THIRD_CLASS_GROUP)))
		{
			final QuestState st = getQuestState(player, true);
			if (st.isCreated())
			{
				player.sendPacket(new TutorialShowQuestionMark(st));
			}
		}
	}
}