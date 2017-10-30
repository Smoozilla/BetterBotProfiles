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

public class WarriorSolver implements RotationSolver {

	BetterBot mBot;
	Keyboard mKeyboard;
	Unit mPlayer;
	int mActionBar;

	float mEatPercent;
	boolean mEating;
	int mPlayerLevel;

	// Everything sorted by rank!
	// Spells
	int mCharge[] = { 100, 6178, 11578 };
	int mRend[] = { 772, 6546, 6547, 6548, 11572, 11573, 11574 };
	int mThunderClap[] = { 6343, 8198, 8204, 8205, 11580, 11581 };
	int mPummel[] = { 6552, 6554 };
	int mBloodthirst[] = { 23881, 23892, 23893, 23894 };
	int mMortalStrike[] = { 12294, 21551, 21552, 21553, 27580 };

	// Buffs
	int mBattleShout[] = { 6673, 5242, 6192, 11549, 11550, 11551 };
	int mBloodrage = 2687;
	int mRetaliation = 20230;

	// Eating Buffs
	int mEatingBuffs[] = { 433, 434, 435, 1127, 1129, 1131, 2639, 6410, 7737, 24005, 25700, 25886, 28616, 29008,
			29073 };

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

	public WarriorSolver(BetterBot bot) {
		this.mBot = bot;
		mPlayer = bot.getPlayer();
		mKeyboard = bot.getKeyboard();
		mPlayerLevel = mPlayer.getLevel();
		mActionBar = 1;

		System.out.println("TheCrux's Warrior script started");

		mEatPercent = 0.65f; // Eat if below 65% health
		mEating = false;
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

		// Remove Mount
		if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
			switchActionBar(2);
			mKeyboard.type('9');
			switchActionBar(1);
		}

		if (mPlayer.isCasting()) {
			return;
		}

		int rageValue = mPlayer.getRage();

		float targetDistance = u.getDistance();
		int attackerAmount = mBot.getAttackers().size();

		// Pummel
		if (mPlayerLevel >= 38 && u.isCasting() && !mBot.anyOnCD(mPummel) && rageValue >= 10) {
			mKeyboard.type('8');
		}

		// Retalation
		if (mPlayerLevel >= 20 && mPlayer.getHealthFloat() < 0.2f && !mBot.anyOnCD(mRetaliation)) {
			switchActionBar(2);
			mKeyboard.type('3');
			switchActionBar(1);
		}

		if (mPlayerLevel >= 6 && attackerAmount > 1 && !u.hasAura(mThunderClap) && !mBot.anyOnCD(mThunderClap)) {
			// Thunderclap
			if (rageValue >= 20 && targetDistance <= 5) {
				mKeyboard.type('0');
			} else {
				return;
			}
		}

		// Blood Rage
		if (mPlayerLevel >= 10 && mPlayer.inCombat() && !mBot.anyOnCD(mBloodrage)) {
			mKeyboard.type('-');
		}
		// Battle Shout
		else if (rageValue >= 10 && !mPlayer.hasAura(mBattleShout)) {
			mKeyboard.type('6');
		}
		// Bloodthirst OR Mortal Strike
		else if (mPlayerLevel >= 40 && (!mBot.anyOnCD(mBloodthirst) || !mBot.anyOnCD(mMortalStrike))
				&& rageValue >= 30) {
			mKeyboard.type('4');
		}
		// Execute
		else if (mPlayerLevel >= 24 && u.getHealthFloat() < 0.2f && rageValue >= 15) {
			mKeyboard.type('5');
		}
		// Rend
		else if (mPlayerLevel >= 4 && rageValue >= 10 && !u.hasAura(mRend)) {
			mKeyboard.type('3');
		}
		// Cleave
		else if (mPlayerLevel >= 20 && rageValue >= 20 && attackerAmount > 1) {
			mKeyboard.type('2');
		}
		// Heroic Strike
		else if (rageValue >= 15 && targetDistance <= 5) {
			mKeyboard.type('1');
		}
		// Melee Attack
		else if (targetDistance <= 5) {
			mKeyboard.type('7');
		}
	}

	@Override
	public void pull(Unit u) {
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
			switchActionBar(2);
			mKeyboard.type('0'); // eat bind
			mBot.sleep(1200, 1700); // prevent double eating
			switchActionBar(1);
		}

		if (mEating && mPlayer.getHealthFloat() > 0.9f) {
			// over 90% health, good enough
			mKeyboard.type('w'); // force to be standing
			mEating = false;
		}
	}

	@Override
	public int getPullDistance(Unit u) {
		if (mPlayerLevel >= 4)
			return 20;
		return 4;
	}

	@Override
	public void approaching(Unit u) {
		// Remove Mount
		if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
			switchActionBar(2);
			mKeyboard.type('9');
			switchActionBar(1);
		}
		// Charge
		if (mPlayerLevel >= 4 && u.getDistance() < 25 && !mBot.anyOnCD(mCharge)) {
			switchActionBar(2);
			mKeyboard.type('2');
			switchActionBar(1);
		}
		// Start Auto Attack
		else if (u.getDistance() < 10) {
			mKeyboard.type('7');
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
			mKeyboard.type('9');
			switchActionBar(1);
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

		// Remove Mount
		if (mPlayerLevel >= 40 && !mPlayer.hasAura(mMounts)) {
			switchActionBar(2);
			mKeyboard.type('9');
			switchActionBar(1);
		}
		return false;
	}
}
