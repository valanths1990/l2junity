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
package org.l2junity.scripts.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import org.l2junity.gameserver.config.PlayerConfig;
import org.l2junity.gameserver.handler.AdminCommandHandler;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.stat.PcStat;
import org.l2junity.gameserver.scripting.annotations.GameScript;

/**
 * @author Psychokiller1888
 */
public class AdminVitality implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_set_vitality",
		"admin_full_vitality",
		"admin_empty_vitality",
		"admin_get_vitality"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!PlayerConfig.ENABLE_VITALITY)
		{
			activeChar.sendMessage("Vitality is not enabled on the server!");
			return false;
		}
		
		int vitality = 0;
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();
		
		if (activeChar.getTarget() instanceof PlayerInstance)
		{
			PlayerInstance target;
			target = (PlayerInstance) activeChar.getTarget();
			
			if (cmd.equals("admin_set_vitality"))
			{
				try
				{
					vitality = Integer.parseInt(st.nextToken());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Incorrect vitality");
				}
				
				target.setVitalityPoints(vitality, true);
				target.sendMessage("Admin set your Vitality points to " + vitality);
			}
			else if (cmd.equals("admin_full_vitality"))
			{
				target.setVitalityPoints(PcStat.MAX_VITALITY_POINTS, true);
				target.sendMessage("Admin completly recharged your Vitality");
			}
			else if (cmd.equals("admin_empty_vitality"))
			{
				target.setVitalityPoints(PcStat.MIN_VITALITY_POINTS, true);
				target.sendMessage("Admin completly emptied your Vitality");
			}
			else if (cmd.equals("admin_get_vitality"))
			{
				vitality = target.getVitalityPoints();
				activeChar.sendMessage("Player vitality points: " + vitality);
			}
			return true;
		}
		activeChar.sendMessage("Target not found or not a player");
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	@GameScript
	public static void main()
	{
		AdminCommandHandler.getInstance().registerHandler(new AdminVitality());
	}
}