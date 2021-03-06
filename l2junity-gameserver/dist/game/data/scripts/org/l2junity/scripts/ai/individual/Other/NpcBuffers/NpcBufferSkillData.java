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
package org.l2junity.scripts.ai.individual.Other.NpcBuffers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * @author UnAfraid
 */
public class NpcBufferSkillData
{
	private final SkillHolder _skill;
	private final int _scaleToLevel;
	private final int _initialDelay;
	
	public NpcBufferSkillData(StatsSet set)
	{
		_skill = new SkillHolder(set.getInt("id"), set.getInt("level"));
		_scaleToLevel = set.getInt("scaleToLevel", -1);
		_initialDelay = set.getInt("initialDelay", 0) * 1000;
	}
	
	public Skill getSkill()
	{
		return _skill.getSkill();
	}
	
	public int getScaleToLevel()
	{
		return _scaleToLevel;
	}
	
	public int getInitialDelay()
	{
		return _initialDelay;
	}
}
