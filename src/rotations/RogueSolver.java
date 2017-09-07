package rotations;

import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Movement;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.RotationSolver;

/**
*
* @author TheCrux
*/

public class RogueSolver implements RotationSolver {

	BetterBot mBot;
	Keyboard mKeyboard;
	Unit mPlayer;
	Movement mMove;
	
	float mEatPercent;
	boolean mEating;
	int mPlayerLevel;

	// Everything sorted by rank!
	// Spells
	int mKick[] = { 1766, 1767, 1768, 1769 };

	// Buffs
	int mStealth[] = { 1784, 1785, 1786, 1787 };
	int mEvasion = 5277;

	// Drinking Buffs
	int mEatingBuffs[] = { 433, 434, 1127, 1129, 1131, 25700, 25886, 28616, 29008, 29073 };

	public RogueSolver(BetterBot bot) {
		mBot = bot;
		mPlayer = bot.getPlayer();
		mKeyboard = bot.getKeyboard();
		mPlayerLevel = mPlayer.getLevel();
		mMove = bot.getMovement();		
		
		System.out.println("TheCrux's Rogue script started");

		mEatPercent = 0.65f; // Eat if below 65% health
		mEating = false;
	}

	@Override
	public void combat(Unit u) {
	
		int energyValue = mPlayer.getEnergy();
		int comboPoints = 0;

		float targetDistance = u.getDistance();
		float targetHealth = u.getHealthFloat();
		
		// Kick
		if(mPlayerLevel >= 12 && u.isCasting() && !mBot.anyOnCD(mKick)) {
		  mKeyboard.type('1');
		}
		
		// Evasion
		if(mPlayerLevel >= 8 && mPlayer.getHealthFloat() <= 0.5f && !mPlayer.hasAura(mEvasion)) {
			mKeyboard.type('9');
		}
		
		if(targetDistance <= 5) {			
			if(energyValue >= 35 && (
					//(comboPoints == 1 && targetHealth <= 0.1f) ||
					//(comboPoints == 2 && targetHealth <= 0.15f) ||
					(comboPoints == 3 && targetHealth <= 0.1f) ||
					(comboPoints == 4 && targetHealth <= 0.15f) ||
					comboPoints == 5)) {
				mKeyboard.type('3');
			}
			// Sinister Strike OR Hemorrhage
			else if(energyValue >= 45) {
				mKeyboard.type('4');
			}
			// Melee Attack
			else {
				mKeyboard.type('7');
			}			
		}
	}

	@Override
	public void pull(Unit u) {		
		// Stealth
		if(mPlayerLevel >= 2 &&  !mPlayer.hasAura(mStealth) && !mBot.anyOnCD(mStealth)) {
			mKeyboard.type('0');
		}
				
		if(u.getDistance() > 5) {
		  mMove.walkTo(u, 4f);
		  return;
		}
		else {
		  // Cheap Shot
		  if(mPlayerLevel >= 26) {
		    mKeyboard.type('1');
		  }
		}
		
		
		combat(u);
	}

	@Override
	public boolean combatEnd(Unit u) {
		eat();
		if (mEating)
			return true;
		
		return false;
	}

	void eat() {		
		
		if(mPlayer.getHealthFloat() < mEatPercent)
			mEating = true;

		if (mEating && !mPlayer.hasAura(mEatingBuffs) && mPlayer.getHealthFloat() < mEatPercent + 0.05f) {
			mKeyboard.type('-'); // eat bind
			mBot.sleep(1200, 1700); // prevent double eating
		}
		
		if (mEating && mPlayer.getHealthFloat() > 0.9f) {
			// over 90% health, good enough
			mKeyboard.type('w'); // force to be standing
			mEating = false;
		}
	}

	@Override
	public int getPullDistance(Unit u) {
		return 30;
	}

  @Override
  public void approaching(Unit u) {    
  }
}
