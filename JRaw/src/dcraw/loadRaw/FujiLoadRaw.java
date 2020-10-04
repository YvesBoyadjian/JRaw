/*
 * FujiLoadRaw.java
 *
 * Created on 26 octobre 2007, 22:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dcraw.loadRaw;

import dcraw.*;

/**
 *
 * @author YvesFabienne
 */
public class FujiLoadRaw implements LoadRaw {
    
    /** Creates a new instance of FujiLoadRaw */
    public FujiLoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.fuji_load_raw();
    }
}
