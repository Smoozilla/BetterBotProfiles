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

public class PriestSolver implements RotationSolver {

  BetterBot mBot;
  Keyboard mKeyboard;
  Unit mPlayer;

  float mDrinkPercent;
  boolean mDrinking;
  int mPlayerLevel;

  // Everything sorted by rank!
  // Spells
  int mMindBlast[] = { 8092, 8102, 8103, 8104, 8105, 8106, 10945, 10946, 10947 };

  // Buffs
  int mPWFortitude[] = { 1243, 1244, 1245, 2791, 10937, 10938 };
  int mPWShield[] = { 17, 592, 600, 3747, 6065, 6066, 10898, 10899, 10900, 10901 };
  int mInnerFire[] = { 588, 7128, 602, 1006, 10951, 10952 };

  // Debuffs
  int mWeakenedSoul = 6788;
  int mSWPain[] = { 589, 594, 970, 992, 2767, 10892, 10893, 10894 };

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

  public PriestSolver(BetterBot bot) {
    mBot = bot;
    mPlayer = bot.getPlayer();
    mKeyboard = bot.getKeyboard();
    mPlayerLevel = mPlayer.getLevel();

    System.out.println("TheCrux's Priest script started");

    mDrinkPercent = 0.4f; // drink if below 40% mana
    mDrinking = false;
  }

  @Override
  public void combat(Unit u) {

    // Remove Mount
    if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
      mKeyboard.type('-');
    }

    if (mPlayer.isCasting()) {
      return;
    }

    // Player at good health
    if (mPlayer.getHealthFloat() > 0.60f) {

      float manaValue = mPlayer.getManaFloat();

      float targetHealth = u.getHealthFloat();

      // Power Word: Shield
      if (mPlayerLevel >= 6 && !mBot.anyOnCD(mPWShield) && !mPlayer.hasAura(mPWShield)
          && !mPlayer.hasAura(mWeakenedSoul) && manaValue > 0.2f) {
        mKeyboard.type('6');
      }
      // Shadow Word: Pain
      else if (mPlayerLevel >= 4 && !u.hasAura(mSWPain)) {
        mKeyboard.type('2');
      }
      // Mind Blast
      else if (mPlayerLevel >= 10 && !mBot.anyOnCD(mMindBlast) && manaValue >= 0.2f && targetHealth > 0.15f) {
        mKeyboard.type('5');
      }
      // Smite
      else if (manaValue > 0.1f) {
        mKeyboard.type('4');
      }
      // Wand
      else {
        mKeyboard.type('1');
      }
    }
    // Player below 60% Health
    else {
      // Lesser Heal
      if (mPlayer.getManaFloat() > 0.2f) {
        mKeyboard.type('3');
      }
    }
  }

  @Override
  public void pull(Unit u) {

    // Pull with Mind Blast
    if (mPlayerLevel >= 10) {
      mKeyboard.type('5');
    }
    // Pull with Smite
    else {
      mKeyboard.type('4');
    }

    combat(u);
  }

  @Override
  public boolean combatEnd(Unit u) {

    drink();

    if (!isFullBuffed())
      return true;

    return mDrinking;
  }

  private boolean isFullBuffed() {

    // Power Word: Fortitude
    if (mPlayerLevel >= 2 && !mPlayer.hasAura(mPWFortitude)) {
      mKeyboard.type('9');
      return false;
    }

    // Power Word: Shield
    if (mPlayerLevel >= 6 && !mBot.anyOnCD(mPWShield) && !mPlayer.hasAura(mPWShield)
        && !mPlayer.hasAura(mWeakenedSoul)) {
      mKeyboard.type('6');
      return false;
    }

    // Inner Fire
    if (mPlayerLevel >= 12 && !mPlayer.hasAura(mInnerFire)) {
      mKeyboard.type('8');
    }
    return true;
  }

  private void drink() {

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
      mDrinking = false;
    }
  }

  @Override
  public int getPullDistance(Unit u) {
    return 30;
  }

  @Override
  public void approaching(Unit u) {
    // Remove Mount
    if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
      mKeyboard.type('-');
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
      mKeyboard.type('-');
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
      mKeyboard.type('-');
      return true;
    }
    return false;
  }
}
