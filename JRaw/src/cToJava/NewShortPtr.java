/*
 * NewShortPtr.java
 *
 * Created on 13 novembre 2007, 22:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cToJava;

/**
 *
 * @author YvesFabienne
 */
public class NewShortPtr {
    short[] array;
    int index = 0;
    
    /** Creates a new instance of NewShortPtr */
    public NewShortPtr() {
    }
    public NewShortPtr( short[] arrayArg) {
        array = arrayArg;        
    }

    public int length() {
        if ( array == null) {
            return 0;
        }
        return array.length;
    }

    public void nullify() {
        array = null;
    }
    
    public short at( int indice) {
        return array[ index + indice];
    }
    
    public short at( int indice, short value) {
        array[index+ indice] = value;
        return value;
    }
    
    public NewShortPtr assign( NewShortPtr source) {
        array = source.array;
        index = source.index;
        return this;
    }

    public void setOffset( int offset) {
        index = offset;
    }
    
    public NewShortPtr add(int n) {
        index+= n;
        return this;
    }
    public NewShortPtr plus( int n) {
        NewShortPtr ret = new NewShortPtr();
        ret.assign(this);
        ret.add(n);
        return ret;
    }
}
