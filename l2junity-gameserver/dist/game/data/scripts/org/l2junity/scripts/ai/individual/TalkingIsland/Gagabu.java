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
package org.l2junity.scripts.ai.individual.TalkingIsland;

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;
import org.l2junity.gameserver.scripting.annotations.GameScript;

import org.l2junity.scripts.ai.AbstractNpcAI;

/**
 * Gagabu AI.
 * @author Gladicek
 */
public final class Gagabu extends AbstractNpcAI
{
	// NPCs
	private static final int GAGABU = 33284;
	// Misc
	private static final NpcStringId[] GAGABU_SHOUT =
	{
		NpcStringId.SPIRITSHOTS_ARE_MIXED,
		NpcStringId.WHEN_WILL_I_ORGANIZE_THIS_ALL
	};
	
	private Gagabu()
	{
		addSpawnId(GAGABU);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("SPAM_TEXT") && (npc != null))
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, GAGABU_SHOUT[getRandom(2)], 1000);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("SPAM_TEXT", 8000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	@GameScript
	public static void main()
	{
		new Gagabu();
	}
}