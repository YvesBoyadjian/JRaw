/*
 * LongConverter.java
 *
 * Created on 24 février 2006, 16:19
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
public class LongConverter extends Converter {
    MutableLong ml;
    public int[] jni = new int[1];
    
    /** Creates a new instance of LongConverter */
    public LongConverter( MutableLong mlArg) {
        ml = mlArg;
        jni[0] = (int)ml.value;
    }

    void release() {
        ml.value = jni[0];
    }
}
