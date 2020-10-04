/*
 * RLimit.java
 *
 * Created on 11 octobre 2005, 16:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

/** Portage JAVA des fonctions C "int getrlimit(int resource, struct rlimit *rlp)"
 * et                            "int setrlimit(int resource, const struct rlimit *rlp)"
 *
 * @author w22w087
 */
public class RLimit {
    public static final int RLIMIT_CPU = 0,
            RLIMIT_FSIZE = 1,
            RLIMIT_DATA = 2,
            RLIMIT_STACK = 3,
            RLIMIT_CORE = 4,
            RLIMIT_NOFILE = 5,
            RLIMIT_VMEM = 6,
            RLIMIT_AS = 6,
            RLIM_NLIMITS = 7;
    public native int getrlimit(int resource);
    public native int setrlimit(int resource);
    
    public int rlim_cur;
    public int rlim_max;
    
    /** Creates a new instance of RLimit */
    public RLimit() {
    }
    
}
