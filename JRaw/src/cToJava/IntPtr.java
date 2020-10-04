/*
 * IntPtr.java
 *
 * Created on 18 octobre 2007, 20:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cToJava;

/**
 *
 * @author YvesFabienne
 */
public class IntPtr  extends VoidPtr {
    
    /** Creates a new instance of IntPtr */
    public IntPtr() {
    }
    
    public IntPtr( int num) {
        super(4*num);
    }
    
    public void copy( int [] src) {
        for ( int i=0; i< src.length; i++) {
            at( i, src[i]);
        }
    }
    public int at( int indice, int value) {
        bb.putInt( index+ indice*4,value);
        return value;
    }
    
    public int at( int indice) {
        return bb.getInt(index + indice*4);
    }
    
    public IntPtr plus( int offset) {
        IntPtr newPtr = new IntPtr();
        newPtr.assign(this);
        newPtr.index += offset*4;
        return newPtr;
    }
    
}
