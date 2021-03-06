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
package org.l2junity.scripts.handlers.targethandlers;

import org.l2junity.gameserver.handler.ITargetTypeHandler;
import org.l2junity.gameserver.handler.TargetHandler;
import org.l2junity.gameserver.instancemanager.MentorManager;
import org.l2junity.gameserver.model.Mentee;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.scripting.annotations.SkillScript;

/**
 * Target my mentor.
 * @author Nik
 */
public class MyMentor implements ITargetTypeHandler
{
	@Override
	public WorldObject getTarget(Creature activeChar, WorldObject selectedTarget, Skill skill, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		if (activeChar.isPlayer())
		{
			Mentee mentor = MentorManager.getInstance().getMentor(activeChar.getObjectId());
			if (mentor != null)
			{
				return mentor.getPlayerInstance();
			}
		}
		
		return null;
	}
	
	@SkillScript
	public static void main()
	{
		TargetHandler.getInstance().registerTargetTypeHandler("MY_MENTOR", new MyMentor());
	}
}