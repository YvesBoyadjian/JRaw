/*
 * BaseBytePtr.java
 *
 * Created on 14 novembre 2006, 21:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package portage_v3;

/**
 *
 * @author YvesFabienne
 */
public class BaseBytePtr {
    
    private static int RESERVE_DE_DEPASSEMENT_MEMOIRE = 64;
    
    protected byte[] buffer = null;
    protected int index = 0;
    
    /** Creates a new instance of BaseBytePtr */
    public BaseBytePtr() {
    }
    
    /** Creates a new instance of BaseBytePtr */
    protected BaseBytePtr( int size) {
        buffer = new byte[size + RESERVE_DE_DEPASSEMENT_MEMOIRE];
    }
    
//    protected BaseBytePtr( String arg) {
//        buffer = new byte[arg.length()+1 + RESERVE_DE_DEPASSEMENT_MEMOIRE];
//        set(arg);
//    }
    
    protected BaseBytePtr( BaseBytePtr other, int offset) {
        if ( other instanceof MutableBytePtr)
            assert( !((MutableBytePtr)other).isNull());
        if ( other != null) {
            buffer = other.buffer;
            index = other.index + offset;
        }
        else {
            assert( offset == 0);
        }
    }
    
    protected BaseBytePtr( BaseBytePtr other) {
        if ( other instanceof MutableBytePtr)
            assert( !((MutableBytePtr)other).isNull());
        if ( other != null) {
            buffer = other.buffer;
            index = other.index;
        }
    }
    
    /**
     * Utile pour comparer les pointeurs
     */
    public int toInt() {
        return index;
    }
    
    public int moins( BaseBytePtr other) {
        assert( buffer.equals(other.buffer));
        return index - other.index;
    }
    
    public OldBytePtr plus( int n) {
        return new OldBytePtr( this, n);
    }
    
    public OldBytePtr copy() {
        return plus(0);
    }
    
    public byte byteAt( int i) {
        return buffer[index+i];
    }
    
    public byte etoile() {
        return buffer[index];
    }
    
    public byte etoile( byte car) {
        return setAt(0, car);
    }
    
    public byte setAt( int i, byte car) {
        return (buffer[i+index] = car);
    }
    
//    public void set( String arg) {
//        int length = arg.length();
//        arg.getChars(0, length, buffer, index);
//        buffer[length+index] = 0;        
//    }
//    
    public int capacity() {
        return buffer.length - index;
    }
    
//    public String toString() {
//        int end = index;
//        while( buffer[end] != 0)
//            end++;
//        return String.copyValueOf(buffer, index, end - index);
//    }
    
    public boolean equals( Object obj) {
        if ( ! (obj instanceof BaseBytePtr))
            return false;
        BaseBytePtr other = (BaseBytePtr) obj;
        if ( buffer == other.buffer && index == other.index)
            return true;
        return false;
    }
}
