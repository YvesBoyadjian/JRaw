/*
 * Packed12LoadRaw.java
 *
 * Created on 27 octobre 2007, 09:07
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
public class Packed12LoadRaw implements LoadRaw  {
    
    /** Creates a new instance of Packed12LoadRaw */
    public Packed12LoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.packed_12_load_raw();
    }
}
