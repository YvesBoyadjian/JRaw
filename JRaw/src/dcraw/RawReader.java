/*
 * Main.java
 *
 * Created on 7 novembre 2006, 21:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/*
   dcraw.c -- Dave Coffin's raw photo decoder
   Copyright 1997-2006 by Dave Coffin, dcoffin a cybercom o net

   This is a command-line ANSI C program to convert raw photos from
   any digital camera on any computer running any operating system.

   Attention!  Some parts of this program are restricted under the
   terms of the GNU General Public License.  Such code is enclosed
   in "BEGIN GPL BLOCK" and "END GPL BLOCK" declarations.
   Any code not declared GPL is free for all uses.

   Starting in Revision 1.237, the code to support Foveon cameras
   is under GPL.

   To lawfully redistribute dcraw.c, you must either (a) include
   full source code for all executable files containing restricted
   functions, (b) remove these functions, re-implement them, or
   copy them from an earlier, non-GPL Revision of dcraw.c, or (c)
   purchase a license from the author.

   $Revision: 1.354 $
   $Date: 2006/10/25 22:35:03 $
 */

package dcraw;

import dcraw.loadRaw.Canon600LoadRaw;
import dcraw.loadRaw.CanonA5LoadRaw;
import dcraw.loadRaw.CanonCompressedLoadRaw;
import dcraw.loadRaw.EightBitLoadRaw;
import dcraw.loadRaw.FoveonLoadRaw;
import dcraw.loadRaw.FujiLoadRaw;
import dcraw.loadRaw.ImaconFullLoadRaw;
import dcraw.loadRaw.KodakEasyLoadRaw;
import dcraw.loadRaw.KodakRadcLoadRaw;
import dcraw.loadRaw.LeafHdrLoadRaw;
import dcraw.loadRaw.LosslessJpegLoadRaw;
import dcraw.loadRaw.MinoltaRd175LoadRaw;
import dcraw.loadRaw.NikonCompressedLoadRaw;
import dcraw.loadRaw.NikonE2100LoadRaw;
import dcraw.loadRaw.NikonE900LoadRaw;
import dcraw.loadRaw.NikonLoadRaw;
import dcraw.loadRaw.OlympusCseriesLoadRaw;
import dcraw.loadRaw.Packed12LoadRaw;
import dcraw.loadRaw.RolleiLoadRaw;
import dcraw.loadRaw.SonyArwLoadRaw;
import dcraw.loadRaw.SonyLoadRaw;
import dcraw.loadRaw.UnpackedLoadRaw;
import JRaw.ImageLoadedNotified;
import JRaw.ImageThread;
import JRaw.ImageThreadDCRAW_9_12;
import java.nio.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import portage_v3.*;
import cToJava.*;
import dcraw.loadRaw.CanonSrawLoadRaw;
import dcraw.loadRaw.Kodak262LoadRaw;
import dcraw.loadRaw.Kodak65000LoadRaw;
import dcraw.loadRaw.KodakRgbLoadRaw;
import dcraw.loadRaw.KodakYcbcrLoadRaw;
import dcraw.loadRaw.OlympusE300LoadRaw;
import dcraw.loadRaw.PanasonicLoadRaw;
import dcraw.loadRaw.PentaxK10LoadRaw;
import dcraw.loadRaw.SonyArw2LoadRaw;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author YvesFabienne
 */
public class RawReader implements Runnable {
    private boolean hasBeenRead = false;
    int priority = Thread.MIN_PRIORITY;
    boolean isReadyToRun = false;
    public final float SCALE_FACTOR = 0.8f;
    public final int SHARP_9 = 1;
    public final int SHARP_16 = 2;
    File rawPhoto;
    File rawProperties;
    int sharpMode = SHARP_16;
    int memorySize = -1;
    
    int user_black = -1;
    int orientation = RawReader.NORMAL;
    float ev = 0.0f;
    boolean auto_wb = true;
    float red = 1.0f, green = 0.51f, blue = 1.0f;
    
    BufferedImage loadedImage = null;
    BufferedImage loadedImageHighlight = null;
    int rawIndex = -1;
    ImageLoadedNotified client;
    boolean hasFinished = false;
    boolean discarded = false;
    
    public final boolean SRGB_GAMMA = true;
    
    public static final int GAUCHE = -1;
    public static final int NORMAL = 0;
    public static final int DROIT = 1;
    
        int x_red, y_red, width_red, height_red;
        
        int x_green, y_green, width_green, height_green;
        
        int x_blue, y_blue, width_blue, height_blue;
        
        
        double[][] matrix = new double[3][3];
        
       boolean calibrating = false;
        
    /** Creates a new instance of Main */
    public RawReader(/*int indexArg,*/ ImageLoadedNotified clientArg) {
        System.out.println("____Création RawReader");
        client = clientArg;
        
        init_decode();
    }
    private void setRawIndex( int indexArg) {
        rawIndex = indexArg;
    }

    public int getIndex() {
        return rawIndex;
    }
    /*
    @Override
    protected void finalize() {
        System.out.println("Finalize, index =" + rawIndex);
        //client.decrement(rawIndex);
        image.nullify();
        try {
            super.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(RawReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    */
    public void discardThread() {
        discarded = true;
    }
    
    public void calibrate( 
            int x_start_red, 
            int x_end_red,
            int x_end_green,
            int x_end_blue,
            int y_start,
            int y_end) {
        
        width_red = (int)((x_end_red - x_start_red) * SCALE_FACTOR);
        height_red = (int)(( y_end - y_start) * SCALE_FACTOR);
        x_red = (x_end_red + x_start_red)/2 - width_red/2;
        y_red = (y_end - y_start)/2 - height_red/2;
        
        width_green = (int)((x_end_green - x_end_red) * SCALE_FACTOR);
        height_green = (int)(( y_end - y_start) * SCALE_FACTOR);
        x_green = (x_end_green + x_end_red)/2 - width_green/2;
        y_green = (y_end - y_start)/2 - height_green/2;
        
        width_blue = (int)((x_end_blue - x_end_green) * SCALE_FACTOR);
        height_blue = (int)(( y_end - y_start) * SCALE_FACTOR);
        x_blue = (x_end_blue + x_end_green)/2 - width_blue/2;
        y_blue = (y_end - y_start)/2 - height_blue/2;
        
        calibrating = true;
        
        run();
        
        
        calibrating =false;
        
        Graphics graph = loadedImage.getGraphics();
                
        graph.setColor(Color.BLACK);
        
        graph.drawRect( x_red, y_red, width_red, height_red);
        graph.drawRect( x_green, y_green, width_green, height_green);        
        graph.drawRect( x_blue, y_blue, width_blue, height_blue);
                
        graph.dispose();
    }
    
    boolean inside_red( int col, int row) {
        if ( col > x_red && col < x_red + width_red) {
            if ( row > y_red && row < y_red + height_red) {
                return true;
            }
        }
        return false;
    }
    
    boolean inside_green( int col, int row) {
        if ( col > x_green && col < x_green + width_green) {
            if ( row > y_green && row < y_green + height_green) {
                return true;
            }
        }
        return false;
    }
    
    boolean inside_blue( int col, int row) {
        if ( col > x_blue && col < x_blue + width_blue) {
            if ( row > y_blue && row < y_blue + height_blue) {
                return true;
            }
        }
        return false;
    }

    public void setEv( float evArg) {
        setOrientationAndEvAndRGB( orientation, evArg, red, green, blue, auto_wb);
    }
    public void setOrientation(int orientationArg) {
        setOrientationAndEvAndRGB( orientationArg, ev, red, green, blue, auto_wb);
    }
    public void setOrientationAndEvAndRGB(int orientationArg, float evArg, float redArg, float greenArg, float blueArg, boolean auto) {
        orientation = orientationArg;
        ev = evArg;
        
        auto_wb = auto;
        if ( auto_wb) {
	   for (int c=0; c < 4; c++)
               user_mul[c] = 0;
        }
        else {
            red = redArg;
            green = greenArg;
            blue = blueArg;
        }
        if ( Math.abs(ev - Math.round(ev)) < 0.01)
            ev = Math.round(ev);
        
        bright = (float)Math.pow(2.0f,ev);

        Properties props = new Properties();
            if( orientation == GAUCHE)
                props.setProperty("orientation", "GAUCHE");
            if ( orientation == NORMAL)
                props.setProperty("orientation", "NORMAL");
            if ( orientation == DROIT)
                props.setProperty("orientation", "DROITE");
            props.setProperty("ev", Float.toString(ev));

            if ( !auto_wb) {
                props.setProperty("red", Float.toString(red));
                props.setProperty("green", Float.toString(green));
                props.setProperty("blue", Float.toString(blue));
            }
            try {
                FileOutputStream fos = new FileOutputStream(rawProperties);
                props.store( fos,null);                
                fos.close();
            } catch ( FileNotFoundException e ) {
                JOptionPane.showMessageDialog(null,"File not found");
            } catch ( IOException e) {
                JOptionPane.showMessageDialog(null,"Error while saving config file");
            }
            //();
    }
    public int getOrientation() {
        return orientation;
    }
    
    public float getEv() {
        return ev;
    }
    
    public float getRed() {
        return red;
    }
    
    public float getGreen() {
        return green;
    }
    
    public float getBlue() {
        return blue;
    }

    public boolean hasAutoWB() {
        return auto_wb;
    }
    
    public float[][] getCalibrationMatrix() {
        return rgb_cam2;
    }
    
    public void setCalibrationMatrix( float[][] matrix) {
        rgb_cam2[0][0] = matrix[0][0];
        rgb_cam2[0][1] = matrix[0][1];
        rgb_cam2[0][2] = matrix[0][2];
        rgb_cam2[1][0] = matrix[1][0];
        rgb_cam2[1][1] = matrix[1][1];
        rgb_cam2[1][2] = matrix[1][2];
        rgb_cam2[2][0] = matrix[2][0];
        rgb_cam2[2][1] = matrix[2][1];
        rgb_cam2[2][2] = matrix[2][2];
        use_rgb_cam2 = true;
    }
    
    public void setFile( int rawIndex, File rawPhotoArg) {
        
        if ( rawIndex == 18) {
            int iii=0;
        }
        System.out.println("SetFile(), index = "+rawIndex);
        hasBeenRead = false;
        setRawIndex(rawIndex);
        rawPhoto = rawPhotoArg;
        
        String inputFile = rawPhoto.getPath();
        
        if ( inputFile.endsWith(".NEF")
        || inputFile.endsWith(".nef")
                || inputFile.endsWith(".PEF")
                || inputFile.endsWith(".pef")
                || inputFile.endsWith(".CRW")
                || inputFile.endsWith(".crw")
                || inputFile.endsWith(".CR2")
                || inputFile.endsWith(".cr2")
                || inputFile.endsWith(".PEF")
                || inputFile.endsWith(".pef")
                || inputFile.endsWith(".RAF")
                || inputFile.endsWith(".raf")
                || inputFile.endsWith(".RAW")
                || inputFile.endsWith(".raw"))
            inputFile = inputFile.substring(0, inputFile.length()-4);
            
         rawProperties = new File( inputFile+".props");

         orientation = RawReader.NORMAL;
         ev = 0.0f;
         bright = (float)Math.pow(2.0f,ev);
         auto_wb = true;
         red = 1.0f; green = 0.51f; blue = 1.0f;

         if ( rawProperties.exists()) {
            Properties props = new Properties();
            try {
                FileInputStream fis = new FileInputStream(rawProperties);
                props.load(fis);
                String dir = props.getProperty("orientation","NORMAL");
                String exposureValue = props.getProperty("ev","0");

                String red_str = props.getProperty("red", "");
                String green_str = props.getProperty("green","");
                String blue_str = props.getProperty("blue", "");

                if ( red_str.length()!=0 && green_str.length()!=0 && blue_str.length()!=0) {
                    auto_wb = false;
                }
                else {
                    auto_wb = true;
                }
                
                fis.close();
                ev = Float.parseFloat(exposureValue);
                bright = (float)Math.pow(2.0f,ev);

                if ( !auto_wb) {
                    red = Float.parseFloat(red_str);
                    green = Float.parseFloat(green_str);
                    blue = Float.parseFloat(blue_str);
                }
                if ( dir.equals("GAUCHE")) {
                    orientation = GAUCHE;
                }
                if ( dir.equals("DROITE")) {
                    orientation = DROIT;
                }
            } catch (FileNotFoundException e) {
            
            } catch ( IOException e) {
                JOptionPane.showMessageDialog( null,"Error while reading config file");
            }
         }
    }
    public BufferedImage getLoadedImage() {
        return loadedImage;
        //return GraphicTools.getScaledInstance(loadedImage, 200, 200, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
    }
    public BufferedImage getLoadedImageHighlight() {
        return loadedImageHighlight;
    }
    public BufferedImage computeLoadedImage() {
        switch( sharpMode ) {
            case SHARP_9:
            loadedImage = convertRawToImageSharp9(false);
            break;
            case SHARP_16:
            loadedImage = convertRawToImageSharp16_DCRAW_9_12(false,loadedImage);
            break;
        }
        return loadedImage;
    }

    public void invalidateLoadedImageHighLight() {
        loadedImageHighlight = null;
    }
    public BufferedImage computeLoadedImageHighlight() {
        switch( sharpMode ) {
            case SHARP_9:
            loadedImageHighlight = convertRawToImageSharp9( true);
            break;
            case SHARP_16:
            loadedImageHighlight = convertRawToImageSharp16_DCRAW_9_12( true,loadedImageHighlight);
            break;
        }
        return loadedImageHighlight;
    }

    /**
     *
     * @return
     * true si le calcul de l'image est terminé et s'est bien passé
     */
    public boolean hasFinished() {
        return hasFinished;
    }
    
public final static String VERSION="8.86";

/*
   NO_JPEG disables decoding of compressed Kodak DC120 files.
   NO_LCMS disables the "-p" option.
 */

public static final int LONG_BIT =8 * 4/*sizeof (long)*/;

/*
   All global variables are defined here, and all functions that
   access them are prefixed with "".  Note that a thread-safe
   C++ class cannot have non-const static local variables.
 */
Object ifp;
short order;
String ifname;// = new MutableCharPtr();
//BytePtr meta_data = new BytePtr();
CharPtr cdesc = new CharPtr(5);
CharPtr desc = new CharPtr(512);
CharPtr make = new CharPtr(64);
CharPtr model = new CharPtr(72);
CharPtr model2 = new CharPtr(64);
CharPtr artist = new CharPtr(64);
float flash_used, canon_ev, iso_speed, shutter, aperture, focal_len;
long timestamp;
int shot_order, kodak_cbpp, unique_id;
long filters, exif_cfa;
IntPtr oprof = new IntPtr();
int profile_offset, profile_length;
int thumb_offset, thumb_length, thumb_width, thumb_height, thumb_misc;
int data_offset, strip_offset, meta_offset, meta_length;
public int tiff_nifds, tiff_flip, tiff_bps, tiff_compress;
int raw_height, raw_width, top_margin, left_margin;
public int height, width, fuji_width, colors, tiff_samples;
int black, maximum, raw_color, use_gamma;
boolean zero_is_bad;
boolean data_error;
int iheight, iwidth, shrink, flip;
int zero_after_ff, is_raw, dng_version, is_foveon;
int tile_width, tile_length;
BytePtr gpsdata = new BytePtr(32);
double pixel_aspect;
public final NewShortArrayPtr image = new NewShortArrayPtr(4);
char[][][] image_sharp;
char[][] white = new char[8][8];
ShortPtr curve = new ShortPtr(0x4001);
ShortPtr new_curve = new ShortPtr(0x10000);
ShortPtr cr2_slice = new ShortPtr(3);
float bright=1.0f;
float[] user_mul={0.f,0.f,0.f,0.f};
float sigma_d=0.0f, sigma_r=0.0f;
int half_size=0, four_color_rgb=0, document_mode=0, highlight=0;
int verbose=0, use_auto_wb=0, use_camera_wb=0;
int output_color=1, output_bps=8, output_tiff=0;
int fuji_layout, shot_select = 0, fuji_secondary, use_secondary=0;
float[] cam_mul = new float[4], pre_mul = new float[4], sraw_mul = new float[4];
float[][] rgb_cam = new float[3][4];	/* RGB from camera color */
float [][] rgb_cam2 = new float[3][3];
boolean use_rgb_cam2 = false;
final double[][] xyz_rgb = {			/* XYZ from RGB */
  { 0.412453, 0.357580, 0.180423 },
  { 0.212671, 0.715160, 0.072169 },
  { 0.019334, 0.119193, 0.950227 } };
final float d65_white[] = { 0.950456f, 1.0f, 1.088754f };
int[][] histogram  = new int[4][0x2000];
FileWriter write_thumb, write_fun;
LoadRaw load_raw;
LoadRaw thumb_load_raw;
double[] gamm = { 0.45,4.5,0,0,0,0 };

final decode[] first_decode = new decode[2048];

void init_decode() {
    for ( int i=0; i< 2048; i++)
        first_decode[i] = new decode();
}
int second_decode, free_decode; // indice dans le tableau first_decode

TiffIfd[] tiff_ifd = new TiffIfd[10];
void init_tiffIfd() {
    for( int i=0; i< 10; i++)
        tiff_ifd[i] = new TiffIfd();
}

Phl ph1 = new Phl();

public float LIM( float x, float min, float max) {
    return Math.max(min,Math.min(x,max));
}
public int LIM( int x, int min, int max) {
    return Math.max(min,Math.min(x,max));
}
public float ULIM( float x, float y, float z) {
    return ((y) < (z) ? LIM(x,y,z) : LIM(x,z,y));
}
public int ULIM( int x, int y, int z) {
    return ((y) < (z) ? LIM(x,y,z) : LIM(x,z,y));
}
public float CLIP( float x) {
    return LIM(x,0,65535);
}
public int CLIP( int x) {
    return LIM(x,0,65535);
}

double SQR(double x) {
    return((x)*(x));
}
/*
   In order to inline this calculation, I make the risky
   assumption that all filter patterns can be described
   by a repeating pattern of eight rows and two columns

   Do not use the FC or BAYER macros with the Leaf CatchLight,
   because its pattern is 16x16, not 2x8.

   Return values are either 0/1/2/3 = G/M/C/Y or 0/1/2/3 = R/G1/B/G2

	PowerShot 600	PowerShot A50	PowerShot Pro70	Pro90 & G1
	0xe1e4e1e4:	0x1b4e4b1e:	0x1e4b4e1b:	0xb4b4b4b4:

	  0 1 2 3 4 5	  0 1 2 3 4 5	  0 1 2 3 4 5	  0 1 2 3 4 5
	0 G M G M G M	0 C Y C Y C Y	0 Y C Y C Y C	0 G M G M G M
	1 C Y C Y C Y	1 M G M G M G	1 M G M G M G	1 Y C Y C Y C
	2 M G M G M G	2 Y C Y C Y C	2 C Y C Y C Y
	3 C Y C Y C Y	3 G M G M G M	3 G M G M G M
			4 C Y C Y C Y	4 Y C Y C Y C
	PowerShot A5	5 G M G M G M	5 G M G M G M
	0x1e4e1e4e:	6 Y C Y C Y C	6 C Y C Y C Y
			7 M G M G M G	7 M G M G M G
	  0 1 2 3 4 5
	0 C Y C Y C Y
	1 G M G M G M
	2 C Y C Y C Y
	3 M G M G M G

   All RGB cameras use one of these Bayer grids:

	0x16161616:	0x61616161:	0x49494949:	0x94949494:

	  0 1 2 3 4 5	  0 1 2 3 4 5	  0 1 2 3 4 5	  0 1 2 3 4 5
	0 B G B G B G	0 G R G R G R	0 G B G B G B	0 R G R G R G
	1 G R G R G R	1 B G B G B G	1 R G R G R G	1 G B G B G B
	2 B G B G B G	2 G R G R G R	2 G B G B G B	2 R G R G R G
	3 G R G R G R	3 B G B G B G	3 R G R G R G	3 G B G B G B
 */

public long FC( int row, int col){
	return (filters >> ((((row) << 1 & 14) + ((col) & 1)) << 1) & 3);
}

public int BAYER( int row, int col) {
	return CTOJ.toUnsigned(image.at((int)(((row) >> shrink)*iwidth + ((col) >> shrink)),(int)FC(row,col)));
}

public int BAYER( int row, int col, short value) {
    return CTOJ.toUnsigned(image.at((int)(((row) >> shrink)*iwidth + ((col) >> shrink)),(int)FC(row,col), value));
}

public int BAYER2( int row, int col) {
	return CTOJ.toUnsigned(image.at(((row) >> shrink)*iwidth + ((col) >> shrink),fc(row,col)));
}

public int BAYER2( int row, int col, short value) {
	return CTOJ.toUnsigned(image.at(((row) >> shrink)*iwidth + ((col) >> shrink),fc(row,col),value));
}

static final byte[][] filter =
  { { 2,1,1,3,2,3,2,0,3,2,3,0,1,2,1,0 },
    { 0,3,0,2,0,1,3,1,0,1,1,2,0,3,3,2 },
    { 2,3,3,2,3,1,1,3,3,1,2,1,2,0,0,3 },
    { 0,1,0,1,0,2,0,2,2,0,3,0,1,3,2,1 },
    { 3,1,1,2,0,1,0,2,1,3,1,3,0,1,3,0 },
    { 2,0,0,3,3,2,3,1,2,0,2,0,3,2,2,1 },
    { 2,3,3,1,2,1,2,1,2,1,1,2,3,0,0,1 },
    { 1,0,0,2,3,0,0,3,0,3,0,3,2,1,2,3 },
    { 2,3,3,1,1,2,1,0,3,2,3,0,2,3,1,3 },
    { 1,0,2,0,3,0,3,2,0,1,1,2,0,1,0,2 },
    { 0,1,1,3,3,2,2,1,1,3,3,0,2,1,3,2 },
    { 2,3,2,0,0,1,3,0,2,0,1,2,3,0,1,0 },
    { 1,3,1,2,3,2,3,2,0,2,0,1,1,0,3,0 },
    { 0,2,0,3,1,0,0,1,1,3,3,2,3,2,2,1 },
    { 2,1,3,2,3,1,2,1,0,3,0,2,0,2,0,2 },
    { 0,3,1,0,0,2,0,3,2,1,3,1,1,3,1,3 } };

int  fc (int row, int col)
{
  if (filters != 1) return (int)FC(row,col);
  return filter[(row+top_margin) & 15][(col+left_margin) & 15];
}

MutableCharPtr memmem (MutableCharPtr haystack, int haystacklen,
	      MutableCharPtr needle, int needlelen)
{
  MutableCharPtr c = new MutableCharPtr();
  for (c.assign(haystack); c.lessOrEqualThan( haystack.plus(haystacklen).plus( - needlelen)); c.plusPlus())
    if ( Uc.memcmp (c, needle, needlelen)==0)
      return c;
  return new MutableCharPtr();
}

void merror ( VoidPtr ptr, String where)
{
  if ( !ptr.isNull()) return;
  System.err.printf ( "%s: Out of memory in %s\n", ifname, where);
  //longjmp (failure, 1);
}

void derror()
{
  if (!data_error) {
    Uc.printf (/*stderr,*/ "%s: ", ifname);
    if (CTOJ.feof(ifp))
      Uc.printf ("Unexpected end of file\n");
    else
      Uc.printf ("Corrupt data \n");
  }
  data_error = true;
}


int  sget2 ( BytePtr s)
{
  if (order == 0x4949)		/* "II" means little-endian */
    return (CTOJ.toUnsigned(s.at(0)) | CTOJ.toUnsigned(s.at(1)) << 8);
  else				/* "MM" means big-endian */
    return (CTOJ.toUnsigned(s.at(0)) << 8 | CTOJ.toUnsigned(s.at(1)));
}

int  sget2 ( byte[] s)
{
  if (order == 0x4949)		/* "II" means little-endian */
    return (CTOJ.toUnsigned(s[0]) | CTOJ.toUnsigned(s[1]) << 8);
  else				/* "MM" means big-endian */
    return (CTOJ.toUnsigned(s[0]) << 8 | CTOJ.toUnsigned(s[1]));
}

    byte str2[] = { (byte)0xff,(byte)0xff };
    byte str4[] = { (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff };
int  get2()
{
  //byte str[] = { (byte)0xff,(byte)0xff };
  /*
  BytePtr str = new BytePtr();
  str.assign( CTOJ.malloc(2));
  str.at(0,(byte)0xff);
  str.at(1,(byte)0xff);
   * 
   */
  
  CTOJ.fread (str2, 1, 2, ifp);
  return sget2(str2);
}

int  sget4 ( BytePtr s)
{
  if (order == 0x4949)
    return CTOJ.toUnsigned(s.at(0)) | CTOJ.toUnsigned(s.at(1)) << 8 | CTOJ.toUnsigned(s.at(2)) << 16 | CTOJ.toUnsigned(s.at(3)) << 24;
  else
    return CTOJ.toUnsigned(s.at(0)) << 24 | CTOJ.toUnsigned(s.at(1)) << 16 | CTOJ.toUnsigned(s.at(2)) << 8 | CTOJ.toUnsigned(s.at(3));
}

int  sget4 ( byte[] s)
{
  if (order == 0x4949)
    return CTOJ.toUnsigned(s[0]) | CTOJ.toUnsigned(s[1]) << 8 | CTOJ.toUnsigned(s[2]) << 16 | CTOJ.toUnsigned(s[3]) << 24;
  else
    return CTOJ.toUnsigned(s[0]) << 24 | CTOJ.toUnsigned(s[1]) << 16 | CTOJ.toUnsigned(s[2]) << 8 | CTOJ.toUnsigned(s[3]);
}

int  get4()
{
  //byte str[] = { (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff };
  /*
  BytePtr str = new BytePtr();
  str.assign( CTOJ.malloc(4));
  str.at(0,(byte)0xff);
  str.at(1,(byte)0xff);
  str.at(2,(byte)0xff);
  str.at(3,(byte)0xff);
   *
   */
  
  CTOJ.fread (str4, 1, 4, ifp);
  return sget4(str4);
}

int  getint (int type)
{
  return type == 3 ? get2() : get4();
}

float  int_to_float (int i)
{
    ByteBuffer buf = ByteBuffer.allocate(4);
    buf.putInt(i);
    buf.rewind();
    return buf.getFloat();
}

double  getreal (int type)
{
    ByteBuffer u = ByteBuffer.allocate(8);
    DoubleBuffer d = u.asDoubleBuffer();
//  union { char c[8]; double d; } u;
  int i, rev;
  double x;

  switch (type) {
    case 3: return get2();
    case 4: return get4();
    case 5:  x =  get4();
      return x / get4();
    case 8: return get2();
    case 9: return get4();
    case 10: x = get4();
      return x / get4();
    case 11: return int_to_float (get4());
    default: return CTOJ.fgetc(ifp);
  }
}
double getrat() {
    return getreal(10);
}

void  read_shorts ( ShortPtr pixel, int count)
{
  CTOJ.fread (pixel, 2, count, ifp);
  if ((order == 0x4949))
    CTOJ.swab (pixel, pixel, count*2);
}

void  read_shorts ( NewShortPtr pixel2, int count)
{
    ShortPtr pixel = new ShortPtr(count);
    
  CTOJ.fread (pixel, 2, count, ifp);
  if ((order == 0x4949))
    CTOJ.swab (pixel, pixel, count*2);
  
  for ( int i=0; i<count;i++)
      pixel2.at(i, pixel.at(i));
}

void  canon_600_fixed_wb (int temp)
{
  final short mul[][] = {
    {  667, 358,397,565,452 },
    {  731, 390,367,499,517 },
    { 1119, 396,348,448,537 },
    { 1399, 485,431,508,688 } };
  int lo, hi, i;
  float frac=0;

  for (lo=4; --lo != 0; )
    if (mul[lo][0] <= temp) break;
  for (hi=0; hi < 3; hi++)
    if (mul[hi][0] >= temp) break;
  if (lo != hi)
    frac = (float) (temp - mul[lo][0]) / ( mul[hi][0] - mul[lo][0]);
  for (i=1; i < 5; i++)
    pre_mul[i-1] = 1 / (frac * mul[hi][i] + (1-frac) * mul[lo][i]);
}

/* Return values:  0 = white  1 = near white  2 = not white */
int  canon_600_color (int ratio[], int mar)
{
  int clipped=0, target, miss;

  if (flash_used != 0.0) {
    if (ratio[1] < -104)
      { ratio[1] = -104; clipped = 1; }
    if (ratio[1] >   12)
      { ratio[1] =   12; clipped = 1; }
  } else {
    if (ratio[1] < -264 || ratio[1] > 461) return 2;
    if (ratio[1] < -50)
      { ratio[1] = -50; clipped = 1; }
    if (ratio[1] > 307)
      { ratio[1] = 307; clipped = 1; }
  }
  target = flash_used != 0.0 || ratio[1] < 197
	? -38 - (398 * ratio[1] >> 10)
	: -123 + (48 * ratio[1] >> 10);
  if (target - mar <= ratio[0] &&
      target + 20  >= ratio[0] && clipped == 0) return 0;
  miss = target - ratio[0];
  if ( Math.abs(miss) >= mar*4) return 2;
  if (miss < -20) miss = -20;
  if (miss > mar) miss = mar;
  ratio[0] = target - miss;
  return 1;
}


void  canon_600_coeff()
{
    final short table[][] = {
    { -190,702,-1878,2390,   1861,-1349,905,-393, -432,944,2617,-2105  },
    { -1203,1715,-1136,1648, 1388,-876,267,245,  -1641,2153,3921,-3409 },
    { -615,1127,-1563,2075,  1437,-925,509,3,     -756,1268,2519,-2007 },
    { -190,702,-1886,2398,   2153,-1641,763,-251, -452,964,3040,-2528  },
    { -190,702,-1878,2390,   1861,-1349,905,-393, -432,944,2617,-2105  },
    { -807,1319,-1785,2297,  1388,-876,769,-257,  -230,742,2067,-1555  } };
  int t=0, i, c;
  float mc, yc;

  mc = pre_mul[1] / pre_mul[2];
  yc = pre_mul[3] / pre_mul[2];
  if (mc > 1 && mc <= 1.28 && yc < 0.8789) t=1;
  if (mc > 1.28 && mc <= 2) {
    if  (yc < 0.8789) t=3;
    else if (yc <= 2) t=4;
  }
  if (flash_used != 0) t=5;
  for (raw_color = i=0; i < 3; i++)
    for (c=0; c < colors; c++) rgb_cam[i][c] = table[t][i*4 + c] / 1024.0f;
}

void remove_zeroes()
{
  int row, col, tot, n, r, c;

  for (row=0; row < height; row++)
    for (col=0; col < width; col++)
      if (BAYER(row,col) == 0) {
	tot = n = 0;
	for (r = row-2; r <= row+2; r++)
	  for (c = col-2; c <= col+2; c++)
	    if (r < height && c < width &&
		FC(r,c) == FC(row,col) && BAYER(r,c)!=0) {
                n++;
	      tot += BAYER(r,c);
            }
	if (n!=0) BAYER(row,col, (short)(tot/n));
      }
}

public void canon_a5_load_raw()
{
    NewShortPtr data = new NewShortPtr( new short[2565]);
    NewShortPtr dp = new NewShortPtr();
    int pixel;
  
  int vbits=0, buf=0, row, col, bc=0;

  order = 0x4949;
  for (row=-top_margin; row < raw_height-top_margin; row++) {
    read_shorts (dp.assign(data), raw_width * 10 / 16);
    for (col=-left_margin; col < raw_width-left_margin; col++) {
      if (vbits < 10) {
          vbits += 16;
            buf =  (buf << 16) + CTOJ.toUnsigned(dp.at(0));
            dp.add(1);
      }
      pixel = buf >> (vbits -= 10) & 0x3ff;
      if ( CTOJ.cutToUnsigned(row) < height && CTOJ.cutToUnsigned(col) < width)
	BAYER(row,col, (short)pixel);
      else if (col > 1-left_margin && col != width) {
          bc++;
	black += pixel;
      }
    }
  }
  if (bc !=0) black /= bc;
  maximum = 0x3ff;
  if (raw_width > 1600) remove_zeroes();
}



/*
   getbits(-1) initializes the buffer
   getbits(n) where 0 <= n <= 25 returns an n-bit integer
 */
  long getbits_bitbuf=0;
  int getbits_vbits=0;
  boolean getbits_reset=false;
long  getbits (int nbits)
{
  int c;

  if (nbits == -1) {
    getbits_reset = false;
    return getbits_bitbuf = getbits_vbits = 0;
  }
  if (nbits == 0 || getbits_reset ) return 0;
  while (getbits_vbits < nbits) {
    c = CTOJ.fgetc(ifp);
    if (( getbits_reset = zero_after_ff!=0 && c == 0xff && CTOJ.fgetc(ifp) != 0)) return 0;
    getbits_bitbuf = CTOJ.cutToUnsigned(CTOJ.cutToUnsigned(getbits_bitbuf << 8) + c);
    getbits_vbits += 8;
  }
  getbits_vbits -= nbits;
  return  CTOJ.cutToUnsigned(getbits_bitbuf << (32-nbits-getbits_vbits)) >> (32-nbits);
}

void  init_decoder()
{
    int nb = first_decode.length;
    for ( int i=0; i< nb; i++) {
        decode dec = first_decode[i];
        dec.leaf = 0;
        dec.branch0 = -1; // equivalent de null
        dec.branch1 = -1;
    }
//  memset (first_decode, 0, sizeof first_decode);
  free_decode = 0; //first_decode;
}

/*
   Construct a decode tree according the specification in *source.
   The first 16 bytes specify how many codes should be 1-bit, 2-bit
   3-bit, etc.  Bytes after that are the leaf values.

   For example, if the source is

    { 0,1,4,2,3,1,2,0,0,0,0,0,0,0,0,0,
      0x04,0x03,0x05,0x06,0x02,0x07,0x01,0x08,0x09,0x00,0x0a,0x0b,0xff  },

   then the code is

	00		0x04
	010		0x03
	011		0x05
	100		0x06
	101		0x02
	1100		0x07
	1101		0x01
	11100		0x08
	11101		0x09
	11110		0x00
	111110		0x0a
	1111110		0x0b
	1111111		0xff
 */
  int make_decoder_leaf;
  
BytePtr make_decoder ( BytePtr source, int level)
{
  int cur;
  int i, next;
  
//  System.out.printf("level = %d, free_decode = %d\n", level, free_decode);

  if (level==0) make_decoder_leaf=0;
  cur = free_decode++;
  if (free_decode > 2048) {
    System.err.printf ("%s: decoder table overflow\n", ifname);
    //longjmp (failure, 2);
  }
  for (i=next=0; i <= make_decoder_leaf && next < 16; ) {
    i += source.uat(next);
    next++;
  }
  if (i > make_decoder_leaf) {
    if (level < next) {
      first_decode[cur].branch0 = free_decode;
      make_decoder (source, level+1);
      first_decode[cur].branch1 = free_decode;
      make_decoder (source, level+1);
    } else {
      first_decode[cur].leaf = source.uat(16 + make_decoder_leaf);
      make_decoder_leaf++;
//      System.out.printf("leaf = %d\n", first_decode[cur].leaf);
    }
  }
  return source.plus(16 + make_decoder_leaf);
}

void crw_init_tables (int table)
{
    short[][] first_tree =    /* [3][29] */
    {
    { 0,1,4,2,3,1,2,0,0,0,0,0,0,0,0,0,
      0x04,0x03,0x05,0x06,0x02,0x07,0x01,0x08,0x09,0x00,0x0a,0x0b,0xff  },
    { 0,2,2,3,1,1,1,1,2,0,0,0,0,0,0,0,
      0x03,0x02,0x04,0x01,0x05,0x00,0x06,0x07,0x09,0x08,0x0a,0x0b,0xff  },
    { 0,0,6,3,1,1,2,0,0,0,0,0,0,0,0,0,
      0x06,0x05,0x07,0x04,0x08,0x03,0x09,0x02,0x00,0x0a,0x01,0x0b,0xff  },
  };
     
  short[][] second_tree = {     /* [3][180] */
    { 0,2,2,2,1,4,2,1,2,5,1,1,0,0,0,139,
      0x03,0x04,0x02,0x05,0x01,0x06,0x07,0x08,
      0x12,0x13,0x11,0x14,0x09,0x15,0x22,0x00,0x21,0x16,0x0a,0xf0,
      0x23,0x17,0x24,0x31,0x32,0x18,0x19,0x33,0x25,0x41,0x34,0x42,
      0x35,0x51,0x36,0x37,0x38,0x29,0x79,0x26,0x1a,0x39,0x56,0x57,
      0x28,0x27,0x52,0x55,0x58,0x43,0x76,0x59,0x77,0x54,0x61,0xf9,
      0x71,0x78,0x75,0x96,0x97,0x49,0xb7,0x53,0xd7,0x74,0xb6,0x98,
      0x47,0x48,0x95,0x69,0x99,0x91,0xfa,0xb8,0x68,0xb5,0xb9,0xd6,
      0xf7,0xd8,0x67,0x46,0x45,0x94,0x89,0xf8,0x81,0xd5,0xf6,0xb4,
      0x88,0xb1,0x2a,0x44,0x72,0xd9,0x87,0x66,0xd4,0xf5,0x3a,0xa7,
      0x73,0xa9,0xa8,0x86,0x62,0xc7,0x65,0xc8,0xc9,0xa1,0xf4,0xd1,
      0xe9,0x5a,0x92,0x85,0xa6,0xe7,0x93,0xe8,0xc1,0xc6,0x7a,0x64,
      0xe1,0x4a,0x6a,0xe6,0xb3,0xf1,0xd3,0xa5,0x8a,0xb2,0x9a,0xba,
      0x84,0xa4,0x63,0xe5,0xc5,0xf3,0xd2,0xc4,0x82,0xaa,0xda,0xe4,
      0xf2,0xca,0x83,0xa3,0xa2,0xc3,0xea,0xc2,0xe2,0xe3,0xff,0xff  },
    { 0,2,2,1,4,1,4,1,3,3,1,0,0,0,0,140,
      0x02,0x03,0x01,0x04,0x05,0x12,0x11,0x06,
      0x13,0x07,0x08,0x14,0x22,0x09,0x21,0x00,0x23,0x15,0x31,0x32,
      0x0a,0x16,0xf0,0x24,0x33,0x41,0x42,0x19,0x17,0x25,0x18,0x51,
      0x34,0x43,0x52,0x29,0x35,0x61,0x39,0x71,0x62,0x36,0x53,0x26,
      0x38,0x1a,0x37,0x81,0x27,0x91,0x79,0x55,0x45,0x28,0x72,0x59,
      0xa1,0xb1,0x44,0x69,0x54,0x58,0xd1,0xfa,0x57,0xe1,0xf1,0xb9,
      0x49,0x47,0x63,0x6a,0xf9,0x56,0x46,0xa8,0x2a,0x4a,0x78,0x99,
      0x3a,0x75,0x74,0x86,0x65,0xc1,0x76,0xb6,0x96,0xd6,0x89,0x85,
      0xc9,0xf5,0x95,0xb4,0xc7,0xf7,0x8a,0x97,0xb8,0x73,0xb7,0xd8,
      0xd9,0x87,0xa7,0x7a,0x48,0x82,0x84,0xea,0xf4,0xa6,0xc5,0x5a,
      0x94,0xa4,0xc6,0x92,0xc3,0x68,0xb5,0xc8,0xe4,0xe5,0xe6,0xe9,
      0xa2,0xa3,0xe3,0xc2,0x66,0x67,0x93,0xaa,0xd4,0xd5,0xe7,0xf8,
      0x88,0x9a,0xd7,0x77,0xc4,0x64,0xe2,0x98,0xa5,0xca,0xda,0xe8,
      0xf3,0xf6,0xa9,0xb2,0xb3,0xf2,0xd2,0x83,0xba,0xd3,0xff,0xff  },
    { 0,0,6,2,1,3,3,2,5,1,2,2,8,10,0,117,
      0x04,0x05,0x03,0x06,0x02,0x07,0x01,0x08,
      0x09,0x12,0x13,0x14,0x11,0x15,0x0a,0x16,0x17,0xf0,0x00,0x22,
      0x21,0x18,0x23,0x19,0x24,0x32,0x31,0x25,0x33,0x38,0x37,0x34,
      0x35,0x36,0x39,0x79,0x57,0x58,0x59,0x28,0x56,0x78,0x27,0x41,
      0x29,0x77,0x26,0x42,0x76,0x99,0x1a,0x55,0x98,0x97,0xf9,0x48,
      0x54,0x96,0x89,0x47,0xb7,0x49,0xfa,0x75,0x68,0xb6,0x67,0x69,
      0xb9,0xb8,0xd8,0x52,0xd7,0x88,0xb5,0x74,0x51,0x46,0xd9,0xf8,
      0x3a,0xd6,0x87,0x45,0x7a,0x95,0xd5,0xf6,0x86,0xb4,0xa9,0x94,
      0x53,0x2a,0xa8,0x43,0xf5,0xf7,0xd4,0x66,0xa7,0x5a,0x44,0x8a,
      0xc9,0xe8,0xc8,0xe7,0x9a,0x6a,0x73,0x4a,0x61,0xc7,0xf4,0xc6,
      0x65,0xe9,0x72,0xe6,0x71,0x91,0x93,0xa6,0xda,0x92,0x85,0x62,
      0xf3,0xc5,0xb2,0xa4,0x84,0xba,0x64,0xa5,0xb3,0xd2,0x81,0xe5,
      0xd3,0xaa,0xc4,0xca,0xf2,0xb1,0xe4,0xd1,0x83,0x63,0xea,0xc3,
      0xe2,0x82,0xf1,0xa3,0xc2,0xa1,0xc1,0xe3,0xa2,0xe1,0xff,0xff  }
  };
  if (table > 2) table = 2;
  init_decoder();
  
  BytePtr[] first_tree_ptr = new BytePtr[3];
  for ( int i=0; i< 3; i++)
      first_tree_ptr[i] = new BytePtr(first_tree[i]);
  
  BytePtr[] second_tree_ptr = new BytePtr[3];
  for ( int i=0; i< 3; i++)
      second_tree_ptr[i] = new BytePtr(second_tree[i]);
  
  make_decoder ( first_tree_ptr[table], 0);
  second_decode = free_decode;
  make_decoder (second_tree_ptr[table], 0);
}

/*
   Return 0 if the image starts with compressed data,
   1 if it starts with uncompressed low-order bits.

   In Canon compressed data, 0xff is always followed by 0x00.
 */
int canon_has_lowbits()
{
  BytePtr test = new BytePtr(0x4000);
  int ret=1, i;

  CTOJ.fseek (ifp, 0, CTOJ.SEEK_SET);
  CTOJ.fread (test, 1, /*sizeof test*/0x4000, ifp);
  for (i=540; i < /*sizeof test*/0x4000 - 1; i++)
    if ( CTOJ.toUnsigned(test.at(i)) == 0xff) {
      if (test.at(i+1) != 0) return 1;
      ret=0;
    }
  return ret;
}

public void canon_compressed_load_raw()
{
  NewShortPtr pixel = new NewShortPtr();
  NewShortPtr prow = new NewShortPtr();
  int nblocks, lowbits, i, row, r, col, save, val;
  long irow, icol;
  int decode, dindex;
  int block, leaf, len, diff, carry=0, pnum=0;
  int [] diffbuf = new int[64], base = new int[2];
  int c;

  crw_init_tables (tiff_compress);
  pixel.assign( CTOJ.callocShort (raw_width*8, 2/*sizeof *pixel*/));
  //merror (pixel, "canon_compressed_load_raw()");
  lowbits = canon_has_lowbits();
  if (lowbits == 0) maximum = 0x3ff;
  CTOJ.fseek (ifp, 540 + lowbits*raw_height*raw_width/4, CTOJ.SEEK_SET);
  zero_after_ff = 1;
  getbits(-1);
  for (row=0; row < raw_height; row+=8) {
    nblocks = Math.min (8, raw_height-row) * raw_width >> 6;
    for (block=0; block < nblocks; block++) {
      Arrays.fill(diffbuf, 0);
      //memset (diffbuf, 0, sizeof diffbuf);
      decode = 0;//first_decode;
      for (i=0; i < 64; i++ ) {
	for (dindex=decode; first_decode[dindex].branch0 != -1; )
	  dindex = first_decode[dindex].branch((int)getbits(1));
	leaf = first_decode[dindex].leaf;
	decode = second_decode;
	if (leaf == 0 && i!=0) break;
	if (leaf == 0xff) continue;
	i  += leaf >> 4;
	len = leaf & 15;
	if (len == 0) continue;
	diff = (int)getbits(len);
	if ((diff & (1 << (len-1))) == 0)
	  diff -= (1 << len) - 1;
	if (i < 64) diffbuf[i] = diff;
      }
      diffbuf[0] += carry;
      carry = diffbuf[0];
      for (i=0; i < 64; i++ ) {
	if (pnum++ % raw_width == 0)
	  base[0] = base[1] = 512;
	if (((pixel.at((block << 6) + i, (short)(base[i & 1] += diffbuf[i]))) >> 10) != 0)
	  derror();
      }
    }
    if (lowbits != 0) {
      save = CTOJ.ftell(ifp);
      CTOJ.fseek (ifp, 26 + row*raw_width/4, CTOJ.SEEK_SET);
      for (prow.assign(pixel), i=0; i < raw_width*2; i++) {
	c = CTOJ.fgetc(ifp);
	for (r=0; r < 8; r+=2, prow.add(1)) {
	  val = ( prow.at(0) << 2) + ((c >> r) & 3);
	  if (raw_width == 2672 && val < 512) val += 2;
	  prow.at(0, (short)val);
	}
      }
      CTOJ.fseek (ifp, save, CTOJ.SEEK_SET);
    }
    for (r=0; r < 8; r++) {
      irow = CTOJ.toUnsigned( (int)(row - top_margin + r));
      if (irow >= height) continue;
      for (col=0; col < raw_width; col++) {
	icol = CTOJ.toUnsigned( (int)(col - left_margin));
	if (icol < width)
	  BAYER((int)irow,(int)icol, pixel.at(r*raw_width+col));
	else
	  black += pixel.at(r*raw_width+col);
      }
    }
  }
  //free (pixel);
  if (raw_width > width)
    black /= (raw_width - width) * height;
}

/*
   Not a full implementation of Lossless JPEG, just
   enough to decode Canon, Kodak and Adobe DNG images.
 */

int  ljpeg_start ( jhead jh, int info_only)
{
  int i, tag, len;
  BytePtr data = new BytePtr();
  data.assign(  CTOJ.malloc(0x10000));
  BytePtr dp = new BytePtr();

  init_decoder();
  for (i=0; i < 4; i++)
    jh.huff[i] = free_decode;
  jh.restart = Integer.MAX_VALUE;
  CTOJ.fread (data, 2, 1, ifp);
  if (data.uat(1) != 0xd8) return 0;
  do {
    CTOJ.fread (data, 2, 2, ifp);
    tag =  data.uat(0) << 8 | data.uat(1);
    len = (data.uat(2) << 8 | data.uat(3)) - 2;
    if (tag <= 0xff00) return 0;
    CTOJ.fread (data, 1, len, ifp);
    switch (tag) {
      case 0xffc3:
          jh.sraw = (data.uat(7) == 0x21) ? 1 :0;
      case 0xffc0:
	jh.bits = data.uat(0);
	jh.high = data.uat(1) << 8 | data.uat(2);
	jh.wide = data.uat(3) << 8 | data.uat(4);
	jh.clrs = data.uat(5) + jh.sraw;
        if ( len == 9 && dng_version == 0) CTOJ.getc(ifp);
	break;
      case 0xffc4:
	if (info_only != 0) break;
	for (dp.assign(data); dp.lessThan(data.plus(len)) && dp.uat(0) < 4; ) {
	  jh.huff[dp.uat(0)] = free_decode;
          dp.plusPlus();
	  dp.assign( make_decoder ( dp, 0));
	}
	break;
      case 0xffda:
	jh.psv = data.uat(1+data.uat(0)*2);
	break;
      case 0xffdd:
	jh.restart = data.uat(0) << 8 | data.uat(1);
    }
  } while (tag != 0xffda);
  if (info_only != 0) return 1;
  
  if (jh.sraw != 0) {
    jh.huff[3] = jh.huff[2] = jh.huff[1];
    jh.huff[1] = jh.huff[0];
  }
  jh.row.assign(CTOJ.calloc(jh.wide*jh.clrs, 4));
  //merror (jh.row, " jpeg_start()");
  return zero_after_ff = 1;
}

int  ljpeg_diff ( int dindex)
{
  int len, diff;

  while ( first_decode[dindex].branch0 != -1)
    dindex = first_decode[dindex].branch((int)getbits(1));
  len = first_decode[dindex].leaf;
  if (len == 16 && ( dng_version == 0 || dng_version >= 0x1010000))
    return -32768;
  diff = (int)getbits(len);
  if ((diff & (1 << (len-1))) == 0)
    diff -= (1 << len) - 1;
  return diff;
}

ShortPtr  ljpeg_row (int jrow, jhead jh)
{
  int col, c, diff, pred;
  int mark=0;
  ShortPtr[] row = new ShortPtr[3];
  
  for ( int i=0;i<3;i++)
    row[i] = new ShortPtr();
  /*
  ShortPtr outp = new ShortPtr();
  outp.assign(jh.row);
*/
  if (jrow * jh.wide % jh.restart == 0) {
    for (c=0; c < 4; c++) jh.vpred[c] = 1 << (jh.bits-1);
    if (jrow != 0)
      do {
        mark = ((mark << 8) + (c = CTOJ.fgetc(ifp)));
      } while (c != CTOJ.EOF && mark >> 4 != 0xffd);
    getbits(-1);
  }
  for ( c=0; c < 3; c++) row[c].assign( jh.row.plus( jh.wide*jh.clrs*((jrow+c) & 1)));
  for (col=0; col < jh.wide; col++)
    for (c=0; c < jh.clrs; c++) {
      diff = ljpeg_diff (jh.huff[c]);
      /*
      outp.etoile( (short)(col!=0 ? outp.at(-jh.clrs)+diff : (jh.vpred[c] += diff)));
      outp.plusPlus();
       */
        if (jh.sraw!=0 && c < 2 && (col | c)!=0)
		    pred = row[0].uat((c << 1)-3);
      else if (col!=0) pred = row[0].uat(-jh.clrs);
      else	    pred = (jh.vpred[c] += diff) - diff;
      if (jrow!=0 && col!=0) switch (jh.psv) {
	case 1:	break;
	case 2: pred = row[1].uat(0);					break;
	case 3: pred = row[1].uat(-jh.clrs);				break;
	case 4: pred = pred +   row[1].uat(0) - row[1].uat(-jh.clrs);		break;
	case 5: pred = pred + ((row[1].uat(0) - row[1].uat(-jh.clrs)) >> 1);	break;
	case 6: pred = row[1].uat(0) + ((pred - row[1].uat(-jh.clrs)) >> 1);	break;
	case 7: pred = (pred + row[1].uat(0)) >> 1;				break;
	default: pred = 0;
      }
      if ((( row[0].etoile( (short)(pred + diff))) >> jh.bits)!=0) derror();
      row[0].plusPlus(); row[1].plusPlus();
  }
  return row[2];
}

public void lossless_jpeg_load_raw()
{
  int jwide, jrow, jcol, val, jidx, i, j, row=0, col=0;
  jhead jh = new jhead();
  int min = CTOJ.INT_MAX;
  ShortPtr rp = new ShortPtr();

  if ( ljpeg_start (jh, 0) == 0) 
      return;
  jwide = jh.wide * jh.clrs;

  for (jrow=0; jrow < jh.high; jrow++) {
    rp.assign( ljpeg_row (jrow, jh));
    for (jcol=0; jcol < jwide; jcol++) {
      val = rp.uat(0);
      rp.plusPlus();
      if (jh.bits <= 12)
	val = curve.at(val & 0xfff);
      if (cr2_slice.at(0) != 0) {
	jidx = jrow*jwide + jcol;
	i = jidx / (cr2_slice.at(1)*jh.high);
	if (( j = (i >= cr2_slice.at(0)) ?1:0)!=0)
		 i  = cr2_slice.at(0);
	jidx -= i * (cr2_slice.at(1)*jh.high);
	row = jidx / cr2_slice.at(1+j);
	col = jidx % cr2_slice.at(1+j) + i*cr2_slice.at(1);
      }
      if (raw_width == 3984 && (col -= 2) < 0) {
          row--;
	col += raw_width;
      }
      if ( CTOJ.cutToUnsigned(row-top_margin) < height) {
	if ( CTOJ.cutToUnsigned(col-left_margin) < width) {
	  BAYER(row-top_margin,col-left_margin, (short)val);
	  if (min > val) min = val;
	} else black += val;
      }
      if (++col >= raw_width) {
          row++;
	col = 0;
      }
    }
  }
  //free (jh.row);
  if (raw_width > width)
    black /= (raw_width - width) * height;
  if ( Uc.strcasecmp(make,"KODAK") == 0)
    black = min;
}


/*
void  adobe_copy_pixel (int row, int col, MutableCharPtr rp)
{
  unsigned r, c;

  r = row -= top_margin;
  c = col -= left_margin;
  if (fuji_secondary && use_secondary) rp.plusPlus();
  if (filters) {
    if (fuji_width) {
      r = row + fuji_width - 1 - (col >> 1);
      c = row + ((col+1) >> 1);
    }
    if (r < height && c < width)
      BAYER(r,c) = rp.etoile() < 0x1000 ? curve[rp.etoile()] : rp.etoile();
    rp.add( 1 + fuji_secondary);
  } else {
    if (r < height && c < width)
      for (c=0; c < tiff_samples; c++)
	image[row*width+col][c] = rp.charAt(c) < 0x1000 ? curve[rp.charAt(c)]:rp.charAt(c);
    rp.add(tiff_samples);
  }
  if (fuji_secondary && use_secondary) rp.moinsMoins();
}
*/
/*
void  adobe_dng_load_raw_lj()
{
  int save, twide, trow=0, tcol=0, jrow, jcol;
  jhead jh;
  MutableCharPtr rp = new MutableCharPtr();

  while (1) {
    save = ftell(ifp);
    fseek (ifp, get4(), SEEK_SET);
    if (!ljpeg_start ( jh, 0)) break;
    if (trow >= raw_height) break;
    if (jh.high > raw_height-trow)
	jh.high = raw_height-trow;
    twide = jh.wide;
    if (filters) twide *= jh.clrs;
    else         colors = jh.clrs;
    if (fuji_secondary) twide /= 2;
    if (twide > raw_width-tcol)
	twide = raw_width-tcol;

    for (jrow=0; jrow < jh.high; jrow++) {
      ljpeg_row (jrow, jh);
      for (rp=jh.row, jcol=0; jcol < twide; jcol++)
	adobe_copy_pixel (trow+jrow, tcol+jcol, rp);
    }
    fseek (ifp, save+4, SEEK_SET);
    if ((tcol += twide) >= raw_width) {
      tcol = 0;
      trow += jh.high;
    }
    free (jh.row);
  }
}
 */
/*
void  adobe_dng_load_raw_nc()
{
  CharPtr pixel;
  MutableCharPtr rp = new MutableCharPtr();
  int row, col;

  pixel = new CharPtr( raw_width * tiff_samples);
  merror (pixel, "adobe_dng_load_raw_nc()");
  for (row=0; row < raw_height; row++) {
    if (tiff_bps == 16)
      read_shorts (pixel, raw_width * tiff_samples);
    else {
      getbits(-1);
      for (col=0; col < raw_width * tiff_samples; col++)
	pixel[col] = getbits(tiff_bps);
    }
    for (rp=pixel, col=0; col < raw_width; col++)
      adobe_copy_pixel (row, col, rp);
  }
  free (pixel);
}
*/
  static byte nikon_tree_tab[][] = { { 0,1,5,1,1,1,1,1,1,2,0,0,0,0,0,0,	/* 12-bit lossy */
      5,4,3,6,2,7,1,0,8,9,11,10,12 },
    { 0,1,5,1,1,1,1,1,1,2,0,0,0,0,0,0,	/* 12-bit lossy after split */
      0x39,0x5a,0x38,0x27,0x16,5,4,3,2,1,0,11,12,12 },
    { 0,1,4,2,3,1,2,0,0,0,0,0,0,0,0,0,  /* 12-bit lossless */
      5,4,6,3,7,2,8,1,9,0,10,11,12 },
    { 0,1,4,3,1,1,1,1,1,2,0,0,0,0,0,0,	/* 14-bit lossy */
      5,6,4,7,8,3,9,2,1,0,10,11,12,13,14 },
    { 0,1,5,1,1,1,1,1,1,1,2,0,0,0,0,0,	/* 14-bit lossy after split */
      8,0x5c,0x4b,0x3a,0x29,7,6,5,4,3,2,1,0,13,14 },
    { 0,1,4,2,2,3,1,2,0,0,0,0,0,0,0,0,	/* 14-bit lossless */
      7,6,8,5,9,4,10,3,11,12,2,0,1,13,14 } };

public void  nikon_compressed_load_raw()
{
    /*
  byte nikon_tree_tab[] = {
    0,1,5,1,1,1,1,1,1,2,0,0,0,0,0,0,
    5,4,3,6,2,7,1,0,8,9,11,10,12
  };
     */
  BytePtr[] nikon_tree = new BytePtr[6];
  for ( int i=0; i<6; i++) {
      nikon_tree[i] = new BytePtr();
    nikon_tree[i].assign(CTOJ.malloc(/*29*/32));
    nikon_tree[i].initialize( nikon_tree_tab[i]);
  }
  int csize;
  ShortPtr vpred = new ShortPtr(4);
  short[] hpred = new short[2];
  //NewShortPtr curve = new NewShortPtr();
  int dindex;
  int ver0, ver1;
  int i, max, step=0, huff=0, split=0, row, col, len, shl, diff;

  CTOJ.fseek (ifp, meta_offset, CTOJ.SEEK_SET);
  ver0 = CTOJ.fgetc(ifp);
  ver1 = CTOJ.fgetc(ifp);
  if (ver0 == 0x49 || ver1 == 0x58)
    CTOJ.fseek (ifp, 2110, CTOJ.SEEK_CUR);
  if (ver0 == 0x46) huff = 2;
  if (tiff_bps == 14) huff += 3;
  read_shorts (vpred, 4);
  max = 1 << tiff_bps & 0x7fff;
  if ((csize = get2()) > 1)
    step = max / (csize-1);
  if (ver0 == 0x44 && ver1 == 0x20 && step > 0) {
    for (i=0; i < csize; i++)
      curve.at(i*step, (short)get2());
    for (i=0; i < max; i++)
      curve.at(i, (short)(( curve.at(i-i%step)*(step-i%step) + curve.at(i-i%step+step)*(i%step) ) / step));
    CTOJ.fseek (ifp, meta_offset+562, CTOJ.SEEK_SET);
    split = get2();
  } else if (ver0 != 0x46 && csize <= 0x4001)
    read_shorts (curve, max=csize);
  init_decoder();
  make_decoder (nikon_tree[huff], 0);
  CTOJ.fseek (ifp, data_offset, CTOJ.SEEK_SET);
  getbits(-1);
  for (row=0; row < height; row++) {
    if (split !=0 && row == split) {
      init_decoder();
      make_decoder (nikon_tree[huff+1], 0);
    }
    for (col=0; col < raw_width; col++) {
      for (dindex= 0; first_decode[dindex].branch0 != -1; )
	dindex = first_decode[dindex].branch((int)getbits(1));
      len = first_decode[dindex].leaf & 15;
      shl = first_decode[dindex].leaf >> 4;
      diff = (int)(((getbits(len-shl) << 1) + 1) << shl >> 1);
      if ((diff & (1 << (len-1))) == 0)
	diff -= (1 << len) - ( shl!=0 ? 0 : 1);
      if (col < 2) {
          vpred.at((row & 1)*2+col, (short)(vpred.at((row & 1)*2+col) + diff));
          hpred[col] = vpred.at((row & 1)*2+col);
      }
      else	   hpred[col & 1] += diff;
      if (hpred[col & 1] >= max) derror();
      if (CTOJ.toUnsigned(col-left_margin) < width)
	BAYER(row,col-left_margin, curve.at(hpred[col & 1] & 0x3fff));
    }
    Thread.yield();
  }
  //CTOJ.free (curve);
}

public void  nikon_load_raw()
{
  int irow, row, col, i;

  getbits(-1);
  for (irow=0; irow < height; irow++) {
    row = irow;
    if (make.charAt(0) == 'O' || model.charAt(0) == 'E') {
      row = irow * 2 % height + irow / (height/2);
      if (row == 1 && data_offset == 0) {
	CTOJ.fseek (ifp, 0, CTOJ.SEEK_END);
	CTOJ.fseek (ifp, CTOJ.ftell(ifp)/2, CTOJ.SEEK_SET);
	getbits(-1);
      }
    }
    for (col=0; col < raw_width; col++) {
      i = (int)getbits(12);
      if ( CTOJ.cutToUnsigned(col-left_margin) < width)
	BAYER(row,col-left_margin, (short)i);
      if (tiff_compress == 34713 && (col % 10) == 9)
	getbits(8);
    }
  }
}

/*
   Figure out if a NEF file is compressed.  These fancy heuristics
   are only needed for the D100, thanks to a bug in some cameras
   that tags all images as "compressed".
 */
boolean  nikon_is_compressed()
{
  BytePtr test = new BytePtr(256);
  int i;

  CTOJ.fseek (ifp, data_offset, CTOJ.SEEK_SET);
  CTOJ.fread (test, 1, 256, ifp);
  for (i=15; i < 256; i+=16)
    if ( test.at(i) != 0) return true;
  return false;
}

/*
   Returns 1 for a Coolpix 995, 0 for anything else.
 */
/*
int  nikon_e995()
{
  int i, histo = new int[256];
  byte often[] = { 0x00, 0x55, 0xaa, 0xff };

  //memset (histo, 0, sizeof histo);
  CTOJ.fseek (ifp, -2000, CTOJ.SEEK_END);
  for (i=0; i < 2000; i++)
    histo[fgetc(ifp)]++;
  for (i=0; i < 4; i++)
    if (histo[often[i]] < 200)
      return 0;
  return 1;
}
*/
/*
   Returns 1 for a Coolpix 2100, 0 for anything else.
 */
/*
int  nikon_e2100()
{
  byte t = new byte[12];
  int i;

  CTOJ.fseek (ifp, 0, CTOJ.SEEK_SET);
  for (i=0; i < 1024; i++) {
    CTOJ.fread (t, 1, 12, ifp);
    if (((t[2] & t[4] & t[7] & t[9]) >> 4
	& t[1] & t[6] & t[8] & t[11] & 3) != 3)
      return 0;
  }
  return 1;
}
 */
/*
void  nikon_3700()
{
  int bits, i;
  byte dp = new byte[24];

  CTOJ.fseek (ifp, 3072, CTOJ.SEEK_SET);
  CTOJ.fread (dp, 1, 24, ifp);
  bits = (dp[8] & 3) << 4 | (dp[20] & 3);
  
  switch( bits) {
      case 0x00:
          Uc.strcpy(make,"PENTAX");
          Uc.strcpy(model,"Optio 33WR");
          break;
      case 0x03:
          Uc.strcpy(make,"NIKON");
          Uc.strcpy(model,"E3200");
          break;
      case 0x32:
          Uc.strcpy(make,"NIKON");
          Uc.strcpy(model,"E3700");
          break;
      case 0x33:
          Uc.strcpy(make,"OLYMPUS");
          Uc.strcpy(model,"C74OUZ");
          break;
  }
}
*/
/*
   Separates a Minolta DiMAGE Z2 from a Nikon E4300.
 */
int  minolta_z2()
{
  int i;
  BytePtr tail = new BytePtr(424);

  CTOJ.fseek (ifp, - 424, CTOJ.SEEK_END);
  CTOJ.fread (tail, 1, 424, ifp);
  for (i=0; i < 424; i++)
    if (tail.at(i)!= 0) return 1;
  return 0;
}

/* Here raw_width is in bytes, not pixels. */
void  nikon_e900_load_raw()
{
  int offset=0, irow, row, col;

  for (irow=0; irow < height; irow++) {
    row = irow * 2 % height;
    if (row == 1)
      offset = - (-offset & -4096);
    CTOJ.fseek (ifp, offset, CTOJ.SEEK_SET);
    offset += raw_width;
    getbits(-1);
    for (col=0; col < width; col++)
      BAYER(row,col, (short)getbits(10));
  }
}
/*
void  nikon_e2100_load_raw()
{
  byte[]   data = new byte[3456];
  BytePtr dp = new BytePtr();
  char[] pixel = new char[2304];
  CharPtr pix = new CharPtr();
  int row, col;

  for (row=0; row <= height; row+=2) {
    if (row == height) {
      CTOJ.fseek (ifp, ((width==1616) << 13) - (-ftell(ifp) & -2048), CTOJ.SEEK_SET);
      row = 1;
    }
    CTOJ.fread (data, 1, width*3/2, ifp);
    for (dp=data, pix=pixel; pix < pixel+width; dp+=12, pix+=8) {
      pix[0] = (dp[2] >> 4) + (dp[ 3] << 4);
      pix[1] = (dp[2] << 8) +  dp[ 1];
      pix[2] = (dp[7] >> 4) + (dp[ 0] << 4);
      pix[3] = (dp[7] << 8) +  dp[ 6];
      pix[4] = (dp[4] >> 4) + (dp[ 5] << 4);
      pix[5] = (dp[4] << 8) +  dp[11];
      pix[6] = (dp[9] >> 4) + (dp[10] << 4);
      pix[7] = (dp[9] << 8) +  dp[ 8];
    }
    for (col=0; col < width; col++)
      BAYER(row,col) = (pixel[col] & 0xfff);
  }
}
*/
/*
   The Fuji Super CCD is just a Bayer grid rotated 45 degrees.
 */

public void  fuji_load_raw()
{
  ShortPtr pixel;
  int wide, row, col, r, c;

  CTOJ.fseek (ifp, (top_margin*raw_width + left_margin) * 2, CTOJ.SEEK_CUR);
  wide = fuji_width << ( fuji_layout!=0 ? 0 : 1);
  pixel = new ShortPtr(raw_width);
  merror (pixel, "fuji_load_raw()");
  for (row=0; row < raw_height; row++) {
    read_shorts (pixel, raw_width);
    for (col=0; col < wide; col++) {
      if (fuji_layout != 0) {
	r = fuji_width - 1 - col + (row >> 1);
	c = col + ((row+1) >> 1);
      } else {
	r = fuji_width - 1 + row - (col >> 1);
	c = row + ((col+1) >> 1);
      }
      BAYER(r,c, (short)pixel.uat(col));
    }
  }
  CTOJ.free (pixel);
}

void  jpeg_thumb (Object tfp)
{
  BytePtr thumb = new BytePtr(thumb_length);
  merror (thumb, "jpeg_thumb()");
  CTOJ.fread  (thumb, 1, thumb_length, ifp);
  thumb.at(0, (byte)0xff);
  CTOJ.fwrite (thumb, 1, thumb_length, tfp);
  CTOJ.free (thumb);
}
/*
void  ppm_thumb (Object tfp)
{
  BytePtr thumb = new BytePtr(thumb_length);
  merror (thumb, "ppm_thumb()");
  fprintf (tfp, "P6\n%d %d\n255\n", thumb_width, thumb_height);
  CTOJ.fread  (thumb, 1, thumb_length, ifp);
  fwrite (thumb, 1, thumb_length, tfp);
  CTOJ.free (thumb);
}
*/
/*
void  layer_thumb (Object tfp)
{
  int i, c;
  CharPtr thumb;
  colors = thumb_misc >> 5;
  thumb = new CharPtr(thumb_length*colors);
  merror (thumb, "layer_thumb()");
  fprintf (tfp, "P%d\n%d %d\n255\n",
	5 + (thumb_misc >> 6), thumb_width, thumb_height);
  CTOJ.fread (thumb, thumb_length, colors, ifp);
  for (i=0; i < thumb_length; i++)
    for (c=0; c < colors; c++) putc (thumb[i+thumb_length*c], tfp);
  CTOJ.free (thumb);
}
*/
/*
void  rollei_thumb (Object tfp)
{
  int i, size = thumb_width * thumb_height;
  CharPtr thumb = new CharPtr(size*2);
  merror (thumb, "rollei_thumb()");
  fprintf (tfp, "P6\n%d %d\n255\n", thumb_width, thumb_height);
  read_shorts (thumb, size);
  for (i=0; i < size; i++) {
    putc (thumb[i] << 3, tfp);
    putc (thumb[i] >> 5  << 2, tfp);
    putc (thumb[i] >> 11 << 3, tfp);
  }
  CTOJ.free (thumb);
}
*/

void  rollei_load_raw()
{
  BytePtr pixel = new BytePtr(10);
  int /*unsigned*/ iten=0, isix, i, buffer=0, row, col, todo[] = new int[16];

  isix = raw_width * raw_height * 5 / 8;
  while (CTOJ.fread (pixel, 1, 10, ifp) == 10) {
    for (i=0; i < 10; i+=2) {
      todo[i]   = iten++;
      todo[i+1] = pixel.at(i) << 8 | pixel.at(i+1);
      buffer    = pixel.at(i) >> 2 | buffer << 6;
    }
    for (   ; i < 16; i+=2) {
      todo[i]   = isix++;
      todo[i+1] = buffer >> (14-i)*5;
    }
    for (i=0; i < 16; i+=2) {
      row = todo[i] / raw_width - top_margin;
      col = todo[i] % raw_width - left_margin;
      if (row < height && col < width)
	BAYER(row,col, (short)(todo[i+1] & 0x3ff));
    }
  }
  maximum = 0x3ff;
}

int  bayer (int row, int col)
{
  return (row < height && col < width) ? BAYER(row,col) : 0;
}
/*
void  phase_one_flat_field (int is_float, int nc)
{
  CharPtr head = new CharPtr(8);
  unsigned wide, y, x, c, rend, cend, row, col;
  float[] mrow;
  float num;
  float[] mult = new float[4];

  read_shorts (head, 8);
  wide = head[2] / head[4];
  mrow = new float[nc*wide];//(float *) calloc (nc*wide, sizeof *mrow);
  //merror (mrow, "phase_one_flat_field()");
  for (y=0; y < head[3] / head[5]; y++) {
    for (x=0; x < wide; x++)
      for (c=0; c < nc; c+=2) {
	num = is_float ? getreal(11) : get2()/32768.0;
	if (y==0) mrow[c*wide+x] = num;
	else mrow[(c+1)*wide+x] = (num - mrow[c*wide+x]) / head[5];
      }
    if (y==0) continue;
    rend = head[1]-top_margin + y*head[5];
    for (row = rend-head[5]; row < height && row < rend; row++) {
      for (x=1; x < wide; x++) {
	for (c=0; c < nc; c+=2) {
	  mult[c] = mrow[c*wide+x-1];
	  mult[c+1] = (mrow[c*wide+x] - mult[c]) / head[4];
	}
	cend = head[0]-left_margin + x*head[4];
	for (col = cend-head[4]; col < width && col < cend; col++) {
	  c = nc > 2 ? FC(row,col) : 0;
	  if (!(c & 1)) {
	    c = BAYER(row,col) * mult[c];
	    BAYER(row,col) = LIM(c,0,65535);
	  }
	  for (c=0; c < nc; c+=2)
	    mult[c] += mult[c+1];
	}
      }
      for (x=0; x < wide; x++)
	for (c=0; c < nc; c+=2)
	  mrow[c*wide+x] += mrow[(c+1)*wide+x];
    }
  }
  CTOJ.free (mrow);
}
*/

/*
void  imacon_full_load_raw()
{
  int row, col;

  for (row=0; row < height; row++)
    for (col=0; col < width; col++)
      read_shorts (image[row*width+col], 3);
}
*/
/* Here raw_width is in bytes, not pixels. */
public void  packed_12_load_raw()
{
  int row, col;

  if (raw_width * 2 < width * 3)
    raw_width = raw_width * 3 / 2;	/* Convert raw_width to bytes */
  getbits(-1);
  for (row=0; row < height; row++) {
    for (col=0; col < left_margin; col++)
      getbits(12);
    for (col=0; col < width; col++)
      BAYER(row,col, (short)getbits(12));
    for (col = (width+left_margin)*3/2; col < raw_width; col++)
      if (getbits(8)!=0 && raw_width-col < 35 && width != 3896) derror();
  }
}

public void unpacked_load_raw()
{
  ShortPtr pixel = new ShortPtr();
  int row, col, bits=0;

  while (1 << ++bits < maximum);
  CTOJ.fseek (ifp, (top_margin*raw_width + left_margin) * 2, CTOJ.SEEK_CUR);
  pixel.assign(  CTOJ.calloc (width, 2));
  merror (pixel, "unpacked_load_raw()");
  for (row=0; row < height; row++) {
    read_shorts (pixel, width);
    CTOJ.fseek (ifp, 2*(raw_width - width), CTOJ.SEEK_CUR);
    for (col=0; col < width; col++)
      if (( BAYER2(row,col, (short)pixel.uat(col)) >> bits)!=0) derror();
  }
  CTOJ.free (pixel);
}

public void olympus_e300_load_raw()
{
  BytePtr  data = new BytePtr(),  dp = new BytePtr();
  ShortPtr pixel = new ShortPtr(), pix = new ShortPtr();
  int dwide, row, col;

  dwide = raw_width * 16 / 10;
  CTOJ.fseek (ifp, dwide*top_margin, CTOJ.SEEK_CUR);
  data.assign( CTOJ.malloc (dwide + raw_width*2));
  merror (data, "olympus_e300_load_raw()");
  pixel.assign(data.plus(dwide));
  for (row=0; row < height; row++) {
    if (CTOJ.fread (data, 1, dwide, ifp) < dwide) derror();
    for (dp.assign(data), pix.assign(pixel); pix.lessThan( pixel.plus(raw_width)); dp.assign(dp.plus(3)), pix.assign(pix.plus(2))) {
      if ((dp.minus(data) & 15) == 15) {
	if ( dp.at(0)!=0 && pix.lessThan( pixel.plus(width+left_margin))) derror();
        dp.plusPlus();
      }
      pix.at(0, (short)( dp.uat(1) << 8 | dp.uat(0)));
      pix.at(1, (short) (dp.uat(2) << 4 | dp.uat(1) >> 4));
    }
    for (col=0; col < width; col++)
      BAYER(row,col, (short)(pixel.uat(col+left_margin) & 0xfff));
  }
  //free (data);
  maximum >>= 4;
  black >>= 4;
}

void  olympus_cseries_load_raw()
{
  int irow, row, col;

  for (irow=0; irow < height; irow++) {
    row = irow * 2 % height + irow / (height/2);
    if (row < 2) {
      CTOJ.fseek (ifp, data_offset - row*(-width*height*3/4 & -2048), CTOJ.SEEK_SET);
      getbits(-1);
    }
    for (col=0; col < width; col++)
      BAYER(row,col, (short)getbits(12));
  }
}
/*
void  kodak_thumb_load_raw()
{
  int row, col;
  colors = thumb_misc >> 5;
  for (row=0; row < height; row++)
    for (col=0; col < width; col++)
      read_shorts (image[row*width+col], colors);
  maximum = (1 << (thumb_misc & 31)) - 1;
}
*/
/*
void  sony_arw_load_raw()
{
  int col, row, len, diff, sum=0;

  getbits(-1);
  for (col = raw_width; col--; )
    for (row=0; row < raw_height+1; row+=2) {
      if (row == raw_height) row = 1;
      len = 4 - getbits(2);
      if (len == 3 && getbits(1)) len = 0;
      if (len == 4)
	while (len < 17 && !getbits(1)) len++;
      diff = getbits(len);
      if ((diff & (1 << (len-1))) == 0)
	diff -= (1 << len) - 1;
      sum += diff;
      if (row < height) BAYER(row,col) = sum;
    }
}
*/

void gamma_curve (double pwr, double ts, int mode, int imax)
{
  int i;
  double[] g = new double[6], bnd = new double[2];
    double r;

  g[0] = pwr;
  g[1] = ts;
  g[2] = g[3] = g[4] = 0;
  bnd[(g[1] >= 1)?1:0] = 1;
  if (g[1]!=0.0 && (g[1]-1)*(g[0]-1) <= 0) {
    for (i=0; i < 48; i++) {
      g[2] = (bnd[0] + bnd[1])/2;
      if (g[0]!=0) bnd[((Math.pow(g[2]/g[1],-g[0]) - 1)/g[0] - 1/g[2] > -1)?1:0] = g[2];
      else	bnd[(g[2]/Math.exp(1-1/g[2]) < g[1])?1:0] = g[2];
    }
    g[3] = g[2] / g[1];
    if (g[0]!=0.0) g[4] = g[2] * (1/g[0] - 1);
  }
  if (g[0]!=0.0) g[5] = 1 / (g[1]*SQR(g[3])/2 - g[4]*(1 - g[3]) +
		(1 - Math.pow(g[3],1+g[0]))*(1 + g[4])/(1 + g[0])) - 1;
  else      g[5] = 1 / (g[1]*SQR(g[3])/2 + 1
		- g[2] - g[3] -	g[2]*g[3]*(Math.log(g[3]) - 1)) - 1;
  if (mode-- == 0) {
    Uc.memcpy (gamm, g, (int)Uc.sizeof(gamm));
    return;
  }
  for (i=0; i < 0x10000; i++) {
    new_curve.at(i, (short)0xffff);
    if ((r = (double) i / imax) < 1)
      new_curve.at(i,(short)(0x10000 * ( mode != 0
	? (r < g[3] ? r*g[1] : (g[0] != 0.0 ? Math.pow( r,g[0])*(1+g[4])-g[4]    : Math.log(r)*g[2]+1))
	: (r < g[2] ? r/g[1] : (g[0] != 0.0 ? Math.pow((r+g[4])/(1+g[4]),1/g[0]) : Math.exp((r-1)/g[2]))))));
  }
}


void  pseudoinverse (double[][] in, double[][] out, int size)
{
  double[][] work = new double[3][6];
  double num;
  int i, j, k;

  for (i=0; i < 3; i++) {
    for (j=0; j < 6; j++)
      work[i][j] = j == i+3 ? 1: 0;
    for (j=0; j < 3; j++)
      for (k=0; k < size; k++)
	work[i][j] += in[k][i] * in[k][j];
  }
  for (i=0; i < 3; i++) {
    num = work[i][i];
    for (j=0; j < 6; j++)
      work[i][j] /= num;
    for (k=0; k < 3; k++) {
      if (k==i) continue;
      num = work[k][i];
      for (j=0; j < 6; j++)
	work[k][j] -= work[i][j] * num;
    }
  }
  for (i=0; i < size; i++)
    for (j=0; j < 3; j++)
      for (out[i][j]=k=0; k < 3; k++)
	out[i][j] += work[j][k+3] * in[i][k];
}

void  cam_xyz_coeff (double[][] cam_xyz)
{
  double[][] cam_rgb = new double[4][3];
  double[][] inverse = new double[4][3];
  double num;
  int i, j, k;

  for (i=0; i < colors; i++)		/* Multiply out XYZ colorspace */
    for (j=0; j < 3; j++)
      for (cam_rgb[i][j] = k=0; k < 3; k++)
	cam_rgb[i][j] += cam_xyz[i][k] * xyz_rgb[k][j];

  for (i=0; i < colors; i++) {		/* Normalize cam_rgb so that */
    for (num=j=0; j < 3; j++)		/* cam_rgb * (1,1,1) is (1,1,1,1) */
      num += cam_rgb[i][j];
    for (j=0; j < 3; j++)
      cam_rgb[i][j] /= num;
    pre_mul[i] = (float)(1 / num);
  }
  pseudoinverse (cam_rgb, inverse, colors);
  for (raw_color = i=0; i < 3; i++)
    for (j=0; j < colors; j++)
      rgb_cam[i][j] = (float)inverse[j][i];
}

void  cam_coeff (double[][] cam_xyz)
{
  double[][] cam_rgb = new double[4][3];
  double[][] inverse = new double[4][3];
  double num;
  int i, j, k;

  for (i=0; i < colors; i++)		/* Multiply out XYZ colorspace */
    for (j=0; j < 3; j++)
	cam_rgb[i][j] += cam_xyz[i][j];

  for (i=0; i < colors; i++) {		/* Normalize cam_rgb so that */
    for (num=j=0; j < 3; j++)		/* cam_rgb * (1,1,1) is (1,1,1,1) */
      num += cam_rgb[i][j];
    for (j=0; j < 3; j++)
      cam_rgb[i][j] /= num;
    pre_mul[i] = (float)(1 / num);
  }
  pseudoinverse (cam_rgb, inverse, colors);
  for (raw_color = i=0; i < 3; i++)
    for (j=0; j < colors; j++)
      rgb_cam2[i][j] = (float)inverse[j][i];

  double[][] amort = new double [3][3];
  
  amort[0][0] = 0.96;
  amort[1][1] = 0.96;
  amort[2][2] = 0.96;
  amort[0][1] = 0.02;
  amort[1][0] = 0.02;
  amort[0][2] = 0.02;
  amort[2][0] = 0.02;
  amort[1][2] = 0.02;
  amort[2][1] = 0.02;
  
  float[][] rgb_cam3 = new float[3][3];
  
  for (i=0; i < colors; i++)		/* Multiply out XYZ colorspace */
    for (j=0; j < 3; j++)
      for (cam_rgb[i][j] = k=0; k < 3; k++)
	rgb_cam3[i][j] += rgb_cam2[i][k] * amort[k][j];

  //rgb_cam2 = rgb_cam3;
}


void  scale_colors()
{
  long  x, y;
  int row, col, c, val;
  int[] min = new int[4], max = new int[4], sum = new int[8];
  double[] dsum = new double[8];
  double dmin, dmax;
  float[] scale_mul = new float[4];

  maximum -= black;
  if (use_auto_wb != 0 || (use_camera_wb != 0 && cam_mul[0] == -1)) {
    for (c=0; c < 4; c++) min[c] = Integer.MAX_VALUE;
    for (c=0; c < 4; c++) max[c] = 0;
    Uc.memset (dsum, 0, 8);
    for (row=0; row < height-7; row += 8)
      for (col=0; col < width-7; col += 8) {
	Uc.memset (sum, 0, 8);
	for (y=row; y < row+8; y++)
	  for (x=col; x < col+8; x++)
	    for (c=0; c < 4; c++) {
	      val = CTOJ.toUnsigned(image.at( (int)(y*width+x),c));
	      if (val == 0) continue;
	      if (min[c] > val) min[c] = val;
	      if (max[c] < val) max[c] = val;
	      val -= black;
	      if (val > maximum-25)  {
                  c =4; x = col+8; y = row+8;
                  continue;
              }
	      if (val < 0) val = 0;
	      sum[c] += val;
	      sum[c+4]++;
	    }
	for (c=0; c < 8; c++) dsum[c] += sum[c];
skip_block:
	continue;
      }
    for (c=0; c < 4; c++) if (dsum[c] != 0) pre_mul[c] = (float)(dsum[c+4] / dsum[c]);
  }
  boolean manual_mode = true; // YB 06/11/2009false; // YB 24/05/2008
  if (use_camera_wb != 0 && cam_mul[0] != -1) {
    Uc.memset (sum, 0, 8);
    for (row=0; row < 8; row++)
      for (col=0; col < 8; col++) {
	c = (int)FC((int)row,(int)col);
	if ((val = white[row][col] - black) > 0)
	  sum[c] += val;
	sum[c+4]++;
      }
    if (sum[0] != 0 && sum[1] !=0 && sum[2] !=0 && sum[3] !=0)
      for (c=0; c < 4; c++) pre_mul[c] = (float) sum[c+4] / sum[c];
    else if (cam_mul[0] != 0 && cam_mul[2] != 0)
      Uc.memcpy (pre_mul, cam_mul, 4);
    else {
      System.err.printf ( "%s: Cannot use camera white balance.\n", ifname);
      manual_mode = true;
    }
  }
  if (user_mul[0] != 0 && manual_mode) {
    Uc.memcpy (pre_mul, user_mul, 4);
  }
  else {
      red = pre_mul[0];
      green = pre_mul[1];
      blue = pre_mul[2];
  }
  if (pre_mul[3] == 0) pre_mul[3] = colors < 4 ? pre_mul[1] : 1;
  for (dmin= Double.MAX_VALUE, dmax=c=0; c < 4; c++) {
    if (dmin > pre_mul[c])
	dmin = pre_mul[c];
    if (dmax < pre_mul[c])
	dmax = pre_mul[c];
  }
  if (highlight == 0) dmax = dmin;
  for (c=0; c < 4; c++) scale_mul[c] = (float)((pre_mul[c] /= dmax) * 65535.0 / maximum);
  if (verbose != 0) {
    System.err.printf ( "Scaling with black=%d, pre_mul[] =", black);
    for (c=0; c < 4; c++) System.err.printf (" %f", pre_mul[c]);
    System.err.println();
  }
  for (row=0; row < height; row++) {
    for (col=0; col < width; col++) {
      for (c=0; c < 4; c++) {
	val = CTOJ.toUnsigned(image.at(row*width+col,c));
	if (val == 0) continue;
	val -= black;
	val *= scale_mul[c];
	image.at(row*width+col,c, (short)CLIP(val));
      }
    }
  }
  if (filters != 0 && colors == 3) {
    if (four_color_rgb != 0) {
      colors++;
      for (c=0; c < 3; c++) rgb_cam[c][3] = rgb_cam[c][1] /= 2;
    } else {
      for (row = (int)FC(1,0) >> 1; row < height; row+=2)
	for (col = (int)FC(row,1) & 1; col < width; col+=2)
	  image.at(row*width+col,1, image.at(row*width+col,3));
      filters &= ~((filters & 0x55555555l) << 1);
    }
  }
}
/*
void pre_interpolate()
{
  ushort (*img)[4];
  int row, col, c;

  if (shrink != 0) {
    if (half_size !=0) {
      height = iheight;
      width  = iwidth;
    } else {
      img = (ushort (*)[4]) calloc (height*width, sizeof *img);
      merror (img, "unshrink()");
      for (row=0; row < height; row++)
	for (col=0; col < width; col++) {
	  c = fc(row,col);
	  img[row*width+col][c] = image[(row >> 1)*iwidth+(col >> 1)][c];
	}
      free (image);
      image = img;
      shrink = 0;
    }
  }
  if (filters && colors == 3) {
    if ((mix_green = four_color_rgb)) colors++;
    else {
      for (row = FC(1,0) >> 1; row < height; row+=2)
	for (col = FC(row,1) & 1; col < width; col+=2)
	  image[row*width+col][1] = image[row*width+col][3];
      filters &= ~((filters & 0x55555555) << 1);
    }
  }
  if (half_size) filters = 0;
}
*/
void  border_interpolate (int border)
{
  int row, col, y, x, f, c;
  int [] sum = new int[8];

  for (row=0; row < height; row++) {
    for (col=0; col < width; col++) {
      if (col==border && row >= border && row < height-border)
	col = width-border;
      Uc.memset (sum, 0, 8);
      for (y=row-1; y != row+2; y++)
	for (x=col-1; x != col+2; x++)
	  if ( CTOJ.toUnsigned(y) < height && CTOJ.toUnsigned(x) < width) {
	    f = fc(y,x);
	    sum[f] += CTOJ.toUnsigned(image.at(y*width+x,f));
	    sum[f+4]++;
	  }
      f = fc(row,col);
      for (c=0; c < colors; c++)
        if (c != f && sum[c+4] != 0)
            image.at(row*width+col,c, (short) (sum[c] / sum[c+4]));
    }
    Thread.yield();
  }
}

//public static final int aa = 3;
//public static final int bb = 4;
public static final int aa = 4;
public static final int bb = 6;

void sinc_interpolate() {
    
    int[][] image_green = new int[width*2+10][height*2+10];
    
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {

            if ( (col+1) < width)
                image_green[ 4 + (col+1)*2][4 + row*2] = CTOJ.toUnsigned(image.at((row)*width+(col+1),1));
            if ( (row+1) < height)
                image_green[4 + col*2][4 + (row+1)*2] = CTOJ.toUnsigned(image.at((row+1)*width+col,1));
        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
        // Interpolation de bas-gauche vers haut-droit
            int a = image_green[4 + col*2-2][4 + row*2+4];
            int b = image_green[4 + col*2  ][4 + row*2+2];
            int c = image_green[4 + col*2+2][4 + row*2  ];
            int d = image_green[4 + col*2+4][4 + row*2-2];
                    
            int sum = (aa*(b+c) -(a+d))/bb;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+1][4 + row*2+1] = sum;
            
            a = image_green[4 + col*2  ][4 + row*2+6];
            b = image_green[4 + col*2+2][4 + row*2+4];
            c = image_green[4 + col*2+4][4 + row*2+2];
            d = image_green[4 + col*2+6][4 + row*2  ];
                    
            sum = (aa*(b+c) -(a+d))/bb;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+3][4 + row*2+3] = sum;
            

        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            // Interpolation de haut-gauche vers bas-droit
            int a = image_green[4 + col*2-3][4 + row*2-3];
            int b = image_green[4 + col*2-1][4 + row*2-1];
            int c = image_green[4 + col*2+1][4 + row*2+1];
            int d = image_green[4 + col*2+3][4 + row*2+3];
            
            int sum = (aa*(b+c) -(a+d))/bb;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2][4 + row*2] = sum;
            
            a = image_green[4 + col*2-1][4 + row*2-1];
            b = image_green[4 + col*2+1][4 + row*2+1];
            c = image_green[4 + col*2+3][4 + row*2+3];
            d = image_green[4 + col*2+5][4 + row*2+5];
            
            sum = (aa*(b+c) -(a+d))/bb;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+2][4 + row*2+2] = sum;
            
        }
        
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            int sum = image_green[ 4 + (col)*2][4 + row*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            

            image.at((row)*width+(col),1, (short)sum);
            if ( (col+1) < width)
                if ( (row+1) < height) {
                
                    sum = image_green[4 + (col+1)*2][4 + (row+1)*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
                
                    image.at((row+1)*width+col+1,1, (short)sum);
                }
        }
    
    for ( int row =0; row < height-1; row +=2)
        for ( int col=0; col <= width; col+=2 ) {

        // Interpolation horizontale rouge
            int a = CTOJ.toUnsigned(image.at((row+1)*width+ Math.max(col-3,0),0));
            int b = CTOJ.toUnsigned(image.at((row+1)*width+ Math.max(col-1,0),0));
            int c = CTOJ.toUnsigned(image.at((row+1)*width+ Math.min(col+1, width-2),0));
            int d = CTOJ.toUnsigned(image.at((row+1)*width+ Math.min(col+3, width-2),0));
            int sum = (aa*(b+c) -(a+d))/bb;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+Math.min(col,width-1),0, (short) sum );
            
            // Interpolation horizontale bleue
            a = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col-4, 0), 2));
            b = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col-2, 0), 2));
            c = CTOJ.toUnsigned(image.at((row)*width+ Math.min(col,width-1), 2));
            d = CTOJ.toUnsigned(image.at((row)*width+ Math.min( col+2, width-1), 2));
            sum = (aa*(b+c) -(a+d))/bb;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row)*width+Math.max(col-1,0),2, (short)sum);
            
            //image.at((row)*width+col,1, image.at((row)*width+Math.min(col+1, width-2),1));
            //image.at((row+1)*width+Math.min(col+1,width-1),1, image.at((row+1)*width+Math.min(col, width-1),1));
            
        }
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width-1; col++ ) {
        
        // Interpolation verticale rouge
            int a = CTOJ.toUnsigned(image.at(Math.max(row-3, 1)*width+ Math.max(col,0),0));
            int b = CTOJ.toUnsigned(image.at(Math.max(row-1, 1)*width+ Math.max(col,0),0));
            int c = CTOJ.toUnsigned(image.at(Math.min(row+1, height-1)*width+ Math.min(col, width-1),0));
            int d = CTOJ.toUnsigned(image.at(Math.min(row+3, height-1)*width+ Math.min(col, width-1),0));
            int sum = (aa*(b+c) -(a+d))/bb;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image.at((row)*width+col,0, (short)sum);

            // Interpolation verticale bleue
            a = CTOJ.toUnsigned(image.at(Math.max(row-2,0)*width+ Math.max(col, 0), 2));
            b = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col, 0), 2));
            c = CTOJ.toUnsigned(image.at(Math.min(row+2, height-1)*width+ Math.min(col,width-1), 2));
            d = CTOJ.toUnsigned(image.at(Math.min(row+4, height-1)*width+ Math.min( col, width-1), 2));
            sum = (aa*(b+c) -(a+d))/bb;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+col,2, (short)sum);
        }
    image_green = null;
}

void lin_interpolate_yb() {
    
    int[][] image_green = new int[width*2+10][height*2+10];
    
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {

            if ( (col+1) < width)
                image_green[ 4 + (col+1)*2][4 + row*2] = CTOJ.toUnsigned(image.at((row)*width+(col+1),1));
            if ( (row+1) < height)
                image_green[4 + col*2][4 + (row+1)*2] = CTOJ.toUnsigned(image.at((row+1)*width+col,1));
        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
        // Interpolation de bas-gauche vers haut-droit
            int b = image_green[4 + col*2  ][4 + row*2+2];
            int c = image_green[4 + col*2+2][4 + row*2  ];
                    
            int sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+1][4 + row*2+1] = sum;
            
            b = image_green[4 + col*2+2][4 + row*2+4];
            c = image_green[4 + col*2+4][4 + row*2+2];
                    
            sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+3][4 + row*2+3] = sum;
            

        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            // Interpolation de haut-gauche vers bas-droit
            int b = image_green[4 + col*2-1][4 + row*2-1];
            int c = image_green[4 + col*2+1][4 + row*2+1];
            
            int sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2][4 + row*2] = sum;
            
            b = image_green[4 + col*2+1][4 + row*2+1];
            c = image_green[4 + col*2+3][4 + row*2+3];
            
            sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+2][4 + row*2+2] = sum;
            
        }
        
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            int sum = image_green[ 4 + (col)*2][4 + row*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            

            image.at((row)*width+(col),1, (short)sum);
            if ( (col+1) < width)
                if ( (row+1) < height) {
                
                    sum = image_green[4 + (col+1)*2][4 + (row+1)*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
                
                    image.at((row+1)*width+col+1,1, (short)sum);
                }
        }
    
    for ( int row =0; row < height-1; row +=2)
        for ( int col=0; col <= width; col+=2 ) {

        // Interpolation horizontale rouge
            int b = CTOJ.toUnsigned(image.at((row+1)*width+ Math.max(col-1,0),0));
            int c = CTOJ.toUnsigned(image.at((row+1)*width+ Math.min(col+1, width-2),0));
            int sum = (b+c)/2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+Math.min(col,width-1),0, (short) sum );
            
            // Interpolation horizontale bleue
            b = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col-2, 0), 2));
            c = CTOJ.toUnsigned(image.at((row)*width+ Math.min(col,width-1), 2));
            sum = (b+c)/2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row)*width+Math.max(col-1,0),2, (short)sum);
            
            //image.at((row)*width+col,1, image.at((row)*width+Math.min(col+1, width-2),1));
            //image.at((row+1)*width+Math.min(col+1,width-1),1, image.at((row+1)*width+Math.min(col, width-1),1));
            
        }
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width-1; col++ ) {
        
        // Interpolation verticale rouge
            int b = CTOJ.toUnsigned(image.at(Math.max(row-1, 1)*width+ Math.max(col,0),0));
            int c = CTOJ.toUnsigned(image.at(Math.min(row+1, height-1)*width+ Math.min(col, width-1),0));
            int sum = (b+c)/2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image.at((row)*width+col,0, (short)sum);

            // Interpolation verticale bleue
            b = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col, 0), 2));
            c = CTOJ.toUnsigned(image.at(Math.min(row+2, height-1)*width+ Math.min(col,width-1), 2));
            sum = (b+c)/2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+col,2, (short)sum);
        }
    
    image_green = null;
}

//public static final int aa2 = 3;
//public static final int bb2 = 4;
public static final int aa2 = 4;
public static final int bb2 = 6;

void sinc_interpolate2() {
   
    char[][] image_green = new char[width*2+10][height*2+10];
    
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {

            if ( (col+1) < width)
                image_green[ 4 + (col+1)*2][4 + row*2] = (char)CTOJ.toUnsigned(image.at((row)*width+(col+1),1));
            if ( (row+1) < height)
                image_green[4 + col*2][4 + (row+1)*2] = (char)CTOJ.toUnsigned(image.at((row+1)*width+col,1));
        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
        // Interpolation de bas-gauche vers haut-droit
            int a = image_green[4 + col*2-2][4 + row*2+4];
            int b = image_green[4 + col*2  ][4 + row*2+2];
            int c = image_green[4 + col*2+2][4 + row*2  ];
            int d = image_green[4 + col*2+4][4 + row*2-2];
                    
            int sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+1][4 + row*2+1] = (char)sum;
            
            a = image_green[4 + col*2  ][4 + row*2+6];
            b = image_green[4 + col*2+2][4 + row*2+4];
            c = image_green[4 + col*2+4][4 + row*2+2];
            d = image_green[4 + col*2+6][4 + row*2  ];
                    
            sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+3][4 + row*2+3] = (char)sum;
            

        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            // Interpolation de haut-gauche vers bas-droit
            int a = image_green[4 + col*2-3][4 + row*2-3];
            int b = image_green[4 + col*2-1][4 + row*2-1];
            int c = image_green[4 + col*2+1][4 + row*2+1];
            int d = image_green[4 + col*2+3][4 + row*2+3];
            
            int sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2][4 + row*2] = (char)sum;
            
            a = image_green[4 + col*2-1][4 + row*2-1];
            b = image_green[4 + col*2+1][4 + row*2+1];
            c = image_green[4 + col*2+3][4 + row*2+3];
            d = image_green[4 + col*2+5][4 + row*2+5];
            
            sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+2][4 + row*2+2] = (char)sum;
            
        }
        
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            int sum = image_green[ 4 + (col)*2][4 + row*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            

            image.at((row)*width+(col),1, (short)sum);
            if ( (col+1) < width)
                if ( (row+1) < height) {
                
                    sum = image_green[4 + (col+1)*2][4 + (row+1)*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
                
                    image.at((row+1)*width+col+1,1, (short)sum);
                }
        }
    
    for ( int row =1; row < height-1; row +=2)
        for ( int col=1; col <= width; col+=2 ) {

        // Interpolation horizontale rouge
            int a = CTOJ.toUnsigned(image.at((row+1)*width+ Math.max(col-3,0),0));
            int b = CTOJ.toUnsigned(image.at((row+1)*width+ Math.max(col-1,0),0));
            int c = CTOJ.toUnsigned(image.at((row+1)*width+ Math.min(col+1, width-2),0));
            int d = CTOJ.toUnsigned(image.at((row+1)*width+ Math.min(col+3, width-2),0));
            int sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+Math.min(col,width-1),0, (short) sum );
            
            // Interpolation horizontale bleue
            a = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col-4, 0), 2));
            b = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col-2, 0), 2));
            c = CTOJ.toUnsigned(image.at((row)*width+ Math.min(col,width-1), 2));
            d = CTOJ.toUnsigned(image.at((row)*width+ Math.min( col+2, width-1), 2));
            sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row)*width+Math.max(col-1,0),2, (short)sum);
            
            //image.at((row)*width+col,1, image.at((row)*width+Math.min(col+1, width-2),1));
            //image.at((row+1)*width+Math.min(col+1,width-1),1, image.at((row+1)*width+Math.min(col, width-1),1));
            
        }
    for ( int row =1; row < height-1; row +=2)
        for ( int col=0; col < width-1; col++ ) {
        
        // Interpolation verticale rouge
            int a = CTOJ.toUnsigned(image.at(Math.max(row-3, 1)*width+ Math.max(col,0),0));
            int b = CTOJ.toUnsigned(image.at(Math.max(row-1, 1)*width+ Math.max(col,0),0));
            int c = CTOJ.toUnsigned(image.at(Math.min(row+1, height-1)*width+ Math.min(col, width-1),0));
            int d = CTOJ.toUnsigned(image.at(Math.min(row+3, height-1)*width+ Math.min(col, width-1),0));
            int sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image.at((row)*width+col,0, (short)sum);

            // Interpolation verticale bleue
            a = CTOJ.toUnsigned(image.at(Math.max(row-2,0)*width+ Math.max(col, 0), 2));
            b = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col, 0), 2));
            c = CTOJ.toUnsigned(image.at(Math.min(row+2, height-1)*width+ Math.min(col,width-1), 2));
            d = CTOJ.toUnsigned(image.at(Math.min(row+4, height-1)*width+ Math.min( col, width-1), 2));
            sum = (aa2*(b+c) -(a+d))/bb2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+col,2, (short)sum);
        }
    
    image_green = null;
}

void lin_interpolate_yb2() {
    
    int[][] image_green = new int[width*2+10][height*2+10];
    
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {

            if ( (col+1) < width)
                image_green[ 4 + (col+1)*2][4 + row*2] = CTOJ.toUnsigned(image.at((row)*width+(col+1),1));
            if ( (row+1) < height)
                image_green[4 + col*2][4 + (row+1)*2] = CTOJ.toUnsigned(image.at((row+1)*width+col,1));
        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
        // Interpolation de bas-gauche vers haut-droit
            int b = image_green[4 + col*2  ][4 + row*2+2];
            int c = image_green[4 + col*2+2][4 + row*2  ];
                    
            int sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+1][4 + row*2+1] = sum;
            
            b = image_green[4 + col*2+2][4 + row*2+4];
            c = image_green[4 + col*2+4][4 + row*2+2];
                    
            sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+3][4 + row*2+3] = sum;
            

        }
    
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            // Interpolation de haut-gauche vers bas-droit
            int b = image_green[4 + col*2-1][4 + row*2-1];
            int c = image_green[4 + col*2+1][4 + row*2+1];
            
            int sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2][4 + row*2] = sum;
            
            b = image_green[4 + col*2+1][4 + row*2+1];
            c = image_green[4 + col*2+3][4 + row*2+3];
            
            sum = (b+c)/2;
//            if ( sum < 0)
//                sum = 0;
//            if ( sum > Short.MAX_VALUE*2)
//                sum = Short.MAX_VALUE*2;
            
            image_green[4 + col*2+2][4 + row*2+2] = sum;
            
        }
        
    // On recopie la matrice de vert
    for ( int row =0; row < height; row +=2)
        for ( int col=0; col < width; col+=2 ) {
        
            int sum = image_green[ 4 + (col)*2][4 + row*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            

            image.at((row)*width+(col),1, (short)sum);
            if ( (col+1) < width)
                if ( (row+1) < height) {
                
                    sum = image_green[4 + (col+1)*2][4 + (row+1)*2];
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
                
                    image.at((row+1)*width+col+1,1, (short)sum);
                }
        }
    
    for ( int row =-1; row < height-1; row +=2)
        for ( int col=1; col <= width; col+=2 ) {

        // Interpolation horizontale rouge
            int b = CTOJ.toUnsigned(image.at((row+1)*width+ Math.max(col-1,0),0));
            int c = CTOJ.toUnsigned(image.at((row+1)*width+ Math.min(col+1, width-2),0));
            int sum = (b+c)/2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+Math.min(col,width-1),0, (short) sum );
            if (row >=0) {
                // Interpolation horizontale bleue
                b = CTOJ.toUnsigned(image.at((row)*width+ Math.max(col-2, 0), 2));
                c = CTOJ.toUnsigned(image.at((row)*width+ Math.min(col,width-1), 2));
                sum = (b+c)/2;
                if ( sum < 0)
                    sum = 0;
                if ( sum > Short.MAX_VALUE*2)
                    sum = Short.MAX_VALUE*2;
                image.at((row)*width+Math.max(col-1,0),2, (short)sum);
            }            
        }
    for ( int row =-1; row < height-1; row +=2)
        for ( int col=0; col < width; col++ ) {
        
        // Interpolation verticale rouge
            if ( row >=0) {
            int b = CTOJ.toUnsigned(image.at(Math.max(row-1, 0)*width+ Math.max(col,0),0));
            int c = CTOJ.toUnsigned(image.at(Math.min(row+1, height-1)*width+ Math.min(col, width-1),0));
            int sum = (b+c)/2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            
            image.at((row)*width+col,0, (short)sum);
            }
            // Interpolation verticale bleue
            int b = CTOJ.toUnsigned(image.at(Math.max(row,1)*width+ Math.max(col, 0), 2));
            int c = CTOJ.toUnsigned(image.at(Math.min(row+2, height-1)*width+ Math.min(col,width-1), 2));
            int sum = (b+c)/2;
            if ( sum < 0)
                sum = 0;
            if ( sum > Short.MAX_VALUE*2)
                sum = Short.MAX_VALUE*2;
            image.at((row+1)*width+col,2, (short)sum);
        }
    
    image_green = null;
}

void lin_interpolate()
{
  int[][][] code = new int[16][16][32];
  int[] ip;
  int c, i, x, y, row, col, shift, color;
  NewShortPtr pix = new NewShortPtr();
  pix.assign(image);

  if (verbose != 0) System.err.printf ( "Bilinear interpolation...\n");

 int[] sum = new int[4];
  border_interpolate(1);
  for (row=0; row < 16; row++)
    for (col=0; col < 16; col++) {
      ip = code[row][col];
      int ip_index =0;
      sum[0]=sum[1]=sum[2]=sum[3]=0;
      //memset (sum, 0, sizeof sum);
      for (y=-1; y <= 1; y++)
	for (x=-1; x <= 1; x++) {
	  shift = ((y==0)?1:0) + ((x==0)?1:0);
	  if (shift == 2) continue;
	  color = fc(row+y,col+x);
	  ip[ip_index] = (width*y + x)*4 + color; ip_index++;
	  ip[ip_index] = shift; ip_index++;
	  ip[ip_index] = color; ip_index++;
	  sum[color] += 1 << shift;
	}
      for (c=0; c < colors; c++)
	if (c != fc(row,col)) {
	  ip[ip_index] = c; ip_index++;
	  ip[ip_index] = sum[c]; ip_index++;
	}
    }
  for (row=1; row < height-1; row++) {
    for (col=1; col < width-1; col++) {
      //pix = image.toShortPtr(row*width+col);
        pix.setOffset(image.getOffset(row*width+col));
      ip = code[row & 15][col & 15];
      int ip_index =0;
      sum[0]=sum[1]=sum[2]=sum[3]=0;
      //memset (sum, 0, sizeof sum);
      for (i=8; i-- != 0; ip_index +=3)
	sum[ip[2+ip_index]] += CTOJ.toUnsigned(pix.at(ip[0+ip_index])) << ip[1+ip_index];
      for (i=colors; --i != 0; ip_index += 2)
	pix.at(ip[0+ip_index],(short)( sum[ip[0+ip_index]] / ip[1+ip_index]));
    }
    Thread.yield();
  }
}

/*
   This algorithm is officially called:

   "Interpolation using a Threshold-based variable number of gradients"

   described in http://scien.stanford.edu/class/psych221/projects/99/tingchen/algodep/vargra.html

   I've extended the basic idea to work with non-Bayer filter arrays.
   Gradients are numbered clockwise from NW=0 to W=7.
 */
void  vng_interpolate()
{
}

void  cam_to_cielab (short[] cam, float[] lab)
{
  int c, i, j, k;
  float r;
  float[] xyz = new float[3];
  float[] cbrt = new float[0x10000];
  float[][] xyz_cam = new float[3][4];

  if (cam == null) {
    for (i=0; i < 0x10000; i++) {
      r = (float)(i / 65535.0);
      cbrt[i] = r > 0.008856 ? (float)(Math.pow(r,1/3.0)) : (float)(7.787*r + 16/116.0);
    }
    for (i=0; i < 3; i++)
      for (j=0; j < colors; j++)
        for (xyz_cam[i][j] = k=0; k < 3; k++)
	  xyz_cam[i][j] += xyz_rgb[i][k] * rgb_cam[k][j] / d65_white[i];
  } else {
    xyz[0] = xyz[1] = xyz[2] = 0.5f;
    for (c=0; c < colors; c++) {
      xyz[0] += xyz_cam[0][c] * cam[c];
      xyz[1] += xyz_cam[1][c] * cam[c];
      xyz[2] += xyz_cam[2][c] * cam[c];
    }
    xyz[0] = cbrt[CLIP((int) xyz[0])];
    xyz[1] = cbrt[CLIP((int) xyz[1])];
    xyz[2] = cbrt[CLIP((int) xyz[2])];
    lab[0] = 116 * xyz[1] - 16;
    lab[1] = 500 * (xyz[0] - xyz[1]);
    lab[2] = 200 * (xyz[1] - xyz[2]);
  }
}

/*
   Adaptive Homogeneity-Directed interpolation is based on
   the work of Keigo Hirakawa, Thomas Parks, and Paul Lee.
 */
public static int TS = 256;		/* Tile Size */



void  tiff_get (int base,
	MutableInteger tag, MutableInteger type, MutableInteger len, MutableInteger save)
{
    CharPtr string = new CharPtr("1112481124848");
    
  tag.value  = get2();
  type.value = get2();
  len.value  = get4();
  save.value = (int)CTOJ.ftell(ifp) + 4;
  if (len.value * ( string.charAt(type.value < 13 ? type.value:0)-'0') > 4)
    CTOJ.fseek (ifp, get4()+base, CTOJ.SEEK_SET);
}

void  parse_thumb_note (int base, int toff, int tlen)
{
  int entries;
  
  MutableInteger tag = new MutableInteger(), type = new MutableInteger(), len = new MutableInteger(), save = new MutableInteger();

  entries = get2();
  while (entries-- != 0) {
    tiff_get (base, tag, type, len, save);
    if (tag.value == toff) thumb_offset = get4();
    if (tag.value == tlen) thumb_length = get4();
    CTOJ.fseek (ifp, save.value, CTOJ.SEEK_SET);
  }
}

void  parse_makernote (int base)
{
  int[][] xlat/*[2][256]*/ = {
  { 0xc1,0xbf,0x6d,0x0d,0x59,0xc5,0x13,0x9d,0x83,0x61,0x6b,0x4f,0xc7,0x7f,0x3d,0x3d,
    0x53,0x59,0xe3,0xc7,0xe9,0x2f,0x95,0xa7,0x95,0x1f,0xdf,0x7f,0x2b,0x29,0xc7,0x0d,
    0xdf,0x07,0xef,0x71,0x89,0x3d,0x13,0x3d,0x3b,0x13,0xfb,0x0d,0x89,0xc1,0x65,0x1f,
    0xb3,0x0d,0x6b,0x29,0xe3,0xfb,0xef,0xa3,0x6b,0x47,0x7f,0x95,0x35,0xa7,0x47,0x4f,
    0xc7,0xf1,0x59,0x95,0x35,0x11,0x29,0x61,0xf1,0x3d,0xb3,0x2b,0x0d,0x43,0x89,0xc1,
    0x9d,0x9d,0x89,0x65,0xf1,0xe9,0xdf,0xbf,0x3d,0x7f,0x53,0x97,0xe5,0xe9,0x95,0x17,
    0x1d,0x3d,0x8b,0xfb,0xc7,0xe3,0x67,0xa7,0x07,0xf1,0x71,0xa7,0x53,0xb5,0x29,0x89,
    0xe5,0x2b,0xa7,0x17,0x29,0xe9,0x4f,0xc5,0x65,0x6d,0x6b,0xef,0x0d,0x89,0x49,0x2f,
    0xb3,0x43,0x53,0x65,0x1d,0x49,0xa3,0x13,0x89,0x59,0xef,0x6b,0xef,0x65,0x1d,0x0b,
    0x59,0x13,0xe3,0x4f,0x9d,0xb3,0x29,0x43,0x2b,0x07,0x1d,0x95,0x59,0x59,0x47,0xfb,
    0xe5,0xe9,0x61,0x47,0x2f,0x35,0x7f,0x17,0x7f,0xef,0x7f,0x95,0x95,0x71,0xd3,0xa3,
    0x0b,0x71,0xa3,0xad,0x0b,0x3b,0xb5,0xfb,0xa3,0xbf,0x4f,0x83,0x1d,0xad,0xe9,0x2f,
    0x71,0x65,0xa3,0xe5,0x07,0x35,0x3d,0x0d,0xb5,0xe9,0xe5,0x47,0x3b,0x9d,0xef,0x35,
    0xa3,0xbf,0xb3,0xdf,0x53,0xd3,0x97,0x53,0x49,0x71,0x07,0x35,0x61,0x71,0x2f,0x43,
    0x2f,0x11,0xdf,0x17,0x97,0xfb,0x95,0x3b,0x7f,0x6b,0xd3,0x25,0xbf,0xad,0xc7,0xc5,
    0xc5,0xb5,0x8b,0xef,0x2f,0xd3,0x07,0x6b,0x25,0x49,0x95,0x25,0x49,0x6d,0x71,0xc7 },
  { 0xa7,0xbc,0xc9,0xad,0x91,0xdf,0x85,0xe5,0xd4,0x78,0xd5,0x17,0x46,0x7c,0x29,0x4c,
    0x4d,0x03,0xe9,0x25,0x68,0x11,0x86,0xb3,0xbd,0xf7,0x6f,0x61,0x22,0xa2,0x26,0x34,
    0x2a,0xbe,0x1e,0x46,0x14,0x68,0x9d,0x44,0x18,0xc2,0x40,0xf4,0x7e,0x5f,0x1b,0xad,
    0x0b,0x94,0xb6,0x67,0xb4,0x0b,0xe1,0xea,0x95,0x9c,0x66,0xdc,0xe7,0x5d,0x6c,0x05,
    0xda,0xd5,0xdf,0x7a,0xef,0xf6,0xdb,0x1f,0x82,0x4c,0xc0,0x68,0x47,0xa1,0xbd,0xee,
    0x39,0x50,0x56,0x4a,0xdd,0xdf,0xa5,0xf8,0xc6,0xda,0xca,0x90,0xca,0x01,0x42,0x9d,
    0x8b,0x0c,0x73,0x43,0x75,0x05,0x94,0xde,0x24,0xb3,0x80,0x34,0xe5,0x2c,0xdc,0x9b,
    0x3f,0xca,0x33,0x45,0xd0,0xdb,0x5f,0xf5,0x52,0xc3,0x21,0xda,0xe2,0x22,0x72,0x6b,
    0x3e,0xd0,0x5b,0xa8,0x87,0x8c,0x06,0x5d,0x0f,0xdd,0x09,0x19,0x93,0xd0,0xb9,0xfc,
    0x8b,0x0f,0x84,0x60,0x33,0x1c,0x9b,0x45,0xf1,0xf0,0xa3,0x94,0x3a,0x12,0x77,0x33,
    0x4d,0x44,0x78,0x28,0x3c,0x9e,0xfd,0x65,0x57,0x16,0x94,0x6b,0xfb,0x59,0xd0,0xc8,
    0x22,0x36,0xdb,0xd2,0x63,0x98,0x43,0xa1,0x04,0x87,0x86,0xf7,0xa6,0x26,0xbb,0xd6,
    0x59,0x4d,0xbf,0x6a,0x2e,0xaa,0x2b,0xef,0xe6,0x78,0xb6,0x4e,0xe0,0x2f,0xdc,0x7c,
    0xbe,0x57,0x19,0x32,0x7e,0x2a,0xd0,0xb8,0xba,0x29,0x00,0x3c,0x52,0x7d,0xa8,0x49,
    0x3b,0x2d,0xeb,0x25,0x49,0xfa,0xa3,0xaa,0x39,0xa7,0xc5,0xa7,0x50,0x11,0x36,0xfb,
    0xc6,0x67,0x4a,0xf5,0xa5,0x12,0x65,0x7e,0xb0,0xdf,0xaf,0x4e,0xb3,0x61,0x7f,0x2f } };
  int offset=0, entries;
  MutableInteger tag = new MutableInteger(), type = new MutableInteger(), len = new MutableInteger(), save = new MutableInteger();
  int c;
  int ver97=0, serial=0, i, wbi = 0;
  int[] wb/*[4]*/={0,0,0,0};
  BytePtr buf97 = new BytePtr(324);
  byte ci, cj, ck;
  short sorder;
  BytePtr buf = new BytePtr(10);
/*
   The MakerNote might have its own TIFF header (possibly with
   its own byte-order!), or it might just be a table.
 */
  sorder = order;
  CTOJ.fread (buf, 1, 10, ifp);
  if (CTOJ.strncmp (buf,"KDK" ,3)==0 ||	/* these aren't TIFF tables */
      CTOJ.strncmp (buf,"VER" ,3)==0 ||
      CTOJ.strncmp (buf,"IIII",4)==0 ||
      CTOJ.strncmp (buf,"MMMM",4)==0) return;
  if (CTOJ.strncmp (buf,"KC"  ,2)==0 ||	/* Konica KD-400Z, KD-510Z */
      CTOJ.strncmp (buf,"MLY" ,3)==0) {	/* Minolta DiMAGE G series */
    order = 0x4d4d;
    while ((i= (int)CTOJ.ftell(ifp)) < data_offset && i < 16384) {
      wb[0] = wb[2];  wb[2] = wb[1];  wb[1] = wb[3];
      wb[3] = get2();
      if (wb[1] == 256 && wb[3] == 256 &&
	  wb[0] > 256 && wb[0] < 640 && wb[2] > 256 && wb[2] < 640)
	for (c=0; c < 4; c++) cam_mul[c] = wb[c];
    }
    {
          order = sorder;
          return;
    }
  }
  if ( CTOJ.strcmp (buf,"Nikon") == 0) {
    base = (int)CTOJ.ftell(ifp);
    order = (short) get2();
    if (get2() != 42) {
          order = sorder;
          return;
    }
    offset = get4();
    CTOJ.fseek (ifp, offset-8, CTOJ.SEEK_CUR);
  } else if (CTOJ.strncmp (buf,"FUJIFILM",8)==0 ||
	     CTOJ.strncmp (buf,"SONY",4)==0 ||
	     CTOJ.strcmp  (buf,"Panasonic")==0) {
    order = 0x4949;
    CTOJ.fseek (ifp,  2, CTOJ.SEEK_CUR);
  } else if (CTOJ.strcmp (buf,"OLYMP")==0 ||
	     CTOJ.strcmp (buf,"LEICA")==0 ||
	     CTOJ.strcmp (buf,"Ricoh")==0 ||
	     CTOJ.strcmp (buf,"EPSON")==0)
    CTOJ.fseek (ifp, -2, CTOJ.SEEK_CUR);
  else if (CTOJ.strcmp (buf,"AOC")==0 ||
	   CTOJ.strcmp (buf,"QVC")==0)
    CTOJ.fseek (ifp, -4, CTOJ.SEEK_CUR);
  else CTOJ.fseek (ifp, -10, CTOJ.SEEK_CUR);

  entries = get2();
  if (entries > 1000) return;
  while (entries-- != 0) {
    tiff_get (base, tag, type, len, save);
    if (tag.value == 2 && Uc.strstr(make,"NIKON") != null) {
        get2();
      iso_speed = get2();
    }
    if (tag.value == 4 && len.value > 26 && len.value < 35) {
        get4();
      iso_speed = (float)(50 * Math.pow (2, (get2())/32.0 - 4));
      get2();
      if ((i=(get2())) != 0x7fff)
	aperture = (float)Math.pow (2, i/64.0);
      if ((i=get2()) != 0xffff)
	shutter = (float)(Math.pow (2, (short) i/-32.0));
      get2();
      wbi = (get2());
      get2();
      shot_order = (get2());
    }
    if (tag.value == 8 && type.value == 4)
      shot_order = get4();
    if (tag.value == 9 && Uc.strcmp(make,"Canon") == 0)
      Uc.fread (artist, 64, 1, ifp);
    if (tag.value == 0xc && len.value == 4) {
      cam_mul[0] = (float)getrat();
      cam_mul[2] = (float)getrat();
    }
    if (tag.value == 0x10 && type.value == 4)
      unique_id = get4();    
    if (tag.value == 0x11 && is_raw!=0 && Uc.strncmp(make,"NIKON",5)==0) {
      CTOJ.fseek (ifp, get4()+base, CTOJ.SEEK_SET);
      parse_tiff_ifd (base);
    }
    if (tag.value == 0x14 && len.value == 2560 && type.value == 7) {
      CTOJ.fseek (ifp, 1248, CTOJ.SEEK_CUR);
      get2_256();
    }
    if (tag.value == 0x15 && type.value == 2 && is_raw!=0)
      Uc.fread (model, 64, 1, ifp);
    if (Uc.strstr(make,"PENTAX") != null) {
      if (tag.value == 0x1b) tag.value = 0x1018;
      if (tag.value == 0x1c) tag.value = 0x1017;
    }
    if (tag.value == 0x1d)
      while ((c = CTOJ.fgetc(ifp)) != 0)
	serial = serial*10 + (Uc.isdigit(c) ? c - '0' : c % 10);
    if (tag.value == 0x81 && type.value == 4) {
      data_offset = get4();
      CTOJ.fseek (ifp, data_offset + 41, CTOJ.SEEK_SET);
      raw_height = get2() * 2;
      raw_width  = get2();
      filters = 0x61616161l;
    }
    if (tag.value == 0x29 && type.value == 1) {
        short[] vals = {0,1,2,3,4,7,8,0,0,0,0,0,0,0,5,8,9,6};
      c = wbi < 18 ? vals[wbi] : 0;
      CTOJ.fseek (ifp, 8 + c*32, CTOJ.SEEK_CUR);
      for (c=0; c < 4; c++)  cam_mul[c ^ (c >> 1) ^ 1] = get4();
    }
    if ((tag.value == 0x81  && type.value == 7) ||
	(tag.value == 0x100 && type.value == 7) ||
	(tag.value == 0x280 && type.value == 1)) {
      thumb_offset = (int)CTOJ.ftell(ifp);
      thumb_length = len.value;
    }
    if (tag.value == 0x88 && type.value == 4 && (thumb_offset = get4()) != 0)
      thumb_offset += base;
    if (tag.value == 0x89 && type.value == 4)
      thumb_length = get4();
    if (tag.value == 0x8c || tag.value == 0x96)
      meta_offset = CTOJ.ftell(ifp);      
    if (tag.value == 0x97) {
      for (i=0; i < 4; i++)
	ver97 = ver97 * 10 + CTOJ.fgetc(ifp)-'0';
      switch (ver97) {
	case 100:
	  CTOJ.fseek (ifp, 68, CTOJ.SEEK_CUR);
	  for (c=0; c < 4; c++) cam_mul[(c >> 1) | ((c & 1) << 1)] = get2();
	  break;
	case 102:
	  CTOJ.fseek (ifp, 6, CTOJ.SEEK_CUR);
	  get2_rggb();
	case 103:
	  CTOJ.fseek (ifp, 16, CTOJ.SEEK_CUR);
	  for (c=0; c < 4; c++) cam_mul[c] = get2();
      }
      if (ver97 >= 200) {
	if (ver97 != 205) CTOJ.fseek (ifp, 280, CTOJ.SEEK_CUR);
	CTOJ.fread (buf97, 324, 1, ifp);
      }
    }
    if (tag.value == 0xa4 && type.value == 3) {
      CTOJ.fseek (ifp, wbi*48, CTOJ.SEEK_CUR);
      for ( c=0; c<3; c++) cam_mul[c] = get2();
    }
    if (tag.value == 0xa7 && CTOJ.toUnsigned(ver97-200) < 12 && cam_mul[0] == 0.0) {
      ci = (byte)xlat[0][serial & 0xff];
      cj = (byte)xlat[1][CTOJ.fgetc(ifp)^CTOJ.fgetc(ifp)^CTOJ.fgetc(ifp)^CTOJ.fgetc(ifp)];
      ck = 0x60;
      for (i=0; i < 324; i++) {
          cj += ci * ck++;
	buf97.at(i, (byte)(buf97.at(i) ^ cj));
      }
      String str = "66666>666;6A";
      i = str.charAt(ver97-200) - '0';
      for (c=0; c < 4; c++) cam_mul[c ^ (c >> 1) ^ ( i & 1)] =
	sget2 (buf97.plus( ( i & -2) + c*2));
    }
    if (tag.value == 0x200 && len.value == 3) {
        get4();
      shot_order = (get4());
    }
    if (tag.value == 0x200 && len.value == 4)
      black = (get2()+get2()+get2()+get2())/4;
    if (tag.value == 0x201 && len.value == 4)
      get2_rggb();
    if (tag.value == 0x401 && len.value == 4) {
      black = (get4()+get4()+get4()+get4())/4;
    }
    if (tag.value == 0xe01) {		/* Nikon Capture Note */
      type.value = order;
      order = 0x4949;
      CTOJ.fseek (ifp, 22, CTOJ.SEEK_CUR);
      for (offset=22; offset+22 < len.value; offset += 22+i) {
	tag.value = get4();
	CTOJ.fseek (ifp, 14, CTOJ.SEEK_CUR);
	i = get4()-4;
	if (tag.value == 0x76a43207) flip = get2();
	else CTOJ.fseek (ifp, i, CTOJ.SEEK_CUR);
      }
      order = (short)type.value;
    }
    if (tag.value == 0xe80 && len.value == 256 && type.value == 7) {
      CTOJ.fseek (ifp, 48, CTOJ.SEEK_CUR);
      cam_mul[0] = (float)(get2() * 508 * 1.078 / 0x10000);
      cam_mul[2] = (float)(get2() * 382 * 1.173 / 0x10000);
    }
    if (tag.value == 0x1011 && len.value == 9 && use_camera_wb != 0) {
      for (i=0; i < 3; i++)
	for (c=0; c < 3; c++) rgb_cam[i][c] = (float)(((short) get2()) / 256.0);
      raw_color = (rgb_cam[0][0] < 1) ?1 : 0;
    }
    if (tag.value == 0x1017)
      cam_mul[0] = get2() / 256.0f;
    if (tag.value == 0x1018)
      cam_mul[2] = get2() / 256.0f;
    if (tag.value == 0x2011 && len.value == 2) {
get2_256:
      order = 0x4d4d;
      cam_mul[0] = get2() / 256.0f;
      cam_mul[2] = get2() / 256.0f;
    }
    if (tag.value == 0x2020)
      parse_thumb_note (base, 257, 258);
    if (tag.value == 0xb028) {
      CTOJ.fseek (ifp, get4(), CTOJ.SEEK_SET);
      parse_thumb_note (base, 136, 137);
    }
    if (tag.value == 0x4001 && type.value == 3) {
      i = len.value == 582 ? 50 : len.value == 653 ? 68 : 126;
      CTOJ.fseek (ifp, i, CTOJ.SEEK_CUR);
get2_rggb:
      for (c=0; c < 4; c++) cam_mul[c ^ (c >> 1)] = get2();
      CTOJ.fseek (ifp, 22, CTOJ.SEEK_CUR);
      for ( c=0; c<4; c++) sraw_mul[c ^ (c >> 1)] = get2();
    }
next:
    CTOJ.fseek (ifp, save.value, CTOJ.SEEK_SET);
  }
quit:
  order = sorder;
}
void get2_rggb() {
      for ( int c=0; c < 4; c++) cam_mul[c ^ (c >> 1)] = get2();    
      CTOJ.fseek (ifp, 22, CTOJ.SEEK_CUR);
      for ( int c=0; c<4; c++) sraw_mul[c ^ (c >> 1)] = get2();
}
void get2_256() {
      order = 0x4d4d;
      cam_mul[0] = (float)(get2() / 256.0);
      cam_mul[2] = (float)(get2() / 256.0);
}

/*
   Since the TIFF DateTime string has no timezone information,
   assume that the camera's clock was set to Universal Time.
 */
void  get_timestamp (int reversed)
{
  BytePtr str = new BytePtr(20);
  int i;

  if (timestamp != 0) return;
  str.at(19, (byte)0);
  if (reversed != 0)
    for (i=19; i-- != 0; ) str.at(i, (byte)CTOJ.fgetc(ifp));
  else
    CTOJ.fread (str, 19, 1, ifp);

  int year = ( str.at(0) - '0')*1000 + (str.at(1) - '0')*100 + (str.at(2) - '0' )*10 + str.at(3) - '0';
  int mon = (str.at(5) - '0')*10 + str.at(6)-'0';
  int day = (str.at(8) - '0')*10 + str.at(9)-'0';
  int hour = (str.at(11) - '0')*10 + str.at(12)-'0';
  int min =  (str.at(14) - '0')*10 + str.at(15)-'0';
  int sec =  (str.at(17) - '0')*10 + str.at(18)-'0';
  
  Calendar cal = new GregorianCalendar();
  cal.set(year,mon-1,day,hour,min,sec);
  timestamp = cal.getTimeInMillis();
}

void  parse_exif (int base)
{
  int kodak, entries, c;
  MutableInteger tag = new MutableInteger(), type = new MutableInteger(), len = new MutableInteger(), save = new MutableInteger();
  double expo;

  kodak = Uc.strncmp(make,"EASTMAN",7)==0 ? 1: 0;
  entries = get2();
  while (entries-- != 0) {
    tiff_get (base, tag, type, len, save);
    switch (tag.value) {
      case 33434:  shutter = (float)getrat();			break;
      case 33437:  aperture = (float)getrat();			break;
      case 34855:  iso_speed = get2();			break;
      case 36867:
      case 36868:  get_timestamp(0);			break;
      case 37377:  if ((expo = -getrat()) < 128)
		     shutter = (float)Math.pow (2, expo);		break;
      case 37378:  aperture = (float)Math.pow (2, getrat()/2);	break;
      case 37386:  focal_len = (float)getrat();		break;
      case 37500:  parse_makernote (base);		break;
      case 40962:  if (kodak != 0) raw_width  = get4();	break;
      case 40963:  if (kodak != 0) raw_height = get4();	break;
      case 41730:
	if (get4() == 0x20002)
	  for (exif_cfa=c=0; c < 8; c+=2)
	    exif_cfa |= CTOJ.fgetc(ifp) * 0x01010101 << c;
    }
    CTOJ.fseek (ifp, save.value, CTOJ.SEEK_SET);
  }
}

void parse_gps (int base)
{
  int entries, c;
  MutableInteger tag = new MutableInteger(), type = new MutableInteger(), len = new MutableInteger(), save = new MutableInteger();

  entries = get2();
  while (entries-- != 0) {
    tiff_get (base, tag, type, len, save);
    switch (tag.value) {
      case 1: case 3: case 5:
	gpsdata.at(29+tag.value/2, (byte)CTOJ.getc(ifp));			break;
      case 2: case 4: case 7:
	for ( c=0; c<6; c++) gpsdata.at(tag.value/3*6+c, (byte)get4());		break;
      case 6:
	for ( c=0; c<2; c++)  gpsdata.at(18+c, (byte)get4());			break;
      case 18: case 29:
	CTOJ.fgets ( gpsdata.plus(14+tag.value/3), Math.min(len.value,12), ifp);
    }
    CTOJ.fseek (ifp, save.value, CTOJ.SEEK_SET);
  }
}


void  romm_coeff (float romm_cam[][])
{
  float[][] rgb_romm =	/* ROMM == Kodak ProPhoto */
  { {  2.034193f, -0.727420f, -0.306766f },
    { -0.228811f,  1.231729f, -0.002922f },
    { -0.008565f, -0.153273f,  1.161839f } };
  int i, j, k;

  for (raw_color = i=0; i < 3; i++)
    for (j=0; j < 3; j++)
      for (rgb_cam[i][j] = k=0; k < 3; k++)
	rgb_cam[i][j] += rgb_romm[i][k] * romm_cam[k][j];
}


void  linear_table (int len)
{
  int i;
  if (len > 0x1000) len = 0x1000;
  read_shorts (curve, len);
  for (i=len; i < 0x1000; i++)
    curve.at(i, curve.at(i-1));
  maximum = curve.at(0xfff);
}

int  parse_tiff_ifd (int base/*, int level*/)
{
  int entries, plen=16;
  MutableInteger tag = new MutableInteger(), type = new MutableInteger(), len = new MutableInteger(), save = new MutableInteger();
  int ifd, use_cm=0, cfa, i, j, c, ima_len=0;
  BytePtr software = new BytePtr(64);
  BytePtr cbuf = new BytePtr(), cp = new BytePtr();
  BytePtr cfa_pat = new BytePtr(16);
  byte cfa_pc[] = { 0,1,2,3 };
  byte[] tab = new byte[256];
  double dblack;
  double[][] cc = new double[4][4], cm = new double[4][3], cam_xyz = new double[4][3];
  double num;
  double ab[]={ 1,1,1,1 }, asn[] = { 0,0,0,0 }, xyz[] = { 1,1,1 };
  int sony_curve[] = { 0,0,0,0,0,4095 };
  int sony_offset=0, sony_length=0, sony_key=0;
  jhead jh = new jhead();
  Object sfp;
/*
  if (tiff_nifds >= sizeof tiff_ifd / sizeof tiff_ifd[0])
    return 1;
 */
  ifd = tiff_nifds++;
  for (j=0; j < 4; j++)
    for (i=0; i < 4; i++)
      cc[j][i] = i == j ? 1 : 0;
  entries = get2();
  if (entries > 512) return 1;
  while (entries-- != 0) {
    tiff_get (base, tag, type, len, save);
    switch (tag.value) {
      case 17: case 18:
	if (type.value == 3 && len.value == 1)
	  cam_mul[(tag.value-17)*2] = (float) get2() / 256.0f;
	break;
      case 23:
	if (type.value == 3) iso_speed = get2();
	break;
      case 36: case 37: case 38:
	cam_mul[tag.value-0x24] = get2();
	break;
      case 39:
	if (len.value < 50 || cam_mul[0] != 0) break;
	CTOJ.fseek (ifp, 12, CTOJ.SEEK_CUR);
	for (c=0; c < 3; c++) cam_mul[c] = get2();
	break;
      case 46:
	if (type.value != 7 || CTOJ.fgetc(ifp) != 0xff || CTOJ.fgetc(ifp) != 0xd8) break;
	thumb_offset = CTOJ.ftell(ifp) - 2;
	thumb_length = len.value;
	break;
      case 2: case 256:			/* ImageWidth */
	tiff_ifd[ifd].width = getint(type.value);
	break;
      case 3: case 257:			/* ImageHeight */
	tiff_ifd[ifd].height = getint(type.value);
	break;
      case 258:				/* BitsPerSample */
	tiff_ifd[ifd].samples = len.value;
	tiff_ifd[ifd].bps = get2();
	break;
      case 259:				/* Compression */
	tiff_ifd[ifd].comp = get2();
	break;
      case 262:				/* PhotometricInterpretation */
	tiff_ifd[ifd].phint = get2();
	break;
      case 270:				/* ImageDescription */
	Uc.fread (desc, 512, 1, ifp);
      case 271:				/* Make */
	Uc.fgets (make, 64, ifp);
	break;
      case 272:				/* Model */
	Uc.fgets (model, 64, ifp);
	break;
      case 273:				/* StripOffset */
      case 513:
	tiff_ifd[ifd].offset = get4()+base;
	if ( tiff_ifd[ifd].width == 0) {
	  CTOJ.fseek (ifp, tiff_ifd[ifd].offset, CTOJ.SEEK_SET);
	  if (ljpeg_start (jh, 1) != 0) {
	    tiff_ifd[ifd].comp    = 6;
	    tiff_ifd[ifd].width   = jh.wide << ((jh.clrs == 2) ? 1 : 0);
	    tiff_ifd[ifd].height  = jh.high;
	    tiff_ifd[ifd].bps     = jh.bits;
	    tiff_ifd[ifd].samples = jh.clrs;
	  }
	}
	break;
      case 274:				/* Orientation */
          int[] nums = { 5,0,1,3,2,4,6,7 };
	tiff_ifd[ifd].flip = nums[get2() & 7];
	break;
      case 277:				/* SamplesPerPixel */
	tiff_ifd[ifd].samples = getint(type.value);
	break;
      case 279:				/* StripByteCounts */
      case 514:
	tiff_ifd[ifd].bytes = get4();
	break;
      case 305:				/* Software */
	CTOJ.fgets (software, 64, ifp);
	if (CTOJ.strncmp(software,"Adobe",5) == 0 ||
	    CTOJ.strncmp(software,"dcraw",5) == 0 ||
	    CTOJ.strncmp(software,"Bibble",6) == 0 ||
	    CTOJ.strncmp(software,"Nikon Scan",10) == 0 ||
	    CTOJ.strcmp (software,"Digital Photo Professional") == 0)
	  is_raw = 0;
	break;
      case 306:				/* DateTime */
	get_timestamp(0);
	break;
      case 315:				/* Artist */
	Uc.fread (artist, 64, 1, ifp);
	break;
      case 322:				/* TileWidth */
	tile_width = getint(type.value);
	break;
      case 323:				/* TileLength */
	tile_length = getint(type.value);
	break;
      case 324:				/* TileOffsets */
	tiff_ifd[ifd].offset = len.value > 1 ? (int)CTOJ.ftell(ifp) : get4();
	break;
      case 330:				/* SubIFDs */
	if ( Uc.strcmp(model,"DSLR-A100") == 0 && tiff_ifd[ifd].width == 3872) {
	  data_offset = get4()+base;
	  ifd++;  break;
	}
	while (len.value-- != 0) {
	  i = (int)CTOJ.ftell(ifp);
	  CTOJ.fseek (ifp, get4()+base, CTOJ.SEEK_SET);
	  if (parse_tiff_ifd (base) != 0) break;
	  CTOJ.fseek (ifp, i+4, CTOJ.SEEK_SET);
	}
	break;
      case 400:
	Uc.strcpy (make, "Sarnoff");
	maximum = 0xfff;
	break;
      case 28688:
	for ( c=0; c<4; c++) sony_curve[c+1] = get2() >> 2 & 0xfff;
	for (i=0; i < 5; i++)
	  for (j = sony_curve[i]+1; j <= sony_curve[i+1]; j++)
	    curve.at(j,(short)( curve.at(j-1) + (1 << i)));
	break;
      case 29184: sony_offset = get4();  break;
      case 29185: sony_length = get4();  break;
      case 29217: sony_key    = get4();  break;
      case 29443:
	for (c=0; c < 4; c++) cam_mul[c ^ ((c < 2) ? 1: 0)] = get2();
	break;
      case 33405:			/* Model2 */
	Uc.fgets (model2, 64, ifp);
	break;
      case 33422:			/* CFAPattern */
      case 64777:			/* Kodak P-series */
	if ((plen=len.value) > 16) plen = 16;
	CTOJ.fread (cfa_pat, 1, plen, ifp);
	for (colors=cfa=i=0; i < plen; i++) {
	  colors += (cfa & (1 << cfa_pat.uat(i))) == 0 ?1:0;
	  cfa |= 1 << cfa_pat.uat(i);
	}
	if (cfa == 070) CTOJ.memcpy (cfa_pc,"\003\004\005",3);	/* CMY */
	if (cfa == 072) CTOJ.memcpy (cfa_pc,"\005\003\004\001",4);	/* GMCY */
	//goto guess_cfa_pc;
guess_cfa_pc:
	for (c=0; c < colors; c++) tab[cfa_pc[c]] = (byte)c;
	cdesc.setAt(c, (char)0);
	for (i=16; i-- != 0; )
	  filters = CTOJ.cutToUnsigned(filters << 2) | tab[cfa_pat.at(i % plen)];
	break;
      case 33424:
	CTOJ.fseek (ifp, get4()+base, CTOJ.SEEK_SET);
	//parse_kodak_ifd (base);
	break;
      case 33434:			/* ExposureTime */
	shutter = (float)getrat();
	break;
      case 33437:			/* FNumber */
	aperture = (float)getrat();
	break;
      case 34306:			/* Leaf white balance */
	for (c=0; c < 4; c++) cam_mul[c ^ 1] = 4096.0f / get2();
	break;
      case 34307:			/* Leaf CatchLight color matrix */
	CTOJ.fread (software, 1, 7, ifp);
	if ( CTOJ.strncmp(software,"MATRIX",6) != 0) break;
	colors = 4;
	for (raw_color = i=0; i < 3; i++) {
	  for (c=0; c < 4; c++) {
              MutableFloat var = new MutableFloat();
//              Uc.fscanf (ifp, "%f", var); TODO: creer fonction ad_hoc
              rgb_cam[i][c^1] = var.value;
          }
	  if ( use_camera_wb == 0) continue;
	  num = 0;
	  for (c=0; c < 4; c++) num += rgb_cam[i][c];
	  for (c=0; c < 4; c++) rgb_cam[i][c] /= num;
	}
	break;
      case 34310:			/* Leaf metadata */
	//parse_mos (CTOJ.ftell(ifp));
      case 34303:
	Uc.strcpy (make, "Leaf");
	break;
      case 34665:			/* EXIF tag */
	CTOJ.fseek (ifp, get4()+base, CTOJ.SEEK_SET);
	parse_exif (base);
	break;
      case 34853:			/* GPSInfo tag */
	CTOJ.fseek (ifp, get4()+base, CTOJ.SEEK_SET);
	parse_gps (base);
	break;
      case 34675:			/* InterColorProfile */
      case 50831:			/* AsShotICCProfile */
	profile_offset = (int)CTOJ.ftell(ifp);
	profile_length = len.value;
	break;
      case 37122:			/* CompressedBitsPerPixel */
	kodak_cbpp = get4();
	break;
      case 37386:			/* FocalLength */
	focal_len = (float)getrat();
	break;
      case 37393:			/* ImageNumber */
	shot_order = getint(type.value);
	break;
      case 37400:			/* old Kodak KDC tag */
	for (raw_color = i=0; i < 3; i++) {
	  getrat();
	  for (c=0; c < 3; c++) rgb_cam[i][c] = (float)getrat();
	}
	break;
      case 46275:			/* Imacon tags */
	Uc.strcpy (make, "Imacon");
	data_offset = CTOJ.ftell(ifp);
	ima_len = len.value;
	break;
      case 46279:
	CTOJ.fseek (ifp, 78, CTOJ.SEEK_CUR);
	raw_width  = get4();
	raw_height = get4();
	left_margin = get4() & 7;
	width = raw_width - left_margin - (get4() & 7);
	top_margin = get4() & 7;
	height = raw_height - top_margin - (get4() & 7);
	if (raw_width == 7262) {
	  height = 5444;
	  width  = 7244;
	  left_margin = 7;
	}
	CTOJ.fseek (ifp, 52, CTOJ.SEEK_CUR);
	for (c=0; c < 3; c++) cam_mul[c] = (float)getreal(11);
	CTOJ.fseek (ifp, 114, CTOJ.SEEK_CUR);
	flip = (get2() >> 7) * 90;
	if (width * height * 6 == ima_len) {
	  if (flip % 180 == 90){
              int buff = height;
              height = width;
              width = buff;
          }
	  filters = flip = 0;
	}
	Uc.sprintf (model, "Ixpress %d-Mp", height*width/1000000);
	load_raw = new ImaconFullLoadRaw();
	if (filters != 0) {
	  if ((left_margin & 1)!=0) filters = 0x61616161l;
	  load_raw = new UnpackedLoadRaw() ;
	}
	maximum = 0xffff;
	break;
      case 50454:			/* Sinar tag */
      case 50455:
	cbuf.assign( CTOJ.malloc(len.value));
	CTOJ.fread (cbuf, 1, len.value, ifp);
	for (cp.assign(cbuf.plus(-1)); !cp.isNull() && cp.lessThan( cbuf.plus(len.value)); cp = CTOJ.strchr(cp,'\n')) {
            cp.plusPlus();
	  if (CTOJ.strncmp (cp,"Neutral ",8)!=0) {
              MutableFloat cam = new MutableFloat();
              MutableFloat cam1 = new MutableFloat();
              MutableFloat cam2 = new MutableFloat();
	    Uc.sscanf (cp.plus(8), "%f %f %f", cam, cam1, cam2);
            cam_mul[0] = cam.value;
            cam_mul[1] = cam1.value;
            cam_mul[2] = cam2.value;
          }
        }
	//free (cbuf);
	break;
      case 50459:			/* Hasselblad tag */
	i = order;
	j = CTOJ.ftell(ifp);
	c = tiff_nifds;
	order = (short)get2();
        get2();
	CTOJ.fseek (ifp, j+get4(), CTOJ.SEEK_SET);
	parse_tiff_ifd (j);
	maximum = 0xffff;
	tiff_nifds = c;
	order = (short)i;
	break;
      case 50706:			/* DNGVersion */
	for (c=0; c < 4; c++) dng_version = (dng_version << 8) + CTOJ.fgetc(ifp);
	break;
      case 50710:			/* CFAPlaneColor */
	if (len.value > 4) len.value = 4;
	colors = len.value;
	//CTOJ.fread (cfa_pc, 1, colors, ifp); TODO a faire
guess_cfa_pc:
	for ( c=0; c <colors; c++) tab[cfa_pc[c]] = (byte)c;
	cdesc.setAt(c, (char)0);
	for (i=16; i-- != 0; )
	  filters = filters << 2 | tab[cfa_pat.at(i % plen)];
	break;
      case 50711:			/* CFALayout */
	if (get2() == 2) {
	  fuji_width = 1;
	  filters = 0x49494949l;
	}
	break;
      case 291:
      case 50712:			/* LinearizationTable */
	linear_table (len.value);
	break;
      case 50714:			/* BlackLevel */
      case 50715:			/* BlackLevelDeltaH */
      case 50716:			/* BlackLevelDeltaV */
	for (dblack=i=0; i < len.value; i++)
	  dblack += getreal(type.value);
	black += dblack/len.value + 0.5;
	break;
      case 50717:			/* WhiteLevel */
	maximum = getint(type.value);
	break;
      case 50718:			/* DefaultScale */
	pixel_aspect = getrat();
	pixel_aspect /= getrat();
	break;
      case 50721:			/* ColorMatrix1 */
      case 50722:			/* ColorMatrix2 */
	for (c=0; c < colors; c++) for (j=0; j < 3; j++)
	  cm[c][j] = getrat();
	use_cm = 1;
	break;
      case 50723:			/* CameraCalibration1 */
      case 50724:			/* CameraCalibration2 */
	for (i=0; i < colors; i++)
	  for (c=0; c < colors; c++) cc[i][c] = getrat();
      case 50727:			/* AnalogBalance */
	for (c=0; c < colors; c++) ab[c] = getrat();
	break;
      case 50728:			/* AsShotNeutral */
	for (c=0; c < colors; c++) asn[c] = getreal(type.value);
	break;
      case 50729:			/* AsShotWhiteXY */
	xyz[0] = getrat();
	xyz[1] = getrat();
	xyz[2] = 1 - xyz[0] - xyz[1];
	for (c=0; c < 3; c++) xyz[c] /= d65_white[c];
	break;
      case 50740:			/* DNGPrivateData */
	if (dng_version != 0) break;
	i = order;
	parse_minolta (j = get4()+base);
	order = (short)i;
	CTOJ.fseek (ifp, j, CTOJ.SEEK_SET);
	parse_tiff_ifd (base);
	break;
      case 50752:
	read_shorts (cr2_slice, 3);
	break;
      case 50829:			/* ActiveArea */
	top_margin = getint(type.value);
	left_margin = getint(type.value);
	height = getint(type.value) - top_margin;
	width = getint(type.value) - left_margin;
	break;
      case 64772:			/* Kodak P-series */
	CTOJ.fseek (ifp, 16, CTOJ.SEEK_CUR);
	data_offset = get4();
	CTOJ.fseek (ifp, 28, CTOJ.SEEK_CUR);
	data_offset += get4();
	load_raw = new Packed12LoadRaw();
    }
    CTOJ.fseek (ifp, save.value, CTOJ.SEEK_SET);
  }
  /*
  if (sony_length && (buf = (unsigned *) malloc(sony_length))) {
    fseek (ifp, sony_offset, SEEK_SET);
    fread (buf, sony_length, 1, ifp);
    sony_decrypt (buf, sony_length/4, 1, sony_key);
    sfp = ifp;
    if ((ifp = tmpfile())) {
      fwrite (buf, sony_length, 1, ifp);
      fseek (ifp, 0, SEEK_SET);
      parse_tiff_ifd (-sony_offset, level);
      fclose (ifp);
    }
    ifp = sfp;
    free (buf);
  }
   */
  for (i=0; i < colors; i++)
    for (c=0; c < colors; c++) cc[i][c] *= ab[i];
  if (use_cm != 0) {
    for (c=0; c < colors; c++) for (i=0; i < 3; i++)
      for (cam_xyz[c][i]=j=0; j < colors; j++)
	cam_xyz[c][i] += cc[c][j] * cm[j][i] * xyz[i];
    cam_xyz_coeff (cam_xyz);
  }
  if (asn[0] != 0)
    for (c=0; c < colors; c++) pre_mul[c] = (float)(1 / asn[c]);
  if ( use_cm == 0)
    for (c=0; c < colors; c++) pre_mul[c] /= cc[c][c];
  return 0;
}

void  parse_tiff (int base)
{
  int doff, max_samp=0, raw=-1, thm=-1, i;
  jhead jh = new jhead();

  CTOJ.fseek (ifp, base, CTOJ.SEEK_SET);
  order = (short)get2();
  if (order != 0x4949 && order != 0x4d4d) return;
  get2();
//  Uc.memset (tiff_ifd, 0, Uc.sizeof(tiff_ifd));
  tiff_nifds = 0;
  while ((doff = get4()) !=0) {
    CTOJ.fseek (ifp, doff+base, CTOJ.SEEK_SET);
    if (parse_tiff_ifd (base) != 0) break;
  }
  thumb_misc = 16;
  if (thumb_offset != 0) {
    CTOJ.fseek (ifp, thumb_offset, CTOJ.SEEK_SET);
    if (ljpeg_start (jh, 1) != 0) {
      thumb_misc   = jh.bits;
      thumb_width  = jh.wide;
      thumb_height = jh.high;
    }
  }
  for (i=0; i < tiff_nifds; i++) {
    if (max_samp < tiff_ifd[i].samples)
	max_samp = tiff_ifd[i].samples;
    if (max_samp > 3) max_samp = 3;
    if ((tiff_ifd[i].comp != 6 || tiff_ifd[i].samples != 3) &&
	tiff_ifd[i].width*tiff_ifd[i].height > raw_width*raw_height) {
      raw_width     = tiff_ifd[i].width;
      raw_height    = tiff_ifd[i].height;
      tiff_bps      = tiff_ifd[i].bps;
      tiff_compress = tiff_ifd[i].comp;
      data_offset   = tiff_ifd[i].offset;
      tiff_flip     = tiff_ifd[i].flip;
      tiff_samples  = tiff_ifd[i].samples;
      fuji_secondary = (tiff_samples == 2) ? 1 : 0;
      raw = i;
    }
  }
  fuji_width *= (raw_width+1)/2;
  if (tiff_ifd[0].flip != 0 ) tiff_flip = tiff_ifd[0].flip;
  if (raw >= 0 && load_raw == null)
    switch (tiff_compress) {
      case 0:  case 1:
	switch (tiff_bps) {
	  case  8: load_raw = new EightBitLoadRaw();	break;
	  case 12: load_raw = new Packed12LoadRaw();
		   if ( Uc.strncmp(make,"NIKON",5) == 0)
		     load_raw = new NikonLoadRaw();
		   if ( Uc.strncmp(make,"PENTAX",6) !=0) break;
	  case 14:
	  case 16: load_raw = new UnpackedLoadRaw() ;		break;
	}
	if (tiff_ifd[raw].bytes*5 == raw_width*raw_height*8)
	  load_raw = new OlympusE300LoadRaw();
	if (tiff_bps == 12 && tiff_ifd[raw].phint == 2)
	  load_raw = new OlympusCseriesLoadRaw();
	break;
      case 6:  case 7:  case 99:
	load_raw = new LosslessJpegLoadRaw();		break;
      case 262:
	load_raw = new Kodak262LoadRaw();			break;
      case 32767:
	load_raw = new SonyArw2LoadRaw();
	if (tiff_ifd[raw].bytes*8 == raw_width*raw_height*tiff_bps)
	  break;
	raw_height += 8;
	load_raw = new SonyArwLoadRaw();			break;
      case 32769:
	load_raw = new NikonLoadRaw();			break;
      case 32773:
	load_raw = new Packed12LoadRaw();			break;
      case 34713:
	load_raw = new NikonCompressedLoadRaw();		break;
      case 65535:
	load_raw = new PentaxK10LoadRaw();			break;
      case 65000:
	switch (tiff_ifd[raw].phint) {
	  case 2: load_raw = new KodakRgbLoadRaw();   filters = 0;  break;
	  case 6: load_raw = new KodakYcbcrLoadRaw(); filters = 0;  break;
	  case 32803: load_raw = new Kodak65000LoadRaw();
	}
      case 32867: break;
      default: is_raw = 0;
    }
  if (tiff_samples == 3 && tiff_bps == 8)
    if ( dng_version == 0) is_raw = 0;
  for (i=0; i < tiff_nifds; i++)
    if (i != raw && tiff_ifd[i].samples == max_samp &&
	tiff_ifd[i].width * tiff_ifd[i].height / Math.pow(tiff_ifd[i].bps+1,2) >
	      thumb_width *       thumb_height / Math.pow(thumb_misc+1,2)) {
      thumb_width  = tiff_ifd[i].width;
      thumb_height = tiff_ifd[i].height;
      thumb_offset = tiff_ifd[i].offset;
      thumb_length = tiff_ifd[i].bytes;
      thumb_misc   = tiff_ifd[i].bps;
      thm = i;
    }
  if (thm >= 0) {
    thumb_misc |= tiff_ifd[thm].samples << 5;
    switch (tiff_ifd[thm].comp) {
    }
  }
}

void  parse_minolta (int base)
{
  int save, tag, len, offset, high=0, wide=0, i, c;

  CTOJ.fseek (ifp, base, CTOJ.SEEK_SET);
  if (CTOJ.fgetc(ifp)!=0 || (CTOJ.fgetc(ifp)-'M')!=0 || (CTOJ.fgetc(ifp)-'R')!=0) return;
  order = (short)(CTOJ.fgetc(ifp) * 0x101);
  offset = base + get4() + 8;
  while ((save=CTOJ.ftell(ifp)) < offset) {
    for (tag=i=0; i < 4; i++)
      tag = tag << 8 | CTOJ.fgetc(ifp);
    len = get4();
    switch (tag) {
      case 0x505244:				/* PRD */
	CTOJ.fseek (ifp, 8, CTOJ.SEEK_CUR);
	high = get2();
	wide = get2();
	break;
      case 0x574247:				/* WBG */
	get4();
	i = Uc.strstr(model,"A200") != null ? 3:0;
	for (c=0; c < 4; c++) cam_mul[c ^ (c >> 1) ^ i] = get2();
	break;
      case 0x545457:				/* TTW */
	parse_tiff (CTOJ.ftell(ifp));
	data_offset = offset;
    }
    CTOJ.fseek (ifp, save+len+8, CTOJ.SEEK_SET);
  }
  raw_height = high;
  raw_width  = wide;
}

/*
   CIFF block 0x1030 contains an 8x8 white sample.
   Load this into white[][] for use in scale_colors().
 */
void ciff_block_1030()
{
  int key[] = new int[2];
  key[0] = 0x410;
  key[1] = 0x45f3;
  int i, bpp, row, col, vbits=0;
  long bitbuf=0;

  get2();
  if ((get4()) != 0x80008 || get4()==0) return;
  bpp = get2();
  if (bpp != 10 && bpp != 12) return;
  for (i=row=0; row < 8; row++)
    for (col=0; col < 8; col++) {
      if (vbits < bpp) {
	bitbuf = bitbuf << 16 | (get2() ^ key[i++ & 1]);
	vbits += 16;
      }
      white[row][col] = ( char)( bitbuf << (LONG_BIT - vbits) >> (LONG_BIT - bpp));
      vbits -= bpp;
    }
}

/*
   Parse a CIFF file, better known as Canon CRW format.
 */
void parse_ciff (int offset, int length)
{
  int tboff, nrecs, c, type, len, save, wbi=-1;
  int key[] = new int[2];
  key[0] = 0x410;
  key[1] = 0x45f3;

  CTOJ.fseek (ifp, offset+length-4, CTOJ.SEEK_SET);
  tboff = get4() + offset;
  CTOJ.fseek (ifp, tboff, CTOJ.SEEK_SET);
  nrecs = get2();
  if (nrecs > 100) return;
  while (nrecs-- != 0) {
    type = get2();
    len  = get4();
    save = CTOJ.ftell(ifp) + 4;
    CTOJ.fseek (ifp, offset+get4(), CTOJ.SEEK_SET);
    if ((((type >> 8) + 8) | 8) == 0x38)
      parse_ciff (CTOJ.ftell(ifp), len);	/* Parse a sub-table */

    if (type == 0x0810)
      Uc.fread (artist, 64, 1, ifp);
    if (type == 0x080a) {
      Uc.fread (make, 64, 1, ifp);
      CTOJ.fseek (ifp, Uc.strlen(make) - 63, CTOJ.SEEK_CUR);
      Uc.fread (model, 64, 1, ifp);
    }
    if (type == 0x1810) {
      CTOJ.fseek (ifp, 12, CTOJ.SEEK_CUR);
      flip = get4();
    }
    if (type == 0x1835)			/* Get the decoder table */
      tiff_compress = get4();
    if (type == 0x2007) {
      thumb_offset = CTOJ.ftell(ifp);
      thumb_length = len;
    }
    if (type == 0x1818) {
        get4();
      shutter = (float)Math.pow (2, -int_to_float((get4())));
      aperture = (float)Math.pow (2, int_to_float(get4())/2);
    }
    if (type == 0x102a) {
        get4();
      iso_speed = (float)Math.pow (2, (get2())/32.0 - 4) * 50;
      get2();
      aperture  = (float)Math.pow (2, ((short)get2())/64.0);
      shutter   = (float)Math.pow (2,-((short)get2())/32.0);
      get2();
      wbi = (get2());
      if (wbi > 17) wbi = 0;
      CTOJ.fseek (ifp, 32, CTOJ.SEEK_CUR);
      if (shutter > 1e6) shutter = (float)get2()/10.0f;
    }
    if (type == 0x102c) {
      if (get2() > 512) {		/* Pro90, G1 */
	CTOJ.fseek (ifp, 118, CTOJ.SEEK_CUR);
	for ( c=0; c<4;c++) cam_mul[c ^ 2] = get2();
      } else {				/* G2, S30, S40 */
	CTOJ.fseek (ifp, 98, CTOJ.SEEK_CUR);
	for ( c=0; c<4;c++) cam_mul[c ^ (c >> 1) ^ 1] = get2();
      }
    }
    if (type == 0x0032) {
      if (len == 768) {			/* EOS D30 */
	CTOJ.fseek (ifp, 72, CTOJ.SEEK_CUR);
	for ( c=0; c<4;c++) cam_mul[c ^ (c >> 1)] = 1024.0f / get2();
	if ( wbi==0 ) cam_mul[0] = -1;	/* use my auto white balance */
      } else if ( cam_mul[0] == 0.0) {
	if (get2() == key[0]) {		/* Pro1, G6, S60, S70 */
            CharPtr chaine = null;
            if (Uc.strstr(model,"Pro1") != null)
	      chaine = new CharPtr("012346000000000000");
            else
                chaine = new CharPtr("01345:000000006008");
            c = chaine.charAt(wbi)-'0'+ 2;
        }
	else {				/* G3, G5, S45, S50 */
            CharPtr chaine = new CharPtr("023457000000006000");
	  c = chaine.charAt(wbi)-'0';
	  key[0] = key[1] = 0;
	}
	CTOJ.fseek (ifp, 78 + c*8, CTOJ.SEEK_CUR);
	for ( c=0; c<4;c++) cam_mul[c ^ (c >> 1) ^ 1] = get2() ^ key[c & 1];
	if (wbi == 0) cam_mul[0] = -1;
      }
    }
    if (type == 0x10a9) {		/* D60, 10D, 300D, and clones */
      if (len > 66) {
          CharPtr chaine = new CharPtr("0134567028");
          wbi = chaine.charAt(wbi)-'0';
      }
      CTOJ.fseek (ifp, 2 + wbi*8, CTOJ.SEEK_CUR);
      for ( c=0; c<4;c++) cam_mul[c ^ (c >> 1)] = get2();
    }
    if (type == 0x1030 && (0x18040 >> wbi & 1) != 0)
      ciff_block_1030();		/* all that don't have 0x10a9 */
    if (type == 0x1031) {
        get2();
      raw_width = (get2());
      raw_height = get2();
    }
    if (type == 0x5029) {
      focal_len = len >> 16;
      if ((len & 0xffff) == 2) focal_len /= 32;
    }
    if (type == 0x5813) flash_used = int_to_float(len);
    if (type == 0x5814) canon_ev   = int_to_float(len);
    if (type == 0x5817) shot_order = len;
    if (type == 0x5834) unique_id  = len;
    if (type == 0x580e) timestamp  = len;
    if (type == 0x180e) timestamp  = get4();
    /*
#ifdef LOCALTIME
    if ((type | 0x4000) == 0x580e)
      timestamp = mktime (gmtime (&timestamp));
#endif
     */
    CTOJ.fseek (ifp, save, CTOJ.SEEK_SET);
  }
}

void  parse_fuji (int offset)
{
  int entries, tag, len, save, c;

  CTOJ.fseek (ifp, offset, CTOJ.SEEK_SET);
  entries = get4();
  if (entries > 255) return;
  while (entries-- != 0) {
    tag = get2();
    len = get2();
    save = CTOJ.ftell(ifp);
    if (tag == 0x100) {
      raw_height = get2();
      raw_width  = get2();
    } else if (tag == 0x121) {
      height = get2();
      if ((width = get2()) == 4284) width += 3;
    } else if (tag == 0x130)
      fuji_layout = CTOJ.fgetc(ifp) >> 7;
    if (tag == 0x2ff0)
      for (c=0; c < 4; c++) cam_mul[c ^ 1] = get2();
    CTOJ.fseek (ifp, save+len, CTOJ.SEEK_SET);
  }
  height <<= fuji_layout;
  width  >>= fuji_layout;
}

int  parse_jpeg (int offset)
{
  int len, save, hlen, mark;

  CTOJ.fseek (ifp, offset, CTOJ.SEEK_SET);
  if (CTOJ.fgetc(ifp) != 0xff || CTOJ.fgetc(ifp) != 0xd8) return 0;

  while ( CTOJ.fgetc(ifp) == 0xff && (mark = CTOJ.fgetc(ifp)) != 0xda) {
    order = 0x4d4d;
    len   = get2() - 2;
    save  = CTOJ.ftell(ifp);
    if (mark == 0xc0 || mark == 0xc3) {
      CTOJ.fgetc(ifp);
      raw_height = get2();
      raw_width  = get2();
    }
    order = (short)get2();
    hlen  = get4();
    if (get4() == 0x48454150)		/* "HEAP" */
      ;//parse_ciff (save+hlen, len-hlen);
    parse_tiff (save+6);
    CTOJ.fseek (ifp, save+len, CTOJ.SEEK_SET);
  }
  return 1;
}

/*
   Thanks to Adobe for providing these excellent CAM -> XYZ matrices!
 */
void adobe_coeff( CharPtr make, CharPtr model) {
    
    adobe_coeff( make.toString(), model.toString());
}
/*
void adobe_coeff( String make, String model) {
    char[] make1 = new char[make.length()+1];
    make1[make.length()] = 0;
    make.getChars(0, make.length(), make1, 0);
    
    char[] model1 = new char[model.length()+1];
    model1[model.length()] = 0;
    model.getChars(0, model.length(), model1, 0);
    
    adobe_coeff( make1, model1);
}
*/
  static AdobeCoeffTable table = new AdobeCoeffTable();
    static {
    table.addElt( "Apple QuickTake", 0, 0,		/* DJC */
	 17576,-3191,-3318,5210,6733,-1942,9031,1280,-124 );
    table.addElt( "Canon EOS D2000", 0, 0,
	 24542,-10860,-3401,-1490,11370,-297,2858,-605,3225 );
    table.addElt( "Canon EOS D6000", 0, 0,
	 20482,-7172,-3125,-1033,10410,-285,2542,226,3136 );
    table.addElt( "Canon EOS D30", 0, 0,
	 9805,-2689,-1312,-5803,13064,3068,-2438,3075,8775 );
    table.addElt( "Canon EOS D60", 0, 0xfa0,
	 6188,-1341,-890,-7168,14489,2937,-2640,3228,8483 );
    table.addElt( "Canon EOS 5D", 0, 0xe6c,
	 6347,-479,-972,-8297,15954,2480,-1968,2131,7649 );
    table.addElt( "Canon EOS 10D", 0, 0xfa0,
	 8197,-2000,-1118,-6714,14335,2592,-2536,3178,8266 );
    table.addElt( "Canon EOS 20Da", 0, 0,
	 14155,-5065,-1382,-6550,14633,2039,-1623,1824,6561 );
    table.addElt( "Canon EOS 20D", 0, 0xfff,
	 6599,-537,-891,-8071,15783,2424,-1983,2234,7462 );
    table.addElt( "Canon EOS 30D", 0, 0,
	 6257,-303,-1000,-7880,15621,2396,-1714,1904,7046 );
    table.addElt( "Canon EOS 40D", 0,  0x3f60,
	 6071,-747,-856,-7653,15365,2441,-2025,2553,7315 );
    table.addElt( "Canon EOS 300D", 0, 0xfa0,
	 8197,-2000,-1118,-6714,14335,2592,-2536,3178,8266 );
    table.addElt( "Canon EOS 350D", 0, 0xfff,
	 6018,-617,-965,-8645,15881,2975,-1530,1719,7642 );
    table.addElt( "Canon EOS 400D", 0,  0xe8e,
	 7054,-1501,-990,-8156,15544,2812,-1278,1414,7796 );
    table.addElt( "Canon EOS 450D", 0, 0x390d,
	 5784,-262,-821,-7539,15064,2672,-1982,2681,7427 );
    table.addElt( "Canon EOS 1000D", 0, 0xe43,
	 7054,-1501,-990,-8156,15544,2812,-1278,1414,7796 );
    table.addElt( "Canon EOS-1Ds Mark III", 0, 0x3bb0,
	 5859,-211,-930,-8255,16017,2353,-1732,1887,7448 );
    table.addElt( "Canon EOS-1Ds Mark II", 0, 0xe80,
	 6517,-602,-867,-8180,15926,2378,-1618,1771,7633 );
    table.addElt( "Canon EOS-1D Mark II N", 0,0xe80,
	 6240,-466,-822,-8180,15825,2500,-1801,1938,8042 );
    table.addElt( "Canon EOS-1D Mark III", 0, 0x3bb0,
	 6291,-540,-976,-8350,16145,2311,-1714,1858,7326 );
    table.addElt( "Canon EOS-1D Mark II", 0, 0xe80,
	 6264,-582,-724,-8312,15948,2504,-1744,1919,8664 );
    table.addElt( "Canon EOS-1DS", 0, 0xe20,
	 4374,3631,-1743,-7520,15212,2472,-2892,3632,8161 );
    table.addElt( "Canon EOS-1D", 0, 0xe20,
	 6806,-179,-1020,-8097,16415,1687,-3267,4236,7690 );
    table.addElt( "Canon EOS", 0, 0,
	 8197,-2000,-1118,-6714,14335,2592,-2536,3178,8266 );
    table.addElt( "Canon PowerShot A50", 0,
	 -5300,9846,1776,3436,684,3939,-5540,9879,6200,-1404,11175,217 );
    table.addElt( "Canon PowerShot A5", 0,
	 -4801,9475,1952,2926,1611,4094,-5259,10164,5947,-1554,10883,547 );
    table.addElt( "Canon PowerShot G1", 0,
	 -4778,9467,2172,4743,-1141,4344,-5146,9908,6077,-1566,11051,557 );
    table.addElt( "Canon PowerShot G2", 0, 0,
	 9087,-2693,-1049,-6715,14382,2537,-2291,2819,7790 );
    table.addElt( "Canon PowerShot G3", 0, 0,
	 9212,-2781,-1073,-6573,14189,2605,-2300,2844,7664 );
    table.addElt( "Canon PowerShot G5", 0, 0,
	 9757,-2872,-933,-5972,13861,2301,-1622,2328,7212 );
    table.addElt( "Canon PowerShot G6", 0, 0,
	 9877,-3775,-871,-7613,14807,3072,-1448,1305,7485 );
    table.addElt( "Canon PowerShot G9", 0, 0,
	 7368,-2141,-598,-5621,13254,2625,-1418,1696,5743 );
    table.addElt( "Canon PowerShot Pro1", 0, 0,
	 10062,-3522,-999,-7643,15117,2730,-765,817,7323 );
    table.addElt( "Canon PowerShot Pro70", 34,
	 -4155,9818,1529,3939,-25,4522,-5521,9870,6610,-2238,10873,1342 );
    table.addElt( "Canon PowerShot Pro90", 0,
	 -4963,9896,2235,4642,-987,4294,-5162,10011,5859,-1770,11230,577 );
    table.addElt( "Canon PowerShot S30", 0, 0,
	 10566,-3652,-1129,-6552,14662,2006,-2197,2581,7670 );
    table.addElt( "Canon PowerShot S40", 0, 0,
	 8510,-2487,-940,-6869,14231,2900,-2318,2829,9013 );
    table.addElt( "Canon PowerShot S45", 0, 0,
	 8163,-2333,-955,-6682,14174,2751,-2077,2597,8041 );
    table.addElt( "Canon PowerShot S50", 0, 0,
	 8882,-2571,-863,-6348,14234,2288,-1516,2172,6569 );
    table.addElt( "Canon PowerShot S60", 0, 0,
	 8795,-2482,-797,-7804,15403,2573,-1422,1996,7082 );
    table.addElt( "Canon PowerShot S70", 0, 0,
	 9976,-3810,-832,-7115,14463,2906,-901,989,7889 );
    table.addElt( "Canon PowerShot A610", 0, 0,	/* DJC */
	 15591,-6402,-1592,-5365,13198,2168,-1300,1824,5075 );
    table.addElt( "Canon PowerShot A620", 0, 0,	/* DJC */
	 15265,-6193,-1558,-4125,12116,2010,-888,1639,5220 );
    table.addElt( "Canon PowerShot A630", 0, 0,	/* DJC */
	 14201,-5308,-1757,-6087,14472,1617,-2191,3105,5348 );
    table.addElt( "Canon PowerShot A640", 0, 0,	/* DJC */
	 13124,-5329,-1390,-3602,11658,1944,-1612,2863,4885 );
    table.addElt( "Canon PowerShot A650", 0, 0,	/* DJC */
	 9427,-3036,-959,-2581,10671,1911,-1039,1982,4430 );
    table.addElt( "Canon PowerShot A720", 0, 0,	/* Yves Boyadjian copié sur A 640 */
	 13124,-5329,-1390,-3602,11658,1944,-1612,2863,4885 );
//    table.addElt( "Canon PowerShot A720", 0, 0,	/* DJC */
//	 14573,-5482,-1546,-1266,9799,1468,-1040,1912,3810 );
//    table.addElt( "Canon PowerShot A720", 0, 	/* Yves Boyadjian copié sur internet */
//	 (int)(0.640019*1e4),(int)(-0.220031*1e4),(int)(-0.096241*1e4),(int)(-0.077419*1e4),(int)(0.639766*1e4),(int)(0.044009*1e4),(int)(0.017965*1e4),(int)(0.078396*1e4),(int)(0.231868*1e4) );
//    table.addElt( "Canon PowerShot A720", 0, 	/* Yves Boyadjian copié sur internet */
//	 (int)(1.307006*1e4),(int)(-0.490421*1e4),(int)(-0.194771*1e4),(int)(-0.271197*1e4),(int)(1.444538*1e4),(int)(0.037449*1e4),(int)(-0.119053*1e4),(int)(0.286961*1e4),(int)(0.455555*1e4) );
    table.addElt( "Canon PowerShot S3 IS", 0,  0,	/* DJC */
	 14062,-5199,-1446,-4712,12470,2243,-1286,2028,4836 );
    table.addElt( "CINE 650", 0,  0,
	 3390,480,-500,-800,3610,340,-550,2336,1192 );
    table.addElt( "CINE 660", 0,  0,
	 3390,480,-500,-800,3610,340,-550,2336,1192 );
    table.addElt( "CINE", 0,  0,
	 20183,-4295,-423,-3940,15330,3985,-280,4870,9800 );
    table.addElt( "Contax N Digital", 0, 0xf1e,
	 7777,1285,-1053,-9280,16543,2916,-3677,5679,7060 );
    table.addElt( "EPSON R-D1", 0, 0,
	 6827,-1878,-732,-8429,16012,2564,-704,592,7145 );
    table.addElt( "FUJIFILM FinePix E550", 0, 0,
	 11044,-3888,-1120,-7248,15168,2208,-1531,2277,8069 );
    table.addElt( "FUJIFILM FinePix E900", 0, 0,
	 9183,-2526,-1078,-7461,15071,2574,-2022,2440,8639 );
    table.addElt( "FUJIFILM FinePix F8", 0, 0,
	 11044,-3888,-1120,-7248,15168,2208,-1531,2277,8069 );
    table.addElt( "FUJIFILM FinePix F7", 0, 0,
	 10004,-3219,-1201,-7036,15047,2107,-1863,2565,7736 );
    table.addElt( "FUJIFILM FinePix S100FS", 514, 0,
	 11521,-4355,-1065,-6524,13767,3058,-1466,1984,6045 );
    table.addElt( "FUJIFILM FinePix S20Pro", 0, 0,
	 10004,-3219,-1201,-7036,15047,2107,-1863,2565,7736 );
    table.addElt( "FUJIFILM FinePix S2Pro", 128, 0,
	 12492,-4690,-1402,-7033,15423,1647,-1507,2111,7697 );
    table.addElt( "FUJIFILM FinePix S3Pro", 0, 0,
	 11807,-4612,-1294,-8927,16968,1988,-2120,2741,8006 );
    table.addElt( "FUJIFILM FinePix S5Pro", 0, 0,
	 12300,-5110,-1304,-9117,17143,1998,-1947,2448,8100 );
    table.addElt( "FUJIFILM FinePix S5000", 0,  0,
	 8754,-2732,-1019,-7204,15069,2276,-1702,2334,6982 );
    table.addElt( "FUJIFILM FinePix S5100", 0, 0x3e00,
	 11940,-4431,-1255,-6766,14428,2542,-993,1165,7421 );
    table.addElt( "FUJIFILM FinePix S5500", 0, 0x3e00,
	 11940,-4431,-1255,-6766,14428,2542,-993,1165,7421 );
    table.addElt( "FUJIFILM FinePix S5200", 0, 0,
	 9636,-2804,-988,-7442,15040,2589,-1803,2311,8621 );
    table.addElt( "FUJIFILM FinePix S5600", 0, 0,
	 9636,-2804,-988,-7442,15040,2589,-1803,2311,8621 );
    table.addElt( "FUJIFILM FinePix S6", 0, 0,
	 12628,-4887,-1401,-6861,14996,1962,-2198,2782,7091 );
    table.addElt( "FUJIFILM FinePix S7000", 0, 0,
	 10190,-3506,-1312,-7153,15051,2238,-2003,2399,7505 );
    table.addElt( "FUJIFILM FinePix S9000", 0, 0,
	 10491,-3423,-1145,-7385,15027,2538,-1809,2275,8692 );
    table.addElt( "FUJIFILM FinePix S9500", 0, 0,
	 10491,-3423,-1145,-7385,15027,2538,-1809,2275,8692 );
    table.addElt( "FUJIFILM FinePix S9100", 0, 0,
	 12343,-4515,-1285,-7165,14899,2435,-1895,2496,8800 );
    table.addElt( "FUJIFILM FinePix S9600", 0, 0,
	 12343,-4515,-1285,-7165,14899,2435,-1895,2496,8800 );
    table.addElt( "FUJIFILM IS-1", 0, 0,
	 21461,-10807,-1441,-2332,10599,1999,289,875,7703 );
    table.addElt( "Imacon Ixpress", 0,	0,/* DJC */
	 7025,-1415,-704,-5188,13765,1424,-1248,2742,6038 );
    table.addElt( "KODAK NC2000", 0, 0,	/* DJC */
	 16475,-6903,-1218,-851,10375,477,2505,-7,1020 );
    table.addElt( "Kodak DCS315C", 8, 0,
	 17523,-4827,-2510,756,8546,-137,6113,1649,2250 );
    table.addElt( "Kodak DCS330C", 8, 0,
	 20620,-7572,-2801,-103,10073,-396,3551,-233,2220 );
    table.addElt( "KODAK DCS420", 0, 0,
	 10868,-1852,-644,-1537,11083,484,2343,628,2216 );
    table.addElt( "KODAK DCS460", 0, 0,
	 10592,-2206,-967,-1944,11685,230,2206,670,1273 );
    table.addElt( "KODAK EOSDCS1", 0, 0,
	 10592,-2206,-967,-1944,11685,230,2206,670,1273 );
    table.addElt( "KODAK EOSDCS3B", 0, 0,
	 9898,-2700,-940,-2478,12219,206,1985,634,1031 );
    table.addElt( "Kodak DCS520C", 180, 0,
	 24542,-10860,-3401,-1490,11370,-297,2858,-605,3225 );
    table.addElt( "Kodak DCS560C", 188, 0,
	 20482,-7172,-3125,-1033,10410,-285,2542,226,3136 );
    table.addElt( "Kodak DCS620C", 180, 0,
	 23617,-10175,-3149,-2054,11749,-272,2586,-489,3453 );
    table.addElt( "Kodak DCS620X", 185, 0,
	 13095,-6231,154,12221,-21,-2137,895,4602,2258 );
    table.addElt( "Kodak DCS660C", 214, 0,
	 18244,-6351,-2739,-791,11193,-521,3711,-129,2802 );
    table.addElt( "Kodak DCS720X", 0, 0,
	 11775,-5884,950,9556,1846,-1286,-1019,6221,2728 );
    table.addElt( "Kodak DCS760C", 0, 0,
	 16623,-6309,-1411,-4344,13923,323,2285,274,2926 );
    table.addElt( "Kodak DCS Pro SLR", 0, 0,
	 5494,2393,-232,-6427,13850,2846,-1876,3997,5445 );
    table.addElt( "Kodak DCS Pro 14nx", 0, 0,
	 5494,2393,-232,-6427,13850,2846,-1876,3997,5445 );
    table.addElt( "Kodak DCS Pro 14", 0, 0,
	 7791,3128,-776,-8588,16458,2039,-2455,4006,6198 );
    table.addElt( "Kodak ProBack645", 0, 0,
	 16414,-6060,-1470,-3555,13037,473,2545,122,4948 );
    table.addElt( "Kodak ProBack", 0, 0,
	 21179,-8316,-2918,-915,11019,-165,3477,-180,4210 );
    table.addElt( "KODAK P712", 0, 0,
	 9658,-3314,-823,-5163,12695,2768,-1342,1843,6044 );
    table.addElt( "KODAK P850", 0, 0xf7c,
	 10511,-3836,-1102,-6946,14587,2558,-1481,1792,6246 );
    table.addElt( "KODAK P880", 0, 0xfff,
	 12805,-4662,-1376,-7480,15267,2360,-1626,2194,7904 );
    table.addElt( "Leaf CMost", 0, 0,
	 3952,2189,449,-6701,14585,2275,-4536,7349,6536 );
    table.addElt( "Leaf Valeo 6", 0, 0,
	 3952,2189,449,-6701,14585,2275,-4536,7349,6536 );
    table.addElt( "Leaf Aptus 54S", 0, 0,
	 8236,1746,-1314,-8251,15953,2428,-3673,5786,5771 );
    table.addElt( "Leaf Aptus 65", 0, 0,
	 7914,1414,-1190,-8777,16582,2280,-2811,4605,5562 );
    table.addElt( "Leaf Aptus 75", 0, 0,
	 7914,1414,-1190,-8777,16582,2280,-2811,4605,5562 );
    table.addElt( "Leaf", 0, 0,
	 8236,1746,-1314,-8251,15953,2428,-3673,5786,5771 );
    table.addElt( "Mamiya ZD", 0, 0,
	 7645,2579,-1363,-8689,16717,2015,-3712,5941,5961 );
    table.addElt( "Micron 2010", 110, 0,	/* DJC */
	 16695,-3761,-2151,155,9682,163,3433,951,4904 );
    table.addElt( "Minolta DiMAGE 5", 0, 0xf7d,
	 8983,-2942,-963,-6556,14476,2237,-2426,2887,8014 );
    table.addElt( "Minolta DiMAGE 7Hi", 0, 0xf7d,
	 11368,-3894,-1242,-6521,14358,2339,-2475,3056,7285 );
    table.addElt( "Minolta DiMAGE 7", 0, 0xf7d,
	 9144,-2777,-998,-6676,14556,2281,-2470,3019,7744 );
    table.addElt( "Minolta DiMAGE A1", 0, 0xf8b,
	 9274,-2547,-1167,-8220,16323,1943,-2273,2720,8340 );
    table.addElt( "MINOLTA DiMAGE A200", 0, 0,
	 8560,-2487,-986,-8112,15535,2771,-1209,1324,7743 );
    table.addElt( "Minolta DiMAGE A2", 0, 0xf8f,
	 9097,-2726,-1053,-8073,15506,2762,-966,981,7763 );
    table.addElt( "Minolta DiMAGE Z2", 0, 0,	/* DJC */
	 11280,-3564,-1370,-4655,12374,2282,-1423,2168,5396 );
    table.addElt( "MINOLTA DYNAX 5", 0, 0xffb,
	 10284,-3283,-1086,-7957,15762,2316,-829,882,6644 );
    table.addElt( "MINOLTA DYNAX 7", 0, 0xffb,
	 10239,-3104,-1099,-8037,15727,2451,-927,925,6871 );
    table.addElt( "NIKON D100", 0, 0,
	 5902,-933,-782,-8983,16719,2354,-1402,1455,6464 );
    table.addElt( "NIKON D1H", 0, 0,
	 7577,-2166,-926,-7454,15592,1934,-2377,2808,8606 );
    table.addElt( "NIKON D1X", 0, 0,
	 7702,-2245,-975,-9114,17242,1875,-2679,3055,8521 );
    table.addElt( "NIKON D1", 0, 0,	/* multiplied by 2.218750, 1.0, 1.148438 */
	 16772,-4726,-2141,-7611,15713,1972,-2846,3494,9521 );
    table.addElt( "NIKON D2H", 0, 0,
	 5710,-901,-615,-8594,16617,2024,-2975,4120,6830 );
    table.addElt( "NIKON D2X", 0, 0,
	 10231,-2769,-1255,-8301,15900,2552,-797,680,7148 );
    table.addElt( "NIKON D40X", 0, 0,
	 8819,-2543,-911,-9025,16928,2151,-1329,1213,8449 );
    table.addElt( "NIKON D40", 0, 0,
	 6992,-1668,-806,-8138,15748,2543,-874,850,7897 );
    table.addElt( "NIKON D50", 0, 0,
	 7732,-2422,-789,-8238,15884,2498,-859,783,7330 );
    table.addElt( "NIKON D60", 0, 0,
	 8736,-2458,-935,-9075,16894,2251,-1354,1242,8263 );
    table.addElt( "NIKON D700", 0, 0,
	 8139,-2171,-663,-8747,16541,2295,-1925,2008,8093 );
    table.addElt( "NIKON D70", 0, 0,
	 7732,-2422,-789,-8238,15884,2498,-859,783,7330 );
    table.addElt( "NIKON D80", 0, 0,
	 8629,-2410,-883,-9055,16940,2171,-1490,1363,8520 );
    table.addElt( "NIKON D90", 0, 0xf00,		/* DJC */
	 9692,-2519,-831,-5396,13053,2344,-1818,2682,7084 );
    table.addElt( "NIKON D200", 0, 0xfbc,
	 8367,-2248,-763,-8758,16447,2422,-1527,1550,8053 );
    table.addElt( "NIKON D300", 0, 0,
	 9030,-1992,-715,-8465,16302,2255,-2689,3217,8069 );
    table.addElt( "NIKON D3", 0, 0,
	 8139,-2171,-663,-8747,16541,2295,-1925,2008,8093 );
    table.addElt( "NIKON E950", 0,		/* DJC */
	 -3746,10611,1665,9621,-1734,2114,-2389,7082,3064,3406,6116,-244 );
    table.addElt( "NIKON E995", 0,	/* copied from E5000 */
	 -5547,11762,2189,5814,-558,3342,-4924,9840,5949,688,9083,96 );
    table.addElt( "NIKON E2100", 0, 0,	/* copied from Z2, new white balance */
	 13142,-4152,-1596,-4655,12374,2282,-1769,2696,6711 );
    table.addElt( "NIKON E2500", 0,
	 -5547,11762,2189,5814,-558,3342,-4924,9840,5949,688,9083,96 );
    table.addElt( "NIKON E4300", 0, 0,/* copied from Minolta DiMAGE Z2 */
	 11280,-3564,-1370,-4655,12374,2282,-1423,2168,5396 );
    table.addElt( "NIKON E4500", 0,
	 -5547,11762,2189,5814,-558,3342,-4924,9840,5949,688,9083,96 );
    table.addElt( "NIKON E5000", 0,
	 -5547,11762,2189,5814,-558,3342,-4924,9840,5949,688,9083,96 );
    table.addElt( "NIKON E5400", 0, 0,
	 9349,-2987,-1001,-7919,15766,2266,-2098,2680,6839 );
    table.addElt( "NIKON E5700", 0,
	 -5368,11478,2368,5537,-113,3148,-4969,10021,5782,778,9028,211 );
    table.addElt( "NIKON E8400", 0, 0,
	 7842,-2320,-992,-8154,15718,2599,-1098,1342,7560 );
    table.addElt( "NIKON E8700", 0, 0,
	 8489,-2583,-1036,-8051,15583,2643,-1307,1407,7354 );
    table.addElt( "NIKON E8800", 0, 0,
	 7971,-2314,-913,-8451,15762,2894,-1442,1520,7610 );
    table.addElt( "OLYMPUS C5050", 0, 0,
	 10508,-3124,-1273,-6079,14294,1901,-1653,2306,6237 );
    table.addElt( "OLYMPUS C5060", 0, 0,
	 10445,-3362,-1307,-7662,15690,2058,-1135,1176,7602 );
    table.addElt( "OLYMPUS C7070", 0, 0,
	 10252,-3531,-1095,-7114,14850,2436,-1451,1723,6365 );
    table.addElt( "OLYMPUS C70", 0, 0,
	 10793,-3791,-1146,-7498,15177,2488,-1390,1577,7321 );
    table.addElt( "OLYMPUS C80", 0, 0,
	 8606,-2509,-1014,-8238,15714,2703,-942,979,7760 );
    table.addElt( "OLYMPUS E-10", 0, 0xffc0,
	 12745,-4500,-1416,-6062,14542,1580,-1934,2256,6603 );
    table.addElt( "OLYMPUS E-1", 0, 0xfff0,
	 11846,-4767,-945,-7027,15878,1089,-2699,4122,8311 );
    table.addElt( "OLYMPUS E-20", 0, 0xffc0,
	 13173,-4732,-1499,-5807,14036,1895,-2045,2452,7142 );
    table.addElt( "OLYMPUS E-300", 0, 0,
	 7828,-1761,-348,-5788,14071,1830,-2853,4518,6557 );
    table.addElt( "OLYMPUS E-330", 0, 0,
	 8961,-2473,-1084,-7979,15990,2067,-2319,3035,8249 );
     table.addElt( "OLYMPUS E-3", 0, 0xf99,
	 9487,-2875,-1115,-7533,15606,2010,-1618,2100,7389 );
     table.addElt( "OLYMPUS E-400", 0, 0xfff0,
	 6169,-1483,-21,-7107,14761,2536,-2904,3580,8568 );
     table.addElt( "OLYMPUS E-410", 0, 0xf6a,
	 8856,-2582,-1026,-7761,15766,2082,-2009,2575,7469 );
     table.addElt( "OLYMPUS E-420", 0, 0xfd7, /* copied from above */
	 8856,-2582,-1026,-7761,15766,2082,-2009,2575,7469 );
    table.addElt( "OLYMPUS E-500", 0, 0,
	 8136,-1968,-299,-5481,13742,1871,-2556,4205,6630 );
    table.addElt( "OLYMPUS E-510", 0, 0xf6a,
	 8785,-2529,-1033,-7639,15624,2112,-1783,2300,7817 );
    table.addElt( "OLYMPUS E-520", 0, 0xfd2,
	 8344,-2322,-1020,-7596,15635,2048,-1748,2269,7287 );
    table.addElt( "OLYMPUS SP350", 0, 0,
	 12078,-4836,-1069,-6671,14306,2578,-786,939,7418 );
    table.addElt( "OLYMPUS SP3", 0, 0,
	 11766,-4445,-1067,-6901,14421,2707,-1029,1217,7572 );
    table.addElt( "OLYMPUS SP500UZ", 0, 0xfff,
	 9493,-3415,-666,-5211,12334,3260,-1548,2262,6482 );
    table.addElt( "OLYMPUS SP510UZ", 0, 0xffe,
	 10593,-3607,-1010,-5881,13127,3084,-1200,1805,6721 );
    table.addElt( "OLYMPUS SP550UZ", 0, 0xffe,
	 11597,-4006,-1049,-5432,12799,2957,-1029,1750,6516 );
    table.addElt( "OLYMPUS SP560UZ", 0, 0xff9,
	 10915,-3677,-982,-5587,12986,2911,-1168,1968,6223 );
    table.addElt( "OLYMPUS SP570UZ", 0, 0,
	 11522,-4044,-1146,-4736,12172,2904,-988,1829,6039 );
    table.addElt( "PENTAX *ist DL2", 0, 0,
	 10504,-2438,-1189,-8603,16207,2531,-1022,863,12242 );
    table.addElt( "PENTAX *ist DL", 0, 0,
	 10829,-2838,-1115,-8339,15817,2696,-837,680,11939 );
    table.addElt( "PENTAX *ist DS2", 0, 0,
	 10504,-2438,-1189,-8603,16207,2531,-1022,863,12242 );
    table.addElt( "PENTAX *ist DS", 0, 0,
	 10371,-2333,-1206,-8688,16231,2602,-1230,1116,11282 );
    table.addElt( "PENTAX *ist D", 0, 0,
	 9651,-2059,-1189,-8881,16512,2487,-1460,1345,10687 );
    table.addElt(  "PENTAX K10D", 0, 0,
	 9566,-2863,-803,-7170,15172,2112,-818,803,9705 );
    table.addElt(  "PENTAX K1", 0, 0,
	 11095,-3157,-1324,-8377,15834,2720,-1108,947,11688 );
    table.addElt(  "PENTAX K20D", 0, 0,
	 9427,-2714,-868,-7493,16092,1373,-2199,3264,7180 );
    table.addElt(  "PENTAX K200D", 0, 0,
	 9186,-2678,-907,-8693,16517,2260,-1129,1094,8524 );
    table.addElt(  "Panasonic DMC-FZ8", 0, 0xf7f0,
	 8986,-2755,-802,-6341,13575,3077,-1476,2144,6379 );
    table.addElt(  "Panasonic DMC-FZ18", 0, 0,
	 9932,-3060,-935,-5809,13331,2753,-1267,2155,5575 );
    table.addElt( "Panasonic DMC-FZ30", 0, 0xf94c,
	 10976,-4029,-1141,-7918,15491,2600,-1670,2071,8246 );
    table.addElt( "Panasonic DMC-FZ50", 0, 0xfff0,
	 7906,-2709,-594,-6231,13351,3220,-1922,2631,6537 );
    table.addElt( "Panasonic DMC-L10", 15, 0xf96,
	 8025,-1942,-1050,-7920,15904,2100,-2456,3005,7039 );
    table.addElt( "Panasonic DMC-L1", 0, 0xf7fc,	/* aka "LEICA DIGILUX 3" */
	 8054,-1885,-1025,-8349,16367,2040,-2805,3542,7629 );
    table.addElt( "Panasonic DMC-LC1", 0, 0,
	 11340,-4069,-1275,-7555,15266,2448,-2960,3426,7685 );
    table.addElt( "Panasonic DMC-LX1", 0, 0xf7f0,
	 10704,-4187,-1230,-8314,15952,2501,-920,945,8927 );
    table.addElt( "Panasonic DMC-LX2", 0, 0,	/* aka "LEICA D-LUX3" */
	 8048,-2810,-623,-6450,13519,3272,-1700,2146,7049 );
    table.addElt( "Phase One H 20", 0, 0,		/* DJC */
	 1313,1855,-109,-6715,15908,808,-327,1840,6020 );
    table.addElt( "Phase One P 2", 0, 0,
	 2905,732,-237,-8134,16626,1476,-3038,4253,7517 );
    table.addElt( "Phase One P 30", 0, 0,
	 4516,-245,-37,-7020,14976,2173,-3206,4671,7087 );
    table.addElt( "Phase One P 45", 0, 0,
	 5053,-24,-117,-5684,14076,1702,-2619,4492,5849 );
    table.addElt( "SAMSUNG GX-1", 0, 0,
	 10504,-2438,-1189,-8603,16207,2531,-1022,863,12242 );
    table.addElt( "Sinar", 0, 0,		/* DJC */
	 16442,-2956,-2422,-2877,12128,750,-1136,6066,4559 );
    table.addElt( "SONY DSC-F828", 491,
	 7924,-1910,-777,-8226,15459,2998,-1517,2199,6818,-7242,11401,3481 );
    table.addElt( "SONY DSC-R1", 512, 0,
	 8512,-2641,-694,-8042,15670,2526,-1821,2117,7414 );
    table.addElt( "SONY DSC-V3", 0, 0,
	 7511,-2571,-692,-7894,15088,3060,-948,1111,8128 );
    table.addElt( "SONY DSLR-A100", 0, 0xfeb,
	 9437,-2811,-774,-8405,16215,2290,-710,596,7181 );
    table.addElt( "SONY DSLR-A200", 0, 0,
	 9847,-3091,-928,-8485,16345,2225,-715,595,7103 );
    table.addElt( "SONY DSLR-A300", 0, 0,
	 9847,-3091,-928,-8485,16345,2225,-715,595,7103 );
    table.addElt( "SONY DSLR-A350", 0, 0xffc,
	 6038,-1484,-578,-9146,16746,2513,-875,746,7217 );
    table.addElt( "SONY DSLR-A700", 254, 0x1ffe,
	 5775,-805,-359,-8574,16295,2391,-1943,2341,7249 );
    table.addElt( "SONY DSLR-A900", 254, 0x1ffe,	/* DJC */
	 6971,-1730,-794,-5763,13529,2236,-1500,2251,6715 );
    }
void  adobe_coeff (String make, String model)
{
    /*
  static const struct {
    const char *prefix;
    short black, trans[12];
  } 
     */

  double[][] cam_xyz = new double[4][3];
  String name;
  int i, j;

  name = make + " "+model;
  //sprintf (name, "%s %s", make, model);
  for (i=0; i < table.size(); i++)
    if ( Uc.strncmp( name, table.get(i).prefix, Uc.strlen(table.get(i).prefix) ) == 0) {
      if (table.get(i).black != 0)
	black = table.get(i).black;
      if (table.get(i).maximum != 0) 
          maximum = table.get(i).maximum;
      for (j=0; j < 12; j++)
	cam_xyz[/*0*/j/3][j%3] = table.get(i).trans[j] / 10000.0;
      cam_xyz_coeff (cam_xyz);
      break;
    }
}

/*
   Identify which camera created this file, and set global variables
   accordingly.
 */
void  identify()
{
  BytePtr head = new BytePtr(32);
  BytePtr cp = new BytePtr();
  int hlen, fsize, i, c;
  boolean is_canon;
  jhead jh = new jhead();
  /*
  static const struct {
    int fsize;
    char make[12], model[15], withjpeg;
  } 
   */
  IdentifyTable table = new IdentifyTable();
    table.addElt(    62464, "Kodak",    "DC20"       ,0 );
    table.addElt(   124928, "Kodak",    "DC20"       ,0 );
    table.addElt(  1652736, "Kodak",    "DCS200"     ,0 );
    table.addElt(  4159302, "Kodak",    "C330"       ,0 );
    table.addElt(  4162462, "Kodak",    "C330"       ,0 );
    table.addElt(   460800, "Kodak",    "C603v"           ,0 );
    table.addElt(   614400, "Kodak",    "C603v"           ,0 );
    table.addElt(  6163328, "Kodak",    "C603"            ,0 );
    table.addElt(  6166488, "Kodak",    "C603"            ,0 );
    table.addElt(  9116448, "Kodak",    "C603y"           ,0 );
    table.addElt(   311696, "ST Micro", "STV680 VGA" ,0 );  /* SPYz */
    table.addElt(   614400, "Kodak",    "KAI-0340"   ,0 );
    table.addElt(   787456, "Creative", "PC-CAM 600" ,0 );
    table.addElt(  1138688, "Minolta",  "RD175"      ,0 );
    table.addElt(  3840000, "Foculus",  "531C"       ,0 );
    table.addElt(   786432, "AVT",      "F-080C"          ,0 );
    table.addElt(  1447680, "AVT",      "F-145C"     ,0 );
    table.addElt(  1920000, "AVT",      "F-201C"     ,0 );
    table.addElt(  5067304, "AVT",      "F-510C"     ,0 );
    table.addElt( 10134608, "AVT",      "F-510C"     ,0 );
    table.addElt( 16157136, "AVT",      "F-810C"     ,0 );
    table.addElt(  1409024, "Sony",     "XCD-SX910CR",0 );
    table.addElt(  2818048, "Sony",     "XCD-SX910CR",0 );
    table.addElt(  3884928, "Micron",   "2010"       ,0 );
    table.addElt(  6624000, "Pixelink", "A782"       ,0 );
    table.addElt( 13248000, "Pixelink", "A782"       ,0 );
    table.addElt(  6291456, "RoverShot","3320AF"     ,0 );
    table.addElt(  6553440, "Canon",    "PowerShot A460",0 );
    table.addElt(  6653280, "Canon",    "PowerShot A530",0 );
    table.addElt(  6573120, "Canon",    "PowerShot A610",0 );
    table.addElt(  9219600, "Canon",    "PowerShot A620",0 );
    table.addElt( 10341600, "Canon",    "PowerShot A720",0 );
    table.addElt( 10383120, "Canon",    "PowerShot A630",0 );
    table.addElt( 12945240, "Canon",    "PowerShot A640",0 );
    table.addElt( 15636240, "Canon",    "PowerShot A650",0 );
    table.addElt(  5298000, "Canon",    "PowerShot SD300" ,0 );
    table.addElt(  7710960, "Canon",    "PowerShot S3 IS",0 );    
    table.addElt(  5939200, "OLYMPUS",  "C770UZ"     ,0 );
    table.addElt(  1581060, "NIKON",    "E900"       ,1 );  /* or E900s,E910 */
    table.addElt(  2465792, "NIKON",    "E950"       ,1 );  /* or E800,E700 */
    table.addElt(  2940928, "NIKON",    "E2100"      ,1 );  /* or E2500 */
    table.addElt(  4771840, "NIKON",    "E990"       ,1 );  /* or E995, Oly C3030Z */
    table.addElt(  4775936, "NIKON",    "E3700"      ,1 );  /* or Optio 33WR */
    table.addElt(  5869568, "NIKON",    "E4300"      ,1 );  /* or DiMAGE Z2 */
    table.addElt(  5865472, "NIKON",    "E4500"      ,1 );
    table.addElt(  7438336, "NIKON",    "E5000"      ,1 );  /* or E5700 */
    table.addElt(  8998912, "NIKON",    "COOLPIX S6" ,1 );
    table.addElt(  1976352, "CASIO",    "QV-2000UX"  ,1 );
    table.addElt(  3217760, "CASIO",    "QV-3*00EX"  ,1 );
    table.addElt(  6218368, "CASIO",    "QV-5700"    ,1 );
    table.addElt(  6054400, "CASIO",    "QV-R41"          ,1 );
    table.addElt(  7530816, "CASIO",    "QV-R51"     ,1 );
    table.addElt(  7684000, "CASIO",    "QV-4000"    ,1 );
    table.addElt(  4948608, "CASIO",    "EX-S100"    ,1 );
    table.addElt(  7542528, "CASIO",    "EX-Z50"     ,1 );
    table.addElt(  7753344, "CASIO",    "EX-Z55"     ,1 );
    table.addElt(  7426656, "CASIO",    "EX-P505"    ,1 );
    table.addElt(  9313536, "CASIO",    "EX-P600"    ,1 );
    table.addElt( 10979200, "CASIO",    "EX-P700"    ,1 );
    table.addElt(  3178560, "PENTAX",   "Optio S"    ,1 );
    table.addElt(  4841984, "PENTAX",   "Optio S"    ,1 );
    table.addElt(  6114240, "PENTAX",   "Optio S4"   ,1 );  /* or S4i */
    table.addElt( 10702848, "PENTAX",   "Optio 750Z" ,1 );
    table.addElt( 12582980, "Sinar",    ""           ,0 );
    table.addElt( 33292868, "Sinar",    ""           ,0 );
    table.addElt( 44390468, "Sinar",    ""           ,0 );
    
  String[] corp =
    { "Canon", "NIKON", "EPSON", "KODAK", "Kodak", "OLYMPUS", "PENTAX",
      "MINOLTA", "Minolta", "Konica", "CASIO", "Sinar", "Phase One",
      "SAMSUNG", "Mamiya" };

  tiff_flip = flip = -1;	/* 0 is valid, so -1 is unknown */
  filters = CTOJ.toUnsigned(-1);
  cr2_slice.at(0,(short)0);
  raw_height = raw_width = fuji_width = 0;
  maximum = height = width = top_margin = left_margin = 0;
  cdesc.setAt(0,(char) 0);
  desc.setAt(0,(char) 0);
  artist.setAt(0,(char) 0);
  make.setAt(0,(char)0);
  model.setAt(0,(char)0);
  model2.setAt(0,(char)0);
  iso_speed = shutter = aperture = focal_len = unique_id = 0;
  //Uc.memset (white, 0, white.sizeof());
  thumb_offset = thumb_length = thumb_width = thumb_height = 0;
  load_raw = null;
  thumb_load_raw = null;
  //write_thumb = & jpeg_thumb;
  data_offset = meta_length = tiff_bps = tiff_compress = 0;
  kodak_cbpp = zero_after_ff = dng_version = fuji_secondary = 0;
  timestamp = shot_order = tiff_samples = black = is_foveon = 0;
  data_error = false;
  zero_is_bad = false;
  pixel_aspect = is_raw = raw_color = use_gamma = 1;
  tile_length = Integer.MAX_VALUE;
  for (i=0; i < 4; i++) {
    cam_mul[i] = (i == 1 ? 1 : 0);
    pre_mul[i] = (i < 3 ?1:0);
    for (c=0; c < 3; c++) rgb_cam[c][i] = (c == i? 1: 0);
  }
  colors = 3;
  tiff_bps = 12;
  for (i=0; i < 0x4000; i++) curve.at(i, (short)i);
  profile_length = 0;

  order = (short)get2();
  hlen = get4();
  CTOJ.fseek (ifp, 0, CTOJ.SEEK_SET);
  CTOJ.fread (head, 1, 32, ifp);
  CTOJ.fseek (ifp, 0, CTOJ.SEEK_END);
  fsize = CTOJ.ftell(ifp);
  if (( !cp.assign(CTOJ.memmem (head, 32, "MMMM", 4)).isNull()) ||
      ( !cp.assign(CTOJ.memmem (head, 32, "IIII", 4)).isNull())) {
    //parse_phase_one (cp.minus(head));
    if (cp.minus(head)!=0) parse_tiff(0);
  } else if (order == 0x4949 || order == 0x4d4d) {
    if (CTOJ.memcmp (head.plus(6),"HEAPCCDR",8) == 0) {
      data_offset = hlen;
      parse_ciff (hlen, fsize - hlen);
    } else {
      parse_tiff(0);
    }
  } else if ( CTOJ.memcmp (head,"\u00ff\u00d8\u00ff\u00e1",4) == 0 &&
	      CTOJ.memcmp (head.plus(6),"Exif",4) == 0) {
    CTOJ.fseek (ifp, 4, CTOJ.SEEK_SET);
    data_offset = 4 + get2();
    CTOJ.fseek (ifp, data_offset, CTOJ.SEEK_SET);
    if (CTOJ.fgetc(ifp) != 0xff)
      parse_tiff(12);
    thumb_offset = 0;
  } else if ( CTOJ.memcmp (head,"BM",2)==0 &&
	head.at(26) == 1 && head.at(28) == 16 && head.at(30) == 0) {
    data_offset = 0x1000;
    order = 0x4949;
    CTOJ.fseek (ifp, 38, CTOJ.SEEK_SET);
    if (get4() == 2834 && get4() == 2834 && get4() == 0 && get4() == 4096) {
      Uc.strcpy (model, "BMQ");
      flip = 3;
//      goto nucore;
    Uc.strcpy (make, "Nucore");
    order = 0x4949;
    CTOJ.fseek (ifp, 10, CTOJ.SEEK_SET);
    data_offset += get4();
    get4();
    raw_width = get4();
    raw_height = get4();
    if (model.charAt(0) == 'B' && raw_width == 2597) {
      raw_width++;
      data_offset -= 0x1000;
    }
    }
  } else if ( CTOJ.memcmp (head,"BR",2) == 0) {
    Uc.strcpy (model, "RAW");
nucore:
    Uc.strcpy (make, "Nucore");
    order = 0x4949;
    CTOJ.fseek (ifp, 10, CTOJ.SEEK_SET);
    data_offset += get4();
    get4();
    raw_width = get4();
    raw_height = get4();
    if (model.charAt(0) == 'B' && raw_width == 2597) {
      raw_width++;
      data_offset -= 0x1000;
    }
  } else if ( CTOJ.memcmp (head.plus(25),"ARECOYK",7) == 0) {
    Uc.strcpy (make, "Contax");
    Uc.strcpy (model,"N Digital");
    CTOJ.fseek (ifp, 33, CTOJ.SEEK_SET);
    get_timestamp(1);
    CTOJ.fseek (ifp, 60, CTOJ.SEEK_SET);
    for (c=0; c < 4; c++) cam_mul[c ^ (c >> 1)] = get4();
  } else if ( CTOJ.strcmp (head, "PXN") == 0) {
    Uc.strcpy (make, "Logitech");
    Uc.strcpy (model,"Fotoman Pixtura");
  } else if ( CTOJ.memcmp (head,"FUJIFILM",8) == 0) {
    CTOJ.fseek (ifp, 84, CTOJ.SEEK_SET);
    thumb_offset = get4();
    thumb_length = get4();
    CTOJ.fseek (ifp, 92, CTOJ.SEEK_SET);
    parse_fuji (get4());
    if (thumb_offset > 120) {
      CTOJ.fseek (ifp, 120, CTOJ.SEEK_SET);
      fuji_secondary = ((i = get4()) !=0 && true) ? 1:0;
      if (fuji_secondary != 0 && use_secondary != 0)
	parse_fuji (i);
    }
    CTOJ.fseek (ifp, 100, CTOJ.SEEK_SET);
    data_offset = get4();
    parse_tiff (thumb_offset+12);
  } else if ( CTOJ.memcmp (head,"RIFF",4)==0) {
    CTOJ.fseek (ifp, 0, CTOJ.SEEK_SET);
    //parse_riff();
  } else if (CTOJ.memcmp (head,"DSC-Image",9)==0)
    ;//parse_rollei();
  else if (CTOJ.memcmp (head,"\0MRM",4)==0)
    parse_minolta(0);
  else if (CTOJ.memcmp (head,"FOVb",4)==0)
    ;//parse_foveon();
  else
    for (i=0; i < table.size(); i++)
      if (fsize == table.at(i).fsize) {
	Uc.strcpy (make,  table.at(i).make );
	Uc.strcpy (model, table.at(i).model);
	if (table.at(i).withjpeg)
	  ;//parse_external_jpeg();
      }
  if (make.charAt(0) == 0) ;//parse_smal (0, fsize);
  if (make.charAt(0) == 0) parse_jpeg (is_raw = 0);

  for (i=0; i < corp.length; i++)
    if (Uc.strstr (make, corp[i]) != null)		/* Simplify company names */
	Uc.strcpy (make, corp[i]);
  if ( Uc.strncmp (make,"KODAK",5) == 0) {
    make.setAt(16,(char)0); model.setAt(16, (char)0);
  }
  CharPtr cp2;
  cp2 = make.plus(Uc.strlen(make));		/* Remove trailing spaces */
  while ( cp2.moinsmoins().etoile() == ' ') cp2.etoile((char)0);
  //while (*--cp == ' ') *cp = 0;
  cp2 = model.plus(Uc.strlen(model));
  while ( cp2.moinsmoins().etoile() == ' ') cp2.etoile((char)0);
  //while (*--cp == ' ') *cp = 0;
  i = Uc.strlen(make);			/* Remove make from model */
  if ( Uc.strncmp (model, make, i)==0 && model.charAt(i++) == ' ')
    Uc.memmove (model, model.plus(i), 64-i);
  if ( Uc.strncmp (model,"Digital Camera ",15) == 0)
    Uc.strcpy (model, model.plus(15));
  make.setAt(63,(char)0);
  model.setAt(63,(char)0);
  model2.setAt(63,(char)0);
  if ( is_raw == 0) return;

  if ((raw_height | raw_width) < 0)
       raw_height = raw_width  = 0;
  if ( maximum == 0) maximum = (1 << tiff_bps) - 1;
  if ( height ==0) height = raw_height;
  if ( width == 0)  width  = raw_width;
  if (fuji_width != 0) {
    width = height + fuji_width;
    height = width - 1;
    pixel_aspect = 1;
  }
  if (height == 2624 && width == 3936)	/* Pentax K10D and Samsung GX10 */
    { height  = 2616;   width  = 3896; }
  if (height == 3136 && width == 4864)	/* Pentax K20D */
    { height  = 3124;   width  = 4688; }  
  /*
  if (dng_version) {
    strcat (model," DNG");
    if (filters == UINT_MAX) filters = 0;
    if (!filters)
      colors = tiff_samples;
    if (tiff_compress == 1)
      load_raw = & adobe_dng_load_raw_nc;
    if (tiff_compress == 7)
      load_raw = & adobe_dng_load_raw_lj;
    for (c=0; c < 4; c++) cam_mul[c] = pre_mul[c];
    goto dng_skip;
  }
*/
/*  We'll try to decode anything from Canon or Nikon. */

  if ((is_canon = (Uc.strcmp(make,"Canon")==0))) {
    load_raw = CTOJ.memcmp (head.plus(6),"HEAPCCDR",8)!=0 ?
	new LosslessJpegLoadRaw() : new CanonCompressedLoadRaw();
    //maximum = 0xfff;
  }
  if ( Uc.strcmp(make,"NIKON") == 0 && load_raw == null)
    load_raw = new NikonLoadRaw();
  if ( Uc.strncmp (make,"OLYMPUS",7) == 0)
    height += height & 1;

/* Set parameters based on camera name (for non-DNG files). */

  if (is_foveon != 0) {
    if (height*2 < width) pixel_aspect = 0.5;
    if (height   > width) pixel_aspect = 2;
    filters = 0;
    load_raw = new FoveonLoadRaw();
    //simple_coeff(0);
  }  else if (is_canon && tiff_samples == 4) {
    filters = 0;
    load_raw = new CanonSrawLoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot 600")==0) {
    height = 613;
    width  = 854;
    raw_width = 896;
    pixel_aspect = 607/628.0;
    colors = 4;
    filters = 0xe1e4e1e4l;
    load_raw = new Canon600LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A5")==0 ||
	     Uc.strcmp(model,"PowerShot A5 Zoom") == 0) {
    height = 773;
    width  = 960;
    raw_width = 992;
    pixel_aspect = 256/235.0;
    colors = 4;
    filters = 0x1e4e1e4el;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A50") == 0) {
    height =  968;
    width  = 1290;
    raw_width = 1320;
    colors = 4;
    filters = 0x1b4e4b1el;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot Pro70") == 0) {
    height = 1024;
    width  = 1552;
    colors = 4;
    filters = 0x1e4b4e1bl;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A460") == 0) {
    height = 1960;
    width  = 2616;
    raw_height = 1968;
    raw_width  = 2664;
    top_margin  = 4;
    left_margin = 4;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A530") == 0) {
    height = 1984;
    width  = 2620;
    raw_height = 1992;
    raw_width  = 2672;
    top_margin  = 6;
    left_margin = 10;
    load_raw = new CanonA5LoadRaw();
    raw_color = 0;
  } else if ( Uc.strcmp(model,"PowerShot A610") == 0) {
    //if (canon_s2is()) Uc.strcpy (model+10, "S2 IS"); // TODO
    height = 1960;
    width  = 2616;
    raw_height = 1968;
    raw_width  = 2672;
    top_margin  = 8;
    left_margin = 12;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A620") == 0) {
    height = 2328;
    width  = 3112;
    raw_height = 2340;
    raw_width  = 3152;
    top_margin  = 12;
    left_margin = 36;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A720") == 0) {
    height = 2472;
    width  = 3298;
    raw_height = 2480;
    raw_width  = 3336;
    top_margin  = 5;
    left_margin = 6;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A630") == 0) {
    height = 2472;
    width  = 3288;
    raw_height = 2484;
    raw_width  = 3344;
    top_margin  = 6;
    left_margin = 12;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A640") == 0) {
    height = 2760;
    width  = 3672;
    raw_height = 2772;
    raw_width  = 3736;
    top_margin  = 6;
    left_margin = 12;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot A650") == 0) {
    height = 3024;
    width  = 4032;
    raw_height = 3048;
    raw_width  = 4104;
    top_margin  = 12;
    left_margin = 48;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot S3 IS") == 0) {
    height = 2128;
    width  = 2840;
    raw_height = 2136;
    raw_width  = 2888;
    top_margin  = 8;
    left_margin = 44;
    load_raw = new CanonA5LoadRaw();
  } else if ( Uc.strcmp(model,"PowerShot Pro90 IS") == 0) {
    width  = 1896;
    colors = 4;
    filters = 0xb4b4b4b4l;
  } else if (is_canon && raw_width == 2144) {
    height = 1550;
    width  = 2088;
    top_margin  = 8;
    left_margin = 4;
    if ( Uc.strcmp(model,"PowerShot G1")==0) {
      colors = 4;
      filters = 0xb4b4b4b4l;
    }
  } else if (is_canon && raw_width == 2224) {
    height = 1448;
    width  = 2176;
    top_margin  = 6;
    left_margin = 48;
  } else if (is_canon && raw_width == 2376) {
    height = 1720;
    width  = 2312;
    top_margin  = 6;
    left_margin = 12;
  } else if (is_canon && raw_width == 2672) {
    height = 1960;
    width  = 2616;
    top_margin  = 6;
    left_margin = 12;
  } else if (is_canon && raw_width == 3152) {
    height = 2056;
    width  = 3088;
    top_margin  = 12;
    left_margin = 64;
    if (unique_id == 0x80000170)
      adobe_coeff ("Canon","EOS 300D");
//    maximum = 0xfa0;
  } else if (is_canon && raw_width == 3160) {
    height = 2328;
    width  = 3112;
    top_margin  = 12;
    left_margin = 44;
  } else if (is_canon && raw_width == 3344) {
    height = 2472;
    width  = 3288;
    top_margin  = 6;
    left_margin = 4;
  } else if ( Uc.strcmp(model,"EOS D2000C")==0) {
    filters = 0x61616161l;
    black = curve.at(200);
  } else if (is_canon && raw_width == 3516) {
    top_margin  = 14;
    left_margin = 42;
    if (unique_id == 0x80000189)
      adobe_coeff ("Canon","EOS 350D");
    canon_cr2();
  } else if (is_canon && raw_width == 3596) {
    top_margin  = 12;
    left_margin = 74;
    canon_cr2();
  } else if (is_canon && raw_width == 3944) {
    height = 2602;
    width  = 3908;
    top_margin  = 18;
    left_margin = 30;
  } else if (is_canon && raw_width == 3948) {
    top_margin  = 18;
    left_margin = 42;
    height -= 2;
    if (unique_id == 0x80000236)
      adobe_coeff ("Canon","EOS 400D");
    canon_cr2();
  } else if (is_canon && raw_width == 3984) {
    top_margin  = 20;
    left_margin = 76;
    height -= 2;
    canon_cr2();
  } else if (is_canon && raw_width == 4104) {
    height = 3024;
    width  = 4032;
    top_margin  = 12;
    left_margin = 48;
  } else if (is_canon && raw_width == 4312) {
    top_margin  = 18;
    left_margin = 22;
    height -= 2;
    if (unique_id == 0x80000176)
      adobe_coeff ("Canon","EOS 450D");
    canon_cr2();
  } else if (is_canon && raw_width == 4476) {
    top_margin  = 34;
    left_margin = 90;
    //maximum = 0xe6c;
    canon_cr2();
  } else if (is_canon && raw_width == 1208) {
    top_margin  = 51;
    left_margin = 62;
    raw_width = width *= 4;
    maximum = 0x3d93;
    canon_cr2();
  } else if (is_canon && raw_width == 5108) {
    top_margin  = 13;
    left_margin = 98;
    //maximum = 0xe80;
    canon_cr2();
  } else if (is_canon && raw_width == 5712) {
    height = 3752;
    width  = 5640;
    top_margin  = 20;
    left_margin = 62;
  } else if (Uc.strcmp(model,"D1")==0) {
    cam_mul[0] *= 256/527.0;
    cam_mul[2] *= 256/317.0;
  } else if (Uc.strcmp(model,"D1X")==0) {
    width -= 4;
    //ymag = 2;
    pixel_aspect = 0.5;
  } else if ( Uc.strcmp(model,"D40X")==0 ||
	     Uc.strcmp(model,"D60")==0  ||
	     Uc.strcmp(model,"D80")==0) {
    height -= 3;
    width  -= 4;
  } else if (Uc.strncmp(model,"D40",3)==0 ||
	     Uc.strncmp(model,"D50",3)==0 ||
	     Uc.strncmp(model,"D70",3)==0) {
    width--;
  } else if ( Uc.strcmp(model,"D90") == 0) {
    width -= 42;
  } else if (Uc.strcmp(model,"D100")==0) {
    if (tiff_compress == 34713 &&  !nikon_is_compressed())
      load_raw = new NikonLoadRaw();
      raw_width = (width += 3) + 3;
    maximum = 0xf44;
  } else if (Uc.strcmp(model,"D200")==0) {
    left_margin = 1;
    width -= 4;
    maximum = 0xfbc;
    filters = 0x94949494l;
  } else if (Uc.strncmp(model,"D2H",3)==0) {
    left_margin = 6;
    width -= 14;
  } else if (Uc.strcmp(model,"D2X")==0) {
    if (width == 3264) width -= 32;
    width -= 8;
  } else if ( Uc.strcmp(model,"D3")==0) {
    width -= 4;
    left_margin = 2;
  } else if (Uc.strcmp(model,"D300")==0) {
    width -= 32;
  } else if (fsize == 1581060) {
    height = 963;
    width = 1287;
    raw_width = 1632;
    load_raw = new NikonE900LoadRaw();
    maximum = 0x3f4;
    colors = 4;
    filters = 0x1e1e1e1el;
    //simple_coeff(3);
    pre_mul[0] = 1.2085f;
    pre_mul[1] = 1.0943f;
    pre_mul[3] = 1.1103f;
  } else if (fsize == 2465792) {
    height = 1203;
    width  = 1616;
    raw_width = 2048;
    load_raw = new NikonE900LoadRaw();
    maximum = 0x3dd;
    colors = 4;
    filters = 0x4b4b4b4bl;
    adobe_coeff ("NIKON","E950");
  } else if (fsize == 4771840) {
    height = 1540;
    width  = 2064;
    colors = 4;
    filters = 0xe1e1e1e1l;
    load_raw = new NikonLoadRaw();
//    if (!timestamp && nikon_e995())
//      Uc.strcpy (model, "E995");
    if (Uc.strcmp(model,"E995") != 0) {
      filters = 0xb4b4b4b4l;
      //simple_coeff(3);
      pre_mul[0] = 1.196f;
      pre_mul[1] = 1.246f;
      pre_mul[2] = 1.018f;
    }
  } else if (Uc.strcmp(model,"E2100")==0) {
      /*
    if ( timestamp==0 && !nikon_e2100()) {
            Uc.strcpy (model, "E2500");
    height = 1204;
    width  = 1616;
    colors = 4;
    filters = 0x4b4b4b4b;

    }
    else {
    height = 1206;
    width  = 1616;
    load_raw = new NikonE2100LoadRaw();
    pre_mul[0] = 1.945;
    pre_mul[2] = 1.040;
    }
       */
  } else if ( Uc.strcmp(model,"E2500")==0) {
cp_e2500:
    Uc.strcpy (model, "E2500");
    height = 1204;
    width  = 1616;
    colors = 4;
    filters = 0x4b4b4b4bl;
  } else if (fsize == 4775936) {
    height = 1542;
    width  = 2064;
    load_raw = new NikonE2100LoadRaw();
    pre_mul[0] = 1.818f;
    pre_mul[2] = 1.618f;
    //if (!timestamp) nikon_3700();
    if (model.charAt(0) == 'E' && Uc.atoi(model.plus(1)) < 3700)
      filters = 0x49494949l;
    if (Uc.strcmp(model,"Optio 33WR")==0) {
      flip = 1;
      filters = 0x16161616l;
      pre_mul[0] = 1.331f;
      pre_mul[2] = 1.820f;
    }
  } else if (fsize == 5869568) {
    height = 1710;
    width  = 2288;
    filters = 0x16161616l;
    if ( timestamp==0 && minolta_z2()!=0) {
      Uc.strcpy (make, "Minolta");
      Uc.strcpy (model,"DiMAGE Z2");
    }
    if (make.charAt(0) == 'M')
      load_raw = new NikonE2100LoadRaw();
  } else if ( Uc.strcmp(model,"E4500") == 0) {
    height = 1708;
    width  = 2288;
    colors = 4;
    filters = 0xb4b4b4b4l;
  } else if (fsize == 7438336) {
    height = 1924;
    width  = 2576;
    colors = 4;
    filters = 0xb4b4b4b4l;
  } else if ( Uc.strncmp(model,"R-D1",4) == 0) {
    tiff_compress = 34713;
    load_raw = new NikonLoadRaw();
  } else if (Uc.strcmp(model,"FinePix S5100")==0 ||
	     Uc.strcmp(model,"FinePix S5500")==0) {
    load_raw = new UnpackedLoadRaw() ;
    maximum = 0x3e00;
  } else if ( Uc.strcmp(make,"FUJIFILM") == 0) {
    if ( Uc.strcmp(model.plus(7),"S2Pro") == 0) {
      Uc.strcpy (model.plus(7)," S2Pro");
      height = 2144;
      width  = 2880;
      flip = 6;
    } else
      maximum = 0x3e00;
    if (is_raw == 2 && shot_select != 0)
      maximum = 0x2f00;
    top_margin = (raw_height - height)/2;
    left_margin = (raw_width - width )/2;
    if (is_raw == 2)
      data_offset += ((shot_select > 0)?1:0) * ( fuji_layout!=0 ?
		(raw_width *= 2) : raw_height*raw_width*2 );
    fuji_width = width >> (fuji_layout!=0 ? 0 : 1);
    width = (height >> fuji_layout) + fuji_width;
    raw_height = height;
    height = width - 1;
    load_raw = new FujiLoadRaw();
    if ((fuji_width & 1) == 0) filters = 0x49494949l;
  } else if ( Uc.strcmp(model,"RD175")==0) {
    height = 986;
    width = 1534;
    data_offset = 513;
    filters = 0x61616161l;
    load_raw = new MinoltaRd175LoadRaw();
  } else if ( Uc.strcmp(model,"KD-400Z")==0) {
    height = 1712;
    width  = 2312;
    raw_width = 2336;
      load_raw = new UnpackedLoadRaw() ;
      maximum = 0x3df;
      order = 0x4d4d;
  } else if ( Uc.strcmp(model,"KD-510Z")==0) {
	height = 1956;
	width  = 2607;
	raw_width = 2624;
  } else if ( Uc.strcasecmp(make,"MINOLTA")==0) {
    load_raw = new UnpackedLoadRaw() ;
    maximum = 0xf7d;
    if ( Uc.strncmp(model,"DiMAGE A",8)==0) {
      if (Uc.strcmp(model,"DiMAGE A200")==0)
	filters = 0x49494949l;
      load_raw = new Packed12LoadRaw();
      maximum = model.charAt(8) == '1' ? 0xf8b : 0xfff;
    } else if ( Uc.strncmp(model,"ALPHA",5)==0 ||
	        Uc.strncmp(model,"DYNAX",5)==0 ||
	        Uc.strncmp(model,"MAXXUM",6)==0) {
      Uc.sprintf (model.plus(20), "DYNAX %-10s", model.plus(6+((model.charAt(0)=='M')?1:0)));
      adobe_coeff (make, model.plus(20));
      load_raw = new Packed12LoadRaw();
      maximum = 0xffb;
    } else if (Uc.strncmp(model,"DiMAGE G",8)==0) {
      if (model.charAt(8) == '4') {
	height = 1716;
	width  = 2304;
      } else if (model.charAt(8) == '5') {
konica_510z:
	height = 1956;
	width  = 2607;
	raw_width = 2624;
      } else if (model.charAt(8) == '6') {
	height = 2136;
	width  = 2848;
      }
      data_offset += 14;
      filters = 0x61616161l;
konica_400z:
      load_raw = new UnpackedLoadRaw() ;
      maximum = 0x3df;
      order = 0x4d4d;
    }
  } else if ( Uc.strcmp(model,"*ist DS")==0) {
    height -= 2;
  } else if ( Uc.strcmp(model,"Optio S")==0) {
    if (fsize == 3178560) {
      height = 1540;
      width  = 2064;
      load_raw = new EightBitLoadRaw();
      cam_mul[0] *= 4;
      cam_mul[2] *= 4;
      pre_mul[0] = 1.391f;
      pre_mul[2] = 1.188f;
    } else {
      height = 1544;
      width  = 2068;
      raw_width = 3136;
      load_raw = new Packed12LoadRaw();
      maximum = 0xf7c;
      pre_mul[0] = 1.137f;
      pre_mul[2] = 1.453f;
    }
  } else if (Uc.strncmp(model,"Optio S4",8)==0) {
    height = 1737;
    width  = 2324;
    raw_width = 3520;
    load_raw = new Packed12LoadRaw();
    maximum = 0xf7a;
    pre_mul[0] = 1.980f;
    pre_mul[2] = 1.570f;
  } else if (Uc.strcmp(model,"STV680 VGA")==0) {
    height = 484;
    width  = 644;
    load_raw = new EightBitLoadRaw();
    flip = 2;
    filters = 0x16161616l;
    black = 16;
    pre_mul[0] = 1.097f;
    pre_mul[2] = 1.128f;
  } else if (Uc.strcmp(model,"KAI-0340")==0) {
    height = 477;
    width  = 640;
    order = 0x4949;
    data_offset = 3840;
    load_raw = new UnpackedLoadRaw() ;
    pre_mul[0] = 1.561f;
    pre_mul[2] = 2.454f;
  } else if ( Uc.strcmp(model,"531C")==0) {
    height = 1200;
    width  = 1600;
    load_raw = new UnpackedLoadRaw() ;
    filters = 0x49494949l;
    pre_mul[1] = 1.218f;
  } else if ( Uc.strcmp(model,"F-145C")==0) {
    height = 1040;
    width  = 1392;
    load_raw = new EightBitLoadRaw();
  } else if ( Uc.strcmp(model,"F-201C")==0) {
    height = 1200;
    width  = 1600;
    load_raw = new EightBitLoadRaw();
  } else if (Uc.strcmp(model,"F-510C")==0) {
    height = 1958;
    width  = 2588;
    load_raw = fsize < 7500000 ?
	new EightBitLoadRaw() : new UnpackedLoadRaw() ;
    maximum = 0xfff0;
  } else if (Uc.strcmp(model,"F-810C")==0) {
    height = 2469;
    width  = 3272;
    load_raw = new UnpackedLoadRaw() ;
    maximum = 0xfff0;
  } else if (Uc.strcmp(model,"XCD-SX910CR")==0) {
    height = 1024;
    width  = 1375;
    raw_width = 1376;
    filters = 0x49494949l;
    maximum = 0x3ff;
    load_raw = fsize < 2000000 ?
	new EightBitLoadRaw() : new UnpackedLoadRaw() ;
  } else if (Uc.strcmp(model,"2010")==0) {
    height = 1207;
    width  = 1608;
    order = 0x4949;
    filters = 0x16161616l;
    data_offset = 3212;
    maximum = 0x3ff;
    load_raw = new UnpackedLoadRaw() ;
  } else if (Uc.strcmp(model,"A782")==0) {
    height = 3000;
    width  = 2208;
    filters = 0x61616161l;
    load_raw = fsize < 10000000 ?
	new EightBitLoadRaw() : new UnpackedLoadRaw() ;
    maximum = 0xffc0;
  } else if (Uc.strcmp(model,"3320AF")==0) {
    height = 1536;
    raw_width = width = 2048;
    filters = 0x61616161l;
    load_raw = new UnpackedLoadRaw() ;
    maximum = 0x3ff;
    pre_mul[0] = 1.717f;
    pre_mul[2] = 1.138f;
    CTOJ.fseek (ifp, 0x300000, CTOJ.SEEK_SET);
//    if ((order = guess_byte_order(0x10000)) == 0x4d4d) {
//      height -= (top_margin = 16);
//      width -= (left_margin = 28);
//      maximum = 0xf5c0;
//      Uc.strcpy (make, "ISG");
//      model[0] = 0;
//    }
  } else if ( Uc.strcmp(make,"Imacon")==0) {
    Uc.sprintf (model, "Ixpress %d-Mp", height*width/1000000);
    load_raw = new ImaconFullLoadRaw();
    if (filters != 0) {
      if ((left_margin & 1)!= 0) filters = 0x61616161l;
      load_raw = new UnpackedLoadRaw() ;
    }
    maximum = 0xffff;
  } else if ( Uc.strcmp(make,"Sinar")==0) {
    if (CTOJ.memcmp(head,"8BPS",4) == 0) {
      CTOJ.fseek (ifp, 14, CTOJ.SEEK_SET);
      height = get4();
      width  = get4();
      filters = 0x61616161l;
      data_offset = 68;
    }
    load_raw = new UnpackedLoadRaw() ;
    maximum = 0x3fff;
  } else if (Uc.strcmp(make,"Leaf")==0) {
    maximum = 0x3fff;
    if (tiff_samples > 1) filters = 0;
    if (tiff_samples > 1 || tile_length < raw_height)
      load_raw = new LeafHdrLoadRaw();
    if ((width | height) == 2048) {
      if (tiff_samples == 1) {
	filters = 1;
	Uc.strcpy (cdesc, "RBTG");
	Uc.strcpy (model, "CatchLight");
	top_margin =  8; left_margin = 18; height = 2032; width = 2016;
      } else {
	Uc.strcpy (model, "DCB2");
	top_margin = 10; left_margin = 16; height = 2028; width = 2022;
      }
    } else if (width+height == 3144+2060) {
      if ( model.charAt(0) == 0) Uc.strcpy (model, "Cantare");
      if (width > height) {
	 top_margin = 6; left_margin = 32; height = 2048;  width = 3072;
	filters = 0x61616161l;
      } else {
	left_margin = 6;  top_margin = 32;  width = 2048; height = 3072;
	filters = 0x16161616l;
      }
      if ( cam_mul[0] == 0 || model.charAt(0) == 'V') filters = 0;
    } else if (width == 2116) {
      Uc.strcpy (model, "Valeo 6");
      height -= 2 * (top_margin = 30);
      width -= 2 * (left_margin = 55);
      filters = 0x49494949l;
    } else if (width == 3171) {
      Uc.strcpy (model, "Valeo 6");
      height -= 2 * (top_margin = 24);
      width -= 2 * (left_margin = 24);
      filters = 0x16161616l;
    }
  } else if ( Uc.strcmp(make,"LEICA")==0 || Uc.strcmp(make,"Panasonic")==0) {
    maximum = 0xfff0;
    if ((fsize-data_offset) / (width*8/7) == height)
      load_raw = new PanasonicLoadRaw();
    if (load_raw==null) load_raw = new UnpackedLoadRaw() ;
    if (width == 2568)
      adobe_coeff ("Panasonic","DMC-LC1");
    else if (width == 3304) {
      maximum = 0xf94c;
      width -= 16;
      adobe_coeff ("Panasonic","DMC-FZ30");
    } else if ( width == 3330) {
        width = 3291;
        left_margin = 9;
        maximum = 0xf7f0;
fz18:	if (height > 2480)
	    height = 2480 - (top_margin = 10);
	filters = 0x49494949l;
	zero_is_bad = true;
    } else if (width == 3690) {
      maximum = 0xf7f0;
      height -= 3;
      width = 3672;
      left_margin = 3;
      filters = 0x49494949l;
      adobe_coeff ("Panasonic","DMC-FZ50");
    } else if (width == 3770) {
      height = 2760;
      width  = 3672;
      top_margin  = 15;
      left_margin = 17;
      adobe_coeff ("Panasonic","DMC-FZ50");
    } else if (width == 3880) {
      maximum = 0xf7f0;
      width -= 22;
      left_margin = 6;
      adobe_coeff ("Panasonic","DMC-LX1");
    } else if (width == 4290) {
      height--;
      width = 4248;
      left_margin = 3;
      filters = 0x49494949l;
      adobe_coeff ("Panasonic","DMC-LX2");
    } else if (width == 4330) {
      height = 2400;
      width  = 4248;
      top_margin  = 15;
      left_margin = 17;
      adobe_coeff ("Panasonic","DMC-LX2");
    }
  } else if ( Uc.strcmp(model,"E-1")==0) {
    filters = 0x61616161l;
    maximum = 0xfff0;
    black = 1024;
  } else if ( Uc.strcmp(model,"E-10")==0) {
    maximum = 0xfff0;
    black = 2048;
  } else if (Uc.strncmp(model,"E-20",4)==0) {
    maximum = 0xffc0;
    black = 2560;
  } else if ( Uc.strcmp(model,"E-300")==0 ||
	     Uc.strcmp(model,"E-500")==0) {
    width -= 20;
    maximum = 0xfc30;
  } else if (Uc.strcmp(model,"E-330")==0) {
    width -= 30;
  } else if (Uc.strcmp(model,"C770UZ")==0) {
    height = 1718;
    width  = 2304;
    filters = 0x16161616l;
    load_raw = new NikonE2100LoadRaw();
  } else if (Uc.strcmp(make,"OLYMPUS")==0) {
    load_raw = new OlympusCseriesLoadRaw();
    if ( Uc.strcmp(model,"C5050Z")==0 ||
	Uc.strcmp(model,"C8080WZ")==0)
      filters = 0x16161616l;
    if (Uc.strcmp(model,"SP500UZ")==0)
      filters = 0x49494949l;
  } else if (Uc.strcmp(model,"N Digital")==0) {
    height = 2047;
    width  = 3072;
    filters = 0x61616161l;
    data_offset = 0x1a00;
    load_raw = new Packed12LoadRaw();
    maximum = 0xf1e;
  } else if (Uc.strcmp(model,"DSC-F828")==0) {
    width = 3288;
    left_margin = 5;
    data_offset = 862144;
    load_raw = new SonyLoadRaw();
    filters = 0x9c9c9c9cl;
    colors = 4;
    Uc.strcpy (cdesc, "RGBE");
  } else if (Uc.strcmp(model,"DSC-V3")==0) {
    width = 3109;
    left_margin = 59;
    data_offset = 787392;
    load_raw = new SonyLoadRaw();
  } else if (Uc.strcmp(make,"SONY")==0 && raw_width == 3984) {
    adobe_coeff ("SONY","DSC-R1");
    width = 3925;
    order = 0x4d4d;
  } else if (Uc.strcmp(model,"DSLR-A100")==0) {
    height--;
    load_raw = new SonyArwLoadRaw();
    maximum = 0xfeb;
  } else if (Uc.strncmp(model,"P850",4)==0) {
    maximum = 0xf7c;
  } else if (Uc.strcasecmp(make,"KODAK")==0) {
    if (filters == CTOJ.UINT_MAX) filters = 0x61616161l;
    if (Uc.strncmp(model,"NC2000",6)==0) {
      width -= 4;
      left_margin = 2;
    } else if (Uc.strcmp(model,"EOSDCS3B")==0) {
      width -= 4;
      left_margin = 2;
    } else if (Uc.strcmp(model,"EOSDCS1")==0) {
      width -= 4;
      left_margin = 2;
    } else if (Uc.strcmp(model,"DCS420")==0) {
      width -= 4;
      left_margin = 2;
    } else if (Uc.strcmp(model,"DCS460")==0) {
      width -= 4;
      left_margin = 2;
    } else if (Uc.strcmp(model,"DCS460A")==0) {
      width -= 4;
      left_margin = 2;
      colors = 1;
      filters = 0;
    } else if (Uc.strcmp(model,"DCS660M")==0) {
      black = 214;
      colors = 1;
      filters = 0;
    } else if (Uc.strcmp(model,"DCS760M")==0) {
      colors = 1;
      filters = 0;
    }
    if (load_raw.getClass().equals( EightBitLoadRaw.class))
	load_raw = new KodakEasyLoadRaw();
    if (Uc.strstr(model,"DC25") != null) {
      Uc.strcpy (model, "DC25");
      data_offset = 15424;
    }
    if (Uc.strncmp(model,"DC2",3)==0) {
      height = 242;
      if (fsize < 100000) {
	raw_width = 256; width = 249;
      } else {
	raw_width = 512; width = 501;
      }
      data_offset += raw_width + 1;
      colors = 4;
      filters = 0x8d8d8d8dl;
      //simple_coeff(1);
      pre_mul[1] = 1.179f;
      pre_mul[2] = 1.209f;
      pre_mul[3] = 1.036f;
      load_raw = new KodakEasyLoadRaw();
    } else if (Uc.strcmp(model,"40")==0) {
      Uc.strcpy (model, "DC40");
      height = 512;
      width  = 768;
      data_offset = 1152;
//      load_raw = new KodakRadcLoadRaw();
    } else if (Uc.strstr(model,"DC50") != null) {
      Uc.strcpy (model, "DC50");
      height = 512;
      width  = 768;
      data_offset = 19712;
  //    load_raw = new KodakRadcLoadRaw();
    } else if (Uc.strstr(model,"DC120") != null) {
      Uc.strcpy (model, "DC120");
      height = 976;
      width  = 848;
      pixel_aspect = height/0.75/width;
//      load_raw = tiff_compress == 7 ?
//	new KodakJpegLoadRaw() : new KodakDc120LoadRaw();
    }
  } else if (Uc.strcmp(model,"Fotoman Pixtura")==0) {
    height = 512;
    width  = 768;
    data_offset = 3632;
    load_raw = new KodakRadcLoadRaw();
    filters = 0x61616161l;
    //simple_coeff(2);
  } else if (Uc.strcmp(make,"Rollei")==0) {
    switch (raw_width) {
      case 1316:
	height = 1030;
	width  = 1300;
	top_margin  = 1;
	left_margin = 6;
	break;
      case 2568:
	height = 1960;
	width  = 2560;
	top_margin  = 2;
	left_margin = 8;
    }
    filters = 0x16161616l;
    load_raw = new RolleiLoadRaw();
    pre_mul[0] = 1.8f;
    pre_mul[2] = 1.3f;
  } else if ( Uc.strcmp(model,"PC-CAM 600")==0) {
    height = 768;
    data_offset = width = 1024;
    filters = 0x49494949l;
    load_raw = new EightBitLoadRaw();
    pre_mul[0] = 1.14f;
    pre_mul[2] = 2.73f;
  } else if ( Uc.strcmp(model,"QV-2000UX")==0) {
    height = 1208;
    width  = 1632;
    data_offset = width * 2;
    load_raw = new EightBitLoadRaw();
  } else if (fsize == 3217760) {
    height = 1546;
    width  = 2070;
    raw_width = 2080;
    load_raw = new EightBitLoadRaw();
  } else if (Uc.strcmp(model,"QV-4000")==0) {
    height = 1700;
    width  = 2260;
    load_raw = new UnpackedLoadRaw() ;
    maximum = 0xffff;
  } else if (Uc.strcmp(model,"QV-5700")==0) {
    height = 1924;
    width  = 2576;
    //load_raw = new CasioQv5700LoadRaw();
  } else if (Uc.strcmp(model,"QV-R51")==0) {
    height = 1926;
    width  = 2576;
    raw_width = 3904;
    load_raw = new Packed12LoadRaw();
    pre_mul[0] = 1.340f;
    pre_mul[2] = 1.672f;
  } else if (Uc.strcmp(model,"EX-S100")==0) {
    height = 1544;
    width  = 2058;
    raw_width = 3136;
    load_raw = new Packed12LoadRaw();
    pre_mul[0] = 1.631f;
    pre_mul[2] = 1.106f;
  } else if (Uc.strcmp(model,"EX-Z50")==0) {
    height = 1931;
    width  = 2570;
    raw_width = 3904;
    load_raw = new Packed12LoadRaw();
    pre_mul[0] = 2.529f;
    pre_mul[2] = 1.185f;
  } else if (Uc.strcmp(model,"EX-Z55")==0) {
    height = 1960;
    width  = 2570;
    raw_width = 3904;
    load_raw = new Packed12LoadRaw();
    pre_mul[0] = 1.520f;
    pre_mul[2] = 1.316f;
  } else if (Uc.strcmp(model,"EX-P505")==0) {
    height = 1928;
    width  = 2568;
    raw_width = 3852;
    load_raw = new Packed12LoadRaw();
    pre_mul[0] = 2.07f;
    pre_mul[2] = 1.88f;
  } else if (fsize == 9313536) {	/* EX-P600 or QV-R61 */
    height = 2142;
    width  = 2844;
    raw_width = 4288;
    load_raw = new Packed12LoadRaw();
    pre_mul[0] = 1.797f;
    pre_mul[2] = 1.219f;
  } else if (Uc.strcmp(model,"EX-P700")==0) {
    height = 2318;
    width  = 3082;
    raw_width = 4672;
    load_raw = new Packed12LoadRaw();
    pre_mul[0] = 1.758f;
    pre_mul[2] = 1.504f;
  } else if (Uc.strcmp(make,"Nucore")==0) {
    filters = 0x61616161l;
    load_raw = new UnpackedLoadRaw() ;
    if (width == 2598) {
      filters = 0x16161616l;
      //load_raw = new NucoreLoadRaw();
      flip = 2;
    }
  }
  if ( model.charAt(0)==0)
    Uc.sprintf (model, "%dx%d", width, height);
  if (filters == CTOJ.UINT_MAX) 
      filters = 0x94949494l;
  if (raw_color != 0) adobe_coeff (make, model);
  if (thumb_offset!=0 && thumb_height==0) {
    CTOJ.fseek (ifp, thumb_offset, CTOJ.SEEK_SET);
    if (ljpeg_start (jh, 1)!=0) {
      thumb_width  = jh.wide;
      thumb_height = jh.high;
    }
  }
dng_skip:
  if (load_raw==null || height==0) is_raw = 0;
  if (flip == -1) flip = tiff_flip;
  if (flip == -1) flip = 0;
  if (cdesc.charAt(0)==0)
    Uc.strcpy (cdesc, colors == 3 ? "RGB":"GMCY");
  if ( raw_height == 0) raw_height = height;
  if ( raw_width == 0) raw_width  = width;
  if (filters!= 0 && colors == 3)
    for (i=0; i < 32; i+=4) {
      if ((filters >> i & 15) == 9) {
	filters |= 2 << i;
        filters = CTOJ.toUnsigned((int)filters);
      }
      if ((filters >> i & 15) == 6) {
	filters |= 8 << i;
        filters = CTOJ.toUnsigned((int)filters);
      }
    }
}

void canon_cr2() {
    height -= top_margin;
    width  -= left_margin;    
}

void  convert_to_rgb()
{
  int mix_green, row, col, c, i, j, k;
  NewShortPtr img = new NewShortPtr();// = new CharPtr();
  float[] out = new float[3];
  float[][] out_cam = new float[3][4];
  double num;
  double[][] inverse = new double[3][3];
  double[][] xyzd50_srgb =
  { { 0.436083, 0.385083, 0.143055 },
    { 0.222507, 0.716888, 0.060608 },
    { 0.013930, 0.097097, 0.714022 } };
  double[][] rgb_rgb =
  { { 1,0,0 }, { 0,1,0 }, { 0,0,1 } };
  double[][] adobe_rgb =
  { { 0.715146, 0.284856, 0.000000 },
    { 0.000000, 1.000000, 0.000000 },
    { 0.000000, 0.041166, 0.958839 } };
  double[][] wide_rgb =
  { { 0.593087, 0.404710, 0.002206 },
    { 0.095413, 0.843149, 0.061439 },
    { 0.011621, 0.069091, 0.919288 } };
  double[][] prophoto_rgb =
  { { 0.529317, 0.330092, 0.140588 },
    { 0.098368, 0.873465, 0.028169 },
    { 0.016879, 0.117663, 0.865457 } };
  double[][][] out_rgb =
  { rgb_rgb, adobe_rgb, wide_rgb, prophoto_rgb, xyz_rgb };
  String[] name =
  { "sRGB", "Adobe RGB (1998)", "WideGamut D65", "ProPhoto D65", "XYZ" };
  IntPtr phead = new IntPtr(20);
  int phead_tab[] =
  { 1024, 0, 0x2100000, 0x6d6e7472, 0x52474220, 0x58595a20, 0, 0, 0,
    0x61637370, 0, 0, 0x6e6f6e65, 0, 0, 0, 0, 0xf6d6, 0x10000, 0xd32d };
  
  phead.copy( phead_tab);
  
  IntPtr pbody = new IntPtr(31);
  int pbody_tab[] =
  { 10, 0x63707274, 0, 36,	/* cprt */
	0x64657363, 0, 40,	/* desc */
	0x77747074, 0, 20,	/* wtpt */
	0x626b7074, 0, 20,	/* bkpt */
	0x72545243, 0, 14,	/* rTRC */
	0x67545243, 0, 14,	/* gTRC */
	0x62545243, 0, 14,	/* bTRC */
	0x7258595a, 0, 20,	/* rXYZ */
	0x6758595a, 0, 20,	/* gXYZ */
	0x6258595a, 0, 20 };	/* bXYZ */
  pbody.copy(pbody_tab);
  
  IntPtr pwhite = new IntPtr();
  pwhite.assign(CTOJ.calloc(3,4));
  int pwhite_tab[] = { 0xf351, 0x10000, 0x116cc };
  pwhite.copy(pwhite_tab);
  
  IntPtr pcurve = new IntPtr();
  pcurve.assign(CTOJ.calloc(4,4));
  int pcurve_tab[] = { 0x63757276, 0, 1, 0x1000000 };
  pcurve.copy(pcurve_tab);
  
  String vendor =  make.toString();
  String modelS = model.toString();
      
  gamma_curve (gamm[0], gamm[1], 0, 0);
  
  if ( calibrating) {
      
      int nb_red = 0;
      int nb_green = 0;
      int nb_blue = 0;
    for (img=image.toShortPtr(0), row=0; row < height; row++) {
        for (col=0; col < width; col++, img.add(4)) {
            if ( inside_red(col, row)) {
                nb_red ++;
                matrix[0][0] += CTOJ.toUnsigned(img.at(0));
                matrix[0][1] += CTOJ.toUnsigned(img.at(1));
                matrix[0][2] += CTOJ.toUnsigned(img.at(2));
            }
            if ( inside_green(col, row)) {
                nb_green ++;
                matrix[1][0] += CTOJ.toUnsigned(img.at(0));
                matrix[1][1] += CTOJ.toUnsigned(img.at(1));
                matrix[1][2] += CTOJ.toUnsigned(img.at(2));
            }
            if ( inside_blue(col, row)) {
                nb_blue ++;
                matrix[2][0] += CTOJ.toUnsigned(img.at(0));
                matrix[2][1] += CTOJ.toUnsigned(img.at(1));
                matrix[2][2] += CTOJ.toUnsigned(img.at(2));
            }
        }
    }
      matrix[0][0] /= nb_red;
      matrix[0][1] /= nb_red;
      matrix[0][2] /= nb_red;
      
      matrix[1][0] /= nb_green;
      matrix[1][1] /= nb_green;
      matrix[1][2] /= nb_green;
      
      matrix[2][0] /= nb_blue;
      matrix[2][1] /= nb_blue;
      matrix[2][2] /= nb_blue;
      
      double[][] matrix2 = new double[3][3];
      
      matrix2[0][0] = matrix[0][0];
      matrix2[0][1] = matrix[1][0];
      matrix2[0][2] = matrix[2][0];
      matrix2[1][0] = matrix[0][1];
      matrix2[1][1] = matrix[1][1];
      matrix2[1][2] = matrix[2][1];
      matrix2[2][0] = matrix[0][2];
      matrix2[2][1] = matrix[1][2];
      matrix2[2][2] = matrix[2][2];
      
      cam_coeff( matrix); // calcule rgb_cam2
      use_rgb_cam2 = true;
      
      client.registerCalibrationMatrix( vendor, modelS, rgb_cam2);
  }
  
  float[][] mat = client.getCalibrationMatrix( vendor, modelS);
  if ( mat != null) {
      rgb_cam2 = mat;
      use_rgb_cam2 = true;
  }
  else {
      use_rgb_cam2 = false;
  }
  Uc.memcpy (out_cam, rgb_cam, Uc.sizeof(out_cam));
  raw_color |= (colors == 1 || document_mode!=0 ||	output_color < 1 || output_color > 5)?1:0;
  if (raw_color == 0) {
    oprof.assign( CTOJ.calloc (phead.at(0), 1));
    merror (oprof, "convert_to_rgb()");
    CTOJ.memcpy (oprof, phead, CTOJ.sizeof(phead));
    if (output_color == 5) oprof.at(4, oprof.at(5));
    oprof.at(0, 132 + 12*pbody.at(0));
    for (i=0; i < pbody.at(0); i++) {
      oprof.at(oprof.at(0)/4, i!=0 ? (i > 1 ? 0x58595a20 : 0x64657363) : 0x74657874);
      pbody.at(i*3+2, oprof.at(0));
      oprof.at(0, oprof.at(0)+ (pbody.at(i*3+3) + 3) & -4);
    }
    CTOJ.memcpy (oprof.plus(32), pbody, CTOJ.sizeof(pbody));
    oprof.at(pbody.at(5)/4+2, Uc.strlen(name[output_color-1]) + 1);
    CTOJ.memcpy (/*(char *)*/oprof.toBytePtr().plus(pbody.at(8)+8), pwhite, CTOJ.sizeof(pwhite));
    if (output_bps == 8)
if(SRGB_GAMMA)
      pcurve.at(3, 0x2330000);
else
      pcurve.at(3, 0x1f00000);

    for (i=4; i < 7; i++)
      CTOJ.memcpy ( /*(char *)*/oprof.toBytePtr().plus(pbody.at(i*3+2)), pcurve, CTOJ.sizeof(pcurve));
    pseudoinverse ( /*(double (*)[3])*/ out_rgb[output_color-1], inverse, 3);
    for (i=0; i < 3; i++)
      for (j=0; j < 3; j++) {
	for (num = k=0; k < 3; k++)
	  num += xyzd50_srgb[i][k] * inverse[j][k];
        oprof.at( pbody.at(j*3+23)/4+i+2, (int)(num * 0x10000 + 0.5));
      }
    for (i=0; i < phead.at(0)/4; i++)
      oprof.at(i, CTOJ.htonl(oprof.at(i)));
    CTOJ.strcpy ( /*(char *)*/oprof.toBytePtr().plus(pbody.at(2)+8), "auto-generated by dcraw");
    CTOJ.strcpy ( /*(char *)*/oprof.toBytePtr().plus(pbody.at(5)+12), name[output_color-1]);
    for (i=0; i < 3; i++)
      for (j=0; j < colors; j++)
	for (out_cam[i][j] = k=0; k < 3; k++) {
            if ( use_rgb_cam2)
        	out_cam[i][j] += out_rgb[output_color-1][i][k] * rgb_cam2[k][j];
            else
                out_cam[i][j] += out_rgb[output_color-1][i][k] * rgb_cam[k][j];
        }
  }
  if (verbose != 0)
    System.err.printf ( raw_color!=0 ? "Building histograms...\n" :
	"Converting to %s colorspace...\n", name[output_color-1]);

  mix_green = (rgb_cam[1][1] == rgb_cam[1][3] ? 1: 0);
  //Uc.memset (histogram, 0, Uc.sizeof(histogram));
  
  for (img=image.toShortPtr(0), row=0; row < height; row++) {
    for (col=0; col < width; col++, img.add(4)) {
      if (raw_color == 0) {
	out[0] = out[1] = out[2] = 0;
	for (c=0; c < colors; c++) {
	  out[0] += out_cam[0][c] * CTOJ.toUnsigned(img.at(c));
	  out[1] += out_cam[1][c] * CTOJ.toUnsigned(img.at(c));
	  out[2] += out_cam[2][c] * CTOJ.toUnsigned(img.at(c));
	}
	for (c=0; c < 3; c++) img.at(c, (short)CLIP((int) out[c]));
      }
      else if (document_mode != 0)
	img.at(0, img.at((int)FC(row,col)));
      else if (mix_green != 0)
	img.at(1, (short)((CTOJ.toUnsigned(img.at(1)) + CTOJ.toUnsigned(img.at(3))) >> 1));
      for (c=0; c < colors; c++) histogram[c][CTOJ.toUnsigned(img.at(c)) >> 3]++;
    }
    Thread.yield();
  }
  if (colors == 4 && (output_color!=0 || mix_green!=0)) colors = 3;
  if (document_mode!=0 && filters!=0) colors = 1;
  img.nullify();
}

void  fuji_rotate()
{
  int i, wide, high, row, col;
  double step;
  double r, c, fr, fc;
  int ur, uc;
  
  NewShortArrayPtr img = new NewShortArrayPtr(4);
  NewShortArrayPtr pix = new NewShortArrayPtr(4);


  if ( fuji_width == 0) return;
  fuji_width = (fuji_width - 1 + shrink) >> shrink;
  step = Math.sqrt(0.5);
  wide = (int)(fuji_width / step);
  high = (int)( (height - fuji_width) / step);
  img.assign(CTOJ.callocShort (wide*high, 8));

  //img = (ushort (*)[4]) calloc (wide*high, sizeof *img);
  //merror (img, "fuji_rotate()");

  for (row=0; row < high; row++)
    for (col=0; col < wide; col++) {
        r = fuji_width + (row-col)*step;
      ur =  (int)r;
      c = (row+col)*step;
      uc = (int) c;
      if (ur > height-2 || uc > width-2) continue;
      fr = r - ur;
      fc = c - uc;
      pix.assign( image.plus( ur*width + uc));
      for (i=0; i < colors; i++)
	img.at(row*wide+col,i, ( short) (
                (pix.uat(    0,i)*(1-fc) + pix.uat(      1,i)*fc) * (1-fr) +
	  (pix.uat(width,i)*(1-fc) + pix.uat(width+1,i)*fc) * fr
          ));
    }
  //free (image);
  width  = wide;
  height = high;
  image.assign( img);
  fuji_width = 0;
}
/*
void CLASS stretch()
{
  ushort newdim, (*img)[4], *pix0, *pix1;
  int row, col, c;
  double rc, frac;

  if (pixel_aspect == 1) return;
  if (verbose) fprintf (stderr,_("Stretching the image...\n"));
  if (pixel_aspect < 1) {
    newdim = height / pixel_aspect + 0.5;
    img = (ushort (*)[4]) calloc (width*newdim, sizeof *img);
    merror (img, "stretch()");
    for (rc=row=0; row < newdim; row++, rc+=pixel_aspect) {
      frac = rc - (c = rc);
      pix0 = pix1 = image[c*width];
      if (c+1 < height) pix1 += width*4;
      for (col=0; col < width; col++, pix0+=4, pix1+=4)
	FORCC img[row*width+col][c] = pix0[c]*(1-frac) + pix1[c]*frac + 0.5;
    }
    height = newdim;
  } else {
    newdim = width * pixel_aspect + 0.5;
    img = (ushort (*)[4]) calloc (height*newdim, sizeof *img);
    merror (img, "stretch()");
    for (rc=col=0; col < newdim; col++, rc+=1/pixel_aspect) {
      frac = rc - (c = rc);
      pix0 = pix1 = image[c];
      if (c+1 < width) pix1 += 4;
      for (row=0; row < height; row++, pix0+=width*4, pix1+=width*4)
	FORCC img[row*newdim+col][c] = pix0[c]*(1-frac) + pix1[c]*frac + 0.5;
    }
    width = newdim;
  }
  free (image);
  image = img;
}
*/
int  flip_index (int row, int col)
{
  if ((flip & 4) != 0){
      int buff = col;
      col = row;
      row = buff;
  }
  if ((flip & 2)!=0) row = iheight - 1 - row;
  if ((flip & 1)!=0) col = iwidth  - 1 - col;
  return row * iwidth + col;
}

public void  gamma_lut (byte[] lut/*[0x10000]*/)
{
  int perc, c, val, total, i;
  float white=0, r;

//  perc = (int)(width * height * 0.01f);		/* 99th percentile white point */
  perc = (int)(width * height * 0.002f);		/* 1/500eme point blanc */
  if (fuji_width!=0) perc /= 2;
  if (highlight!=0) perc = 0;
  for (c=0; c < colors; c++) {
    for (val=0x2000, total=0; --val > 32; )
      if ((total += histogram[c][val]) > perc) break;
    if (white < (val+1)) white = val+1;
  }
  gamma_curve (gamm[0], gamm[1], 2, (int)((white * 8)/bright));
  
  white *= 8 / bright;
  for (i=0; i < 0x10000; i++) {
    r = i / white;
    val = (int)( 256.0 * ( use_gamma==0 ? r : r <= 0.018 ? r*4.5 : Math.pow(r,0.45)*1.099-0.099 ));
    if (val > 255) 
        val = 255;
    lut[i] = (byte)val;
  }
}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        String[] c_args = new String[args.length+2];
        c_args[0] = "dcraw";
        for ( int i=0; i< args.length; i++)
            c_args[i+1] = args[i];
        
        RawReader main = new RawReader(null);
        
        int ret_code = main.main1_( args.length+1, c_args);
        ret_code = main.main2_(0, 0, 0, -1,1,4);
        System.err.printf("Return code: %d", ret_code);
    }
    
    public void prepare() {
        hasFinished = false;
//        client.addRunningTask();
                
        Vector<String> args = new Vector<String>();

        if ( !auto_wb) {
        args.add("-r");
         
        // daylight A640        
        /*
        args[1] = "1.0";
        args[2] = "0.55";
        args[3] = "1.15";
        args[4] = "0.55";
         */
        ///* tungstene A640
        
        args.add(Float.toString(red));
        args.add(Float.toString(green));
        args.add(Float.toString(blue));
        args.add(Float.toString(green));
        }
        //*/
        
        args.add("-w");
        args.add("-o");
        args.add("1");
        args.add("-T");
        args.add("-q");
        args.add("4");//"0";
        
        args.add("-k");
        args.add("5");
        
        args.add("-g");
        args.add("2.4");
        args.add("12.92");
        
        args.add(rawPhoto.getPath());
        
        String[] c_args = new String[args.size()+2];
        c_args[0] = "dcraw";
        for ( int i=0; i< args.size(); i++)
            c_args[i+1] = args.get(i);
        
        int ret_code = main1_( args.size()+1, c_args);
        //client.increment(getMemoryConsumption());
        System.err.printf("Return code: %d\n", ret_code);
        isReadyToRun = true;
    }
    public void go() {
        isReadyToRun = false;
         client.addRunningTask();
         System.out.println("Go, Raw Index = "+rawIndex+", Priority = " + priority);
            discarded = false;
    Thread tr = new Thread(this);
        tr.setPriority(priority);
        tr.start();
    }

    public synchronized boolean isReadyToRun() {
        return isReadyToRun;
    }
     public void run()   {
        long start = System.currentTimeMillis();
           try {
               this.main2_(0, 0, 0, -1,1,4);
        switch( sharpMode) {
            case SHARP_9:
            loadedImage = convertRawToImageSharp9(false);
            break;
            case SHARP_16:
            loadedImage = convertRawToImageSharp16_DCRAW_9_12(false,loadedImage);
            break;
        }
        //System.out.println("repere 100");
        invalidateLoadedImageHighLight();
        //System.out.println("repere 101");

        hasFinished = true;

           } catch ( OutOfMemoryError e) {
               //JOptionPane.showMessageDialog(null, "Veuillez utiliser l'option \"-Xmx1500m\" pour lancer JRaw. Pour cela, utiliser le fichier .bat fourni.");
                System.out.printf("Out of Memory Error, index = %d\n", rawIndex);
                e.printStackTrace();
               client.memoryFull(rawIndex);
           } catch ( ArrayIndexOutOfBoundsException e) {
               JOptionPane.showMessageDialog(null, "Erreur interne pour le fichier: "+rawPhoto.getPath());
           }
        //System.out.println("repere 102");
        
        if ( discarded)
            return;
        
        //System.out.println("repere 103");
        client.removeRunningTask();
        //System.out.println("repere 104");
        client.imageIsLoaded(rawIndex, this);
        //System.out.println("repere 105");
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("Temps de calcul en sec: %f\n", elapsed/1000.);
    }
    
    public int main1_(int argc, String args[]) {
        String[] argv = args;
        
        init_tiffIfd();
        
  int arg, status=0, user_flip=-1, user_sat=-1, user_qual=-1;
  int timestamp_only=0, thumbnail_only=0, identify_only=0, write_to_stdout=0;
  int half_size=0, use_fuji_rotate=1, quality, i, c;
  char opt;
  BytePtr cp = new BytePtr();
  BytePtr sp = new BytePtr();
  CharPtr write_ext;

  if (argc == 1)
  {
    System.err.printf ( "\nRaw Photo Decoder \"dcraw\" v8.41");
    System.err.printf(
    "\nby Dave Coffin, dcoffin a cybercom o net");
    System.err.printf(
    "\n\nUsage:  %s [options] file1 file2 ...\n", argv[0]);
    System.err.printf(
    "\nValid options:");
    System.err.printf(
    "\n-v        Print verbose messages");
    System.err.printf(
    "\n-c        Write image data to standard output");
    System.err.printf(
    "\n-e        Extract embedded thumbnail image");
    System.err.printf(
    "\n-i        Identify files without decoding them");
    System.err.printf(
    "\n-z        Change file dates to camera timestamp");
    System.err.printf(
    "\n-a        Use automatic white balance");
    System.err.printf(
    "\n-w        Use camera white balance, if possible");
    System.err.printf(
    "\n-r <nums> Set raw white balance (four values required)");
    System.err.printf(
    "\n-b <num>  Adjust brightness (default = 1.0)");
    System.err.printf(
    "\n-k <num>  Set black point");
    System.err.printf(
    "\n-H [0-9]  Highlight mode (0=clip, 1=no clip, 2+=recover)");
    System.err.printf(
    "\n-t [0-7]  Flip image (0=none, 3=180, 5=90CCW, 6=90CW)");
    System.err.printf(
    "\n-o [0-5]  Output colorspace (raw,sRGB,Adobe,Wide,ProPhoto,XYZ)");
    System.err.printf(
    "\n-d        Document Mode (no color, no interpolation)");
    System.err.printf(
    "\n-D        Document Mode without scaling (totally raw)");
    System.err.printf(
    "\n-q [0-3]  Set the interpolation quality");
    System.err.printf(
    "\n-h        Half-size color image (twice as fast as \"-q 0\")");
    System.err.printf(
    "\n-f        Interpolate RGGB as four colors");
    System.err.printf(
    "\n-B <domain> <range>  Apply bilateral filter to reduce noise");
    System.err.printf(
    "\n-j        Show Fuji Super CCD images tilted 45 degrees");
    System.err.printf(
    "\n-s        Use secondary pixels (Fuji Super CCD SR only)");
    System.err.printf(
    "\n-4        Write 16-bit linear instead of 8-bit with gamma");
    System.err.printf(
    "\n-T        Write TIFF instead of PPM");
    System.err.printf(
    "\n\n");
    return 1;
  }

  argv[argc] = "";
  for (arg=1; argv[arg].charAt(0) == '-'; ) {
    opt = argv[arg++].charAt(1);
    if (!cp.assign(CTOJ.strchr (sp.copy("BbrktqH"), opt)).isNull()) {
        int[] buffer = { 2,1,4,1,1,1,1};
      for (i=0; i < buffer[cp.minus(sp)]; i++)
	if (!Uc.isdigit(argv[arg+i].charAt(0))) {
	  System.err.printf( "Non-numeric argument to \"-%c\"\n", opt);
	  return 1;
	}
    }
    switch (opt)
    {
      case 'B':  sigma_d     = (float)Uc.atof(argv[arg++]);
		 sigma_r     = (float)Uc.atof(argv[arg++]);  break;
      case 'b':  bright      = (float)Uc.atof(argv[arg++]);  break;
      case 'r':
	   for (c=0; c < 4; c++) user_mul[c] = (float)Uc.atof(argv[arg++]);  break;
      case 'g':  gamm[0] =     Uc.atof(argv[arg++]);
		 gamm[1] =     Uc.atof(argv[arg++]);
		 if (gamm[0] != 0.0) gamm[0] = 1/gamm[0]; break;
      case 'k':  user_black  = Uc.atoi(argv[arg++]);  break;
      case 't':  user_flip   = Uc.atoi(argv[arg++]);  break;
      case 'q':  user_qual   = Uc.atoi(argv[arg++]);  break;
      case 'H':  highlight   = Uc.atoi(argv[arg++]);  break;
      case 'o':
	if (Uc.isdigit(argv[arg].charAt(0)) && argv[arg].length() == 1/* .charAt(1)==0*/)
	  output_color = Uc.atoi(argv[arg++]);

	break;
      case 'z':  timestamp_only    = 1;  break;
      case 'e':  thumbnail_only    = 1;  break;
      case 'i':  identify_only     = 1;  break;
      case 'c':  write_to_stdout   = 1;  break;
      case 'v':  verbose           = 1;  break;
      case 'h':  half_size         = 1;		/* "-h" implies "-f" */
      case 'f':  four_color_rgb    = 1;  break;
      case 'd':  document_mode     = 1;  break;
      case 'D':  document_mode     = 2;  break;
      case 'a':  use_auto_wb       = 1;  break;
      case 'w':  use_camera_wb     = 1;  break;
      case 'j':  use_fuji_rotate   = 0;  break;
      case 's':  use_secondary     = 1;  break;
      case 'm':  output_color      = 0;  break;
      case 'T':  output_tiff       = 1;  break;
      case '4':  output_bps       = 16;  break;
      default:
	System.err.printf( "Unknown option \"-%c\".\n", opt);
	return 1;
    }
  }
  if (arg == argc) {
    System.err.printf( "No files to process.\n");
    return 1;
  }
  if (write_to_stdout != 0) {
//    if (isatty(1)) {
//      System.err.printf( "Will not write an image to the terminal!\n");
//      return 1;
//    }
  }
  //for ( ; arg < argc; arg++) {
    status = 1;
    //image = new NewShortArrayPtr(4);
    oprof = new IntPtr();
//    if (setjmp (failure)) {
//      if (fileno(ifp) > 2) fclose(ifp);
//      if (fileno(ofp) > 2) fclose(ofp);
//      if (image) CTOJ.free (image);
//      status = 1;
//      continue;
//    }
    ifname = argv[arg];
    if ((ifp = CTOJ.fopen (ifname.toString(), "rb"))== null) {
      //perror (ifname);
      //continue;
    }
    identify();
    memorySize = (int)(6.5e-5*width*height)+50;
    return 0;
        }

    public int main2_(int timestamp_only, int identify_only, int thumbnail_only, int user_flip, int use_fuji_rotate, int quality) {
  CharPtr ofname = null;
  Object ofp = System.out;
    int status = is_raw == 0 ?1:0;
    if (timestamp_only != 0) {
      if ((status = (timestamp ==0 ? 1:0)) != 0)
	System.err.printf( "%s has no timestamp.\n", ifname);
      else if (identify_only != 0)
	System.out.printf ("%10ld%10d %s\n", (long) timestamp, shot_order, ifname);
      else {
	if (verbose != 0)
	  System.err.printf( "%s time set to %d.\n", ifname, (int) timestamp);
//	ut.actime = ut.modtime = timestamp;
      }
      CTOJ.fclose(ifp);
      return -1;
    }
    //write_fun = & write_ppm_tiff;
    if (thumbnail_only != 0) {
      if ((status = thumb_offset==0 ?1:0) != 0) {
	System.err.printf( "%s has no thumbnail.\n", ifname);
      CTOJ.fclose(ifp);
      return -1;
      } else if (thumb_load_raw != null) {
	load_raw = thumb_load_raw;
	data_offset = thumb_offset;
	height = thumb_height;
	width  = thumb_width;
	filters = 0;
      }
    }
    if (identify_only != 0 && verbose != 0 && make.charAt(0)!=0) {
      System.out.printf ("\nFilename: %s\n", ifname);
      //System.out.printf ("Timestamp: %s", ctime(timestamp));
      System.out.printf ("Camera: %s %s\n", make, model);
      System.out.printf ("ISO speed: %d\n", (int) iso_speed);
      System.out.printf ("Shutter: ");
      if (shutter > 0 && shutter < 1){
          System.out.printf("1/");
	shutter = 1 / shutter;
      }
      System.out.printf ("%0.1f sec\n", shutter);
      System.out.printf ("Aperture: f/%0.1f\n", aperture);
      System.out.printf ("Focal Length: %0.1f mm\n", focal_len);
      System.out.printf ("Secondary pixels: %s\n", fuji_secondary!=0 ? "yes":"no");
      System.out.printf ("Embedded ICC profile: %s\n", profile_length!=0 ? "yes":"no");
      System.out.printf ("Decodable with dcraw: %s\n", is_raw!=0 ? "yes":"no");
      if (thumb_offset !=0)
	System.out.printf ("Thumb size:  %4d x %d\n", thumb_width, thumb_height);
      System.out.printf ("Full size:   %4d x %d\n", raw_width, raw_height);
    } else if (is_raw == 0)
      System.err.printf( "Cannot decode %s\n", ifname);
    if (user_flip >= 0)
      flip = user_flip;
    switch ((flip+3600) % 360) {
      case 270:  flip = 5;  break;
      case 180:  flip = 3;  break;
      case  90:  flip = 6;
    }
    shrink = (half_size!=0 && filters!=0) ?1:0;
    iheight = (height + shrink) >> shrink;
    iwidth  = (width  + shrink) >> shrink;
    if (identify_only!=0) {
      if (verbose!=0) {
	if (fuji_width!=0 && use_fuji_rotate!=0) {
	  fuji_width = (fuji_width - 1 + shrink) >> shrink;
	  iwidth = (int)(fuji_width / Math.sqrt(0.5));
	  iheight = (int)((iheight - fuji_width) / Math.sqrt(0.5));
	}
	if ((flip & 4)!=0) {
            int buff = iwidth;
            iwidth = iheight;
            iheight = buff;
        }
	System.out.printf ("Image size:  %4d x %d\n", width, height);
	System.out.printf ("Output size: %4d x %d\n", iwidth, iheight);
	System.out.printf ("Raw colors: %d", colors);
//	if (filters!=0) {
//	  System.out.printf ("\nFilter pattern: ");
//	  if ( cdesc.charAt(3)==0) cdesc.charAt(3, 'G');
//	  for (i=0; i < 16; i++)
//	    putchar (cdesc[fc(i >> 1,i & 1)]);
//	}
	System.out.printf ("\nDaylight multipliers:");
	for (int c=0; c < colors; c++) System.out.printf (" %f", pre_mul[c]);
	if (cam_mul[0] > 0) {
	  System.out.printf ("\nCamera multipliers:");
	  for (int c=0; c < 4; c++) System.out.printf (" %f", cam_mul[c]);
	}
	System.out.print('\n');
      } else
	System.out.printf ("%s is a %s %s image.\n", ifname, make, model);
next:
      CTOJ.fclose(ifp);
      return -1;
    }
    if ( image.length()*2 != iheight*iwidth* 8/*sizeof *image*/ + meta_length) {
        image.assign(CTOJ.callocShort (iheight*iwidth* 8/*sizeof *image*/ + meta_length, 1));
    } else {
        int iiii=0;
    }
    //merror (image, "main()");
    //meta_data.assign(image.plus(iheight*iwidth));
    if (verbose != 0)
      System.err.printf(
	"Loading %s %s image from %s...\n", make, model, ifname);
    CTOJ.fseek (ifp, data_offset, CTOJ.SEEK_SET);
    load_raw.loadRaw(this);
    if (zero_is_bad) remove_zeroes();
    //bad_pixels();
    height = iheight;
    width  = iwidth;
    quality = 2 + fuji_width ==0 ? 1:0;
    //if (user_qual >= 0) quality = user_qual;
    if (user_black >= 0) black = user_black;
    //if (user_sat > 0) maximum = user_sat;
    //if (is_foveon != 0 && document_mode==0) foveon_interpolate();
    if ( is_foveon ==0 && document_mode < 2) scale_colors();
    //pre_interpolate();
    if (shrink !=0) filters = 0;
//    cam_to_cielab (null,null);
    if (filters !=0 && document_mode ==0) {
      if (quality == 0)
	lin_interpolate();
      else if (quality == 4) {
          if (  /*model.toString().equals("D40")
          || model.toString().equals("D50")
          || model.toString().equals("D70")
          || model.toString().equals("D70s")
          || model.toString().equals("D1")
          */ filters == 0x16161616l) {
              System.out.print("sinc_interpolate , filters = ");
              System.out.println(filters);
              System.out.println(model);
            sinc_interpolate();
              //lin_interpolate_yb();
          } else if (  /*model.toString().equals("PowerShot A720")
               ||   model.toString().equals("PowerShot A620")
               ||   model.toString().equals("PowerShot A640")
               ||   model.toString().equals("PowerShot G9")
               ||   model.toString().equals("PowerShot S30")
               || model.toString().equals("EOS 5D")
               || model.toString().equals("EOS 30D")
               || model.toString().equals("EOS 40D")
               || model.toString().equals("EOS 300D DIGITAL")
               || model.toString().equals("EOS 350D DIGITAL")
               || model.toString().equals("EOS 400D DIGITAL")
               || model.toString().equals("EOS 450D DIGITAL")
          || model.toString().equals("D200")
          || model.toString().equals("D300") */
            filters == 0x94949494l) {
              System.out.print("sinc_interpolate2 , filters = ");
              System.out.println(filters);
              System.out.println(model);
              sinc_interpolate2();
              //lin_interpolate_yb2();
          } else {
              System.out.print("lin_interpolate , filters = ");
              System.out.println(filters);
              System.out.println(model);
              lin_interpolate();
          }
      }
      else if (quality < 3 || colors > 3)
	   vng_interpolate();
//      else ahd_interpolate();
    }
    //if (sigma_d > 0 && sigma_r > 0) bilateral_filter();
    //if (!is_foveon && highlight > 1) recover_highlights();
    if (use_fuji_rotate!=0) fuji_rotate();
    convert_to_rgb();
    convert_sharp();
//    if (use_fuji_rotate != 0) stretch(); TODO : A faire pour D1X
    if (verbose !=0)
      System.err.printf( "Writing data to %s ...\n", ofname);
//    (*write_fun)(ofp);
    CTOJ.fclose(ifp);
    ifp = null;
    //System.gc();
    //System.runFinalization();
    if (ofp != System.out) CTOJ.fclose(ofp);
cleanup:
    if ( !oprof.isNull()) CTOJ.free(oprof);
    //CTOJ.free (ofname);
    //CTOJ.free (image);
  //}
  return status;
    }
    
        public BufferedImage convertRawToImage(boolean highlight) {
        int image_width;
        int image_height;
        int imageType;
        BufferedImage loadedImage;
        
        image_width = width;
        image_height = height;
        imageType = BufferedImage.TYPE_INT_RGB;
        
        int finalOrientation = NORMAL;
        
        if ( tiff_flip == 5)
            finalOrientation = GAUCHE;
        if ( tiff_flip == 6)
            finalOrientation = DROIT;
        
        finalOrientation += orientation;
        
        if ( finalOrientation < GAUCHE)
            finalOrientation = GAUCHE;
        if ( finalOrientation > DROIT)
            finalOrientation = DROIT;
        
        if ( finalOrientation != NORMAL) {
            loadedImage = new BufferedImage(image_height, image_width, imageType);
        }
        else {
            loadedImage = new BufferedImage(image_width, image_height, imageType);
        }
        
        byte[] lut = new byte[0x10000];
        gamma_lut(lut);
  
        int indiceb = 2;
        int indicev = 1;
        int indicer = 0;
        for ( int x=0; x < image_width; x++)
            for( int y=0; y < image_height; y++) {
                int indice1 = y*image_width+ x;
            
                short bleu = CTOJ.toUnsigned(lut[CTOJ.toUnsigned(image.at(indice1, indiceb))]);
                short vert = CTOJ.toUnsigned(lut[CTOJ.toUnsigned(image.at(indice1, indicev))]);
                short rouge = CTOJ.toUnsigned(lut[CTOJ.toUnsigned(image.at(indice1, indicer))]);
                
                if ( highlight) {
                    if ( bleu >252)
                        bleu = 0;
                    if ( vert > 252)
                        vert = 0;
                    if ( rouge > 252)
                        rouge = 0;
                }
//                short bleu = (short)(CTOJ.toUnsigned(main.image.at(indice1, indiceb))/256);
//                short vert = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicev))/256);
//                short rouge = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicer))/256);
                
                int rgb = bleu + (vert << 8) + (rouge << 16);
                if ( finalOrientation ==  GAUCHE)
                        loadedImage.setRGB( y, image_width - x - 1, rgb);
                else if ( finalOrientation ==  DROIT)
                        loadedImage.setRGB( image_height - y - 1, x, rgb);
                else {
                        loadedImage.setRGB( x, y, rgb);
                }
            }
        
        return loadedImage;
    }

        public BufferedImage convertRawToImageSharp(boolean highlight) {
        int image_width;
        int image_height;
        int imageType;
        BufferedImage loadedImage;
        
        image_width = width;
        image_height = height;
        imageType = BufferedImage.TYPE_INT_RGB;
        
        int finalOrientation = NORMAL;
        
        if ( tiff_flip == 5)
            finalOrientation = GAUCHE;
        if ( tiff_flip == 6)
            finalOrientation = DROIT;
        
        finalOrientation += orientation;
        
        if ( finalOrientation < GAUCHE)
            finalOrientation = GAUCHE;
        if ( finalOrientation > DROIT)
            finalOrientation = DROIT;
        
        if ( finalOrientation != NORMAL) {
            loadedImage = new BufferedImage(image_height, image_width, imageType);
        }
        else {
            loadedImage = new BufferedImage(image_width, image_height, imageType);
        }
        
        byte[] lut = new byte[0x10000];
        gamma_lut(lut);
  
        int indiceb = 2;
        int indicev = 1;
        int indicer = 0;
        for ( int x=1; x < image_width-1; x++)
            for( int y=1; y < image_height-1; y++) {
                int indice1 = y*image_width+ x;
                int indiceUp = (y-1)*image_width+ x;
                int indiceDown = (y+1)*image_width+x;
                int indiceLeft = indice1-1;
                int indiceRight = indice1+1;
                
                int bleu1 = CTOJ.toUnsigned(image.at(indice1, indiceb));
                int vert1 = CTOJ.toUnsigned(image.at(indice1, indicev));
                int rouge1 = CTOJ.toUnsigned(image.at(indice1, indicer));
            
                int bleuUp = CTOJ.toUnsigned(image.at(indiceUp, indiceb));
                int vertUp = CTOJ.toUnsigned(image.at(indiceUp, indicev));
                int rougeUp = CTOJ.toUnsigned(image.at(indiceUp, indicer));
            
                int bleuDown = CTOJ.toUnsigned(image.at(indiceDown, indiceb));
                int vertDown = CTOJ.toUnsigned(image.at(indiceDown, indicev));
                int rougeDown = CTOJ.toUnsigned(image.at(indiceDown, indicer));
            
                int bleuLeft = CTOJ.toUnsigned(image.at(indiceLeft, indiceb));
                int vertLeft = CTOJ.toUnsigned(image.at(indiceLeft, indicev));
                int rougeLeft = CTOJ.toUnsigned(image.at(indiceLeft, indicer));
            
                int bleuRight = CTOJ.toUnsigned(image.at(indiceRight, indiceb));
                int vertRight = CTOJ.toUnsigned(image.at(indiceRight, indicev));
                int rougeRight = CTOJ.toUnsigned(image.at(indiceRight, indicer));
                
                int bleu16 = (int)(1.4*bleu1 - 0.1*( bleuUp+bleuDown+bleuLeft+bleuRight));
                int vert16 = (int)(1.4*vert1 - 0.1*( vertUp+vertDown+vertLeft+vertRight));
                int rouge16 = (int)(1.4*rouge1 - 0.1*( rougeUp+rougeDown+rougeLeft+rougeRight));
            
                short bleu = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, bleu16), 0xffff)]);
                short vert = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, vert16), 0xffff)]);
                short rouge = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, rouge16), 0xffff)]);
                
                if ( highlight) {
                    if ( bleu >252)
                        bleu = 0;
                    if ( vert > 252)
                        vert = 0;
                    if ( rouge > 252)
                        rouge = 0;
                }
//                short bleu = (short)(CTOJ.toUnsigned(main.image.at(indice1, indiceb))/256);
//                short vert = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicev))/256);
//                short rouge = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicer))/256);
                
                int rgb = bleu + (vert << 8) + (rouge << 16);
                if ( finalOrientation ==  GAUCHE)
                        loadedImage.setRGB( y, image_width - x - 1, rgb);
                else if ( finalOrientation ==  DROIT)
                        loadedImage.setRGB( image_height - y - 1, x, rgb);
                else {
                        loadedImage.setRGB( x, y, rgb);
                }
            }
        
        return loadedImage;
    }

        public BufferedImage convertRawToImageSharp9(boolean highlight) {
        int image_width;
        int image_height;
        int imageType;
        BufferedImage loadedImage;
        
        image_width = width;
        image_height = height;
        imageType = BufferedImage.TYPE_INT_RGB;
        
        int finalOrientation = NORMAL;
        
        if ( tiff_flip == 5)
            finalOrientation = GAUCHE;
        if ( tiff_flip == 6)
            finalOrientation = DROIT;
        
        finalOrientation += orientation;
        
        if ( finalOrientation < GAUCHE)
            finalOrientation = GAUCHE;
        if ( finalOrientation > DROIT)
            finalOrientation = DROIT;
        
        if ( finalOrientation != NORMAL) {
            loadedImage = new BufferedImage(image_height, image_width, imageType);
        }
        else {
            loadedImage = new BufferedImage(image_width, image_height, imageType);
        }
        
        byte[] lut = new byte[0x10000];
        gamma_lut(lut);
  
        int indiceb = 2;
        int indicev = 1;
        int indicer = 0;
        
        for ( int x=1; x < image_width-1; x++)
            for( int y=1; y < image_height-1; y++) {
                
                int rouge_sub =0, vert_sub=0, bleu_sub = 0;
                int rouge_plus =0, vert_plus=0, bleu_plus = 0;
                
                for ( int i=-1; i<=1; i++)
                    for ( int j=-1; j<=1; j++) {
                        int indice1 =  (y+j)*image_width+ x+i;
                        
                        if(( i==-1 && j==-1 ) 
                                || ( i==-1 && j==1 ) 
                                || ( i==1 && j==-1 ) 
                                || ( i==1 && j==1 ) 
                                )
                        {
                            rouge_sub += CTOJ.toUnsigned(image.at(indice1, indicer));
                            vert_sub += CTOJ.toUnsigned(image.at(indice1, indicev));
                            bleu_sub += CTOJ.toUnsigned(image.at(indice1, indiceb));
                        }
                        else {
                            rouge_plus += CTOJ.toUnsigned(image.at(indice1, indicer));
                            vert_plus += CTOJ.toUnsigned(image.at(indice1, indicev));
                            bleu_plus += CTOJ.toUnsigned(image.at(indice1, indiceb));                            
                        }
                    }
                int indice1 =  y*image_width+ x;
                
                int rouge16 = (int)(CTOJ.toUnsigned(image.at(indice1, indicer)) + 0.24*rouge_plus - 0.3*rouge_sub);
                int vert16 = (int)(CTOJ.toUnsigned(image.at(indice1, indicev)) + 0.24*vert_plus - 0.3*vert_sub);
                int bleu16 = (int)(CTOJ.toUnsigned(image.at(indice1, indiceb)) + 0.24*bleu_plus - 0.3*bleu_sub);
            
                short bleu = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, bleu16), 0xffff)]);
                short vert = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, vert16), 0xffff)]);
                short rouge = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, rouge16), 0xffff)]);
                
                if ( highlight) {
                    if ( bleu >252)
                        bleu = 0;
                    if ( vert > 252)
                        vert = 0;
                    if ( rouge > 252)
                        rouge = 0;
                }
//                short bleu = (short)(CTOJ.toUnsigned(main.image.at(indice1, indiceb))/256);
//                short vert = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicev))/256);
//                short rouge = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicer))/256);
                
                int rgb = bleu + (vert << 8) + (rouge << 16);
                if ( finalOrientation ==  GAUCHE)
                        loadedImage.setRGB( y, image_width - x - 1, rgb);
                else if ( finalOrientation ==  DROIT)
                        loadedImage.setRGB( image_height - y - 1, x, rgb);
                else {
                        loadedImage.setRGB( x, y, rgb);
                }
            }
         
        for ( int x=0; x < image_width; x++)
            for( int y=0; y < image_height; y++) {
                
                if( (y != 0 && y != image_height-1) && ( x!=0 && x != image_width-1 ) )
                    continue;
                
                int indice1 =  y*image_width+ x;
                
                int rouge16 = (int)(CTOJ.toUnsigned(image.at(indice1, indicer)));
                int vert16 = (int)(CTOJ.toUnsigned(image.at(indice1, indicev)));
                int bleu16 = (int)(CTOJ.toUnsigned(image.at(indice1, indiceb)));
            
                short bleu = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, bleu16), 0xffff)]);
                short vert = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, vert16), 0xffff)]);
                short rouge = CTOJ.toUnsigned(lut[Math.min( Math.max( 0, rouge16), 0xffff)]);
                
                if ( highlight) {
                    if ( bleu >252)
                        bleu = 0;
                    if ( vert > 252)
                        vert = 0;
                    if ( rouge > 252)
                        rouge = 0;
                }
//                short bleu = (short)(CTOJ.toUnsigned(main.image.at(indice1, indiceb))/256);
//                short vert = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicev))/256);
//                short rouge = (short)(CTOJ.toUnsigned(main.image.at(indice1, indicer))/256);
                
                int rgb = bleu + (vert << 8) + (rouge << 16);
                if ( finalOrientation ==  GAUCHE)
                        loadedImage.setRGB( y, image_width - x - 1, rgb);
                else if ( finalOrientation ==  DROIT)
                        loadedImage.setRGB( image_height - y - 1, x, rgb);
                else {
                        loadedImage.setRGB( x, y, rgb);
                }
            }
        
        return loadedImage;
    }

        public BufferedImage convertRawToImageSharp16(boolean highlightArg, BufferedImage reusableImage) {
        int image_width;
        int image_height;
        int imageType;
        BufferedImage loadedImageNew = null;
        
        image_width = width;
        image_height = height;
        imageType = BufferedImage.TYPE_INT_RGB;
        
        int finalOrientation = NORMAL;
        
        if ( tiff_flip == 5)
            finalOrientation = GAUCHE;
        if ( tiff_flip == 6)
            finalOrientation = DROIT;
        
        finalOrientation += orientation;
        
        if ( finalOrientation < GAUCHE)
            finalOrientation = GAUCHE;
        if ( finalOrientation > DROIT)
            finalOrientation = DROIT;
        
        byte[] lut = new byte[0x10000];
        gamma_lut(lut);
        
        int nb_cores = 1;

        ImageThread[] Imt = new ImageThread[nb_cores];

        //WritableRaster wr = loadedImageNew.getRaster();

        int image_out_width = 0;
        int image_out_height = 0;

        if ( finalOrientation ==  RawReader.GAUCHE) {
            image_out_width = image_height-3;
            image_out_height = image_width-3;
        }
        else if ( finalOrientation ==  RawReader.DROIT) {
            image_out_width = image_height-3;
            image_out_height = image_width-3;
        }
        else {
            image_out_width = image_width-3;
            image_out_height = image_height-3;
        }

        long start = System.nanoTime();
/*
        int[] pixels = new int[image_out_width*image_out_height];

        DataBufferInt dbi = new DataBufferInt(pixels,image_out_width*image_out_height);
        int red = 0x00ff0000;
        int green = 0x0000ff00;
        int blue = 0x000000ff;
        int[] bitMask = new int[3];
        bitMask[0] = red;
        bitMask[1] = green;
        bitMask[2] = blue;
        SampleModel sm =new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,image_out_width,image_out_height,bitMask);
        WritableRaster wr = Raster.createWritableRaster(sm,dbi, new Point());

        loadedImageNew = new BufferedImage(new DirectColorModel(32,red,green,blue),wr,true,null);
 *
 */

        if (    reusableImage != null
                && reusableImage.getWidth() == image_out_width
                && reusableImage.getHeight() == image_out_height
                ) {
            loadedImageNew = reusableImage;
        }
        else {
            loadedImageNew = new BufferedImage(image_out_width, image_out_height, imageType);
    }
        DataBufferInt dbi = (DataBufferInt)loadedImageNew.getRaster().getDataBuffer();
        int[] pixels = dbi.getData();
/*
        loadedImageNew.getRaster().setDataElements( 0, 0, image_out_width, image_out_height, pixels);
*/
        for ( int no_core = 0; no_core < nb_cores; no_core++) {
            Imt[no_core] = new ImageThread( image_width, image_height, lut, highlightArg, finalOrientation, image_sharp, /*loadedImageNew*/pixels, nb_cores, no_core);
            Imt[no_core].setPriority(priority);
            Imt[no_core].start();
        }

        try {
            for ( int no_core = 0; no_core < nb_cores; no_core++) {
                Imt[no_core].join();
            }
        } catch (InterruptedException ex) {
            for ( int no_core = 0; no_core < nb_cores; no_core++) {
                Imt[no_core].interrupt();
            }
            return loadedImageNew;
        }

       long end = System.nanoTime();


       double nb_seconds = (end - start)/1.e9;
       System.out.printf("Delta time setPixels = %f sec\n", nb_seconds);
       pixels = null;
        return loadedImageNew;
    }

        public BufferedImage convertRawToImageSharp16_DCRAW_9_12(boolean highlightArg, BufferedImage reusableImage) {
        int image_width;
        int image_height;
        int imageType;
        BufferedImage loadedImageNew = null;
        
        image_width = width;
        image_height = height;
        imageType = BufferedImage.TYPE_INT_RGB;
        
        int finalOrientation = NORMAL;
        
        if ( tiff_flip == 5)
            finalOrientation = GAUCHE;
        if ( tiff_flip == 6)
            finalOrientation = DROIT;
        
        finalOrientation += orientation;
        
        if ( finalOrientation < GAUCHE)
            finalOrientation = GAUCHE;
        if ( finalOrientation > DROIT)
            finalOrientation = DROIT;
        
        byte[] lut = new byte[0x10000];
        gamma_lut(lut);
        
        int nb_cores = 1;

        ImageThreadDCRAW_9_12[] Imt = new ImageThreadDCRAW_9_12[nb_cores];

        //WritableRaster wr = loadedImageNew.getRaster();

        int image_out_width = 0;
        int image_out_height = 0;

        if ( finalOrientation ==  RawReader.GAUCHE) {
            image_out_width = image_height-3;
            image_out_height = image_width-3;
        }
        else if ( finalOrientation ==  RawReader.DROIT) {
            image_out_width = image_height-3;
            image_out_height = image_width-3;
        }
        else {
            image_out_width = image_width-3;
            image_out_height = image_height-3;
        }

        long start = System.nanoTime();
/*
        int[] pixels = new int[image_out_width*image_out_height];

        DataBufferInt dbi = new DataBufferInt(pixels,image_out_width*image_out_height);
        int red = 0x00ff0000;
        int green = 0x0000ff00;
        int blue = 0x000000ff;
        int[] bitMask = new int[3];
        bitMask[0] = red;
        bitMask[1] = green;
        bitMask[2] = blue;
        SampleModel sm =new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,image_out_width,image_out_height,bitMask);
        WritableRaster wr = Raster.createWritableRaster(sm,dbi, new Point());

        loadedImageNew = new BufferedImage(new DirectColorModel(32,red,green,blue),wr,true,null);
 *
 */

        if (    reusableImage != null
                && reusableImage.getWidth() == image_out_width
                && reusableImage.getHeight() == image_out_height
                ) {
            loadedImageNew = reusableImage;
        }
        else {
            loadedImageNew = new BufferedImage(image_out_width, image_out_height, imageType);
    }
        DataBufferInt dbi = (DataBufferInt)loadedImageNew.getRaster().getDataBuffer();
        int[] pixels = dbi.getData();
/*
        loadedImageNew.getRaster().setDataElements( 0, 0, image_out_width, image_out_height, pixels);
*/
        for ( int no_core = 0; no_core < nb_cores; no_core++) {
            Imt[no_core] = new ImageThreadDCRAW_9_12( image_width, image_height, new_curve, highlightArg, finalOrientation, image_sharp, /*loadedImageNew*/pixels, nb_cores, no_core);
            Imt[no_core].setPriority(priority);
            Imt[no_core].start();
        }

        try {
            for ( int no_core = 0; no_core < nb_cores; no_core++) {
                Imt[no_core].join();
            }
        } catch (InterruptedException ex) {
            for ( int no_core = 0; no_core < nb_cores; no_core++) {
                Imt[no_core].interrupt();
            }
            return loadedImageNew;
        }

       long end = System.nanoTime();


       double nb_seconds = (end - start)/1.e9;
       System.out.printf("Delta time setPixels = %f sec\n", nb_seconds);
       pixels = null;
        return loadedImageNew;
    }

    public void convert_sharp() {
        int image_width;
        int image_height;

        image_width = width;
        image_height = height;

        int finalOrientation = NORMAL;

        if ( tiff_flip == 5)
            finalOrientation = GAUCHE;
        if ( tiff_flip == 6)
            finalOrientation = DROIT;

        finalOrientation += orientation;

        if ( finalOrientation < GAUCHE)
            finalOrientation = GAUCHE;
        if ( finalOrientation > DROIT)
            finalOrientation = DROIT;
        image_sharp = new char[image_width-3][image_height-3][3];


        double plus = 0.75;
        double minus = (4*plus-1)/8;

        int indiceb = 2;
        int indicev = 1;
        int indicer = 0;

        int xstart = 1;
        int ystart = 1;
        int xend = image_width-2;
        int yend = image_height-2;


        for ( int x=xstart; x < xend; x++) {

            for( int y=ystart; y < yend; y++) {


                int rouge_sub =0, vert_sub=0, bleu_sub = 0;
                int rouge_plus =0, vert_plus=0, bleu_plus = 0;

                for ( int i=-1; i<=2; i++) {
                    int min = -1,max = 2;
                    if ( i==-1 || i==2) {
                        min = 0; max = 1;
                    }
                    for ( int j=min; j<=max; j++) {
                        int indice1 =  (y+j)*image_width+ x+i;

                        if( (i!=0&&i!=1) || (j!=0 && j!=1) ) {
                            rouge_sub += CTOJ.toUnsigned(image.at(indice1, indicer));
                            vert_sub += CTOJ.toUnsigned(image.at(indice1, indicev));
                            bleu_sub += CTOJ.toUnsigned(image.at(indice1, indiceb));
                        }
                        else {
                            rouge_plus += CTOJ.toUnsigned(image.at(indice1, indicer));
                            vert_plus += CTOJ.toUnsigned(image.at(indice1, indicev));
                            bleu_plus += CTOJ.toUnsigned(image.at(indice1, indiceb));
                        }
                    }
                }
                int rouge16 = (int)(plus*rouge_plus - minus*rouge_sub);
                int vert16 = (int)(plus*vert_plus - minus*vert_sub);
                int bleu16 = (int)(plus*bleu_plus - minus*bleu_sub);


                image_sharp[x-1][y-1][2] = (char)Math.min( Math.max( 0, bleu16), 0xffff);
                image_sharp[x-1][y-1][1] = (char)Math.min( Math.max( 0, vert16), 0xffff);
                image_sharp[x-1][y-1][0] = (char)Math.min( Math.max( 0, rouge16), 0xffff);

            }
            Thread.yield();
        }
    }
    /**
     *
     * @return consommation mémoire en méga-octets
     */
    public int getMemoryConsumption() {
        while ( memorySize == -1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(RawReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return memorySize;
    }

    public void setPriority( int priorityArg) {
        priority = priorityArg;
    }
    public void setRead() {
        hasBeenRead = true;
    }
    public boolean hasBeenRead() {
        return hasBeenRead;
    }
}
