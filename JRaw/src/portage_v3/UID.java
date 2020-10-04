/*
 * UID.java
 *
 * Created on 11 octobre 2005, 16:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

/**
 *
 * @author w22w087
 */
public class UID {
    public static native int getuid();
    public static native int geteuid();
    public static native int setuid(int uid);
    
    /** Creates a new instance of UID */
    public UID() {
    }
    
}
