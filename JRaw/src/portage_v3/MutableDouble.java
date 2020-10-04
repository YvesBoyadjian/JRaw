/*
 * MutableDouble.java
 *
 * Created on 14 octobre 2005, 14:38
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
public class MutableDouble implements MutableValue {
    public double value;
    
    /** Creates a new instance of MutableDouble */
    public MutableDouble() {
        value = 0.0;
    }
    
    public MutableDouble( double value) {
        this.value = value;
    }
    
    public Object clone() {
        return new MutableDouble(value);
    }
    
    public String toString() {
        return String.valueOf(value);
    }
}
