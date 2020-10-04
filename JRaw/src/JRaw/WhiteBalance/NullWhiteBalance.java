/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw.WhiteBalance;

/**
 *
 * @author YvesFabienne
 */
public class NullWhiteBalance extends WhiteBalance {

    NullWhiteBalance() {
        
    }

    public String getName() {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("JRaw/jraw");
        return bundle.getString("default");
    }

    public boolean isDefault() {
        return true;
    }
}
