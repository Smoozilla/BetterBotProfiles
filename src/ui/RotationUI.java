/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import com.betterbot.api.pub.RotationSolver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import rotations.ICommonSettingFunctions;

/**
 *
 * @author TheCrux
 */
public class RotationUI extends javax.swing.JPanel {

  RotationSolver mSolver;
  ICommonSettingFunctions mSolverSettingsInterface; // probably pick better names for these lol 

  /**
   * Creates new form RotationUI
   *
   * @param solver The combat solver of a class
   */
  public RotationUI(RotationSolver solver) {
    initComponents();
    
    mSolver = solver;
    if (mSolver instanceof ICommonSettingFunctions) {
      mSolverSettingsInterface = (ICommonSettingFunctions) mSolver;
    }
    
    loadSettings();

    DocumentListener drinkListener = new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        if (mSolverSettingsInterface != null) {
          mSolverSettingsInterface.setDrinkPercentage(getDrinkPercentage());
        }
        saveSettings();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        if (mSolverSettingsInterface != null) {
          mSolverSettingsInterface.setDrinkPercentage(getDrinkPercentage());
        }
        saveSettings();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        if (mSolverSettingsInterface != null) {
          mSolverSettingsInterface.setDrinkPercentage(getDrinkPercentage());
        }
        saveSettings();
      }
    };
    jDrinkPercentage.getDocument().addDocumentListener(drinkListener);
    
    DocumentListener eatListener = new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        if (mSolverSettingsInterface != null) {
          mSolverSettingsInterface.setEatPercentage(getEatPercentage());
        }
        saveSettings();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        if (mSolverSettingsInterface != null) {
          mSolverSettingsInterface.setEatPercentage(getEatPercentage());
        }
        saveSettings();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        if (mSolverSettingsInterface != null) {
          mSolverSettingsInterface.setEatPercentage(getEatPercentage());
        }
        saveSettings();
      }
    };
    jEatPercentage.getDocument().addDocumentListener(eatListener);

    ChangeListener checkListener = (ChangeEvent e) -> {
      saveSettings();
    };
    jUseMount.addChangeListener(checkListener);
  }

  public boolean useMount() {
    return jUseMount.isSelected();
  }

  public float getEatPercentage() {
    String text = jEatPercentage.getText();
    float value;
    try {
      value = Float.parseFloat(text) / 100;
      if (value > 0 && value < 1) {
        System.out.println("Eat Percentage OK");
        jEatOkay.setText("% - Value OK");
        return value;
      }
    }
    catch (NumberFormatException e) {
      System.out.println("Invalid Eat Percentage: \"" + text + "%\" -> " + e.getMessage());
    }

    System.out.println("Eat Percentage NOT OK");
    jEatOkay.setText("% - Value NOT OK");
    return 0;
  }

  public float getDrinkPercentage() {
    String text = jDrinkPercentage.getText();
    float value;
    try {
      value = Float.parseFloat(text) / 100;
      if (value > 0 && value < 1) {
        System.out.println("Drink Percentage OK");
        jDrinkOkay.setText("% - Value OK");
        return value;
      }
    }
    catch (NumberFormatException e) {
      System.out.println("Invalid Drink Percentage: \"" + text + "%\" -> " + e.getMessage());
    }

    System.out.println("Drink Percentage NOT OK");
    jDrinkOkay.setText("% - Value NOT OK");
    return 0;
  }
  
  public boolean shouldUseMount(){
    return jUseMount.isSelected();
  }

  private void loadSettings() {
    System.out.println("Loading combat settings.");

    DataInputStream in;
    try {
      in = new DataInputStream(new FileInputStream("./combatSettings.bin"));

      HashMap<String, String> prop = new HashMap<>();
      int propcount = in.read();
      for (int i = 0; i < propcount; i++) {
        int kl = in.read();
        byte[] b = new byte[kl];
        in.readFully(b);
        String key = new String(b);
        b = new byte[in.readInt()];
        in.readFully(b);
        prop.put(key, new String(b));
      }
      if (prop.size() > 0) {
        jUseMount.setSelected(Boolean.valueOf(prop.get("useMount")));
        jEatPercentage.setText(prop.get("eatPercentage"));
        jDrinkPercentage.setText(prop.get("drinkPercentage"));
      }
    }
    catch (IOException ex) {
      System.out.println("Couldn't load combat settings -> " + ex.getMessage());
      Logger.getLogger(RotationUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void saveSettings() {
    System.out.println("Saving combat settings.");

    DataOutputStream out;
    try {
      out = new DataOutputStream(new FileOutputStream(("./combatSettings.bin")));
      HashMap<String, String> props = new HashMap<>();

      props.put("eatPercentage", jEatPercentage.getText());
      props.put("drinkPercentage", jDrinkPercentage.getText());

      props.put("useMount", jUseMount.isSelected() ? "true" : "false");

      out.write(props.size());
      for (String s : props.keySet()) {
        byte[] b = s.getBytes();
        out.write(b.length);
        out.write(b);
        b = props.get(s).getBytes();
        out.writeInt(b.length);
        out.write(b);
      }
      out.flush();
      out.close();
    }
    catch (IOException ex) {
      System.out.println("Couldn't save combat settings -> " + ex.getMessage());
      Logger.getLogger(RotationUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jEatPercentage = new javax.swing.JTextField();
    jLabel2 = new javax.swing.JLabel();
    jDrinkPercentage = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    jUseMount = new javax.swing.JCheckBox();
    jEatOkay = new javax.swing.JLabel();
    jDrinkOkay = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();

    jEatPercentage.setText("40");

    jLabel2.setLabelFor(jEatPercentage);
    jLabel2.setText("Eat Percentage");

    jDrinkPercentage.setText("40");

    jLabel3.setLabelFor(jDrinkPercentage);
    jLabel3.setText("Drink Percentage");

    jUseMount.setText("Use Mount");

    jEatOkay.setText("%");

    jDrinkOkay.setText("%");

    jLabel4.setFont(new java.awt.Font("Dialog", 1, 15)); // NOI18N
    jLabel4.setText("Rotation Settings");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(jLabel2)
              .addComponent(jDrinkPercentage)
              .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(jUseMount)
              .addComponent(jEatPercentage))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jEatOkay)
              .addComponent(jDrinkOkay)))
          .addGroup(layout.createSequentialGroup()
            .addContainerGap(137, Short.MAX_VALUE)
            .addComponent(jLabel4)))
        .addContainerGap(140, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel4)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel2)
        .addGap(2, 2, 2)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jEatPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jEatOkay))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jDrinkPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jDrinkOkay))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jUseMount)
        .addContainerGap(138, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  public JCheckBox getUseMountComp() {
    return jUseMount;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel jDrinkOkay;
  private javax.swing.JTextField jDrinkPercentage;
  private javax.swing.JLabel jEatOkay;
  private javax.swing.JTextField jEatPercentage;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JCheckBox jUseMount;
  // End of variables declaration//GEN-END:variables
}
