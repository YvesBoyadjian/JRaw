/*
 * InputException.java
 *
 * Created on 16 décembre 2005, 13:29
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
public class InputException extends Exception {
    int error_code;
    
    /** Creates a new instance of InputException */
    public InputException( int error_codeArg) {
        error_code = error_codeArg;
    }
    
}
