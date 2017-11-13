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
package org.l2junity.gameserver.model.buylist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.l2junity.commons.sql.DatabaseFactory;
import org.l2junity.commons.util.concurrent.ThreadPool;
import org.l2junity.gameserver.config.RatesConfig;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.type.EtcItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NosBit
 */
public final class Product
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Product.class);
	
	private final int _buyListId;
	private final L2Item _item;
	private final long _price;
	private final long _restockDelay;
	private final long _maxCount;
	private final double _baseTax;
	private AtomicLong _count = null;
	private ScheduledFuture<?> _restockTask = null;
	
	public Product(int buyListId, L2Item item, long price, long restockDelay, long maxCount, int baseTax)
	{
		Objects.requireNonNull(item);
		_buyListId = buyListId;
		_item = item;
		_price = (price < 0) ? item.getReferencePrice() : price;
		_restockDelay = restockDelay * 60000;
		_maxCount = maxCount;
		_baseTax = baseTax / 100.0;
		if (hasLimitedStock())
		{
			_count = new AtomicLong(maxCount);
		}
	}
	
	public L2Item getItem()
	{
		return _item;
	}
	
	public int getItemId()
	{
		return getItem().getId();
	}
	
	public long getPrice()
	{
		long price = _price;
		if (_item.getItemType().equals(EtcItemType.CASTLE_GUARD))
		{
			price *= RatesConfig.RATE_SIEGE_GUARDS_PRICE;
		}
		return price;
	}
	
	public double getBaseTaxRate()
	{
		return _baseTax;
	}
	
	public long getRestockDelay()
	{
		return _restockDelay;
	}
	
	public long getMaxCount()
	{
		return _maxCount;
	}
	
	public long getCount()
	{
		if (_count == null)
		{
			return 0;
		}
		long count = _count.get();
		return count > 0 ? count : 0;
	}
	
	public void setCount(long currentCount)
	{
		if (_count == null)
		{
			_count = new AtomicLong();
		}
		_count.set(currentCount);
	}
	
	public boolean decreaseCount(long val)
	{
		if (_count == null)
		{
			return false;
		}
		if ((_restockTask == null) || _restockTask.isDone())
		{
			_restockTask = ThreadPool.schedule(this::restock, getRestockDelay(), TimeUnit.MILLISECONDS);
		}
		boolean result = _count.addAndGet(-val) >= 0;
		save();
		return result;
	}
	
	public boolean hasLimitedStock()
	{
		return getMaxCount() > -1;
	}
	
	public void restartRestockTask(long nextRestockTime)
	{
		long remainTime = nextRestockTime - System.currentTimeMillis();
		if (remainTime > 0)
		{
			_restockTask = ThreadPool.schedule(this::restock, remainTime, TimeUnit.MILLISECONDS);
		}
		else
		{
			restock();
		}
	}
	
	public void restock()
	{
		setCount(getMaxCount());
		save();
	}
	
	private void save()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO `buylists`(`buylist_id`, `item_id`, `count`, `next_restock_time`) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `count` = ?, `next_restock_time` = ?"))
		{
			statement.setInt(1, _buyListId);
			statement.setInt(2, getItemId());
			statement.setLong(3, getCount());
			statement.setLong(5, getCount());
			if ((_restockTask != null) && (_restockTask.getDelay(TimeUnit.MILLISECONDS) > 0))
			{
				long nextRestockTime = System.currentTimeMillis() + _restockTask.getDelay(TimeUnit.MILLISECONDS);
				statement.setLong(4, nextRestockTime);
				statement.setLong(6, nextRestockTime);
			}
			else
			{
				statement.setLong(4, 0);
				statement.setLong(6, 0);
			}
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to save Product buylist_id:" + _buyListId + " item_id:" + getItemId(), e);
		}
	}
}
