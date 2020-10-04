/*
 * ShortArrayPtr.java
 *
 * Created on 28 octobre 2007, 20:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cToJava;

/**
 *
 * @author Yves
 */
public class ShortArrayPtr extends VoidPtr {
    int nindice;
    
    /** Creates a new instance of ShortArrayPtr */
    public ShortArrayPtr(int nindiceArg) {
        nindice = nindiceArg;
    }
    
    public ShortArrayPtr plus( int offset) {
        ShortArrayPtr newPtr = new ShortArrayPtr(nindice);
        newPtr.assign(this);
        newPtr.index += 2*offset*nindice;
        return newPtr;
    }
    
    public short at( int indice1, int indice2, short value) {
        int indice = nindice*indice1+indice2;
        bb.putShort( index+ indice*2,value);
        return value;
    }
    
    public short at( int indice1, int indice2) {
        int indice = nindice* indice1+ indice2;
        return bb.getShort(index + indice*2);
    }
    
    public ShortPtr toShortPtr( int indice) {
        ShortPtr ret = new ShortPtr();
        ret.assign(this);
        ret.index += 2*indice*nindice;
        return ret;
    }
    
}
