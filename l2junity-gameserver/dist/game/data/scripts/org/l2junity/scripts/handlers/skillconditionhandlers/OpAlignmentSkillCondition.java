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

import org.l2junity.gameserver.enums.SkillConditionAffectType;
import org.l2junity.gameserver.enums.SkillConditionAlignment;
import org.l2junity.gameserver.handler.SkillConditionHandler;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.ISkillCondition;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.scripting.annotations.SkillScript;

/**
 * @author Sdw
 */
public class OpAlignmentSkillCondition implements ISkillCondition
{
	private final SkillConditionAffectType _affectType;
	private final SkillConditionAlignment _alignment;
	
	public OpAlignmentSkillCondition(StatsSet params)
	{
		_affectType = params.getEnum("affectType", SkillConditionAffectType.class);
		_alignment = params.getEnum("alignment", SkillConditionAlignment.class);
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		switch (_affectType)
		{
			case CASTER:
			{
				return _alignment.test(caster.getActingPlayer());
			}
			case TARGET:
			{
				if ((target != null) && target.isPlayer())
				{
					return _alignment.test(target.getActingPlayer());
				}
				break;
			}
		}
		return false;
	}

	@SkillScript
	public static void main()
	{
		SkillConditionHandler.getInstance().registerHandler("OpAlignment", OpAlignmentSkillCondition::new);
	}
}
