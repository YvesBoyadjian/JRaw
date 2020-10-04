/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw.ihm;

import JRaw.WhiteBalance.WhiteBalance;
import JRaw.ihm.MainJFrame;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;

/**
 *
 * @author YvesFabienne
 */
public class WhiteBalanceActionListener implements ActionListener {

    WhiteBalance wb;
    MainJFrame mjf;

    public WhiteBalanceActionListener( WhiteBalance wbArg, MainJFrame mjfArg) {
        wb = wbArg;
        mjf = mjfArg;
    }

    public void actionPerformed(ActionEvent e) {
        mjf.getParentApplication().current_white = wb;
        if ( wb.isDefault()) {
            mjf.setWhiteBalanceDefault();
        }
        else {
            mjf.setWhiteBalance();
        }
    }

}
