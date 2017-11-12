package rotations;

import com.betterbot.api.pub.BetterBot;
import com.betterbot.api.pub.Database.Vendor;
import com.betterbot.api.pub.Keyboard;
import com.betterbot.api.pub.Movement;
import com.betterbot.api.pub.Unit;
import com.betterbot.api.pub.Vector3f;
import com.betterbot.api.pub.RotationSolver;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ui.RotationUI;

/**
 *
 * @author TheCrux
 */
public class HunterSolver implements RotationSolver, ICommonSettingFunctions {

  BetterBot mBot;
  RotationUI mUI;
  Keyboard mKeyboard;
  Movement mMovement;
  Unit mPlayer;
  Unit mPet;
  int mActionBar;

  JLabel jVendor;
  JCheckBox jBuyAmmo;
  JLabel jAmmoAmountLabel;
  JTextField jAmmoAmount;
  JLabel jAmmoLabel;
  JComboBox<String> jAmmoName;
  JRadioButton jBow;
  JRadioButton jGun;
  JCheckBox jUseSerpentSting;

  float mDrinkPercentage;
  float mEatPercentage;
  boolean mDrinking;
  boolean mEating;
  int mPlayerLevel;

  // Everything sorted by rank!
  // Spells
  int mRaptorStrike[] = {2973, 14260, 14261, 14262, 14263, 14264, 14265, 14266};
  int mArcaneShot[] = {3044, 14281, 14282, 14283, 14284, 14285, 14286, 14287};
  int mConcussiveShot = 5116;
  int mMongooseBite[] = {1495, 14269, 14270, 14271};
  int mDisengage[] = {781, 14272, 14273};
  int mRapidFire = 3045;
  int mFeignDeath = 5384;

  // Buffs
  int mAspectOfTheMonkey = 13163;
  int mAspectOfTheHawk[] = {13165, 14318, 14319, 14320, 14321, 14322, 25296};
  int mAspectOfTheCheetah = 5118;

  // Debuffs
  int mSerpentString[] = {1978, 13549, 13550, 13551, 13552, 13553, 13554, 13555, 25295};
  int mHuntersMark[] = {1130, 14323, 14324, 14325};

  // Drinking Buffs
  int mDrinkingBuffs[] = {430, 431, 432, 1133, 1135, 1137, 10250, 22734, 24355, 29007};

  // Eating Buffs
  int mEatingBuffs[] = {433, 434, 435, 1127, 1129, 1131, 2639, 6410, 7737, 24005, 25700, 25886, 28616, 29008, 29073};
  int mBuffFood[] = {5004, 5005, 5006, 5007, 10256, 10257, 18229, 18230, 18231, 24800, 24869, 25660};

  // Ammo
  String mArrows[] = {"Rough Arrow", "Sharp Arrow", "Razor Arrow", "Jagged Arrow"};
  String mBullets[] = {"Light Shot", "Heavy Shot", "Solid Shot", "Accurate Slugs"};

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

  public HunterSolver(BetterBot bot) {
    mBot = bot;
    mUI = new RotationUI(this);
    mKeyboard = bot.getKeyboard();
    mMovement = bot.getMovement();
    mPlayer = bot.getPlayer();
    mPet = mPlayer.getPet();
    mPlayerLevel = mPlayer.getLevel();
    mActionBar = 1;

    // ====== UI Settings ======
    jVendor = new JLabel();
    jVendor.setText("Vendor settings");
    jVendor.setBounds(250, 30, 120, 30);
    mUI.add(jVendor);

    // Should vendor
    jBuyAmmo = new JCheckBox();
    jBuyAmmo.setText("Buy ammo");
    jBuyAmmo.setBounds(250, 55, 120, 30);
    jBuyAmmo.setSelected(mUI.getBoolProp("buyAmmo"));
    jBuyAmmo.addActionListener((ActionEvent e) -> {
      boolean buyAmmo = jBuyAmmo.isSelected();
      mUI.setProp("buyAmmo", buyAmmo);
      jBow.setEnabled(buyAmmo);
      jGun.setEnabled(buyAmmo);
      jAmmoName.setEnabled(buyAmmo);
      jAmmoAmount.setEnabled(buyAmmo);
    });
    mUI.add(jBuyAmmo);

    // Maybe not necessary
    // Using Bow or Gun
    jBow = new JRadioButton();
    jBow.setText("(Cross-)Bow");
    jBow.setBounds(250, 85, 120, 30);
    jBow.setEnabled(jBuyAmmo.isSelected());
    jBow.addChangeListener((ChangeEvent e) -> {
      boolean useBow = jBow.isSelected();
      mUI.setProp("bow", useBow);
      if (jAmmoName != null && useBow) {
        jAmmoName.setModel(new DefaultComboBoxModel<>(mArrows));
      }
    });

    jGun = new JRadioButton();
    jGun.setText("Gun");
    jGun.setBounds(250, 110, 120, 30);
    jGun.setEnabled(jBuyAmmo.isSelected());
    jGun.addChangeListener((ChangeEvent e) -> {
      boolean useGun = jGun.isSelected();
      mUI.setProp("gun", useGun);
      if (jAmmoName != null && useGun) {
        jAmmoName.setModel(new DefaultComboBoxModel<>(mBullets));
      }
    });

    boolean useBow = mUI.getBoolProp("bow");
    boolean useGun = mUI.getBoolProp("gun");

    // First use nothing is selected            
    if (useBow == false && useGun == false) {
      if (mBot.getInventory().getItemCount(11285, 3030, 2515, 2512) > 0) {
        System.out.println("Using Bow");
        jBow.setSelected(true);
      }
      else/* if (mBot.getInventory().getItemCount(11284, 3033, 2519, 2516) > 0)*/ {
        System.out.println("Using Gun");
        jGun.setSelected(true);
      }
    }
    else {
      jBow.setSelected(useBow);
      jGun.setSelected(useGun);
    }

    mUI.add(jBow);
    mUI.add(jGun);

    ButtonGroup weaponUsage = new ButtonGroup();
    weaponUsage.add(jBow);
    weaponUsage.add(jGun);

    // Select ammo
    jAmmoLabel = new JLabel();
    jAmmoLabel.setText("Ammo Name");
    jAmmoLabel.setBounds(250, 140, 120, 30);
    mUI.add(jAmmoLabel);

    if (jBow.isSelected()) {
      jAmmoName = new JComboBox<>(mArrows);
    }
    else {
      jAmmoName = new JComboBox<>(mBullets);
    }
    jAmmoName.setBounds(250, 165, 120, 30);
    jAmmoName.setEnabled(jBuyAmmo.isSelected());
    jAmmoName.setSelectedIndex(mUI.getIndexProp("ammo"));
    jAmmoName.addActionListener((ActionEvent e) -> {
      mUI.setProp("ammo", jAmmoName.getSelectedIndex());
    });
    mUI.add(jAmmoName);

    // Ammo amount
    jAmmoAmountLabel = new JLabel();
    jAmmoAmountLabel.setText("Ammo amount");
    jAmmoAmountLabel.setBounds(250, 190, 160, 30);
    mUI.add(jAmmoAmountLabel);

    jAmmoAmount = new JTextField();
    String ammoAmount = mUI.getStringProp("ammoAmount");
    if (ammoAmount.isEmpty()) {
      ammoAmount = "6";
    }
    jAmmoAmount.setText(ammoAmount);
    jAmmoAmount.setBounds(250, 215, 120, 30);
    jAmmoAmount.setEnabled(jBuyAmmo.isSelected());
    jAmmoAmount.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        mUI.setProp("ammoAmount", jAmmoAmount.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        mUI.setProp("ammoAmount", jAmmoAmount.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        mUI.setProp("ammoAmount", jAmmoAmount.getText());
      }

    });
    mUI.add(jAmmoAmount);

    // Serpent Sting
    jUseSerpentSting = new JCheckBox();
    jUseSerpentSting.setText("Use Serpent Sting");
    jUseSerpentSting.setBounds(12, 165, 160, 30);
    jUseSerpentSting.setSelected(mUI.getBoolProp("useSerpentSting"));
    jUseSerpentSting.addChangeListener((ChangeEvent e) -> {
      mUI.setProp("useSerpentSting", jUseSerpentSting.isSelected());
    });
    mUI.add(jUseSerpentSting);

    mUI.revalidate();
    mUI.repaint();
    // =========================

    System.out.println("TheCrux's Hunter script started");

    mDrinkPercentage = mUI.getDrinkPercentage();
    mEatPercentage = mUI.getEatPercentage();
    mDrinking = false;
    mEating = false;
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
    // Remove Aspect of the Cheetah / Mount
    if (mPlayerLevel >= 20 && (mPlayer.hasAura(mAspectOfTheCheetah) || mPlayer.hasAura(mMounts))) {
      switchActionBar(2);
      mKeyboard.type('0');
      switchActionBar(1);
    }

    switchActionBar(1);

    // Make sure the pet is attacked and not the player
    if (mPlayerLevel >= 10) {
      List<Unit> attackers = mBot.getAttackers();
      if (attackers.size() > 1) {
        for (Unit a : attackers) {
          if (a.getTarget() == mPlayer.getGUID()) {
            petAttack(a);
            break;
          }
        }
        // Rapid Fire
        if (mPlayerLevel >= 26 && !mBot.anyOnCD(mRapidFire)) {
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
      // Walk in range
      if (mPet.getDistance() > 20 && !mMovement.isMoving()) {
        mMovement.walkTo(mPet.getVector(), 18);
        return;
      }
      switchActionBar(2);
      mKeyboard.type('4');
      switchActionBar(1);
      return;
    }

    // Feign Death
    if (mPlayerLevel >= 30 && u.getTarget() == mPlayer.getGUID() && mPet != null && !mPet.isDead()
            && !mBot.anyOnCD(mFeignDeath) && targetHealth < 0.85f) {
      mKeyboard.type('6');
    }

    // Range Attacks
    if (targetDistance > 9) {
      // Hunter's Mark
      if (mPlayerLevel >= 6 && !u.hasAura(mHuntersMark) && targetHealth >= 0.2f && manaValue >= 0.1f) {
        mKeyboard.type('8');
      }
      // Serpent Sting
      else if (mPlayerLevel >= 4 && !u.hasAura(mSerpentString) && jUseSerpentSting.isSelected() && targetHealth >= 0.15f && manaValue >= 0.15f) {
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
      // Try to use Mongoose Bite
      if (mPlayerLevel >= 16 && !mBot.anyOnCD(mMongooseBite)) {
        mKeyboard.type('5');
      }
      // Aspect of the Monkey with dead pet
      if (mPet != null && mPet.isDead() && !mPlayer.hasAura(mAspectOfTheMonkey)) {
        switchActionBar(2);
        mKeyboard.type('8');
        switchActionBar(1);
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

  void petAttack(Unit mob) {
    mob.target();
    mKeyboard.press(KeyEvent.VK_CONTROL);
    mKeyboard.type('1');
    mKeyboard.release(KeyEvent.VK_CONTROL);
  }

  @Override
  public void pull(Unit u) {
    if (isPetAliveAndWell()) {
      combat(u);
    }
  }

  @Override
  public boolean combatEnd(Unit u) {

    drinkAndEat();
    if (mDrinking || mEating) {
      return true;
    }

    return !isFullBuffed();
  }

  boolean isPetAliveAndWell() {

    if (mPlayer.isCasting()) {
      return false;
    }

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
    // Aspect of the Hawk
    if (mPlayerLevel >= 10 && !mPlayer.hasAura(mAspectOfTheHawk)) {
      switchActionBar(2);
      mKeyboard.type('7');
      return false;
    }
    // Aspect of the Money
    else if (mPlayerLevel >= 4 && mPlayerLevel < 10 && !mPlayer.hasAura(mAspectOfTheMonkey)) {
      switchActionBar(2);
      mKeyboard.type('8');
      return false;
    }

    switchActionBar(1);
    return true;
  }

  long mFoodTimer;

  void drinkAndEat() {
    switchActionBar(1);

    if (mPlayer.getManaFloat() < mDrinkPercentage) {
      mDrinking = true;
    }

    if (mPlayer.getHealthFloat() < mEatPercentage) {
      if (!mEating) {
        mFoodTimer = 0;
      }
      mEating = true;
    }

    // Drink
    if (mDrinking && !mPlayer.hasAura(mDrinkingBuffs) && mPlayer.getManaFloat() < mDrinkPercentage + 0.05f) {
      mKeyboard.type('0');
      mBot.sleep(1200, 1700); // prevent double drinking
    }

    // Eat
    if (mEating && !mPlayer.hasAura(mEatingBuffs) && !mPlayer.hasAura(mEatingBuffs) && mPlayer.getHealthFloat() < mEatPercentage + 0.05f) {
      mKeyboard.type('9');
      if (mFoodTimer == 0) {
        mFoodTimer = System.currentTimeMillis();
      }
      mBot.sleep(1200, 1700); // prevent double eating
    }

    if (mDrinking && mPlayer.getManaFloat() > 0.9f) {
      mDrinking = false; // over 90% mana, good enough
      if (!mEating) {
        mKeyboard.type('w'); // force to be standing
      }
    }

    if (mEating && mPlayer.getHealthFloat() > 0.9f) {
      if (!mDrinking && !mPlayer.hasAura(mBuffFood) || ((System.currentTimeMillis() - mFoodTimer) > 10500)) {
        mEating = false; // over 90% health, good enough
        mKeyboard.type('w'); // force to be standing
      }
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

      // Remove Aspect of the Cheetah / Mount
      if (mPlayerLevel >= 20 && (mPlayer.hasAura(mAspectOfTheCheetah) || mPlayer.hasAura(mMounts))) {
        switchActionBar(2);
        mKeyboard.type('0');
        switchActionBar(1);
      }

      // Check for buffs & anly attack while walking if the pet is alive
      if (isFullBuffed() && mPet != null && !mPet.isDead()) {
        // Hunter's Mark
        if (mPlayerLevel >= 6 && !u.hasAura(mHuntersMark) && u.getDistance() < 40) {
          mKeyboard.type('8');
        }
        // Pet Attack
        else if (mPlayerLevel >= 10 && u.getDistance() < 40) {
          petAttack(u);
        }
        // Concussive Shot
        else if (mPlayerLevel >= 8 && !u.hasAura(mConcussiveShot) && !mBot.anyOnCD(mConcussiveShot)
                && u.getDistance() < 35) {
          mKeyboard.type('1');
        }
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
  public boolean atVendor(Vendor vend) {
    int stackAmount = Integer.parseInt(jAmmoAmount.getText());

    System.out.println("At vendor!");

    String ammoName = (String) jAmmoName.getSelectedItem();

    // Check inventory
    if (mBot.getInventory().getItemCount(ammoName) >= stackAmount) {
      System.out.println("We've got enough ammo -> go back fighting");
      return false;
    }

    System.out.println("Going to buy " + stackAmount + " stacks of " + ammoName + "!");

    mBot.buyItem(ammoName, stackAmount);

    /*
    if (mPlayerLevel >= 40) {
      // Jagged Arrow or Accurate Slugs
      if (useBow) {
        mBot.buyItem("Jagged Arrow", stackAmount);
      }
      else {
        mBot.buyItem("Accurate Slugs", stackAmount);
      }
    }
    else if (mPlayerLevel >= 25) {
      // Razor Arrow and Solid Shot
      if (useBow) {
        mBot.buyItem("Razor Arrow", stackAmount);
      }
      else {
        mBot.buyItem("Solid Shot", stackAmount);
      }
    }
    else if (mPlayerLevel >= 10) {
      // Sharp Arrow and Heavy Shot
      if (useBow) {
        mBot.buyItem("Sharp Arrow", stackAmount);
      }
      else {
        mBot.buyItem("Heavy Shot", stackAmount);
      }
    }
    else {
      // Rough Arrow and Light Shot
      if (useBow) {
        mBot.buyItem("Rough Arrow", stackAmount);
      }
      else {
        mBot.buyItem("Light Shot", stackAmount);
      }
    }*/
    return false;
  }

  @Override
  public boolean beforeInteract() {
    // Remove Aspect of the Cheetah / Mount
    if (mPlayerLevel >= 20 && (mPlayer.hasAura(mAspectOfTheCheetah) || mPlayer.hasAura(mMounts))) {
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

    // Going to vendor if ammo is below 200
    if (jBuyAmmo.isSelected() && mBot.getInventory().getItemCount((String) jAmmoName.getSelectedItem()) < 200) {
      return mBot.getDatabase().getNearestAmmo();
    }

    return null;
  }

  @Override
  public boolean prepareForTravel(Vector3f travelTarget) {

    if (mPlayer.isCasting()) {
      return true;
    }

    // Aspect of the Cheetah / Mount
    if (mPlayerLevel >= 20 && !mPlayer.hasAura(mAspectOfTheCheetah) && !mPlayer.hasAura(mMounts)) {
      switchActionBar(2);
      mKeyboard.type('0');
      switchActionBar(1);
      return true;
    }
    return false;
  }

  @Override
  public void setDrinkPercentage(float value) {
    mDrinkPercentage = value;
  }

  @Override
  public void setEatPercentage(float value) {
    mEatPercentage = value;
  }
}
