/*
 * Pixmap.java
 *
 * Created on 23 janvier 2006, 16:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.io.File;
import javax.swing.ImageIcon;

/**
 *
 * @author yvboyadj
 */
public class Pixmap extends ImageIcon {
    
    /** Creates a new instance of Pixmap */
    public Pixmap( File filename) {
        super( filename.toString());
    }
    
}
