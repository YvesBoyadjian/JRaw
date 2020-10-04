/*
 * BlinkingTask.java
 *
 * Created on 13 décembre 2007, 21:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package JRaw;

import JRaw.ihm.MainJFrame;
import java.util.TimerTask;

/**
 *
 * @author YvesFabienne
 */
public class BlinkingTask extends TimerTask {
    MainJFrame parent;
    
    /** Creates a new instance of BlinkingTask */
    public BlinkingTask(MainJFrame parentArg) {
        parent = parentArg;
    }
    
    public void run() {
        parent.blink();
    }
}
