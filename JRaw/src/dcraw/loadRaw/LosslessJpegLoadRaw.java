/*
 * LosslessJpegLoadRaw.java
 *
 * Created on 30 septembre 2007, 20:15
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
public class LosslessJpegLoadRaw implements LoadRaw{
    
    /** Creates a new instance of LosslessJpegLoadRaw */
    public LosslessJpegLoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.lossless_jpeg_load_raw();
    }
}
