package rotations;

import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Database.Vendor;
import com.betterbot.api.pub.Frame;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Movement;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.Vector3f;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import com.betterbot.api.pub.RotationSolver;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import ui.RotationUI;

/**
 *
 * @author TheCrux
 */
public class RogueCombatSolver implements RotationSolver, ICommonSettingFunctions {

  BetterBot mBot;
  RotationUI mUI;
  Movement mMove;
  Keyboard mKeyboard;
  Unit mPlayer;

  JCheckBox jUsePoison;
  JLabel jPoisonLabel;
  JComboBox jPoisonSelect;

  float mEatPercent;
  boolean mEating;
  int mPlayerLevel;
  int mActionBar;

  // Everything sorted by rank!
  // Spells
  int mKick[] = {1766, 1767, 1768, 1769};
  int mVanish[] = {1856, 1857};

  // Buffs
  int mStealth[] = {1784, 1785, 1786, 1787};
  int mEvasion = 5277;
  int mSliceAndDice[] = {5171, 6774};
  int mBladeFlurry = 13877;
  int mAdrenalineRush = 13750;

  // Poisons
  String mPoisonNames[] = {
    "Deadly Poison", "Deadly Poison II", "Deadly Poison III", "Deadly Poison IV", "Deadly Poison V",
    "Instant Poison", "Instant Poison II", "Instant Poison III", "Instant Poison IV", "Instant Poison V", "Instant Poison VI",
    "Mind-numbing Poison", "Mind-numbing Poison II", "Mind-numbing Poison III",
    "Wound Poison", "Wound Poison II", "Wound Poison III", "Wound Poison IV"
  };

  // Eating Buffs
  int mEatingBuffs[] = {433, 434, 435, 1127, 1129, 1131, 2639, 6410, 7737, 24005, 25700, 25886, 28616, 29008, 29073};
  int mBuffFood[] = {5004, 5005, 5006, 5007, 10256, 10257, 18229, 18230, 18231, 24800, 24869, 25660};

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

  public RogueCombatSolver(BetterBot bot) {
    mBot = bot;
    mUI = new RotationUI(this);
    mMove = mBot.getMovement();
    mPlayer = bot.getPlayer();
    mKeyboard = bot.getKeyboard();
    mPlayerLevel = mPlayer.getLevel();
    mActionBar = 1;

    // ====== UI Settings ======
    // Drinking is irrelevenat since Rogues have energy
    mUI.removeDrinkingSettings();

    jUsePoison = new JCheckBox();
    jUsePoison.setText("Use Poison");
    jUsePoison.setBounds(12, 110, 150, 30);
    jUsePoison.setSelected(mUI.getBoolProp("poison"));

    jUsePoison.addChangeListener((ChangeEvent e) -> {
      mUI.setProp("poison", jUsePoison.isSelected());
      jPoisonSelect.setEnabled(jUsePoison.isSelected());
    });
    mUI.add(jUsePoison);

    jPoisonLabel = new JLabel();
    jPoisonLabel.setText("Poison name");
    jPoisonLabel.setBounds(12, 135, 150, 30);
    mUI.add(jPoisonLabel);

    jPoisonSelect = new JComboBox<>(mPoisonNames);
    jPoisonSelect.setBounds(12, 160, 200, 30);
    jPoisonSelect.setSelectedIndex(mUI.getIndexProp("poisonName"));
    jPoisonSelect.addActionListener((ActionEvent e) -> {
      mUI.setProp("poisonName", jPoisonSelect.getSelectedIndex());
    });
    jPoisonSelect.setEnabled(jUsePoison.isSelected());
    mUI.add(jPoisonSelect);

    mUI.revalidate();
    mUI.repaint();
    // =========================

    System.out.println("TheCrux's Combat Rogue script started");

    mEatPercent = mUI.getEatPercentage();
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
    // Remove Mount
    if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
      switchActionBar(2);
      mKeyboard.type('0');
      switchActionBar(1);
    }

    int energyValue = mPlayer.getEnergy();
    int comboPoints = mBot.getComboPoints();

    float targetDistance = u.getDistance();
    float targetHealth = u.getHealthFloat();
    int attackerAmount = mBot.getAttackers().size();

    // Vanish
    if (mPlayerLevel >= 22 && mPlayer.getHealthFloat() < 0.1f && (targetHealth > 0.2f || attackerAmount > 1)
            && mPlayer.inCombat() && mBot.getInventory().getItemCount(5140) > 0 && !mBot.anyOnCD(mVanish) && !mMove.isMoving()) {
      mKeyboard.type('5');
      mMove.walkPath(mMove.getBacktrackPath(), 5);
      return;
    }

    if (targetDistance <= 5) {
      // Kick
      if (mPlayerLevel >= 12 && u.isCasting() && u.getManaMax() > 0 && !mBot.anyOnCD(mKick)) {
        mKeyboard.type('6');
      }

      // Evasion
      if (mPlayerLevel >= 8 && (mPlayer.getHealthFloat() <= 0.5f || attackerAmount > 1) && !mBot.anyOnCD(mEvasion)) {
        mKeyboard.type('8');
      }

      // Blade Flurry
      if (mPlayerLevel >= 30 && attackerAmount > 1 && energyValue >= 25 && !mBot.anyOnCD(mBladeFlurry)) {
        switchActionBar(2);
        mKeyboard.type('2');
        switchActionBar(1);
      }
      // Adrenaline Rush
      else if (mPlayerLevel >= 40 && attackerAmount > 1 && !mBot.anyOnCD(mAdrenalineRush)) {
        switchActionBar(2);
        mKeyboard.type('3');
        switchActionBar(1);
      }
      // Slice and Dice
      else if (mPlayerLevel >= 10 && energyValue >= 25 && !mPlayer.hasAura(mSliceAndDice) && comboPoints > 0) {
        mKeyboard.type('3');
      }
      // Eviscerate		
      else if (energyValue >= 35 && ((comboPoints == 1 && targetHealth <= 0.1f)
              || (comboPoints == 2 && targetHealth <= 0.15f) || (comboPoints == 3 && targetHealth <= 0.2f)
              || (comboPoints == 4 && targetHealth <= 0.25f) || comboPoints == 5)) {
        mKeyboard.type('2');
      }
      // Sinister Strike OR Hemorrhage
      else if (energyValue >= 45 || (mPlayerLevel >= 11 && energyValue >= 40)) {
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
    // Try to use Cheap Shot
    if (mPlayerLevel >= 26) {
      mKeyboard.type('1');
    }
    else if (mPlayerLevel >= 14) {
      // Try to use Garrote
      mKeyboard.type("3");
    }
    combat(u);
  }

  @Override
  public boolean combatEnd(Unit u) {
    if (mPlayerLevel >= 20 && jUsePoison != null && jUsePoison.isSelected()) {
      checkPoisons();
    }
    eat();
    return mEating;
  }

  long mFoodTimer;

  void eat() {

    if (mPlayer.getHealthFloat() < mEatPercent) {
      if (!mEating) {
        mFoodTimer = 0;
      }
      mEating = true;
    }

    if (mPlayer.isCasting()) {
      return;
    }

    if (mEating && !mPlayer.hasAura(mEatingBuffs) && !mPlayer.hasAura(mBuffFood)
            && mPlayer.getHealthFloat() < mEatPercent + 0.05f) {
      mKeyboard.type('0'); // eat bind
      if (mFoodTimer == 0) {
        mFoodTimer = System.currentTimeMillis();
      }
      mBot.sleep(1200, 1700); // prevent double eating
    }

    // over 90% health, good enough
    if (mEating && mPlayer.getHealthFloat() > 0.9f) {
      // But only if we didn't ate bufffood or ate for 10.5 sec.
      if (!mPlayer.hasAura(mBuffFood) || ((System.currentTimeMillis() - mFoodTimer) > 10500)) {
        mKeyboard.type('w'); // force to be standing
        mEating = false;
      }
    }
  }

  long mLastPoisonCheck = 0;

  public void applyPoison(String tooltipData, String slot) {
    String poisonName = (String) jPoisonSelect.getSelectedItem();

    boolean apply = false;
    if (tooltipData.contains(poisonName)) {
      String timeData = tooltipData.split(poisonName)[1].split("\n")[0];
      String[] bits = timeData.split("\\(");
      long time = 0;
      int charges = -1;

      if (bits.length == 2) {
        // no charges
        if (bits[1].contains("min")) {
          time = Integer.parseInt(bits[1].split("\\ min")[0]);
        }
      }
      else {
        // charges
        if (bits[1].contains("min")) {
          time = Integer.parseInt(bits[1].split("\\ min")[0]) * 60000;
        }
        charges = Integer.parseInt(bits[2].split("\\ Charges")[0]);
      }
      System.out.println("Poison time remaining: " + time + "ms, " + charges + " charges");
      if (charges != -1 && charges < 3) {
        apply = true;
      }

      if (time < 120000) {
        apply = true;
      }
    }
    else {
      // No poison -> apply some
      apply = true;
    }

    if (apply) {
      mBot.getInventory().rightClickItem(poisonName);
      mBot.getFrame(slot).leftClick();
      mBot.sleep(400, 500);
      Frame yesButton = mBot.getFrame("StaticPopup1Button1");
      if (yesButton != null && yesButton.isShowing()) {
        yesButton.leftClick();
        mBot.sleep(400, 500);
      }
      while (mPlayer.isCasting()) {
        mBot.sleep(100, 100);
      }
    }
  }

  public void checkPoisons() {
    long now = System.currentTimeMillis();
    if (now - mLastPoisonCheck > 60000) {
      // dont check poisons more than once a minute
      mLastPoisonCheck = now;
      String poisonName = (String) jPoisonSelect.getSelectedItem();

      // No poison left
      if (mBot.getInventory().getItemCount(poisonName) == 0) {
        System.out.println("No " + poisonName + " left.");
        return;
      }

      mBot.focusGame();
      Frame cf = mBot.getFrame("CharacterFrame");
      if (cf == null || !cf.isShowing()) {
        mBot.getFrame("CharacterMicroButton").leftClick();
        while (cf == null || !cf.isShowing() && !mPlayer.inCombat()) {
          mBot.sleep(40, 80); // todo add more checks to this loop so it doesnt get stuck
        }
      }
      if (cf != null && cf.isShowing()) {
        mBot.getFrame("CharacterMainHandSlot").mouseOver();
        mBot.sleep(500, 500); // todo change sleep
        String toolTip = mBot.getTooltip();
        if (toolTip != null && toolTip.contains("Durability")) {
          // there is a weapon in slot 1
          applyPoison(toolTip, "CharacterMainHandSlot");
        }

        mBot.getFrame("CharacterSecondaryHandSlot").mouseOver();
        mBot.sleep(500, 500);
        toolTip = mBot.getTooltip();
        if (toolTip != null && toolTip.contains("Durability")) {
          // there is a weapon in slot 2
          applyPoison(toolTip, "CharacterSecondaryHandSlot");
        }
        System.out.println("Closing character frame");
        mBot.getFrame("CharacterFrameCloseButton").leftClick(); // close character frame
      }
    }
  }

  @Override
  public int getPullDistance(Unit u) {
    return 5;
  }

  long mWaitFlag = System.currentTimeMillis();

  @Override
  public void approaching(Unit u) {
    long now = System.currentTimeMillis();
    // Slow dooooown!
    if (now - mWaitFlag > 500 && u.getDistance() < 25) {
      mWaitFlag = now;

      // Remove Mount
      if (mPlayerLevel >= 40 && mPlayer.hasAura(mMounts)) {
        switchActionBar(2);
        mKeyboard.type('0');
        switchActionBar(1);
        return;
      }

      // Stealth
      if (mPlayerLevel >= 2 && !mPlayer.hasAura(mStealth) && !mBot.anyOnCD(mStealth)) {
        mKeyboard.type('9');
      }
    }
  }

  @Override
  public boolean afterResurrect() {
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
      mKeyboard.type('0');
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
    if (mPlayer.isCasting() || mEating) {
      return true;
    }

    // Mount
    if (mPlayerLevel >= 40 && !mPlayer.hasAura(mMounts) && mUI.shouldUseMount()) {
      switchActionBar(2);
      mKeyboard.type('0');
      switchActionBar(1);
      return true;
    }
    return false;
  }

  @Override
  public void setDrinkPercentage(float value) {
  }

  @Override
  public void setEatPercentage(float value) {
    mEatPercent = value;
  }
}
