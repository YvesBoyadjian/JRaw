/*
 * CanonA5LoadRaw.java
 *
 * Created on 26 octobre 2007, 21:41
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
public class CanonA5LoadRaw implements LoadRaw {
    
    /** Creates a new instance of CanonA5LoadRaw */
    public CanonA5LoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.canon_a5_load_raw();
    }
}
