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
 * This rotation is for the Elemental spec.
 * @author TheCrux
 */

public class ShamanElementalSolver implements RotationSolver {

  BetterBot mBot;
  Keyboard mKeyboard;
  Unit mPlayer;
  int mActionBar;

  float mDrinkPercent;
  boolean mDrinking;
  boolean mWeaponBuff;
  int mPlayerLevel;

  // Everything sorted by rank!
  // Spells
  int mFlameShock[] = { 8050, 8052, 8053, 10447, 10448, 29228 };
  int mEarthShock[] = { 8042, 8044, 8045, 8046, 10412, 10413, 10414 };

  // Buffs
  int mLightningShield[] = { 324, 325, 905, 945, 8134, 10431, 10432 };
  int mGhostWolf = 2645;

  // Totem Buffs
  int mStoneskinTotem[] = { 8071, 8154, 8155, 10406, 10407, 10408 };
  int mHealingStreamTotem[] = { 5672, 6371, 6372, 10460, 10461 };

  // Totem Names
  String mSearingTotem[] = { "Searing Totem", "Searing Totem II", "Searing Totem III", "Searing Totem IV",
      "Searing Totem V", "Searing Totem VI" };

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

  public ShamanElementalSolver(BetterBot bot) {
    mBot = bot;
    mPlayer = bot.getPlayer();
    mKeyboard = bot.getKeyboard();
    mPlayerLevel = mPlayer.getLevel();
    mActionBar = 1;

    System.out.println("TheCrux's Elemental Shaman script started");

    mDrinkPercent = 0.4f; // drink if below 40% mana
    mDrinking = false;
    mWeaponBuff = false;
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

    // Remove Ghost Wolf
    if (mPlayerLevel >= 20 && mPlayerLevel < 40 && mPlayer.hasAura(mGhostWolf)) {
      switchActionBar(2);
      mKeyboard.type('9');
      switchActionBar(1);
      return;
    }
    // Remove Mount
    else if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
      switchActionBar(2);
      mKeyboard.type('0');
      switchActionBar(1);
    }

    if (mPlayer.isCasting()) {
      return;
    }

    // Player at good health
    if (mPlayer.getHealthFloat() > 0.60f) {

      float manaValue = mPlayer.getManaFloat();

      float targetDistance = u.getDistance();
      float targetHealth = u.getHealthFloat();

      if (mPlayerLevel >= 4 && targetDistance < 20) {
        // Flame Shock
        if (mPlayerLevel >= 10 && targetHealth >= 0.3f) {
          if (!u.hasAura(mFlameShock) && !mBot.anyOnCD(mFlameShock) && !mBot.anyOnCD(mEarthShock)
              && targetHealth >= 0.15f && manaValue >= 0.15f) {
            mKeyboard.type('2');
          }
          // Searing Totem
          else if (mBot.getNPCs(mSearingTotem).size() == 0) {
            mKeyboard.type('9');
          }
          // Earth Shock
          else if (!mBot.anyOnCD(mEarthShock) && targetHealth >= 0.15f && manaValue >= 0.15f) {
            mKeyboard.type('1');
          }
        }
        // Earth Shock
        else if (!mBot.anyOnCD(mEarthShock) && targetHealth >= 0.15f && manaValue >= 0.15f) {
          mKeyboard.type('1');
        }
      }

      // Chain Lightning
      if (mBot.getAttackers().size() > 2 && manaValue >= 0.2f) {
        mKeyboard.type('5');
      }
      // Lightning Bolt
      else if (manaValue >= 0.08f) {
        mKeyboard.type('4');
      }
      // Auto Attack
      else {
        mKeyboard.type('7');
      }
    }
    // Player below 60% Health
    else {
      // Healing Wave
      if (mPlayer.getManaFloat() > 0.2f) {
        mKeyboard.type('3');
      }
    }
  }

  @Override
  public void pull(Unit u) {
    if (mPlayer.getManaFloat() < mDrinkPercent) {
      drink();
    }
    // Heal yourself
    else if (mPlayer.getHealthFloat() < 0.85f) {
      mKeyboard.type('3');
    }
    // Searing Totem
    else if (mBot.getNPCs(mSearingTotem).size() == 0) {
      mKeyboard.type('9');
    }
    // Buff up
    else if (isFullBuffed(u.getDistance())) {
      combat(u);
    }
  }

  @Override
  public boolean combatEnd(Unit u) {

    drink();
    return mDrinking;
  }

  boolean isFullBuffed(float distToTarget) {

    // Weapon buff
    if (!mWeaponBuff) {
      mKeyboard.type('-');
      mWeaponBuff = true;
      return false;
    }
    // Lightning Shield
    if (mPlayerLevel >= 8 && !mPlayer.hasAura(mLightningShield) && mPlayer.getManaFloat() > 0.15f) {
      mKeyboard.type('6');
      return false;
    }
    // Stoneskin Totem
    if (mPlayerLevel >= 4 && distToTarget < 20 && !mPlayer.hasAura(mStoneskinTotem) && mPlayer.getManaFloat() > 0.15f) {
      mKeyboard.type('8');
      return false;
    }

    return true;
  }

  void drink() {
    float manaValue = mPlayer.getManaFloat();

    if (manaValue < mDrinkPercent)
      mDrinking = true;

    if (mDrinking && !mPlayer.hasAura(mDrinkingBuffs)) {
      mKeyboard.type('0'); // drink bind
      mBot.sleep(1200, 1700); // prevent double drinking on high latency
    }

    if (mDrinking && manaValue > 0.9f) {
      // over 90% mana, good enough
      mKeyboard.type('w'); // force to be standing
      mWeaponBuff = false; // Refresh the weapon buff after every time you drink
      mDrinking = false;
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

      // Remove Ghost Wolf
      if (mPlayerLevel >= 20 && mPlayerLevel < 40 && mPlayer.hasAura(mGhostWolf)) {
        switchActionBar(2);
        mKeyboard.type('0');
        switchActionBar(1);
      }
      // Remove Mount
      else if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
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

    if (mPlayer.inCombat())
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

    // Remove Ghost Wolf
    if (mPlayerLevel >= 20 && mPlayerLevel < 40 && mPlayer.hasAura(mGhostWolf)) {
      switchActionBar(2);
      mKeyboard.type('0');
      switchActionBar(1);
      return true;
    }
    // Remove Mount
    else if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
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

    // Ghost Wolf
    if (mPlayerLevel >= 20 && mPlayerLevel < 40 && !mPlayer.hasAura(mGhostWolf)) {
      switchActionBar(2);
      mKeyboard.type('9');
      switchActionBar(1);
      return true;
    }
    // Mount
    else if (mPlayerLevel >= 40 && !mPlayer.hasAura(mMounts)) {
      switchActionBar(2);
      mKeyboard.type('0');
      switchActionBar(1);
      return true;
    }
    return false;
  }
}
