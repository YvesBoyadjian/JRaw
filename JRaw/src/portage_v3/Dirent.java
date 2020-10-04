/*
 * Dirent.java
 *
 * Created on 22 décembre 2005, 14:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.io.File;

/**
 *
 * @author w22w087
 */
public class Dirent {
    public CharPtr d_name;
    
    /** Creates a new instance of Dirent */
    public Dirent( File file) {
        d_name = new CharPtr(file.getName());
    }
    
}
