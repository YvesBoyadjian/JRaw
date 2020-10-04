/*
 * MutableInteger.java
 *
 * Created on 13 octobre 2005, 13:52
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
public class MutableInteger implements MutableValue {
    public int value;
    
    /** Creates a new instance of MutableInteger */
    public MutableInteger() {
        value = 0;
    }
    
    public MutableInteger( int value) {
        this.value = value;
    }
    
    public boolean equals( Object obj) {
        if ( obj instanceof MutableInteger) {
            MutableInteger integer = (MutableInteger) obj;
            return integer.value == value;
        }
        if ( obj instanceof MutableLong) {
            MutableLong integer = (MutableLong) obj;
            return integer.value == value;
        }
        return false;
    }
    
    public Object clone() {
        return new MutableInteger(value);
    }
    
    public String toString() {
        return String.valueOf(value);
    }    
}
