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
package org.l2junity.gameserver.network.client.recv;

import java.util.StringTokenizer;

import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.config.GeneralConfig;
import org.l2junity.gameserver.handler.AdminCommandHandler;
import org.l2junity.gameserver.handler.BypassHandler;
import org.l2junity.gameserver.handler.CommunityBoardHandler;
import org.l2junity.gameserver.handler.IBypassHandler;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.debugger.DebugType;
import org.l2junity.gameserver.model.entity.Hero;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.npc.OnNpcManorBypass;
import org.l2junity.gameserver.model.events.impl.character.npc.OnNpcMenuSelect;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerBypass;
import org.l2junity.gameserver.model.events.returns.TerminateReturn;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.Disconnection;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * RequestBypassToServer client packet implementation.
 * @author HorridoJoho
 */
public final class RequestBypassToServer implements IClientIncomingPacket
{
	// FIXME: This is for compatibility, will be changed when bypass functionality got an overhaul by NosBit
	private static final String[] _possibleNonHtmlCommands =
	{
		"_bbs",
		"bbs",
		"_mail",
		"_friend",
		"_match",
		"_diary",
		"_olympiad?command",
		"menu_select",
		"manor_menu_select",
		"_pcc_multisell"
	};
	
	// S
	private String _command;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_command = packet.readS();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (_command.isEmpty())
		{
			LOGGER.warn("Player " + activeChar.getName() + " sent empty bypass!");
			Disconnection.of(client, activeChar).defaultSequence(false);
			return;
		}
		
		if (activeChar.isDebug())
		{
			activeChar.sendDebugMessage("Bypass: " + _command, DebugType.BYPASSES);
		}
		
		boolean requiresBypassValidation = true;
		for (String possibleNonHtmlCommand : _possibleNonHtmlCommands)
		{
			if (_command.startsWith(possibleNonHtmlCommand))
			{
				requiresBypassValidation = false;
				break;
			}
		}
		
		int bypassOriginId = 0;
		if (requiresBypassValidation)
		{
			bypassOriginId = activeChar.validateHtmlAction(_command);
			if (bypassOriginId == -1)
			{
				LOGGER.warn("Player " + activeChar.getName() + " sent non cached bypass: '" + _command + "'");
				return;
			}
			
			if ((bypassOriginId > 0) && !Util.isInsideRangeOfObjectId(activeChar, bypassOriginId, Npc.INTERACTION_DISTANCE))
			{
				// No logging here, this could be a common case where the player has the html still open and run too far away and then clicks a html action
				return;
			}
		}
		
		if (!client.getFloodProtectors().getServerBypass().tryPerformAction(_command))
		{
			return;
		}
		
		final TerminateReturn terminateReturn = EventDispatcher.getInstance().notifyEvent(new OnPlayerBypass(activeChar, _command), activeChar, TerminateReturn.class);
		if ((terminateReturn != null) && terminateReturn.terminate())
		{
			return;
		}
		
		try
		{
			if (_command.startsWith("admin_"))
			{
				AdminCommandHandler.getInstance().useAdminCommand(activeChar, _command, true);
			}
			else if (CommunityBoardHandler.getInstance().isCommunityBoardCommand(_command))
			{
				CommunityBoardHandler.getInstance().handleParseCommand(_command, activeChar);
			}
			else if (_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				
				if (Util.isDigit(id))
				{
					WorldObject object = World.getInstance().findObject(Integer.parseInt(id));
					
					if ((object != null) && object.isNpc() && (endOfId > 0) && activeChar.isInRadius2d(object, Npc.INTERACTION_DISTANCE))
					{
						((Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
				}
				else
				{
					activeChar.sendDebugMessage("ObjectId of npc bypass is not digit: " + id, DebugType.BYPASSES);
				}
				
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("item_"))
			{
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(5, endOfId);
				}
				else
				{
					id = _command.substring(5);
				}
				try
				{
					final ItemInstance item = activeChar.getInventory().getItemByObjectId(Integer.parseInt(id));
					if ((item != null) && (endOfId > 0))
					{
						item.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
					
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe)
				{
					LOGGER.warn("NFE for command [" + _command + "]", nfe);
				}
			}
			else if (_command.startsWith("_match"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_diary"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_olympiad?command"))
			{
				int arenaId = Integer.parseInt(_command.split("=")[2]);
				final IBypassHandler handler = BypassHandler.getInstance().getHandler("arenachange");
				if (handler != null)
				{
					handler.useBypass("arenachange " + (arenaId - 1), activeChar, null);
				}
			}
			else if (_command.startsWith("menu_select"))
			{
				final Npc lastNpc = activeChar.getLastFolkNPC();
				if ((lastNpc != null) && lastNpc.canInteract(activeChar))
				{
					final String[] split = _command.substring(_command.indexOf("?") + 1).split("&");
					final int ask = Integer.parseInt(split[0].split("=")[1]);
					final int reply = Integer.parseInt(split[1].split("=")[1]);
					EventDispatcher.getInstance().notifyEventAsync(new OnNpcMenuSelect(activeChar, lastNpc, ask, reply), lastNpc);
				}
			}
			else if (_command.startsWith("manor_menu_select"))
			{
				final Npc lastNpc = activeChar.getLastFolkNPC();
				if (GeneralConfig.ALLOW_MANOR && (lastNpc != null) && lastNpc.canInteract(activeChar))
				{
					final String[] split = _command.substring(_command.indexOf("?") + 1).split("&");
					final int ask = Integer.parseInt(split[0].split("=")[1]);
					final int state = Integer.parseInt(split[1].split("=")[1]);
					final boolean time = split[2].split("=")[1].equals("1");
					EventDispatcher.getInstance().notifyEventAsync(new OnNpcManorBypass(activeChar, lastNpc, ask, state, time), lastNpc);
				}
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					if (bypassOriginId > 0)
					{
						WorldObject bypassOrigin = World.getInstance().findObject(bypassOriginId);
						if ((bypassOrigin != null) && bypassOrigin.isCreature())
						{
							handler.useBypass(_command, activeChar, (Creature) bypassOrigin);
						}
						else
						{
							handler.useBypass(_command, activeChar, null);
						}
					}
					else
					{
						handler.useBypass(_command, activeChar, null);
					}
				}
				else
				{
					LOGGER.warn(client + " sent not handled RequestBypassToServer: [" + _command + "]");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Exception processing bypass from player " + activeChar.getName() + ": " + _command, e);
			
			if (activeChar.isGM())
			{
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: " + e + "<br1>");
				sb.append("Bypass command: " + _command + "<br1>");
				sb.append("StackTrace:<br1>");
				for (StackTraceElement ste : e.getStackTrace())
				{
					sb.append(ste.toString() + "<br1>");
				}
				sb.append("</body></html>");
				// item html
				final NpcHtmlMessage msg = new NpcHtmlMessage(0, 1, sb.toString());
				msg.disableValidation();
				activeChar.sendPacket(msg);
			}
		}
	}
	
	/**
	 * @param activeChar
	 */
	private static void comeHere(PlayerInstance activeChar)
	{
		WorldObject obj = activeChar.getTarget();
		if (obj == null)
		{
			return;
		}
		if (obj.isNpc())
		{
			Npc temp = (Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, activeChar.getLocation());
		}
	}
}
