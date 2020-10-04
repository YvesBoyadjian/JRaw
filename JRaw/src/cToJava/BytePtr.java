/*
 * BytePtr.java
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
public class BytePtr  extends VoidPtr {
    
    /** Creates a new instance of BytePtr */
    public BytePtr() {
    }
    public BytePtr(int size) {
        assign( CTOJ.malloc(size));
    }
    public BytePtr(String str) {
        copy(str);
    }
    public BytePtr(short[] tab) {
        int len = tab.length;
        assign( CTOJ.malloc(len));
        for ( int i=0; i< len; i++) {
            at( i, (byte)tab[i]);
        }
    }
    
    public BytePtr copy(String str) {
        int len = str.length();
        assign( CTOJ.malloc(len+1));
        byte[] source = str.getBytes();
        for ( int i=0; i< len; i++) {
            at( i, source[i]);
        }
        at(len, (byte)0);
        return this;
    }
    
    public byte etoile( byte value) {
        bb.put( index, value);
        return value;
    }
    
    public byte at( int indice, byte value) {
        bb.put( index+ indice,value);
        return value;
    }
    
    public byte at( int indice) {
        return bb.get(index + indice);
    }
    
    public short uat( int indice) {
        return CTOJ.toUnsigned(at(indice));
    }
    
    public BytePtr plus( int offset) {
        BytePtr newPtr = new BytePtr();
        newPtr.assign(this);
        newPtr.index += offset;
        return newPtr;
    }
    
    public int minus(BytePtr other) {
        assert( bb == other.bb);
        return index - other.index;
    }
    
    public BytePtr plusPlus() {
        index++;
        return this;
    }
    
    public void initialize( byte[] tab) {
        for ( int i=0; i< tab.length; i++) {
            at(i, tab[i] );
        }
    }
    
    public int length() {
        int len  = bb.capacity();
        int i=0;
        while ( bb.get(i) != 0) {
            i++;
        }
        return i;
    }
}
