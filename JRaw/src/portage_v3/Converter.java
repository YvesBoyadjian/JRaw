/*
 * Converter.java
 *
 * Created on 28 février 2006, 14:56
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
public abstract class Converter {
    
    /** Creates a new instance of Converter */
    public Converter() {
        MasterOfConverters.add(this);
    }
    
    abstract void release();
}
