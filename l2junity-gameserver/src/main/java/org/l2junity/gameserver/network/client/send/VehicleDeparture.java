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

import org.l2junity.gameserver.model.actor.instance.L2BoatInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Maktakien
 */
public class VehicleDeparture implements IClientOutgoingPacket
{
	private final int _objectId, _x, _y, _z, _moveSpeed, _rotationSpeed;
	
	public VehicleDeparture(L2BoatInstance boat)
	{
		_objectId = boat.getObjectId();
		_x = (int) boat.getXdestination();
		_y = (int) boat.getYdestination();
		_z = (int) boat.getZdestination();
		_moveSpeed = (int) boat.getMoveSpeed();
		_rotationSpeed = (int) boat.getStat().getRotationSpeed();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.VEHICLE_DEPARTURE.writeId(packet);
		
		packet.writeD(_objectId);
		packet.writeD(_moveSpeed);
		packet.writeD(_rotationSpeed);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		return true;
	}
}
