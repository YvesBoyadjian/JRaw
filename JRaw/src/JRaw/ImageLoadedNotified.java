/*
 * ImageLoadedNotified.java
 *
 * Created on 17 novembre 2007, 21:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package JRaw;

import dcraw.RawReader;

/**
 *
 * @author YvesFabienne
 */
public interface ImageLoadedNotified {
 
    public void imageIsLoaded(int noFile, RawReader rr);
    public void addRunningTask();
    public void removeRunningTask();
    public float[][] getCalibrationMatrix(String vendor, String model);
    public void registerCalibrationMatrix(String vendor, String model, float[][] matrix);
    //public void increment(int memoryConsumption);
    public boolean clearOneRaw();
    //public void decrement(int rawIndex);
    public void memoryFull(int rawIndex);
}
