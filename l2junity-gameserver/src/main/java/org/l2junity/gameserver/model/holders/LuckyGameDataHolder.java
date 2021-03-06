/*
 * Copyright (C) 2004-2016 L2J Unity
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
package org.l2junity.gameserver.model.holders;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.StatsSet;

/**
 * @author Sdw
 */
public class LuckyGameDataHolder
{
	final private int _index;
	final private int _turningPoints;
	final private List<ItemChanceHolder> _commonRewards = new ArrayList<>();
	final private List<ItemPointHolder> _uniqueRewards = new ArrayList<>();
	final private List<ItemChanceHolder> _modifyRewards = new ArrayList<>();
	private int _minModifyRewardGame;
	private int _maxModifyRewardGame;
	
	public LuckyGameDataHolder(StatsSet params)
	{
		_index = params.getInt("index");
		_turningPoints = params.getInt("turning_point");
	}
	
	public void addCommonReward(ItemChanceHolder item)
	{
		_commonRewards.add(item);
	}
	
	public void addUniqueReward(ItemPointHolder item)
	{
		_uniqueRewards.add(item);
	}
	
	public void addModifyReward(ItemChanceHolder item)
	{
		_modifyRewards.add(item);
	}
	
	public List<ItemChanceHolder> getCommonReward()
	{
		return _commonRewards;
	}
	
	public List<ItemPointHolder> getUniqueReward()
	{
		return _uniqueRewards;
	}
	
	public List<ItemChanceHolder> getModifyReward()
	{
		return _modifyRewards;
	}
	
	public void setMinModifyRewardGame(int minModifyRewardGame)
	{
		_minModifyRewardGame = minModifyRewardGame;
	}
	
	public void setMaxModifyRewardGame(int maxModifyRewardGame)
	{
		_maxModifyRewardGame = maxModifyRewardGame;
	}
	
	public int getMinModifyRewardGame()
	{
		return _minModifyRewardGame;
	}
	
	public int getMaxModifyRewardGame()
	{
		return _maxModifyRewardGame;
	}
	
	public int getIndex()
	{
		return _index;
	}
	
	public int getTurningPoints()
	{
		return _turningPoints;
	}
}
