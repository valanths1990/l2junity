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
package org.l2junity.gameserver.network.client.send;

import org.l2junity.gameserver.config.HexIDConfig;
import org.l2junity.gameserver.data.sql.impl.CrestTable;
import org.l2junity.gameserver.model.Crest;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class AllyCrest implements IClientOutgoingPacket
{
	private final int _crestId;
	private final byte[] _data;
	
	public AllyCrest(int crestId)
	{
		_crestId = crestId;
		final Crest crest = CrestTable.getInstance().getCrest(crestId);
		_data = crest != null ? crest.getData() : null;
	}
	
	public AllyCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ALLIANCE_CREST.writeId(packet);
		
		packet.writeD(HexIDConfig.SERVER_ID);
		packet.writeD(_crestId);
		if (_data != null)
		{
			packet.writeD(_data.length);
			packet.writeB(_data);
		}
		else
		{
			packet.writeD(0);
		}
		return true;
	}
}
