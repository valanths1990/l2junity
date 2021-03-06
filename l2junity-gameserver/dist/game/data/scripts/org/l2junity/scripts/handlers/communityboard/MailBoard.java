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
package org.l2junity.scripts.handlers.communityboard;

import org.l2junity.gameserver.cache.HtmCache;
import org.l2junity.gameserver.handler.CommunityBoardHandler;
import org.l2junity.gameserver.handler.IWriteBoardHandler;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.scripting.annotations.GameScript;

/**
 * Mail board.
 * @author Zoey76
 */
public class MailBoard implements IWriteBoardHandler
{
	private static final String[] COMMANDS =
	{
		"_maillist"
	};
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		return COMMANDS;
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, PlayerInstance activeChar)
	{
		CommunityBoardHandler.getInstance().addBypass(activeChar, "Mail Command", command);
		
		final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/mail.html");
		CommunityBoardHandler.separateAndSend(html, activeChar);
		return true;
	}
	
	@Override
	public boolean writeCommunityBoardCommand(PlayerInstance activeChar, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		// TODO: Implement.
		return false;
	}
	
	@GameScript
	public static void main()
	{
		CommunityBoardHandler.getInstance().registerHandler(new MailBoard());
	}
}