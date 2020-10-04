/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author Yves
 */
public class PlainFileFilter implements FileFilter {

    public boolean accept(File pathname) {
        String filename = pathname.getName().toLowerCase();

        if ( filename.endsWith(".nef")
             || filename.endsWith(".pef")
             || filename.endsWith(".crw")
             || filename.endsWith(".cr2")
             || filename.endsWith(".raf")
             || filename.endsWith(".raw")) {
            if ( pathname.isFile()) {
                return true;
            }
        }

        return false;
    }

}
