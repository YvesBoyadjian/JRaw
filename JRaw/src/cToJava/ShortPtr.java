/*
 * ShortPtr.java
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
public class ShortPtr  extends VoidPtr {
    
    /** Creates a new instance of ShortPtr */
    public ShortPtr() {
    }
    
    public ShortPtr( int size) {
        assign( CTOJ.calloc(size,2));
    }
    
    public short etoile( short value) {
        bb.putShort( index, value);
        return value;
    }
    
    public short at( int indice, short value) {
        bb.putShort( index+ indice*2,value);
        return value;
    }
    
    public short at( int indice) {
        return bb.getShort(index + indice*2);
    }
    
    public int uat( int indice) {
        return CTOJ.toUnsigned(at(indice));
    }
    
    public ShortPtr plusPlus() {
        index+=2;
        return this;
    }
    public ShortPtr add(int n) {
        index+= 2*n;
        return this;
    }
    public ShortPtr plus( int n) {
        ShortPtr ret =  new ShortPtr();
        ret.assign(this);
        ret.add(n);
        return ret;
    }
}
