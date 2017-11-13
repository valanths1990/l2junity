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

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.itemcontainer.ItemContainer;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class WareHouseDepositList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3;
	public static final int FREIGHT = 1;
	private final long _playerAdena;
	private final int _whSize;
	private final List<ItemInstance> _items = new ArrayList<>();
	private final List<Integer> _itemsStackable = new ArrayList<>();
	/**
	 * <ul>
	 * <li>0x01-Private Warehouse</li>
	 * <li>0x02-Clan Warehouse</li>
	 * <li>0x03-Castle Warehouse</li>
	 * <li>0x04-Warehouse</li>
	 * </ul>
	 */
	private final int _whType;
	
	public WareHouseDepositList(PlayerInstance player, int type)
	{
		_whType = type;
		_playerAdena = player.getAdena();
		final ItemContainer warehouse = player.getActiveWarehouse();
		_whSize = warehouse != null ? warehouse.getSize() : 0;
		
		final boolean isPrivate = _whType == PRIVATE;
		for (ItemInstance temp : player.getInventory().getAvailableItems(true, isPrivate, false))
		{
			if ((temp != null) && temp.isDepositable(isPrivate))
			{
				_items.add(temp);
			}
			if ((temp != null) && temp.isDepositable(isPrivate) && temp.isStackable())
			{
				_itemsStackable.add(temp.getDisplayId());
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.WAREHOUSE_DEPOSIT_LIST.writeId(packet);
		
		packet.writeH(_whType);
		packet.writeQ(_playerAdena);
		packet.writeD(_whSize);
		packet.writeH(_itemsStackable.size());
		
		for (int itemId : _itemsStackable)
		{
			packet.writeD(itemId);
		}
		
		packet.writeH(_items.size());
		
		for (ItemInstance item : _items)
		{
			writeItem(packet, item);
			packet.writeD(item.getObjectId());
		}
		return true;
	}
}
