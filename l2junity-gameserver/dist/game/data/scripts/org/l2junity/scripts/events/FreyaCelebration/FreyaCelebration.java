/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.scripts.events.FreyaCelebration;

import org.l2junity.commons.util.ArrayUtil;
import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.itemcontainer.Inventory;
import org.l2junity.gameserver.model.quest.LongTimeEvent;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.variables.AccountVariables;
import org.l2junity.gameserver.network.client.send.CreatureSay;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.scripting.annotations.GameScript;

/**
 * Freya Celebration event AI.
 * @author Gnacik
 */
public final class FreyaCelebration extends LongTimeEvent
{
	// NPC
	private static final int FREYA = 13296;
	// Items
	private static final int FREYA_POTION = 15440;
	private static final int FREYA_GIFT = 17138;
	// Misc
	private static final int HOURS = 20;
	private static final String RESET_VAR = "FreyaEvent";
	
	private static final int[] SKILLS =
	{
		9150,
		9151,
		9152,
		9153,
		9154,
		9155,
		9156
	};
	
	private static final NpcStringId[] FREYA_TEXT =
	{
		NpcStringId.EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME,
		NpcStringId.I_JUST_DON_T_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME_ARE_HUMAN_S_EMOTIONS_LIKE_THIS_FEELING,
		NpcStringId.THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME,
		NpcStringId.BUT_I_KIND_OF_MISS_IT_LIKE_I_HAD_FELT_THIS_FEELING_BEFORE,
		NpcStringId.I_AM_ICE_QUEEN_FREYA_THIS_FEELING_AND_EMOTION_ARE_NOTHING_BUT_A_PART_OF_MALISS_A_MEMORIES
	};
	
	private FreyaCelebration()
	{
		addStartNpc(FREYA);
		addFirstTalkId(FREYA);
		addTalkId(FREYA);
		addSkillSeeId(FREYA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equalsIgnoreCase("give_potion"))
		{
			if (getQuestItemsCount(player, Inventory.ADENA_ID) > 1)
			{
				final AccountVariables vars = player.getAccountVariables();
				
				long _curr_time = System.currentTimeMillis();
				long _reuse_time = vars.getLong(RESET_VAR, 0);
				
				if (_curr_time > _reuse_time)
				{
					takeItems(player, Inventory.ADENA_ID, 1);
					giveItems(player, FREYA_POTION, 1);
					vars.set(RESET_VAR, System.currentTimeMillis() + (HOURS * 3600000));
				}
				else
				{
					long remainingTime = (_reuse_time - System.currentTimeMillis()) / 1000;
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) ((remainingTime % 3600) / 60);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_FOR_RE_USE_AFTER_S2_HOUR_S_S3_MINUTE_S);
					sm.addItemName(FREYA_POTION);
					sm.addInt(hours);
					sm.addInt(minutes);
					player.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NEED_S2_S1_S);
				sm.addItemName(Inventory.ADENA_ID);
				sm.addInt(1);
				player.sendPacket(sm);
			}
		}
		return null;
	}
	
	@Override
	public String onSkillSee(Npc npc, PlayerInstance caster, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		if ((caster == null) || (npc == null))
		{
			return null;
		}
		
		if ((npc.getId() == FREYA) && ArrayUtil.contains(targets, npc) && ArrayUtil.contains(SKILLS, skill.getId()))
		{
			if (getRandom(100) < 5)
			{
				CreatureSay cs = new CreatureSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getName(), NpcStringId.DEAR_S1_THINK_OF_THIS_AS_MY_APPRECIATION_FOR_THE_GIFT_TAKE_THIS_WITH_YOU_THERE_S_NOTHING_STRANGE_ABOUT_IT_IT_S_JUST_A_BIT_OF_MY_CAPRICIOUSNESS);
				cs.addStringParameter(caster.getName());
				
				npc.broadcastPacket(cs);
				
				caster.addItem("FreyaCelebration", FREYA_GIFT, 1, npc, true);
			}
			else
			{
				if (getRandom(10) < 2)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getName(), FREYA_TEXT[getRandom(FREYA_TEXT.length - 1)]));
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		return "13296.htm";
	}
	
	@GameScript
	public static void main()
	{
		new FreyaCelebration();
	}
}
