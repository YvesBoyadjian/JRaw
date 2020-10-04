/*
 * DIR.java
 *
 * Created on 14 novembre 2005, 11:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.io.File;

/**
 *
 * @author yvboyadj
 */
public class DIR {
    public File[] files;
    public int index = 0;
    
    /** Creates a new instance of DIR */
    public DIR(File fileArg) {
        files = fileArg.listFiles();
    }
    
    public Dirent readdir() {
        if ( index == files.length)
            return null;
        File file = files[index];
        index++;
        Dirent ret_val = new Dirent( file);
        return ret_val;
    }
}
