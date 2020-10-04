/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw;

import cToJava.CTOJ;
import cToJava.NewShortArrayPtr;
import dcraw.RawReader;
import java.awt.image.WritableRaster;

/**
 *
 * @author YvesFabienne
 */
public class ImageThread extends Thread {

    int image_width;
    int image_height;
    byte[] lut;
    boolean highlightArg;
    int finalOrientation;
    //NewShortArrayPtr image;
    char[][][] image_sharp;
    //BufferedImage loadedImageNew;
    //WritableRaster wr;
    int[] pixels;
    int nb_proc;
    int no_proc;

    public ImageThread(int image_width, int image_height, byte[] lut, boolean highlightArg, int finalOrientation, char[][][] image_sharp, int[] pixels, int nb_proc, int number) {

    this.image_width = image_width;
    this.image_height = image_height;
    this.lut = lut;
    this.highlightArg = highlightArg;
    this.finalOrientation = finalOrientation;
    this.image_sharp = image_sharp;
    //this.loadedImageNew = loadedImageNew;
    //this.wr = wr;
    this.pixels = pixels;
    this.nb_proc = nb_proc;
    this.no_proc = number;
    }
    public void run() {
        doLoop();
    }

    void doLoop()  {

        double plus = 0.75;
        double minus = (4*plus-1)/8;

        int indiceb = 2;
        int indicev = 1;
        int indicer = 0;

        int xstart = 1;
        int ystart = 1;
        int xend = image_width-2;
        int yend = image_height-2;

        int image_out_width = 0;
        int image_out_height = 0;

        if ( finalOrientation ==  RawReader.GAUCHE) {
            image_out_width = image_height-3;
            image_out_height = image_width-3;

            int xstep = image_out_height / nb_proc;
            xstart = 1 + xstep*no_proc;
            xend = 1 + xstep*(no_proc+1);
            if ( no_proc == nb_proc -1) {
                xend = 1 +image_out_height;
            }
        }
        else if ( finalOrientation ==  RawReader.DROIT) {
            image_out_width = image_height-3;
            image_out_height = image_width-3;

            int xstep = image_out_height / nb_proc;
            xstart = 1 + xstep*no_proc;
            xend = 1 + xstep*(no_proc+1);
            if ( no_proc == nb_proc -1) {
                xend = 1 +image_out_height;
            }
        }
        else {
            image_out_width = image_width-3;
            image_out_height = image_height-3;

            int ystep = image_out_height / nb_proc;
            ystart = 1 + ystep*no_proc;
            yend = 1 + ystep*(no_proc+1);
            if ( no_proc == nb_proc-1) {
                yend = 1 + image_out_height;
            }
        }

        int indice_pixel = 0;

        for ( int x=xstart; x < xend; x++) {

            for( int y=ystart; y < yend; y++) {

                int rouge16 = (int)image_sharp[x-1][y-1][0];
                int vert16 = (int)image_sharp[x-1][y-1][1];
                int bleu16 = (int)image_sharp[x-1][y-1][2];

                short bleu = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, bleu16), 0xffff)]);
                short vert = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, vert16), 0xffff)]);
                short rouge = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, rouge16), 0xffff)]);

                if ( highlightArg) {
                    if ( bleu >252)
                        bleu = 0;
                    if ( vert > 252)
                        vert = 0;
                    if ( rouge > 252)
                        rouge = 0;
                }

                int rgb = bleu + (vert << 8) + (rouge << 16);
                int x_out, y_out;
                if ( finalOrientation ==  RawReader.GAUCHE) {
                    x_out = y-1;
                    y_out = image_width-3 - (x-1)  - 1;
                        //loadedImageNew.setRGB( y -1, image_width-3 - (x-1)  - 1, rgb);
                }
                else if ( finalOrientation ==  RawReader.DROIT) {
                    x_out = image_height-3 - (y-1) - 1;
                    y_out = x -1;
                        //loadedImageNew.setRGB( image_height-3 - (y-1) - 1, x -1, rgb);
                }
                else {
                    x_out = x -1;
                    y_out = y -1;
                        //loadedImageNew.setRGB( x -1 , y -1 , rgb);
                }
                indice_pixel = ( x_out + y_out * image_out_width);
                pixels[indice_pixel] = rgb;//rouge;
                //pixels[indice_pixel+1] = vert;
                //pixels[indice_pixel+2] = bleu;
            }
            Thread.yield();
        }
    }
}
