/*
 * MutableBytePtr.java
 *
 * Created on 14 novembre 2006, 21:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package portage_v3;

/**
 *
 * @author YvesFabienne
 */
public class MutableBytePtr extends BaseBytePtr implements MutablePtr, MutableValue  {
    
    /** Représente le pointeur null en C */
    private static MutableBytePtr NULL = new MutableBytePtr();
    
    /** Creates a new instance of MutableBytePtr */
    public MutableBytePtr() {
        super();
    }
    
    /** Creates a new instance of MutableBytePtr */
    public MutableBytePtr( int size) {
        super(size);
    }
    
    public MutableBytePtr( BaseBytePtr ptr) {
        super( ptr);
    }
    
//    public MutableBytePtr( String arg) {
//        super(arg);
//    }
    
    public void plusPlus() {
        index++;
    }
    
    public void moinsMoins() {
        index--;
    }
    
    public MutableBytePtr add( int n) {
        index += n;
        return this;
    }
    
    public MutableBytePtr assign( BaseBytePtr src) {
        if ( src == null) {
            buffer = null;
            index = 0;
            return this;
        }
        buffer = src.buffer;
        index = src.index;
        return this;
    }
    
    public OldBytePtr toBytePtr() {
        if ( isNull())
            return null;
        return new OldBytePtr(this);
    }
    
    public boolean isNull() {
        return equals( NULL);
    }    
}
