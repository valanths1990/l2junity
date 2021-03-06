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
package org.l2junity.scripts.ai.individual.TalkingIsland.Hadel;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.scripting.annotations.GameScript;

import org.l2junity.scripts.ai.AbstractNpcAI;

/**
 * Hadel AI.
 * @author St3eT
 */
public final class Hadel extends AbstractNpcAI
{
	// NPC
	private static final int HADEL = 33344;
	// Locations
	private static final Location GIANTS = new Location(-114562, 227307, -2864);
	private static final Location HARNAK = new Location(-114700, 147909, -7720);
	
	private Hadel()
	{
		addStartNpc(HADEL);
		addTalkId(HADEL);
		addFirstTalkId(HADEL);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		
		switch (event)
		{
			case "33344.html":
			case "33344-01.html":
			{
				htmltext = event;
				break;
			}
			case "teleportToGiants":
			{
				player.teleToLocation(GIANTS);
				break;
			}
			case "teleportToHarnak":
			{
				if ((player.getLevel() < 85) || player.isAwakenedClass())
				{
					htmltext = "33344-noClass.html";
					break;
				}
				player.teleToLocation(HARNAK);
				break;
			}
		}
		return htmltext;
	}
	
	@GameScript
	public static void main()
	{
		new Hadel();
	}
}
