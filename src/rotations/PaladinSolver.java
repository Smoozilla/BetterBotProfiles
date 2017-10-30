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

public class PaladinSolver implements RotationSolver {

	BetterBot mBot;
	Keyboard mKeyboard;
	Unit mPlayer;

	float mDrinkPercent;
	boolean mDrinking;
	int mPlayerLevel;
	int mActionBar;

	// Everything sorted by rank!
	// Spells
	int mJudgement = 20271;
	int mHammerOfJustice[] = { 853, 5588, 5589, 10308 };
	int mLayOnHands[] = { 633, 2800, 10310 };

	// Buffs
	int mSealOfRighteousness[] = { 21084, 20287, 20288, 20289, 20290, 20291, 20292, 20293 };
	int mSealOfTheCrusader[] = { 21082, 20162, 20305, 20306, 20307, 20308 };
	int mDevotionAura[] = { 465, 10290, 643, 10291, 1032, 10292, 10293 };
	int mBlessingOfMight[] = { 19740, 19834, 19835, 19836, 19837, 19838, 25291 };

	// Debuffs
	int mJudgementOfTheCrusader[] = { 21183, 20188, 20300, 20301, 20302, 20303 };

	// Drinking Buffs
	int mDrinkingBuffs[] = { 430, 431, 432, 1133, 1135, 1137, 10250, 22734, 24355, 29007 };

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

	public PaladinSolver(BetterBot bot) {
		this.mBot = bot;
		mPlayer = bot.getPlayer();
		mKeyboard = bot.getKeyboard();
		mPlayerLevel = mPlayer.getLevel();
		mActionBar = 1;

		System.out.println("TheCrux's Paladin script started");

		mDrinkPercent = 0.35f; // drink if below 35% mana
		mDrinking = false;
	}

	void switchActionBar(int bar) {
		if (mActionBar != bar) {
			mKeyboard.press(KeyEvent.VK_SHIFT);
			mKeyboard.type("" + bar);
			mBot.sleep(100, 300);
			mKeyboard.release(KeyEvent.VK_SHIFT);
			mActionBar = bar;
		}
	}

	@Override
	public void combat(Unit u) {
		switchActionBar(1);

		if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
			switchActionBar(2);
			mKeyboard.type('0');
			switchActionBar(1);
		}

		if (mPlayer.isCasting()) {
			return;
		}

		float manaValue = mPlayer.getManaFloat();
		float targetDistance = u.getDistance();

		// Player at good health
		if (mPlayer.getHealthFloat() > 0.6f) {

			// ------ Low Level ------
			if (mPlayerLevel < 6) {
				// Seal of Righteousness
				if (u.getHealthFloat() > 0.2f && !mPlayer.hasAura(mSealOfRighteousness) && manaValue >= 0.1f) {
					mKeyboard.type('2');
				}
				// Judgement
				else if (mPlayerLevel >= 4 && targetDistance <= 10 && mPlayer.hasAura(mSealOfRighteousness)
						&& !mBot.anyOnCD(mSealOfRighteousness)) {
					mKeyboard.type('1');
				}
				// Melee Attack
				else if (targetDistance <= 5) {
					mKeyboard.type('7');
				}
				return;
			}
			// ------------------------

			// Seal of the Crusader
			if (!u.hasAura(mJudgementOfTheCrusader) && !mPlayer.hasAura(mSealOfTheCrusader) && manaValue >= 0.2f) {
				mKeyboard.type('5');
			}
			// Judgement
			else if (targetDistance <= 10
					&& ((!u.hasAura(mJudgementOfTheCrusader) && mPlayer.hasAura(mSealOfTheCrusader))
							|| (u.hasAura(mJudgementOfTheCrusader) && mPlayer.hasAura(mSealOfRighteousness)))
					&& !mBot.anyOnCD(mJudgement)) {
				mKeyboard.type('1');
			}
			// Seal of Righteousness
			else if (u.hasAura(mJudgementOfTheCrusader) && !mPlayer.hasAura(mSealOfRighteousness) && manaValue >= 0.1f) {
				mKeyboard.type('2');
			}
			// Melee Attack
			else if (targetDistance <= 5) {
				mKeyboard.type('7');
			}
		}
		// Player below 60% Health
		else {
			// Lay on Hands
			if (mPlayerLevel >= 10 && mPlayer.getHealthFloat() <= 0.1f && !mBot.anyOnCD(mLayOnHands)) {
				mKeyboard.type('9');
			}
			// Hammer of Justice
			if (mPlayerLevel >= 8 && targetDistance <= 10 && !mBot.anyOnCD(mHammerOfJustice) && manaValue >= 0.2f) {
				mKeyboard.type('8');
			}
			// Holy Light
			if (manaValue >= 0.2f) {
				mKeyboard.type('3');
			}
		}
	}

	@Override
	public void pull(Unit u) {
		// Judgement
		if (mPlayerLevel >= 4 && !mBot.anyOnCD(mJudgement))
			mKeyboard.type('1');

		if (isFullBuffed(u.getDistance()))
			combat(u);
	}

	@Override
	public boolean combatEnd(Unit u) {

		drink();
		return mDrinking;
	}

	boolean isFullBuffed(float distance) {

		// Blessing of Might
		if (mPlayerLevel >= 4 && !mPlayer.hasAura(mBlessingOfMight)) {
			switchActionBar(2);
			mKeyboard.type('8');
			return false;
		}
		// Devotion Aura
		if (mPlayerLevel >= 2 && !mPlayer.hasAura(mDevotionAura)) {
			switchActionBar(2);
			mKeyboard.type('9');
			return false;
		}
		// Seal of the Crusader
		if (mPlayerLevel >= 6 && distance < 15 && !mPlayer.hasAura(mSealOfTheCrusader)) {
			switchActionBar(1);
			mKeyboard.type('5');
			return false;
		}
		// Seal of Righteousness
		if (mPlayerLevel < 6 && distance < 10 && !mPlayer.hasAura(mSealOfRighteousness)) {
			switchActionBar(1);
			mKeyboard.type('2');
		}

		switchActionBar(1);
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
		if (mPlayerLevel < 4)
			return 5;
		return 10;
	}

	long waitFlag = System.currentTimeMillis();

	@Override
	public void approaching(Unit u) {

		long now = System.currentTimeMillis();
		// Slow dooooown!
		if (now - waitFlag > 500) {

			// Remove Mount
			if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
				switchActionBar(2);
				mKeyboard.type('0');
				switchActionBar(1);
			}
			waitFlag = now;
			isFullBuffed(u.getDistance());
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
