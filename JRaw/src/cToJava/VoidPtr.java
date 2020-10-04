/*
 * VoidPtr.java
 *
 * Created on 18 octobre 2007, 20:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cToJava;

import java.nio.ByteBuffer;

/**
 *
 * @author YvesFabienne
 */
public class VoidPtr {
    ByteBuffer bb;
    int index = 0;
    
    /** Creates a new instance of VoidPtr */
    public VoidPtr() {
    }
    public VoidPtr(int size) {
        assign( CTOJ.malloc(size));
    }

    public VoidPtr( ByteBuffer bbArg) {
        bb = bbArg;
    }
    
    public VoidPtr assign( VoidPtr source) {
        bb = source.bb;
        index = source.index;
        return this;
    }
    
    public boolean lessThan( VoidPtr other) {
        assert( bb == other.bb);
        return index < other.index;
    }
    
    public boolean lessOrEqualThan( VoidPtr other) {
        assert( bb == other.bb);
        return index <= other.index;
    }
    
    public boolean isNull() {
        return bb == null;
    }
    
    public int sizeof() {
        return bb.capacity();
    }
    
    public BytePtr toBytePtr() {
        BytePtr newOne = new BytePtr();
        newOne.assign(this);
        return newOne;
    }
}
