/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw;

import java.util.TimerTask;

/**
 *
 * @author YvesFabienne
 */
public class GCTimerTask extends TimerTask {
    public final int MEGA_SPARE = 10;

    @Override
    public void run() {
            System.gc();
            System.runFinalization();
            /*
        try {
            int[] dummy = new int[1024*1024/4*MEGA_SPARE];
        } catch ( OutOfMemoryError e) {
            System.out.println("Ca sent le roussi timer");
            System.gc();
            System.runFinalization();
        }
             * 
             */
    }

}
