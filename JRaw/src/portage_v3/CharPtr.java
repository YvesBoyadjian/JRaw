/*
 * CharPtr.java
 *
 * Created on 15 novembre 2005, 17:32
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
public class CharPtr extends BaseCharPtr implements MutableValue {

    public CharPtr( int size) {
        super(size);
    }
    protected CharPtr( BaseCharPtr other, int offset) {
        super( other, offset);
    }
    protected CharPtr( BaseCharPtr other) {
        super( other);
    }
    public CharPtr( String arg) {
        super( arg);
    }
    
    public File toFile() {
        return new File(toString());
    }
}
