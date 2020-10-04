/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw.WhiteBalance;

import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author YvesFabienne
 */
public class WhiteBalance {
    String name;
    public float red;
    public float green;
    public float blue;


    WhiteBalance() {

    }
    
    WhiteBalance(String property) {
        String[] fields = property.split("_");
        name = fields[0];
        red = Float.parseFloat(fields[1]);
        green = Float.parseFloat(fields[2]);
        blue = Float.parseFloat(fields[3]);
    }

    WhiteBalance(String nameArg, float redArg, float greenArg, float blueArg) {
        name = nameArg;
        red = redArg;
        green = greenArg;
        blue = blueArg;
    }

    public boolean isDefault() {
        return false;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("_");
        sb.append(Float.toString(red));
        sb.append("_");
        sb.append(Float.toString(green));
        sb.append("_");
        sb.append(Float.toString(blue));

        return sb.toString();
    }
}
