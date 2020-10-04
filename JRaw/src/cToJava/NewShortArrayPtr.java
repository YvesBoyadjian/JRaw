/*
 * NewShortArrayPtr.java
 *
 * Created on 13 novembre 2007, 22:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cToJava;

/**
 *
 * @author YvesFabienne
 */
public class NewShortArrayPtr extends NewShortPtr {
    int nindice;
    
    /** Creates a new instance of NewShortArrayPtr */
    public NewShortArrayPtr(int nindiceArg) {
        nindice = nindiceArg;
    }
    
    public short at( int indice1, int indice2, short value) {
        int indice = nindice*indice1+indice2;
        array[index+indice] = value;
        return value;
    }
    
    public short at( int indice1, int indice2) {
        int indice = nindice* indice1+ indice2;
        return array[index + indice];
    }
    
    public int uat( int indice1, int indice2) {
        return CTOJ.toUnsigned(at(indice1, indice2));
    }
    public NewShortArrayPtr plus( int n) {
        NewShortArrayPtr ret = new NewShortArrayPtr(nindice);
        ret.assign(this);
        ret.add(nindice * n);
        return ret;
    }
    
    public NewShortPtr toShortPtr( int indice) {
        NewShortPtr ret = new NewShortPtr();
        ret.assign(this);
        ret.index += indice*nindice;
        return ret;
    }

    public int getOffset( int indice) {

        int ret_val = index + indice*nindice;
        return ret_val;
    }
}
