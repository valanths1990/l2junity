/*
 * Copyright (C) 2004-2015 L2J Unity
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
package org.l2junity.scripts.handlers.skillconditionhandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.l2junity.gameserver.handler.SkillConditionHandler;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.skills.ISkillCondition;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.scripting.annotations.SkillScript;

/**
 * @author UnAfraid
 */
public class OpNeedSummonOrPetSkillCondition implements ISkillCondition
{
	private final List<Integer> _npcIds = new ArrayList<>();
	
	public OpNeedSummonOrPetSkillCondition(StatsSet params)
	{
		final List<String> npcIds = params.getList("npcIds", String.class);
		if (npcIds != null)
		{
			npcIds.stream().map(Integer::valueOf).forEach(_npcIds::add);
		}
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		final Summon pet = caster.getPet();
		final Collection<Summon> summons = caster.getServitors().values();
		return ((pet != null) && _npcIds.stream().anyMatch(npcId -> npcId == pet.getId())) || summons.stream().anyMatch(npcId -> _npcIds.contains(npcId));
	}

	@SkillScript
	public static void main()
	{
		SkillConditionHandler.getInstance().registerHandler("OpNeedSummonOrPet", OpNeedSummonOrPetSkillCondition::new);
	}
}
