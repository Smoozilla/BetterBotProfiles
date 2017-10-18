package rotations;

import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Database.Vendor;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.Vector3f;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import com.betterbot.api.pub.RotationSolver;

/**
*
* @author TheCrux
*/

public class RogueSolver implements RotationSolver {

	BetterBot mBot;
	Keyboard mKeyboard;
	Unit mPlayer;

	float mEatPercent;
	boolean mEating;
	int mPlayerLevel;
	int mActionBar;

	// Everything sorted by rank!
	// Spells
	int mKick[] = { 1766, 1767, 1768, 1769 };
	int mSliceAndDice[] = { 5171, 6774 };

	// Buffs
	int mStealth[] = { 1784, 1785, 1786, 1787 };
	int mEvasion = 5277;

	// Drinking Buffs
	int mEatingBuffs[] = { 433, 434, 1127, 1129, 1131, 25700, 25886, 28616, 29008, 29073 };

	// Mounts
	int mMounts[] = {
			// Wolfs
			1132, 6653, 6654, 23251, 23252, 23250,
			// Raptors
			10799, 10796, 8395, 23243, 23242, 23241,
			// Kodos
			18989, 18990, 23247, 23248, 23249,
			// Undead Horses
			17462, 17464, 17463, 17465, 23246,
			// Horses
			472, 6648, 458, 470, 23228, 23227, 23229,
			// Saber
			10789, 8394, 10793, 23221, 23219, 23338,
			// Rams
			6898, 6777, 6899, 23240, 23239, 23238,
			// Mechanostrider
			17454, 10873, 17453, 10969, 23222, 23223, 23225,
			// PvP - Horde
			22721, 22722, 22724, 22718, 23509,
			// PvP - Alliance
			22717, 22723, 22720, 22719, 23510,
			// Special (mostly Dungeons)
			24252, 17450, 24242, 16084, 18991, 18992, 17229 };

	public RogueSolver(BetterBot bot) {
		mBot = bot;
		mPlayer = bot.getPlayer();
		mKeyboard = bot.getKeyboard();
		mPlayerLevel = mPlayer.getLevel();
		mActionBar = 1;

		System.out.println("TheCrux's Rogue script started");

		mEatPercent = 0.65f; // Eat if below 65% health
		mEating = false;
	}

	void switchActionBar(int bar) {
		if (mActionBar != bar) {
			mKeyboard.press(KeyEvent.VK_SHIFT);
			mKeyboard.type("" + bar);
			mKeyboard.release(KeyEvent.VK_SHIFT);
			mActionBar = bar;
		}
	}

	@Override
	public void combat(Unit u) {

		if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
			switchActionBar(2);
			mKeyboard.type('0');
			switchActionBar(1);
		}

		int energyValue = mPlayer.getEnergy();
		int comboPoints = mBot.getComboPoints();

		float targetDistance = u.getDistance();
		float targetHealth = u.getHealthFloat();

		// Kick
		if (mPlayerLevel >= 12 && u.isCasting() && u.getManaMax() > 0 && !mBot.anyOnCD(mKick)) {
			mKeyboard.type('6');
		}

		// Evasion
		if (mPlayerLevel >= 8 && mPlayer.getHealthFloat() <= 0.5f && !mPlayer.hasAura(mEvasion)
				&& !mBot.anyOnCD(mEvasion)) {
			mKeyboard.type('8');
		}

		if (targetDistance <= 5) {
			// Slice and Dice
			if (mPlayerLevel >= 10 && energyValue >= 25 && !mPlayer.hasAura(mSliceAndDice) && comboPoints > 0) {
				mKeyboard.type('3');
			}
			// Eviscerate		
			else if (energyValue >= 35 && ((comboPoints == 1 && targetHealth <= 0.1f)
					|| (comboPoints == 2 && targetHealth <= 0.15f) || (comboPoints == 3 && targetHealth <= 0.2f)
					|| (comboPoints == 4 && targetHealth <= 0.25f) || comboPoints == 5)) {
				mKeyboard.type('2');
			}
			// Sinister Strike OR Hemorrhage
			else if (energyValue >= 45) {
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

		// Try to Backstap / Ambush / Garrote
		mKeyboard.type("3");
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

		if (mPlayer.getHealthFloat() < mEatPercent)
			mEating = true;

		if (mEating && !mPlayer.hasAura(mEatingBuffs) && mPlayer.getHealthFloat() < mEatPercent + 0.05f) {
			mKeyboard.type('0'); // eat bind
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
		return 5;
	}

	long waitFlag = System.currentTimeMillis();

	@Override
	public void approaching(Unit u) {
		long now = System.currentTimeMillis();
		// Slow dooooown!
		if (now - waitFlag > 500 && u.getDistance() < 25) {

			if (mPlayerLevel >= 40 && !mPlayer.hasAura(mMounts)) {
				switchActionBar(2);
				mKeyboard.type('0');
				switchActionBar(1);
				return;
			}

			waitFlag = now;

			// Stealth
			if (mPlayerLevel >= 2 && !mPlayer.hasAura(mStealth) && !mBot.anyOnCD(mStealth)) {
				mKeyboard.type('9');
			}
			// Cheap Shot
			if (mPlayerLevel >= 26 && u.getDistance() <= 5) {
				mKeyboard.type('1');
			}
		}
	}

	@Override
	public boolean afterResurrect() {
		return false;
	}

	@Override
	public boolean atVendor(Vendor arg0) {
		return false;
	}

	@Override
	public boolean beforeInteract() {
		// Remove Mount
		if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
			switchActionBar(2);
			mKeyboard.type('0');
			switchActionBar(1);
			return true;
		}
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
		if (mPlayer.isCasting()) {
			return true;
		}

		// Mount
		if (mPlayerLevel >= 40 && !mPlayer.hasAura(mMounts)) {
			switchActionBar(2);
			mKeyboard.type('0');
			switchActionBar(1);
			return true;
		}
		return false;
	}
}
