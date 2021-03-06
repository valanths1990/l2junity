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
package org.l2junity.scripts.handlers.effecthandlers.pump;

import org.l2junity.gameserver.handler.EffectHandler;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.BooleanStat;
import org.l2junity.gameserver.scripting.annotations.SkillScript;
import org.l2junity.scripts.handlers.effecthandlers.AbstractBooleanStatEffect;

/**
 * Untargetable effect implementation.
 * @author UnAfraid
 */
public final class PumpDisappearTarget extends AbstractBooleanStatEffect
{
	public PumpDisappearTarget(StatsSet params)
	{
		super(BooleanStat.UNTARGETABLE);
	}
	
	@Override
	public void pumpStart(Creature caster, Creature target, Skill skill)
	{
		// Remove target from those that have the untargetable creature on target.
		World.getInstance().forEachVisibleObject(target, Creature.class, c ->
		{
			if (c.getTarget() == target)
			{
				c.setTarget(null);
			}
		});
	}
	
	@SkillScript
	public static void main()
	{
		EffectHandler.getInstance().registerHandler("p_disappear_target", PumpDisappearTarget::new);
	}
}
