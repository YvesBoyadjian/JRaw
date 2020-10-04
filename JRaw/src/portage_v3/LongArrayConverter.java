/*
 * LongArrayConverter.java
 *
 * Created on 27 février 2006, 10:36
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
public class LongArrayConverter extends Converter {
    MutableLong[] mla;
    public int[] jni;
    
    /** Creates a new instance of LongArrayConverter */
    public LongArrayConverter( MutableLong[] mlaArg) {
        mla = mlaArg;
        jni = new int[mla.length];
        int i = mla.length;
        while (i-- != 0)
            jni[i] = (int)mla[i].value;
    }
    
   void release() {
        int i = mla.length;
        while (i-- != 0)
            mla[i].value = jni[i];
    }
}
