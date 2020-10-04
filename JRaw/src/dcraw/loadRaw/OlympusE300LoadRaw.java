/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dcraw.loadRaw;

import dcraw.LoadRaw;
import dcraw.RawReader;

/**
 *
 * @author YvesFabienne
 */
public class OlympusE300LoadRaw implements LoadRaw  {
    
    /** Creates a new instance of NikonLoadRaw */
    public OlympusE300LoadRaw() {
    }
    
    public void loadRaw(RawReader rr) {
        rr.olympus_e300_load_raw();
    }
}
