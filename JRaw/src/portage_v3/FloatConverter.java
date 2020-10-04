/*
 * FloatConverter.java
 *
 * Created on 24 février 2006, 16:18
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
public class FloatConverter extends Converter {
    MutableFloat mf;
    public float[] jni = new float[1];
    
    /** Creates a new instance of FloatConverter */
    public FloatConverter( MutableFloat mfArg) {
        mf = mfArg;
        jni[0] = mf.value;
    }
    
    void release() {
        mf.value = jni[0];
    }
}
