/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw;

import dcraw.RawReader;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 *
 * @author YvesFabienne
 */
public class HardOrSoftReference {
    private boolean hasBeenRead = false;
    private SoftReference sr;
    private RawReader rr;

    public HardOrSoftReference( RawReader rrArg) {
        rr = rrArg;
        sr = new SoftReference( rrArg);
    }

    public void setSoft() {
        if ( rr == null) {
            return; // déjà soft
        }
        System.out.printf("setSoft(), index = %d\n", rr.getIndex());
        rr = null;
        /*
        System.gc();
        System.runFinalization();
         * 
         */
    }

    public void setNull() {
        rr = null;
        sr.clear();
    }

    public void setRead() {
        hasBeenRead = true;
    }
    
    public RawReader get() {
        if ( rr != null)
            return rr;

        RawReader rrr = (RawReader)sr.get();

        return rrr;
    }
    public RawReader getHard() {
        return rr;
    }

    public boolean hasBeenRead() {
        return hasBeenRead;
    }
}
