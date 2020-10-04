/*
 * MutableLong.java
 *
 * Created on 2 janvier 2006, 12:08
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
public class MutableLong implements MutableValue {
    public long value;
    
    /** Creates a new instance of MutableLong */
    public MutableLong() {
        value = 0;
    }
    
    public MutableLong( long valueArg) {
        value = valueArg;
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
        return new MutableLong(value);
    }
    
    public String toString() {
        return String.valueOf(value);
    }    
}
