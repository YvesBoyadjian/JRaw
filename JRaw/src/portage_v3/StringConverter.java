/*
 * StringConverter.java
 *
 * Created on 27 février 2006, 10:24
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
public class StringConverter extends Converter {
    CharPtr cp;
    public byte[] jni;
    
    /** Creates a new instance of StringConverter */
    public StringConverter( CharPtr cpArg) {
        cp = cpArg;
        int length = cp.capacity();
        jni = new byte[length];
        for ( int i =0; i< length; i++)        
            jni[i] = (byte) cp.charAt(i);
    }
    
    void release() {
        for ( int i=0; i< jni.length; i++)
            cp.setAt(i, (char) jni[i]);
    }
}
