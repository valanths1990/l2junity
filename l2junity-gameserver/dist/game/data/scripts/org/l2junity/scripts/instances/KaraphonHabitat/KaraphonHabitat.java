/*
 * Copyright (C) 2004-2014 L2J DataPack
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
package org.l2junity.scripts.instances.KaraphonHabitat;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.scripting.annotations.GameScript;

import org.l2junity.scripts.instances.AbstractInstance;
import org.l2junity.scripts.quests.Q10745_TheSecretIngredients.Q10745_TheSecretIngredients;

/**
 * Karaphon Habitat instance.
 * @author Sdw
 */
public final class KaraphonHabitat extends AbstractInstance
{
	// NPCs
	private static final int DOLKIN = 33954;
	private static final int DOLKIN_INSTANCE = 34002;
	// Monsters
	private static final int KARAPHON = 23459;
	// Instance
	private static final int TEMPLATE_ID = 253;
	
	public KaraphonHabitat()
	{
		super(TEMPLATE_ID);
		addStartNpc(DOLKIN);
		addFirstTalkId(DOLKIN_INSTANCE);
		addTalkId(DOLKIN);
		addKillId(KARAPHON);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (player.hasQuestState(Q10745_TheSecretIngredients.class.getSimpleName()))
		{
			switch (event)
			{
				case "enter_instance":
					enterInstance(player, npc, TEMPLATE_ID);
					break;
				case "exit_instance":
					finishInstance(player, 0);
					break;
			}
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final Instance world = killer.getInstanceWorld();
		if (world != null)
		{
			world.setReenterTime();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@GameScript
	public static void main()
	{
		new KaraphonHabitat();
	}
}