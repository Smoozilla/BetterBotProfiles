package rotations;

import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Database.Vendor;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.Vector3f;
import javax.swing.JComponent;
import com.betterbot.api.pub.RotationSolver;

/**
*
* @author TheCrux
*/

public class DruidSolver implements RotationSolver {

	BetterBot mBot;
	Keyboard mKeyboard;
	Unit mPlayer;

	float mDrinkPercent;
	boolean mDrinking;
	int mPlayerLevel;

	// Everything sorted by rank!
	// Spells
	int mMoonfire[] = { 8921, 8924, 8925, 8926, 8927, 8928, 8929, 9833, 9834, 9835 };

	// Buffs
	int mMarkOfTheWild[] = { 1126, 5232, 6756, 5234, 8907, 9884, 9885 };
	int mThorns[] = { 467, 782, 1075, 8914, 9756, 9910 };

	// Drinking Buffs
	int mDrinkingBuffs[] = { 430, 431, 432, 1133, 1135, 1137, 10250, 22734, 24355, 29007 };

	public DruidSolver(BetterBot bot) {
		this.mBot = bot;
		mPlayer = bot.getPlayer();
		mKeyboard = bot.getKeyboard();
		mPlayerLevel = mPlayer.getLevel();

		System.out.println("TheCrux's Druid script started");

		mDrinkPercent = 0.35f; // drink if below 35% mana
		mDrinking = false;
	}

	@Override
	public void combat(Unit u) {

		if (mPlayer.isCasting()) {
			return;
		}

		// Player at good health
		if (mPlayer.getHealthFloat() > 0.60f) {

			float manaValue = mPlayer.getManaFloat();
			float targetDistance = u.getDistance();

			// To make sure the bot is attacking on low level
			if(mPlayerLevel < 10){
				mKeyboard.type('7');
			}

			// Moonfire
			if (mPlayerLevel >= 4 && targetDistance < 30 && u.getHealthFloat() > 0.2f && !u.hasAura(mMoonfire)
					&& manaValue >= 0.3f) {
				mKeyboard.type('2');
			}
			// Wrath
			else if (targetDistance < 30 && manaValue >= 0.2f) {
				mKeyboard.type('4');
			}
			// Melee Attack
			else if (targetDistance <= 5) {
				mKeyboard.type('7');
			}
		}
		// Player below 60% Health
		else {
			// Healing Touch
			if (mPlayer.getManaFloat() >= 0.2f) {
				mKeyboard.type('3');
			}
		}
	}

	@Override
	public void pull(Unit u) {
		if (!isFullBuffed())
			return;

		// Pull with Wrath
		mKeyboard.type('4');

		combat(u);
	}

	@Override
	public boolean combatEnd(Unit u) {

		drink();
		return mDrinking;
	}

	boolean isFullBuffed() {

		// Mark of the Wild
		if (mPlayerLevel >= 2 && !mPlayer.hasAura(mMarkOfTheWild)) {
			mKeyboard.type('-');
			return false;
		}
		// Thorns
		if (mPlayerLevel >= 6 && !mPlayer.hasAura(mThorns)) {
			mKeyboard.type('9');
			return false;
		}

		return true;
	}

	void drink() {

		if (mPlayer.getManaFloat() < mDrinkPercent)
			mDrinking = true;

		if (mDrinking && !mPlayer.hasAura(mDrinkingBuffs) && mPlayer.getManaFloat() < mDrinkPercent + 0.05f) {
			mKeyboard.type('0'); // drink bind
			mBot.sleep(1200, 1700); // prevent double drinking
		}

		if (mDrinking && mPlayer.getManaFloat() > 0.9f) {
			// over 90% mana, good enough
			mKeyboard.type('w'); // force to be standing
			mDrinking = false;
		}
	}

	@Override
	public int getPullDistance(Unit u) {
		return 30;
	}

	@Override
	public void approaching(Unit u) {
	}

	@Override
	public boolean afterResurrect() {

		if(mPlayer.inCombat())
			return false;

		drink();

		return mDrinking;
	}

	@Override
	public boolean atVendor(Vendor arg0) {
		return false;
	}

	@Override
	public boolean beforeInteract() {
		return false;
	}

	@Override
	public JComponent getUI() {
		return null;
	}

	@Override
	public Vendor getVendor() {
		return null;
	}

	@Override
	public boolean prepareForTravel(Vector3f arg0) {
		return false;
	}
}
