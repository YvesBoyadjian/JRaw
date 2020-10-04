/*
 * MutableObject.java
 *
 * Created on 20 octobre 2005, 16:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

/**
 *
 * @author yvboyadj
 */
public class MutableObject implements MutableValue {
    public Object value;
    
    /** Creates a new instance of MutableObject */
    public MutableObject() {
        value = new Object();
    }
    
    public MutableObject(Object valueArg) {
        value = valueArg;
    }
}
