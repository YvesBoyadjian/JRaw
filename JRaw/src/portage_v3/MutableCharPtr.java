/*
 * MutableCharPtr.java
 *
 * Created on 15 décembre 2005, 13:44
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
public class MutableCharPtr extends BaseCharPtr implements MutablePtr, MutableValue {
    /** Représente le pointeur null en C */
    private static MutableCharPtr NULL = new MutableCharPtr();
    
    public MutableCharPtr() {
        super();
    }
    
    /** Creates a new instance of MutableCharPtr */
    public MutableCharPtr( int size) {
        super(size);
    }
    
    public MutableCharPtr( BaseCharPtr ptr) {
        super( ptr);
    }
    
    public MutableCharPtr( String arg) {
        super(arg);
    }
    
    public void plusPlus() {
        index++;
    }
    
    public void moinsMoins() {
        index--;
    }
    
    public MutableCharPtr add( int n) {
        index += n;
        return this;
    }
    
    public MutableCharPtr assign( BaseCharPtr src) {
        if ( src == null) {
            buffer = null;
            index = 0;
            return this;
        }
        buffer = src.buffer;
        index = src.index;
        return this;
    }
    
    public CharPtr toCharPtr() {
        if ( isNull())
            return null;
        return new CharPtr(this);
    }
    
    public boolean isNull() {
        return equals( NULL);
    }    
}
