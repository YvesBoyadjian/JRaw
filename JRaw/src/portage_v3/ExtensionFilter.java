/*
 * ExtensionFilter.java
 *
 * Created on 21 février 2006, 16:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author yvboyadj
 */
public class ExtensionFilter implements FilenameFilter {
    String ext;
    
    /** Creates a new instance of ExtensionFilter */
    public ExtensionFilter(String extArg) {
        ext = "."+extArg;
    }
    
    public boolean accept(File f,String s) {
        return s.endsWith( ext);
    }
}
