/*
 * NikonCompressedLoadRaw.java
 *
 * Created on 26 octobre 2007, 21:36
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
public class NikonCompressedLoadRaw implements LoadRaw {
    
    /** Creates a new instance of NikonCompressedLoadRaw */
    public NikonCompressedLoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.nikon_compressed_load_raw();
    }
}
