/*
 * MutableFloat.java
 *
 * Created on 24 octobre 2005, 17:06
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
public class MutableFloat implements MutableValue {
    public float value;
    
    /** Creates a new instance of MutableFloat */
    public MutableFloat() {
        value = 0.0f;
    }
    
    public MutableFloat( float valueArg) {
        value = valueArg;
    }
    
    public Object clone() {
        return new MutableFloat(value);
    }
    
    public String toString() {
        return String.valueOf(value);
    }
    
}
