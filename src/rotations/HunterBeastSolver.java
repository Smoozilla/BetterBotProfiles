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

public class HunterBeastSolver implements RotationSolver {

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
  int mConcussiveShot = 5116;
  int mMongooseBite[] = { 1495, 14269, 14270, 14271 };
  int mDisengage[] = { 781, 14272, 14273 };
  int mRapidFire = 3045;
  int mFeignDeath = 5384;
  int mIntimidation = 19577;
  int mBestialWrath = 19574;

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

  public HunterBeastSolver(BetterBot bot) {
    mBot = bot;
    mPlayer = bot.getPlayer();
    mPet = mPlayer.getPet();
    mKeyboard = bot.getKeyboard();
    mPlayerLevel = mPlayer.getLevel();
    mActionBar = 1;

    System.out.println("TheCrux's Hunter script started");

    mDrinkPercent = 0.35f; // drink if below 35% mana
    mEatPercent = 0.6f; // eat if below 60% health
    mDrinking = false;
  }

  void switchActionBar(int bar) {
    if (mActionBar != bar) {
      mKeyboard.press(KeyEvent.VK_SHIFT);
      mKeyboard.type("" + bar);
      mBot.sleep(300, 500);
      mKeyboard.release(KeyEvent.VK_SHIFT);
      mActionBar = bar;
    }
  }

  @Override
  public void combat(Unit u) {
    switchActionBar(1);

    // Make sure the pet is attacked and not the player
    if (mPlayerLevel >= 10) {
      List<Unit> attackers = mBot.getAttackers();
      if (attackers.size() > 1) {
        for (Unit a : attackers) {
          if (a.getTarget() == mPlayer.getGUID()) {
            a.target();
            // Intimidation
            if (mPlayerLevel >= 30 && !mBot.anyOnCD(mIntimidation)) {
              switchActionBar(2);
              mKeyboard.type('3');
              return;
            }
            // Just let the pet attack
            else {
              mKeyboard.press(KeyEvent.VK_CONTROL);
              mKeyboard.type('1');
              mKeyboard.release(KeyEvent.VK_CONTROL);
            }
            break;
          }
        }
        // Bestial Wrath
        if (mPlayerLevel >= 40 && !mBot.anyOnCD(mBestialWrath) && !mPet.hasAura(mBestialWrath)
            && !mPlayer.hasAura(mRapidFire)) {
          switchActionBar(2);
          mKeyboard.type('0');
          return;
        }
        // Rapid Fire
        else if (mPlayerLevel >= 26 && !mBot.anyOnCD(mRapidFire) && mPet != null && !mPet.hasAura(mBestialWrath)) {
          switchActionBar(2);
          mKeyboard.type('-');
          return;
        }
      }
    }

    if (mPlayer.isCasting()) {
      return;
    }

    float manaValue = mPlayer.getManaFloat();

    float targetDistance = u.getDistance();
    float targetHealth = u.getHealthFloat();

    // Mend Pet
    if (mPet != null && !mPet.isDead() && mPet.getHealthFloat() <= 0.4f && mPlayer.getHealthFloat() >= 0.5f) {
      switchActionBar(2);
      mKeyboard.type('4');
      switchActionBar(1);
    }

    // Range Attacks
    if (targetDistance > 9) {

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
    else {
      // Mongoose Bite
      //if(mPlayerLevel >= 16 && !mBot.anyOnCD(mMongooseBite)) {
      //  mKeyboard.type('5');
      //}
      // Aspect of the Monkey with dead pet
      if (mPet != null && mPet.isDead() && !mPlayer.hasAura(mAspectOfTheMonkey)) {
        switchActionBar(2);
        mKeyboard.type('8');
        switchActionBar(1);
      }
      // Feign Death
      else if (mPlayerLevel >= 30 && u.getTarget() == mPlayer.getGUID() && mPet != null && !mPet.isDead()
          && !mBot.anyOnCD(mFeignDeath)) {
        mKeyboard.type('6');
      }
      // Disengage
      else if (mPlayerLevel >= 20 && u.getTarget() == mPlayer.getGUID() && mPet != null && !mPet.isDead()
          && !mBot.anyOnCD(mDisengage)) {
        mKeyboard.type('-');
      }
      // Raptor Strike
      else if (!mBot.anyOnCD(mRaptorStrike) && manaValue >= 0.15f) {
        mKeyboard.type('2');
      }
      // Auto Attack
      else {
        mKeyboard.type('7');
      }
    }
  }

  @Override
  public void pull(Unit u) {
    if (isPetAliveAndWell())
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

  boolean isPetAliveAndWell() {

    if (mPlayer.isCasting())
      return false;

    if (mPlayerLevel >= 10) {
      mPet = mPlayer.getPet(); // Make sure the pet is null again after the player died
      // Call Pet
      if (mPet == null) {
        switchActionBar(2);
        mKeyboard.type('5');
        return false;
      }
      // Revive Pet
      else if (mPet.isDead()) {
        switchActionBar(2);
        mKeyboard.type('6');
        return false;
      }
      // Mend Pet
      else if (mPet.getHealthFloat() <= 0.6f) {
        switchActionBar(2);
        mKeyboard.type('4');
        return false;
      }
    }

    switchActionBar(1);
    return true;
  }

  boolean isFullBuffed() {

    if (mPlayerLevel >= 10 && !mPlayer.hasAura(mAspectOfTheHawk)) {
      switchActionBar(2);
      mKeyboard.type('7');
      return false;
    }
    // Aspect of the Money OR Aspect of the Hawk
    else if (mPlayerLevel >= 4 && mPlayerLevel < 10 && !mPlayer.hasAura(mAspectOfTheMonkey)) {
      switchActionBar(2);
      mKeyboard.type('8');
      return false;
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
    return 30;
  }

  long waitFlag = System.currentTimeMillis();

  @Override
  public void approaching(Unit u) {

    long now = System.currentTimeMillis();
    // Slow dooooown!
    if (now - waitFlag > 500) {
      waitFlag = now;
      // Check for buffs
      if (!isFullBuffed()) {
        return;
      } else if (mPet != null && !mPet.isDead()) { // Only attack while walking if the pet is alive
        // Hunter's Mark
        if (mPlayerLevel >= 6 && !u.hasAura(mHuntersMark) && u.getDistance() < 40) {
          mKeyboard.type('8');
          return;
        }
        // Concussive Shot
        else if (mPlayerLevel >= 8 && !u.hasAura(mConcussiveShot) && !mBot.anyOnCD(mConcussiveShot)
            && u.getDistance() < 35) {
          mKeyboard.type('1');
          return;
        }
      }
    }
  }
}
