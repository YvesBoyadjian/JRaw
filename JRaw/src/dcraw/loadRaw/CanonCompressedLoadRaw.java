/*
 * CanonCompressedLoadRaw.java
 *
 * Created on 26 octobre 2007, 21:34
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
public class CanonCompressedLoadRaw implements LoadRaw {
    
    /** Creates a new instance of CanonCompressedLoadRaw */
    public CanonCompressedLoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.canon_compressed_load_raw();
    }
}
