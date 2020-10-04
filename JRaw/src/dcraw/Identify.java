/*
 * Identify.java
 *
 * Created on 7 octobre 2007, 21:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dcraw;

/**
 *
 * @author YvesFabienne
 */
public class Identify {
    int fsize;
    String make;
    String model;
    boolean withjpeg;
    
    /** Creates a new instance of Identify */
    public Identify(int fsizeArg, String makeArg, String modelArg, boolean withjpegArg) {
        fsize = fsizeArg;
        make = makeArg;
        model = modelArg;
        withjpeg = withjpegArg;
    }
    
}
