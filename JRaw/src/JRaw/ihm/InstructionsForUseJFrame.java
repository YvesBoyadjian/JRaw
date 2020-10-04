/*
 * ModeEmploiJFrame.java
 *
 * Created on 25 novembre 2007, 14:19
 */

package JRaw.ihm;

import javax.swing.JFrame;

/**
 *
 * @author  YvesFabienne
 */
public class InstructionsForUseJFrame extends javax.swing.JDialog {
    
    /** Creates new form ModeEmploiJFrame */
    public InstructionsForUseJFrame(JFrame owner) {
        super( owner, true);
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        okButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("JRaw/jraw"); // NOI18N
        setTitle(bundle.getString("mode_d_emploi_")); // NOI18N

        jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 300));
        jScrollPane1.setRequestFocusEnabled(false);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("You can open a RAW file by Drag and Drop.\n\nTo switch to full screen mode, press on F11 or\ndouble-click the picture.\nTo go back to normal mode, press again on F11 or\ndouble-click the picture.\n\nFor best performance, select the number of cores \nequal to the number of cores of your processor.\n\nClick on \"expo +\" or \"expo -\" to increase or \ndecrease the contrast of image from a third of EV.\n\nClick on \"Daylight\" or \"Tungstene\" to change the \nwhite balance ( note: This function is active only\nif the RAW file doesn't have an integrated white\nbalance.)\n\nThe \"Compression\" menu allows you to choose\nbetween three different levels of compression.");
        jScrollPane1.setViewportView(jTextArea1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        getContentPane().add(okButton, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
// add your handling code here:
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InstructionsForUseJFrame(null).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
    
}
