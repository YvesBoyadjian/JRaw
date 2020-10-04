/*
 * MutableBoolean.java
 *
 * Created on 28 novembre 2005, 14:03
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
public class MutableBoolean implements MutableValue {
    public boolean value;
    
    /** Creates a new instance of MutableBoolean */
    public MutableBoolean() {
        value = false;
    }
    
    public MutableBoolean( boolean valueArg) {
        value = valueArg;
    }
    
    public String toString() {
        return String.valueOf(value);
    }    
}
