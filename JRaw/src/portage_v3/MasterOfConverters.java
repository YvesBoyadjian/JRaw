/*
 * MasterOfConverters.java
 *
 * Created on 28 février 2006, 14:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.util.Vector;

/**
 *
 * @author w22w087
 */
public class MasterOfConverters {
    static Vector converters = new Vector();
    
    /** Creates a new instance of MasterOfConverters */
    public MasterOfConverters() {
    }
    
    static void add( Converter cv) {
        converters.add(cv);
    }
    
    private static Converter get( int i) {
        return ((Converter)converters.get(i));
    }
    
    public static void release() {
        for ( int i=0; i< converters.size(); i++)
            get(i).release();
        converters.clear();
    }
}
