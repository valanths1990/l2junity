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
package org.l2junity.gameserver.network.gameserverpackets;

import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.l2junity.util.network.BaseSendablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends BaseSendablePacket
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BlowFishKey.class);
	
	/**
	 * @param blowfishKey
	 * @param publicKey
	 */
	public BlowFishKey(byte[] blowfishKey, RSAPublicKey publicKey)
	{
		writeC(0x00);
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] encrypted = rsaCipher.doFinal(blowfishKey);
			writeD(encrypted.length);
			writeB(encrypted);
		}
		catch (Exception e)
		{
			LOGGER.error("Error While encrypting blowfish key for transmision (Crypt error): " + e.getMessage(), e);
		}
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
