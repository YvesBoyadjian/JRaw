/*
 * AdobeCoeff.java
 *
 * Created on 7 octobre 2007, 20:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dcraw;

/**
 *
 * @author YvesFabienne
 */
public class AdobeCoeff {
    String prefix;
    short black;
    short maximum;
    short[] trans = new short[12];
    
    /** Creates a new instance of AdobeCoeff */
    public AdobeCoeff(String prefixArg, short blackArg, short maximumArg, short[] transArg) {
        prefix = prefixArg;
        black = blackArg;
        maximum = maximumArg;
        System.arraycopy(transArg, 0, trans, 0, transArg.length);
    }
    
}
