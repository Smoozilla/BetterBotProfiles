package rotations;

import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Database.Vendor;
import com.betterbot.api.pub.Inventory;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.Vector3f;
import com.betterbot.api.pub.RotationSolver;
import ui.RotationUI;

/**
 *
 * @author TheCrux
 */
public class MageFrostSolver implements RotationSolver, ICommonSettingFunctions {

  BetterBot mBot;
  RotationUI mUI;
  Keyboard mKeyboard;
  Unit mPlayer;
  Inventory mInventory;

  float mDrinkPercentage;
  float mEatPercentage;
  boolean mDrinking;
  boolean mEating;
  int mPlayerLevel;
  int mActionBar;

  // Everything sorted by rank!
  // Spells
  int mFrostNova[] = {122, 865, 6131, 10230};
  int mFireBlast[] = {2136, 2137, 2138, 8412, 8413, 10197, 10199};
  int mCounterspell = 2139;
  int mIceBlock = 11958;

  // Buffs
  int mArcaneIntellect[] = {1459, 1460, 1461, 10156, 10157};
  int mFrostArmor[] = {168, 7300, 7301, 7302, 7320, 10219, 10220};
  int mDampenMagic[] = {604, 8450, 8451, 10173, 10174};
  int mIceBarrier[] = {11426, 13031, 13032, 13033};

  // Drinking Buffs
  int mDrinkingBuffs[] = {430, 431, 432, 1133, 1135, 1137, 10250, 22734, 24355, 29007};
  int mConjuredWater[] = {5350, 2288, 2136, 3772, 8077, 8078, 8079};

  // Eating Buffs
  int mEatingBuffs[] = {433, 434, 435, 1127, 1129, 1131, 2639, 6410, 7737, 24005, 25700, 25886, 28616, 29008, 29073};
  int mConjuredFood[] = {5349, 1113, 1114, 1487, 8075, 8076, 22895};
  int mBuffFood[] = {5004, 5005, 5006, 5007, 10256, 10257, 18229, 18230, 18231, 24800, 24869, 25660};

  int mManaGems[] = {5514, 5513, 8007, 8008};

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
    24252, 17450, 24242, 16084, 18991, 18992, 17229};

  public MageFrostSolver(BetterBot bot) {
    mBot = bot;
    mUI = new RotationUI(this);
    mPlayer = bot.getPlayer();
    mKeyboard = bot.getKeyboard();
    mPlayerLevel = mPlayer.getLevel();
    mInventory = mBot.getInventory();
    mActionBar = 1;

    System.out.println("TheCrux's Frost Mage script started");

    mDrinkPercentage = mUI.getDrinkPercentage();
    mEatPercentage = mUI.getEatPercentage();
    mDrinking = false;
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
    switchActionBar(1);

    // Remove Mount
    if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
      switchActionBar(2);
      mKeyboard.type('-');
      switchActionBar(1);
    }

    // Ice Block
    if (mPlayerLevel >= 30 && mPlayer.getHealthFloat() < 0.1f && !mBot.anyOnCD(mIceBlock)) {
      mKeyboard.type('0');
    }

    // Counterspell
    if (mPlayerLevel >= 24 && u.isCasting() && u.getManaMax() > 0 && !mBot.anyOnCD(mCounterspell)) {
      mKeyboard.type('s'); // stop casting
      mKeyboard.type('6');
    }

    float targetDistance = u.getDistance();
    float manaValue = mPlayer.getManaFloat();

    // Frost Nova
    if (mPlayerLevel >= 4 && targetDistance <= 11 && targetDistance >= 7 && !mBot.anyOnCD(mFrostNova)
            && manaValue >= 0.15f) {
      mKeyboard.type('s'); // stop casting
      mKeyboard.type('3');
    }

    if (mPlayer.isCasting()) {
      return;
    }

    // Use a mana gem
    if (mPlayerLevel >= 28 && manaValue < 0.15f && mInventory.getItemCount(mManaGems) > 0) {
      mKeyboard.type('9');
    }
    // Ice Barrier
    else if (mPlayerLevel >= 40 && !mBot.anyOnCD(mIceBarrier) && !mPlayer.hasAura(mIceBarrier) && manaValue >= 0.2f) {
      mKeyboard.type('5');
    }
    // Fire Blast
    else if (mPlayerLevel >= 4 && !mBot.anyOnCD(mFireBlast) && u.getHealthFloat() < 0.08f) {
      mKeyboard.type('2');
    }
    // Frostbolt or Fireball if below level 4
    else if (manaValue >= 0.1f) {
      mKeyboard.type('4');
    }
    // Wand OR Melee Attack
    else {
      mKeyboard.type('7');
    }
  }

  @Override
  public void pull(Unit u) {
    if (isFullBuffed()) {
      combat(u);
    }
  }

  @Override
  public boolean combatEnd(Unit u) {

    if (!mDrinking && !mEating && !hasEnoughConjuredItems()) {
      return true;
    }

    drinkAndEat();
    if (mDrinking || mEating) {
      return true;
    }

    return !isFullBuffed();
  }

  boolean isFullBuffed() {

    // We're conjuring a mana gem
    if (mPlayer.isCasting()) {
      return true;
    }

    // Arcane Intellect
    if (mPlayerLevel >= 2 && !mPlayer.hasAura(mArcaneIntellect)) {
      switchActionBar(2);
      mKeyboard.type('2');
      return false;
    }

    // Frost/Ice Armor
    if (!mPlayer.hasAura(mFrostArmor)) {
      switchActionBar(2);
      mKeyboard.type('3');
      return false;
    }

    // Dampen Magic
    if (mPlayerLevel >= 12 && !mPlayer.hasAura(mDampenMagic)) {
      switchActionBar(2);
      mKeyboard.type('4');
      return false;
    }

    switchActionBar(1);
    return true;
  }

  long mFoodTimer;

  void drinkAndEat() {

    if (mPlayer.isCasting()) {
      return;
    }

    float playerMana = mPlayer.getManaFloat();
    float playerHealth = mPlayer.getHealthFloat();

    if (playerMana < mDrinkPercentage) {
      mDrinking = true;
    }

    if (playerHealth < mEatPercentage) {
      if (!mEating) {
        mFoodTimer = 0;
      }
      mEating = true;
    }

    // Drink
    if (mDrinking && !mPlayer.hasAura(mDrinkingBuffs) && playerMana < mDrinkPercentage + 0.05f) {
      switchActionBar(2);
      mKeyboard.type('0');
      mBot.sleep(1200, 1700); // prevent double drinking
    }

    // Eat
    if (mEating && !mPlayer.hasAura(mEatingBuffs) && !mPlayer.hasAura(mBuffFood) && playerHealth < mEatPercentage + 0.05f) {
      switchActionBar(2);
      mKeyboard.type('9');
      mBot.sleep(1200, 1700); // prevent double eating
    }

    if (mDrinking && playerMana > 0.9f) {
      mDrinking = false; // over 90% mana, good enough
      if (!mEating) {
        mKeyboard.type('w'); // force to be standing
        switchActionBar(1);
      }
    }

    if (mEating && playerHealth > 0.9f) {
      if (!mDrinking && (!mPlayer.hasAura(mBuffFood) || ((System.currentTimeMillis() - mFoodTimer) > 10500))) {
        mKeyboard.type('w'); // force to be standing
        mEating = false; // over 90% health, good enough
        switchActionBar(1);
      }
    }
  }

  boolean hasEnoughConjuredItems() {

    if (mPlayer.isCasting()) {
      return false;
    }

    // Conjure some water
    if (mPlayerLevel >= 4 && mInventory.getItemCount(mConjuredWater) <= 1) {
      switchActionBar(2);
      mKeyboard.type('8');
      return false;
    }

    // Conjure some food
    if (mPlayerLevel >= 6 && mInventory.getItemCount(mConjuredFood) <= 1) {
      switchActionBar(2);
      mKeyboard.type('7');
      return false;
    }

    // Conjure a mana gem
    if (mPlayerLevel >= 28 && mInventory.getItemCount(mManaGems) == 0) {
      switchActionBar(2);
      mKeyboard.type('6');
      return false;
    }
    switchActionBar(1);
    return true;
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

      // Remove Mount
      if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
        switchActionBar(2);
        mKeyboard.type('-');
        switchActionBar(1);
      }
      waitFlag = now;
      isFullBuffed();
      // Ice Barrier
      if (mPlayerLevel >= 40 && u.getDistance() < 40 && !mBot.anyOnCD(mIceBarrier) && !mPlayer.hasAura(mIceBarrier)) {
        mKeyboard.type('5');
      }
    }
  }

  @Override
  public boolean afterResurrect() {
    if (mPlayer.inCombat()) {
      return false;
    }
    return combatEnd(null);
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
      mKeyboard.type('-');
      switchActionBar(1);
      return true;
    }
    return false;
  }

  @Override
  public JComponent getUI() {
    return mUI;
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
    if (mPlayerLevel >= 40 && !mPlayer.hasAura(mMounts) && mUI.shouldUseMount()) {
      switchActionBar(2);
      mKeyboard.type('-');
      switchActionBar(1);
      return true;
    }
    return false;
  }

  /**
   * Called from the UI to set new drink percentages
   *
   * @param value The new drinkpercentage 0.0-1.0
   */
  @Override
  public void setDrinkPercentage(float value) {
    mDrinkPercentage = value;
  }

  /**
   * Called from the UI to set new eat percentages
   *
   * @param value The new eatpercentage 0.0-1.0
   */
  @Override
  public void setEatPercentage(float value) {
    mEatPercentage = value;
  }
}
