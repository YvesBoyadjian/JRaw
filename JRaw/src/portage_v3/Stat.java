/*
 * Stat.java
 *
 * Created on 11 octobre 2005, 15:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.io.File;

/** Portage JAVA de la fonction C "int stat(const char *path, struct stat *buf)"
 *
 * @author w22w087
 */
public class Stat {
    public int stat( File path) {
        return stat_(path.toString());
    }
    public native int stat_(String path);
    
    public int st_uid;
    public long st_mtime;
    
    /** Creates a new instance of Stat */
    public Stat() {
    }
    
}
