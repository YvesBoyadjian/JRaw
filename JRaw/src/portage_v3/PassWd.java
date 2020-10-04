/*
 * PassWd.java
 *
 * Created on 14 octobre 2005, 14:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

/**
 *
 * @author yvboyadj
 */
public class PassWd {
    public native int getpwnam_(String name);
    
    public String pw_name;
    public String pw_passwd;
    public int   pw_uid;
    public int   pw_gid;
    public String pw_age;
    public String pw_comment;
    public String pw_gecos;
    public String pw_dir;
    public String pw_shell;
    
    /** Creates a new instance of PassWd */
    public PassWd() {
    }
    
    public static PassWd getpwnam( String name) {
        PassWd passWd = new PassWd();
        if ( passWd.getpwnam_( name) != 0)
            return null;
        return passWd;
    }
}
