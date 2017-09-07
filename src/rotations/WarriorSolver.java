package rotations;

import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.RotationSolver;

/**
*
* @author TheCrux
*/

public class WarriorSolver implements RotationSolver {

	BetterBot mBot;
	Keyboard mKeyboard;
	Unit mPlayer;
	
	float mEatPercent;
	boolean mEating;
	int mPlayerLevel;

	// Everything sorted by rank!
	// Spells
	int mCharge[] = { 100, 6178, 11578 };
	int mRend[] = { 772, 6546, 6547, 6548, 11572, 11573, 11574 };
	int mThunderClap[] = { 8147 };

	// Buffs
	int mBattleShout[] = { 6673, 5242, 6192, 11549, 11550, 11551 };

	// Eating Buffs
	int mEatingBuffs[] = { 433, 434, 1127, 1129, 1131, 25700, 25886, 28616, 29008, 29073 };
	
	public WarriorSolver(BetterBot bot) {
		this.mBot = bot;
		mPlayer = bot.getPlayer();
		mKeyboard = bot.getKeyboard();
		mPlayerLevel = mPlayer.getLevel();
		
		System.out.println("TheCrux's Warrior script started");

		mEatPercent = 0.65f; // Eat if below 65% health
		mEating = false;
	}

	@Override
	public void combat(Unit u) {

		if (mPlayer.isCasting()) {
			return;
		}
		
		int rageValue = mPlayer.getRage();

		float targetDistance = u.getDistance();
		int attackerAmount = mBot.getAttackers().size();
		
		if(mPlayerLevel >= 6 && attackerAmount > 1 && !u.hasAura(mThunderClap) && !mBot.anyOnCD(mThunderClap)) {			
			// Thunderclap
			if(rageValue >= 20 && targetDistance <= 5) {
				mKeyboard.type("6");
			}
			else {
				return;
			}
		}
				
		// Battle Shout
		if(rageValue >= 10 && !mPlayer.hasAura(mBattleShout))
			mKeyboard.type("5");
		// Rend
		else if(mPlayerLevel >= 4 && rageValue >= 10 && targetDistance <= 5 && !u.hasAura(mRend))
			mKeyboard.type("3");
		// Heroic Strike
		else if(rageValue >= 15 && targetDistance <= 5)
			mKeyboard.type("1");						
		// Melee Attack
		else if(targetDistance <= 5) {
			mKeyboard.type("7");
		}
	}

	@Override
	public void pull(Unit u) {		
		// Charge
		if(mPlayerLevel >= 4 &&  !mBot.anyOnCD(mCharge))
			mKeyboard.type("2");
		
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
			mKeyboard.type('0'); // eat bind
			mBot.sleep(1200, 1700); // prevent double eating
		}
		
		if (mEating && mPlayer.getHealthFloat() > 0.9f) {
			// over 90% health, good enough
			mKeyboard.type("w"); // force to be standing
			mEating = false;
		}
	}

	@Override
	public int getPullDistance(Unit u) {
		return 25;
	}

  @Override
  public void approaching(Unit u) {    
  }
}
