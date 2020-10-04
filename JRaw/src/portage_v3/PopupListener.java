/*
 * PopupListener.java
 *
 * Created on 12 décembre 2005, 15:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.util.EventListener;
import java.util.EventObject;

/**
 *
 * @author w22w087
 */
public interface PopupListener extends EventListener {
    void popupPerformed( EventObject e);
}
