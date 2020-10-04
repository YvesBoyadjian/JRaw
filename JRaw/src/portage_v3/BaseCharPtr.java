/*
 * BaseCharPtr.java
 *
 * Created on 9 janvier 2006, 10:42
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
public class BaseCharPtr {
    // Note: Cette variable a été introduite pour palier aux dépassements
    // mémoire courants en language C.
    // On suppose qu'un écrasement supérieur à 64 caractères aurai de toute
    // manière provoqué le plantage du programme C.
    private static int RESERVE_DE_DEPASSEMENT_MEMOIRE = 64;
    
    protected char[] buffer = null;
    protected int index = 0;
    
    protected BaseCharPtr() {
    }
    /** Creates a new instance of BaseCharPtr */
    protected BaseCharPtr( int size) {
        buffer = new char[size + RESERVE_DE_DEPASSEMENT_MEMOIRE];
    }
    
    protected BaseCharPtr( String arg) {
        buffer = new char[arg.length()+1 + RESERVE_DE_DEPASSEMENT_MEMOIRE];
        set(arg);
    }
    
    protected BaseCharPtr( BaseCharPtr other, int offset) {
        if ( other instanceof MutableCharPtr)
            assert( !((MutableCharPtr)other).isNull());
        if ( other != null) {
            buffer = other.buffer;
            index = other.index + offset;
        }
        else {
            assert( offset == 0);
        }
    }
    
    protected BaseCharPtr( BaseCharPtr other) {
        if ( other instanceof MutableCharPtr)
            assert( !((MutableCharPtr)other).isNull());
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
    
    public int moins( BaseCharPtr other) {
        assert( buffer.equals(other.buffer));
        return index - other.index;
    }
    
    public boolean lessOrEqualThan( BaseCharPtr other) {
        assert( buffer.equals(other.buffer));
        return index <= other.index;
    }
    
    public BaseCharPtr moinsmoins() {
        index--;
        return this;
    }
    
    public CharPtr plus( int n) {
        return new CharPtr( this, n);
    }
    
    public CharPtr copy() {
        return plus(0);
    }
    
    public char charAt( int i) {
        return buffer[index+i];
    }
    
    public char etoile() {
        if ( index==-1) {
            int i=0;
        }
        return buffer[index];
    }
    
    public char etoile( char car) {
        return setAt(0, car);
    }
    
    public char setAt( int i, char car) {
        return (buffer[i+index] = car);
    }
    
    public void set( String arg) {
        int length = arg.length();
        arg.getChars(0, length, buffer, index);
        buffer[length+index] = 0;        
    }
    
    public int capacity() {
        return buffer.length - index;
    }
    
    public String toString() {
        int end = index;
        while( buffer[end] != 0)
            end++;
        return String.copyValueOf(buffer, index, end - index);
    }
    
    public boolean equals( Object obj) {
        if ( ! (obj instanceof BaseCharPtr))
            return false;
        BaseCharPtr other = (BaseCharPtr) obj;
        if ( buffer == other.buffer && index == other.index)
            return true;
        return false;
    }
}
