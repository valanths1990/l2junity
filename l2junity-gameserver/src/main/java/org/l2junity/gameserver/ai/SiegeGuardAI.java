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
package org.l2junity.gameserver.ai;

import static org.l2junity.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static org.l2junity.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static org.l2junity.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.l2junity.commons.util.Rnd;
import org.l2junity.commons.util.concurrent.ThreadPool;
import org.l2junity.gameserver.geodata.GeoData;
import org.l2junity.gameserver.instancemanager.GameTimeManager;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.Playable;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.L2DefenderInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.L2EffectType;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.util.Util;

/**
 * This class manages AI of L2Attackable.
 */
public class SiegeGuardAI extends CharacterAI implements Runnable
{
	private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds
	
	/** The L2Attackable AI task executed every 1s (call onEvtThink method) */
	private Future<?> _aiTask;
	
	/** For attack AI, analysis of mob and its targets */
	private final SelfAnalysis _selfAnalysis = new SelfAnalysis();
	// private TargetAnalysis _mostHatedAnalysis = new TargetAnalysis();
	
	/** The delay after which the attacked is stopped */
	private int _attackTimeout;
	
	/** The L2Attackable aggro counter */
	private int _globalAggro;
	
	/** The flag used to indicate that a thinking action is in progress */
	private boolean _thinking; // to prevent recursive thinking
	
	private final int _attackRange;
	
	public SiegeGuardAI(Creature creature)
	{
		super(creature);
		_selfAnalysis.init();
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
		_attackRange = _actor.getPhysicalAttackRange();
	}
	
	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
		
	}
	
	/**
	 * <B><U> Actor is a L2GuardInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li>
	 * </ul>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The L2PcInstance target isn't a Defender</li>
	 * </ul>
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * </ul>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li>
	 * </ul>
	 * @param target The targeted L2Object
	 * @return True if the target is autoattackable (depends on the actor type).
	 */
	protected boolean autoAttackCondition(Creature target)
	{
		// Check if the target isn't another guard, folk or a door
		if ((target == null) || (target instanceof L2DefenderInstance) || target.isNpc() || target.isDoor() || target.isAlikeDead())
		{
			return false;
		}
		
		// Check if the target isn't invulnerable
		if (target.isInvul())
		{
			return false;
		}
		
		// Get the owner if the target is a summon
		if (target.isSummon())
		{
			PlayerInstance owner = ((Summon) target).getOwner();
			if (_actor.isInRadius3d(owner, 1000))
			{
				target = owner;
			}
		}
		
		// Check if the target is a L2PcInstance
		if (target.isPlayable())
		{
			// Check if the target isn't in silent move mode AND too far (>100)
			if (((Playable) target).isSilentMovingAffected() && !_actor.isInRadius2d(target, 250))
			{
				return false;
			}
		}
		// Los Check Here
		return (_actor.isAutoAttackable(target) && GeoData.getInstance().canSeeTarget(_actor, target));
		
	}
	
	/**
	 * Set the Intention of this L2CharacterAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT>
	 * @param intention The new Intention to set to the AI
	 * @param args The first parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object... args)
	{
		if (intention == AI_INTENTION_IDLE)
		{
			// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
			super.changeIntention(AI_INTENTION_IDLE);
			
			// Stop AI task and detach AI from NPC
			if (_aiTask != null)
			{
				_aiTask.cancel(true);
				_aiTask = null;
			}
			
			// Cancel the AI
			_actor.detachAI();
			
			return;
		}
		
		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, args);
		
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (_aiTask == null)
		{
			_aiTask = ThreadPool.scheduleAtFixedRate(this, 1000, 1000, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.
	 * @param target The L2Character to attack
	 */
	@Override
	protected void onIntentionAttack(Creature target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeManager.getInstance().getGameTicks();
		
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		// if (_actor.getTarget() != null)
		super.onIntentionAttack(target);
	}
	
	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor can't attack, order to it to return to its home location</li>
	 * </ul>
	 */
	private void thinkActive()
	{
		Attackable npc = (Attackable) _actor;
		final WorldObject target = getTarget();
		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}
		
		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			World.getInstance().forEachVisibleObjectInRadius(npc, Creature.class, _attackRange, t ->
			{
				if (autoAttackCondition(t)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					int hating = npc.getHating(t);
					
					// Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
					if (hating == 0)
					{
						npc.addDamageHate(t, 0, 1);
					}
				}
			});
			
			// Chose a target from its aggroList
			Creature hated;
			if (_actor.isConfused() && (target != null) && target.isCreature())
			{
				hated = (Creature) target; // Force mobs to attack anybody if confused
			}
			else
			{
				hated = npc.getMostHated();
				// _mostHatedAnalysis.Update(hated);
			}
			
			// Order to the L2Attackable to attack the target
			if (hated != null)
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);
				
				if ((aggro + _globalAggro) > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!_actor.isRunning())
					{
						_actor.setRunning();
					}
					
					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
				}
				
				return;
			}
			
		}
		// Order to the L2DefenderInstance to return to its home location because there's no target to attack
		((L2DefenderInstance) _actor).returnHome();
	}
	
	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Call all L2Object of its Faction inside the Faction Range</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li>
	 * </ul>
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 */
	private void thinkAttack()
	{
		if (_attackTimeout < GameTimeManager.getInstance().getGameTicks())
		{
			// Check if the actor is running
			if (_actor.isRunning())
			{
				// Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
				_actor.setWalking();
				
				// Calculate a new attack timeout
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeManager.getInstance().getGameTicks();
			}
		}
		
		final WorldObject target = getTarget();
		final Creature attackTarget = (target != null) && target.isCreature() ? (Creature) target : null;
		// Check if target is dead or if timeout is expired to stop this attack
		if ((attackTarget == null) || attackTarget.isAlikeDead() || (_attackTimeout < GameTimeManager.getInstance().getGameTicks()))
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (attackTarget != null)
			{
				Attackable npc = (Attackable) _actor;
				npc.stopHating(attackTarget);
			}
			
			// Cancel target and timeout
			_attackTimeout = Integer.MAX_VALUE;
			setTarget(null);
			
			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE, null, null);
			
			_actor.setWalking();
			return;
		}
		
		factionNotifyAndSupport();
		attackPrepare();
	}
	
	private final void factionNotifyAndSupport()
	{
		WorldObject target = getTarget();
		// Call all L2Object of its Faction inside the Faction Range
		if ((((Npc) _actor).getTemplate().getClans() == null) || (target == null))
		{
			return;
		}
		
		if (target.isInvul())
		{
			return; // speeding it up for siege guards
		}
		
		// Go through all L2Character that belong to its faction
		// for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(((L2NpcInstance) _actor).getFactionRange()+_actor.getTemplate().collisionRadius))
		for (Creature cha : World.getInstance().getVisibleObjects(_actor, Creature.class, 1000))
		{
			if (!cha.isNpc())
			{
				if (_selfAnalysis.hasHealOrResurrect && cha.isPlayer() && (((Npc) _actor).getCastle().getSiege().checkIsDefender(cha.getClan())))
				{
					// heal friends
					if (!_actor.isAttackingDisabled() && (cha.getCurrentHp() < (cha.getMaxHp() * 0.6)) && (_actor.getCurrentHp() > (_actor.getMaxHp() / 2)) && (_actor.getCurrentMp() > (_actor.getMaxMp() / 2)) && cha.isInCombat())
					{
						for (Skill sk : _selfAnalysis.healSkills)
						{
							if (_actor.getCurrentMp() < sk.getMpConsume())
							{
								continue;
							}
							if (_actor.isSkillDisabled(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange(sk.getCastRange(), _actor, cha, true))
							{
								continue;
							}
							
							int chance = 5;
							if (chance >= Rnd.get(100))
							{
								continue;
							}
							if (!GeoData.getInstance().canSeeTarget(_actor, cha))
							{
								break;
							}
							
							WorldObject OldTarget = getTarget();
							setTarget(cha);
							_actor.doCast(sk);
							setTarget(OldTarget);
							return;
						}
					}
				}
				continue;
			}
			
			Npc npc = (Npc) cha;
			
			if (!npc.isInMyClan((Npc) _actor))
			{
				continue;
			}
			
			if (npc.getAI() != null) // TODO: possibly check not needed
			{
				if (!npc.isDead() && (Math.abs(target.getZ() - npc.getZ()) < 600)
				// && _actor.getAttackByList().contains(getTarget())
					&& (npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)
					// limiting aggro for siege guards
					&& npc.isInRadius3d(target, 1500) && GeoData.getInstance().canSeeTarget(npc, target))
				{
					// Notify the L2Object AI with EVT_AGGRESSION
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
					return;
				}
				// heal friends
				if (_selfAnalysis.hasHealOrResurrect && !_actor.isAttackingDisabled() && (npc.getCurrentHp() < (npc.getMaxHp() * 0.6)) && (_actor.getCurrentHp() > (_actor.getMaxHp() / 2)) && (_actor.getCurrentMp() > (_actor.getMaxMp() / 2)) && npc.isInCombat())
				{
					for (Skill sk : _selfAnalysis.healSkills)
					{
						if (_actor.getCurrentMp() < sk.getMpConsume())
						{
							continue;
						}
						if (_actor.isSkillDisabled(sk))
						{
							continue;
						}
						if (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))
						{
							continue;
						}
						
						int chance = 4;
						if (chance >= Rnd.get(100))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(_actor, npc))
						{
							break;
						}
						
						WorldObject OldTarget = getTarget();
						setTarget(npc);
						_actor.doCast(sk);
						setTarget(OldTarget);
						return;
					}
				}
			}
		}
	}
	
	private void attackPrepare()
	{
		final WorldObject target = getTarget();
		Creature attackTarget = (target != null) && target.isCreature() ? (Creature) target : null;
		if (attackTarget == null)
		{
			setTarget(null);
			setIntention(AI_INTENTION_ACTIVE, null, null);
			return;
		}
		
		// Get all information needed to choose between physical or magical attack
		Collection<Skill> skills = null;
		double dist = 0;
		int range = 0;
		L2DefenderInstance sGuard = (L2DefenderInstance) _actor;
		
		try
		{
			setTarget(attackTarget);
			skills = _actor.getAllSkills();
			dist = _actor.distance2d(attackTarget);
			range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + attackTarget.getTemplate().getCollisionRadius();
			if (attackTarget.isMoving())
			{
				range += 50;
			}
		}
		catch (NullPointerException e)
		{
			setTarget(null);
			setIntention(AI_INTENTION_ACTIVE, null, null);
			return;
		}
		
		if (!GeoData.getInstance().canSeeTarget(_actor, attackTarget))
		{
			// Siege guards differ from normal mobs currently:
			// If target cannot seen, don't attack any more
			sGuard.stopHating(attackTarget);
			setTarget(null);
			setIntention(AI_INTENTION_ACTIVE, null, null);
			return;
		}
		
		// Check if the actor isn't muted and if it is far from target
		if (!_actor.isMuted() && (dist > range))
		{
			// check for long ranged skills and heal/buff skills
			for (Skill sk : skills)
			{
				int castRange = sk.getCastRange();
				
				if ((dist <= castRange) && (castRange > 70) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk)) && !sk.isPassive())
				{
					
					WorldObject OldTarget = getTarget();
					if ((sk.isContinuous() && !sk.isDebuff()) || (sk.hasEffectType(L2EffectType.HEAL)))
					{
						boolean useSkillSelf = true;
						if ((sk.hasEffectType(L2EffectType.HEAL)) && (_actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5)))
						{
							useSkillSelf = false;
							break;
						}
						
						if ((sk.isContinuous() && !sk.isDebuff()) && _actor.isAffectedBySkill(sk.getId()))
						{
							useSkillSelf = false;
						}
						
						if (useSkillSelf)
						{
							setTarget(_actor);
						}
					}
					
					_actor.doCast(sk);
					setTarget(OldTarget);
					return;
				}
			}
			
			// Check if the L2SiegeGuardInstance is attacking, knows the target and can't run
			if (!(_actor.isAttackingNow()) && (_actor.getRunSpeed() == 0) && (_actor.isInSurroundingRegion(attackTarget)))
			{
				// Cancel the target
				setTarget(null);
				setIntention(AI_INTENTION_ACTIVE, null, null);
			}
			else
			{
				double dx = _actor.getX() - attackTarget.getX();
				double dy = _actor.getY() - attackTarget.getY();
				double dz = _actor.getZ() - attackTarget.getZ();
				double homeX = attackTarget.getX() - sGuard.getSpawn().getX();
				double homeY = attackTarget.getY() - sGuard.getSpawn().getY();
				
				// Check if the L2SiegeGuardInstance isn't too far from it's home location
				if ((((dx * dx) + (dy * dy)) > 10000) && (((homeX * homeX) + (homeY * homeY)) > 3240000) // 1800 * 1800
					&& (_actor.isInSurroundingRegion(attackTarget)))
				{
					// Cancel the target
					setTarget(null);
					setIntention(AI_INTENTION_ACTIVE, null, null);
				}
				else
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				{
					// Temporary hack for preventing guards jumping off towers,
					// before replacing this with effective geodata checks and AI modification
					if ((dz * dz) < (170 * 170)) // normally 130 if guard z coordinates correct
					{
						if (_selfAnalysis.isHealer)
						{
							return;
						}
						if (_selfAnalysis.isMage)
						{
							range = _selfAnalysis.maxCastRange - 50;
						}
						if (attackTarget.isMoving())
						{
							moveToPawn(attackTarget, range - 70);
						}
						else
						{
							moveToPawn(attackTarget, range);
						}
					}
				}
			}
			
			return;
			
		}
		// Else, if the actor is muted and far from target, just "move to pawn"
		else if (_actor.isMuted() && (dist > range) && !_selfAnalysis.isHealer)
		{
			// Temporary hack for preventing guards jumping off towers,
			// before replacing this with effective geodata checks and AI modification
			double dz = _actor.getZ() - attackTarget.getZ();
			if ((dz * dz) < (170 * 170)) // normally 130 if guard z coordinates correct
			{
				if (_selfAnalysis.isMage)
				{
					range = _selfAnalysis.maxCastRange - 50;
				}
				if (attackTarget.isMoving())
				{
					moveToPawn(attackTarget, range - 70);
				}
				else
				{
					moveToPawn(attackTarget, range);
				}
			}
			return;
		}
		// Else, if this is close enough to attack
		else if (dist <= range)
		{
			// Force mobs to attack anybody if confused
			Creature hated = null;
			if (_actor.isConfused())
			{
				hated = attackTarget;
			}
			else
			{
				hated = ((Attackable) _actor).getMostHated();
			}
			
			if (hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE, null, null);
				return;
			}
			if (hated != attackTarget)
			{
				attackTarget = hated;
			}
			
			_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeManager.getInstance().getGameTicks();
			
			// check for close combat skills && heal/buff skills
			if (!_actor.isMuted() && (Rnd.nextInt(100) <= 5))
			{
				for (Skill sk : skills)
				{
					int castRange = sk.getCastRange();
					
					if ((castRange >= dist) && !sk.isPassive() && (_actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk)) && !_actor.isSkillDisabled(sk))
					{
						WorldObject OldTarget = getTarget();
						if ((sk.isContinuous() && !sk.isDebuff()) || (sk.hasEffectType(L2EffectType.HEAL)))
						{
							boolean useSkillSelf = true;
							if ((sk.hasEffectType(L2EffectType.HEAL)) && (_actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5)))
							{
								useSkillSelf = false;
								break;
							}
							
							if ((sk.isContinuous() && !sk.isDebuff()) && _actor.isAffectedBySkill(sk.getId()))
							{
								useSkillSelf = false;
							}
							
							if (useSkillSelf)
							{
								setTarget(_actor);
							}
						}
						
						_actor.doCast(sk);
						setTarget(OldTarget);
						return;
					}
				}
			}
			// Finally, do the physical attack itself
			if (!_selfAnalysis.isHealer)
			{
				_actor.doAutoAttack(attackTarget);
			}
		}
	}
	
	/**
	 * Manage AI thinking actions of a L2Attackable.
	 */
	@Override
	protected void onEvtThink()
	{
		// if(getIntention() != AI_INTENTION_IDLE && (!_actor.isVisible() || !_actor.hasAI() || !_actor.isKnownPlayers()))
		// setIntention(AI_INTENTION_IDLE);
		
		// Check if the thinking action is already in progress
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		// Start thinking action
		_thinking = true;
		
		try
		{
			// Manage AI thinks of a L2Attackable
			if (getIntention() == AI_INTENTION_ACTIVE)
			{
				thinkActive();
			}
			else if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Attacked.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li>
	 * </ul>
	 * @param attacker The L2Character that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeManager.getInstance().getGameTicks();
		
		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}
		
		// Add the attacker to the _aggroList of the actor
		((Attackable) _actor).addDamageHate(attacker, 0, 1);
		
		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}
		
		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
		}
		
		super.onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Aggression.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Add the target to the actor _aggroList or update hate if already present</li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li>
	 * </ul>
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		if (_actor == null)
		{
			return;
		}
		Attackable me = (Attackable) _actor;
		
		if (target != null)
		{
			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);
			
			// Get the hate of the actor against the target
			aggro = me.getHating(target);
			
			if (aggro <= 0)
			{
				if (me.getMostHated() == null)
				{
					_globalAggro = -25;
					me.clearAggroList();
					setIntention(AI_INTENTION_ACTIVE, null, null);
				}
				return;
			}
			
			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!_actor.isRunning())
				{
					_actor.setRunning();
				}
				
				L2DefenderInstance sGuard = (L2DefenderInstance) _actor;
				double homeX = target.getX() - sGuard.getSpawn().getX();
				double homeY = target.getY() - sGuard.getSpawn().getY();
				
				// Check if the L2SiegeGuardInstance is not too far from its home location
				if (((homeX * homeX) + (homeY * homeY)) < 3240000)
				{
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
				}
			}
		}
		else
		{
			// currently only for setting lower general aggro
			if (aggro >= 0)
			{
				return;
			}
			
			Creature mostHated = me.getMostHated();
			if (mostHated == null)
			{
				_globalAggro = -25;
				return;
			}
			
			for (Creature aggroed : me.getAggroList().keySet())
			{
				me.addDamageHate(aggroed, 0, aggro);
			}
			
			aggro = me.getHating(mostHated);
			if (aggro <= 0)
			{
				_globalAggro = -25;
				me.clearAggroList();
				setIntention(AI_INTENTION_ACTIVE, null, null);
			}
		}
	}
	
	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		_actor.detachAI();
		super.stopAITask();
	}
}
