/*
 * UnpackedLoadRaw.java
 *
 * Created on 30 septembre 2007, 20:11
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
public class UnpackedLoadRaw implements LoadRaw{
    
    /** Creates a new instance of UnpackedLoadRaw */
    public UnpackedLoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.unpacked_load_raw();
    }
}
