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
package org.l2junity.gameserver.datatables;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.l2junity.Config;
import org.l2junity.gameserver.InstanceListManager;
import org.l2junity.gameserver.enums.TaxType;
import org.l2junity.gameserver.instancemanager.CastleManager;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.actor.instance.L2MerchantInstance;
import org.l2junity.gameserver.model.entity.Castle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author KenM
 */
public class MerchantPriceConfigTable implements InstanceListManager
{
	// Zoey76: TODO: Implement using IGameXmlReader.
	private static Logger LOGGER = LoggerFactory.getLogger(MerchantPriceConfigTable.class);
	
	public static MerchantPriceConfigTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static final String MPCS_FILE = "MerchantPriceConfig.xml";
	
	private final Map<Integer, MerchantPriceConfig> _mpcs = new HashMap<>();
	private MerchantPriceConfig _defaultMpc;
	
	public MerchantPriceConfig getMerchantPriceConfig(L2MerchantInstance npc)
	{
		for (MerchantPriceConfig mpc : _mpcs.values())
		{
			if (ZoneManager.getInstance().getRegion(npc).getZones().containsKey(mpc.getZoneId()))
			{
				return mpc;
			}
		}
		return _defaultMpc;
	}
	
	public MerchantPriceConfig getMerchantPriceConfig(int id)
	{
		return _mpcs.get(id);
	}
	
	public void loadXML() throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT + "/data/" + MPCS_FILE);
		if (file.exists())
		{
			int defaultPriceConfigId;
			Document doc = factory.newDocumentBuilder().parse(file);
			
			Node n = doc.getDocumentElement();
			Node dpcNode = n.getAttributes().getNamedItem("defaultPriceConfig");
			if (dpcNode == null)
			{
				throw new IllegalStateException("merchantPriceConfig must define an 'defaultPriceConfig'");
			}
			defaultPriceConfigId = Integer.parseInt(dpcNode.getNodeValue());
			
			MerchantPriceConfig mpc;
			for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
			{
				mpc = parseMerchantPriceConfig(n);
				if (mpc != null)
				{
					_mpcs.put(mpc.getId(), mpc);
				}
			}
			
			MerchantPriceConfig defaultMpc = this.getMerchantPriceConfig(defaultPriceConfigId);
			if (defaultMpc == null)
			{
				throw new IllegalStateException("'defaultPriceConfig' points to an non-loaded priceConfig");
			}
			_defaultMpc = defaultMpc;
		}
	}
	
	private MerchantPriceConfig parseMerchantPriceConfig(Node n)
	{
		if (n.getNodeName().equals("priceConfig"))
		{
			final int id;
			final int baseTax;
			int castleId = -1;
			int zoneId = -1;
			final String name;
			
			Node node = n.getAttributes().getNamedItem("id");
			if (node == null)
			{
				throw new IllegalStateException("Must define the priceConfig 'id'");
			}
			id = Integer.parseInt(node.getNodeValue());
			
			node = n.getAttributes().getNamedItem("name");
			if (node == null)
			{
				throw new IllegalStateException("Must define the priceConfig 'name'");
			}
			name = node.getNodeValue();
			
			node = n.getAttributes().getNamedItem("baseTax");
			if (node == null)
			{
				throw new IllegalStateException("Must define the priceConfig 'baseTax'");
			}
			baseTax = Integer.parseInt(node.getNodeValue());
			
			node = n.getAttributes().getNamedItem("castleId");
			if (node != null)
			{
				castleId = Integer.parseInt(node.getNodeValue());
			}
			
			node = n.getAttributes().getNamedItem("zoneId");
			if (node != null)
			{
				zoneId = Integer.parseInt(node.getNodeValue());
			}
			
			return new MerchantPriceConfig(id, name, baseTax, castleId, zoneId);
		}
		return null;
	}
	
	@Override
	public void loadInstances()
	{
		try
		{
			loadXML();
			LOGGER.info("Loaded {} merchant price configs.", _mpcs.size());
		}
		catch (Exception e)
		{
			LOGGER.error("Failed loading MerchantPriceConfigTable: {}", e);
		}
	}
	
	@Override
	public void updateReferences()
	{
		for (final MerchantPriceConfig mpc : _mpcs.values())
		{
			mpc.updateReferences();
		}
	}
	
	@Override
	public void activateInstances()
	{
	}
	
	/**
	 * @author KenM
	 */
	public static final class MerchantPriceConfig
	{
		private final int _id;
		private final String _name;
		private final int _baseTax;
		private final int _castleId;
		private Castle _castle;
		private final int _zoneId;
		
		public MerchantPriceConfig(final int id, final String name, final int baseTax, final int castleId, final int zoneId)
		{
			_id = id;
			_name = name;
			_baseTax = baseTax;
			_castleId = castleId;
			_zoneId = zoneId;
		}
		
		/**
		 * @return Returns the id.
		 */
		public int getId()
		{
			return _id;
		}
		
		/**
		 * @return Returns the name.
		 */
		public String getName()
		{
			return _name;
		}
		
		/**
		 * @return Returns the baseTax.
		 */
		public int getBaseTax()
		{
			return _baseTax;
		}
		
		/**
		 * @return Returns the baseTax / 100.0.
		 */
		public double getBaseTaxRate()
		{
			return _baseTax / 100.0;
		}
		
		/**
		 * @return Returns the castle.
		 */
		public Castle getCastle()
		{
			return _castle;
		}
		
		/**
		 * @return Returns the zoneId.
		 */
		public int getZoneId()
		{
			return _zoneId;
		}
		
		public boolean hasCastle()
		{
			return getCastle() != null;
		}
		
		public double getCastleTaxRate()
		{
			return hasCastle() ? getCastle().getTaxRate() : 0.0;
		}
		
		public int getTotalTax(TaxType taxType)
		{
			return hasCastle() ? (getCastle().getTaxPercent(taxType) + getBaseTax()) : getBaseTax();
		}
		
		public double getTotalTaxRate(TaxType taxType)
		{
			return getTotalTax(taxType) / 100.0;
		}
		
		public void updateReferences()
		{
			if (_castleId > 0)
			{
				_castle = CastleManager.getInstance().getCastleById(_castleId);
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final MerchantPriceConfigTable _instance = new MerchantPriceConfigTable();
	}
}