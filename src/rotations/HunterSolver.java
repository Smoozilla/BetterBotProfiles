package rotations;

import java.awt.event.KeyEvent;
import java.util.List;

import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.RotationSolver;

/**
 *
 * @author TheCrux
 */

public class HunterSolver implements RotationSolver {

  BetterBot mBot;
  Keyboard mKeyboard;
  Unit mPlayer;
  Unit mPet;
  int mActionBar;

  float mDrinkPercent;
  float mEatPercent;
  boolean mDrinking;
  boolean mEating;
  int mPlayerLevel;

  // Everything sorted by rank!
  // Spells
  int mRaptorStrike[] = { 2973, 14260, 14261, 14262, 14263, 14264, 14265, 14266 };
  int mArcaneShot[] = { 3044, 14281, 14282, 14283, 14284, 14285, 14286, 14287 };
  int mMongooseBite[] = { 1495, 14269, 14270, 14271 };

  // Buffs
  int mAspectOfTheMonkey = 13163;
  int mAspectOfTheHawk[] = { 13165, 14318, 14319, 14320, 14321, 14322, 25296 };

  // Debuffs
  int mSerpentString[] = { 1978, 13549, 13550, 13551, 13552, 13553, 13554, 13555, 25295 };
  int mHuntersMark[] = { 1130, 14323, 14324, 14325 };

  // Drinking Buffs
  int mDrinkingBuffs[] = { 430, 431, 432, 1133, 1135, 1137, 10250, 22734, 24355, 29007 };

  // Eating Buffs
  int mEatingBuffs[] = { 433, 434, 1127, 1129, 1131, 25700, 25886, 28616, 29008, 29073 };

  public HunterSolver(BetterBot bot) {
    mBot = bot;
    mPlayer = bot.getPlayer();
    mKeyboard = bot.getKeyboard();
    mPlayerLevel = mPlayer.getLevel();
    mActionBar = 1;

    System.out.println("TheCrux's Hunter script started");

    mDrinkPercent = 0.35f; // drink if below 35% mana
    mEatPercent = 0.6f; // eat if below 60% health
    mDrinking = false;
  }

  public void switchActionBar(int bar) {
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

    List<Unit> attackers = mBot.getAttackers();
    // Make sure the pet is attacked and not the player
    for (Unit a : attackers) {
      if (a.getTarget() == mPlayer.getGUID()) {
        a.target();
        mKeyboard.press(KeyEvent.VK_CONTROL);
        mKeyboard.type('1');
        mKeyboard.release(KeyEvent.VK_CONTROL);
        break;
      }
    }

    if (mPlayer.isCasting()) {
      return;
    }

    float manaValue = mPlayer.getManaFloat();

    float targetDistance = u.getDistance();
    float targetHealth = u.getHealthFloat();

    // Mend Pet
    if (mPet.getHealthFloat() <= 0.4f && mPlayer.getHealthFloat() >= 0.5f) {
      switchActionBar(2);
      mKeyboard.type('4');
      switchActionBar(1);
    }

    // Range Attacks
    if (targetDistance > 8) {

      // Hunter's Mark
      if (mPlayerLevel >= 6 && !u.hasAura(mHuntersMark) && targetHealth >= 0.2f && manaValue >= 0.1f) {
        mKeyboard.type('8');
      }
      // Serpent Sting
      else if (mPlayerLevel >= 4 && !u.hasAura(mSerpentString) && targetHealth >= 0.15f && manaValue >= 0.15f) {
        mKeyboard.type('3');
      }
      // Arcane Shot
      else if (mPlayerLevel >= 6 && !mBot.anyOnCD(mArcaneShot) && manaValue >= 0.2f) {
        mKeyboard.type('4');
      }
      // Auto Shot
      else {
        mKeyboard.type('7');
      }
    }
    // Melee Attacks
    else if (manaValue >= 0.15f) {
      /*
       * // Mongoose Bite if(mPlayerLevel >= 16 && !mBot.anyOnCD(mMongooseBite)) {
       * mKeyboard.type('5'); } // Raptor Strike else
       */if (!mBot.anyOnCD(mRaptorStrike)) {
        mKeyboard.type('2');
      }
    }
    // Auto Attack
    else {
      mKeyboard.type('7');

    }
  }

  @Override
  public void pull(Unit u) {
    combat(u);
  }

  @Override
  public boolean combatEnd(Unit u) {

    drinkAndEat();
    if (mDrinking || mEating)
      return true;

    if (!isFullBuffed())
      return true;

    return false;
  }

  boolean isFullBuffed() {

    if (mPlayer.isCasting())
      return false;

    // Aspect of the Money OR Aspect of the Hawk
    if (mPlayerLevel >= 4 && !mPlayer.hasAura(mAspectOfTheMonkey) && !mPlayer.hasAura(mAspectOfTheHawk)) {
      switchActionBar(2);
      mKeyboard.type('7');
      return false;
    }

    if (mPlayerLevel >= 10) {

      // Call Pet
      if (mPet == null) {
        switchActionBar(2);
        mKeyboard.type('5');
        mPet = mPlayer.getPet();
        return false;
      }
      // Revive Pet
      else if (mPet.isDead()) {
        switchActionBar(2);
        mKeyboard.type('6');
        return false;
      }
    }

    switchActionBar(1);
    return true;
  }

  void drinkAndEat() {

    if (mPlayer.getManaFloat() < mDrinkPercent)
      mDrinking = true;

    if (mPlayer.getHealthFloat() < mEatPercent)
      mEating = true;

    // Drink
    if (mDrinking && !mPlayer.hasAura(mDrinkingBuffs) && mPlayer.getManaFloat() < mDrinkPercent + 0.05f) {
      mKeyboard.type('0');
      mBot.sleep(1200, 1700); // prevent double drinking
    }

    // Eat
    if (mEating && !mPlayer.hasAura(mEatingBuffs) && mPlayer.getHealthFloat() < mEatPercent + 0.05f) {
      mKeyboard.type('9');
      mBot.sleep(1200, 1700); // prevent double eating
    }

    if (mDrinking && mPlayer.getManaFloat() > 0.9f) {
      mDrinking = false; // over 90% mana, good enough
      if (!mEating)
        mKeyboard.type('w'); // force to be standing
    }

    if (mEating && mPlayer.getHealthFloat() > 0.9f) {
      mEating = false; // over 90% health, good enough
      if (!mDrinking)
        mKeyboard.type('w'); // force to be standing
    }
  }

  @Override
  public int getPullDistance(Unit u) {
    return 35;
  }

  @Override
  public void approaching(Unit u) {    
  }
}
