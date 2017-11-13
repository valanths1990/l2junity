/*
 * Copyright (C) 2004-2017 L2J Unity
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
package org.l2junity.gameserver.network.client.send.changeattribute;

import java.util.Collection;

import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.AbstractItemPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExChangeAttributeItemList extends AbstractItemPacket
{
	private final Collection<ItemInstance> _items;
	
	public ExChangeAttributeItemList(Collection<ItemInstance> items)
	{
		_items = items;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CHANGE_ATTRIBUTE_ITEM_LIST.writeId(packet);
		packet.writeD(1);
		packet.writeD(_items.size());
		for (ItemInstance item : _items)
		{
			writeItem(packet, item);
		}
		return true;
	}
}