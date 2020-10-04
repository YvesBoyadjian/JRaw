/*
 * MainApplication.java
 *
 * Created on 7 décembre 2007, 23:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package JRaw;

import JRaw.WhiteBalance.WhiteBalance;
import JRaw.WhiteBalance.WhiteBalanceFactory;
import JRaw.ihm.MainJFrame;
import dcraw.RawReader;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author YvesFabienne
 */
public class MainApplication implements ImageLoadedNotified,  KeyListener, Runnable{
    public static final float STOCKAGE_QUALITY = 0.92f;
    public static final float INTERNET_QUALITY = 0.5f;
    public static final float CONSTANT_SIZE_QUALITY = 500.0f;
    public static final float PNG_QUALITY = 1.0f;
    public static final float PNG_X2_QUALITY = 2.0f;
    public static final float PNG_X4_QUALITY = 4.0f;
    public static final float PNG_X8_QUALITY = 8.0f;

    JFileChooser chooser = new JFileChooser();
    JFileChooser filesChooser = new JFileChooser();
    //File selectedFile;
    public File[] allFilesInDirectory; // Liste de tous les fichiers dans le directory
    private ArrayList<RawReader> raws; // Contient les RR en mémoire
    private RawReader notStartedRaw;
    //private /*RawReader[]*/HardOrSoftReference[] raws;
    int previousIndex;
    private int displayedIndex;  // Index de l'image couramment affichée
    int nextDisplayedIndex; // Index de l'image dont l'affichage est demandé, -1 si rien demande
    File outputFile;
    //RawReader main;
    //BufferedImage fittedImage;
    public boolean isFitted;
    Cursor curs;
    long start;
    int currentBatchNumber;
    public boolean batchMode;
    public boolean disableSuivant;
    public boolean fullScreen;
    Dimension ancientSize;
    
    Properties props = new Properties();
    
    int nb_running = 0;
    public int nb_cores = 1;
    int nb_white_balance = 0;
    boolean memory_full = false; // Indique si la mémoire est pleine
    public float quality = STOCKAGE_QUALITY;
    
    boolean changing_state = false;
    
    MainJFrame frame;
    
    //int nb_raws_in_memory = 0;

    Timer gcTimer;
    
    public WhiteBalanceFactory wbf = new WhiteBalanceFactory();
    //float red, green, blue;
    public WhiteBalance current_white;

    /** Creates a new instance of MainApplication */
    public MainApplication() {

        //gcTimer = new Timer();

        //gcTimer.schedule(new GCTimerTask(), 0, 5000);

        long mm = Runtime.getRuntime().maxMemory()/1024/1024;

        nb_cores = Runtime.getRuntime().availableProcessors();
        
        fullScreen = false;
        disableSuivant = false;
        batchMode = false;
        nextDisplayedIndex = -1;
        displayedIndex = -1;
        previousIndex = -1;        
        isFitted = false;
        // Note: source for ExampleFileFilter can be found in FileChooserDemo,
        // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
        ExampleFileFilter filter_nef = new ExampleFileFilter();
        filter_nef.addExtension("nef");
        filter_nef.setDescription("NEF Files");
        
        ExampleFileFilter filter_pef = new ExampleFileFilter();
        filter_pef.addExtension("pef");
        filter_pef.setDescription("PEF Files");
        
        ExampleFileFilter filter_crw = new ExampleFileFilter();
        filter_crw.addExtension("crw");
        filter_crw.setDescription("CRW Files");

        ExampleFileFilter filter_cr2 = new ExampleFileFilter();
        filter_cr2.addExtension("cr2");
        filter_cr2.setDescription("CR2 Files");

        ExampleFileFilter filter_raf = new ExampleFileFilter();
        filter_raf.addExtension("raf");
        filter_raf.setDescription("RAF Files");

        ExampleFileFilter filter_raw = new ExampleFileFilter();
        filter_raw.addExtension("raw");
        filter_raw.setDescription("RAW Files");

        chooser.addChoosableFileFilter(filter_nef);
        chooser.addChoosableFileFilter(filter_pef);
        chooser.addChoosableFileFilter(filter_crw);
        chooser.addChoosableFileFilter(filter_cr2);
        chooser.addChoosableFileFilter(filter_raf);
        chooser.addChoosableFileFilter(filter_raw);

        
        filesChooser.setFileFilter(filter_nef);
        filesChooser.addChoosableFileFilter(filter_pef);
        filesChooser.addChoosableFileFilter(filter_crw);
        filesChooser.addChoosableFileFilter(filter_cr2);
        filesChooser.addChoosableFileFilter(filter_raf);
        filesChooser.addChoosableFileFilter(filter_raw);
        filesChooser.setMultiSelectionEnabled(true);
        
        try {
            props.load(new FileInputStream("jraw.properties"));
            String dir = props.getProperty("selectedFile","");
            if ( !dir.equals("")) {
                File directory = new File(dir);

                if ( filter_nef.accept(directory)) {
                    chooser.setFileFilter(filter_nef);
                }
                if ( filter_pef.accept(directory)) {
                    chooser.setFileFilter(filter_pef);
                }
                if ( filter_crw.accept(directory)) {
                    chooser.setFileFilter(filter_crw);
                }
                if ( filter_cr2.accept(directory)) {
                    chooser.setFileFilter(filter_cr2);
                }
                if ( filter_raf.accept(directory)) {
                    chooser.setFileFilter(filter_raf);
                }
                if ( filter_raw.accept(directory)) {
                    chooser.setFileFilter(filter_raw);
                }
                chooser.setSelectedFile(directory);
            }
            String nbProc = props.getProperty("nbCores","");
            if ( !nbProc.equals("")) {
                nb_cores = Integer.parseInt(nbProc);
            }
            String qualityStr = props.getProperty("quality","");
            if ( !qualityStr.equals("")) {
                quality = Float.parseFloat(qualityStr);
            }
            String nbWhiteBalance = props.getProperty("white_balance_number","");
            if ( !nbWhiteBalance.equals("")) {
                nb_white_balance = Integer.parseInt(nbWhiteBalance);
            }
            for ( int i=0; i< nb_white_balance; i++) {
                StringBuffer sb = new StringBuffer();
                sb.append("white_balance_");
                sb.append(i+1);
                String wbs = props.getProperty(sb.toString(),"");
                if ( !wbs.equals("")){
                    wbf.add(wbs);
                }
            }
        current_white = wbf.getNullWhiteBalance();

        } catch (FileNotFoundException e) {
            
        } catch ( IOException e) {
            JOptionPane.showMessageDialog( frame,"Error while reading config file");
        }
        
        setNormal();
    }

    private synchronized ArrayList<RawReader> getRawList() {
        return raws;
    }

    /**
     *
     * @param index
     * @param read
     * @return le RawReader d'index défini si il est dans la liste
     */
    public RawReader getRawReader( int index, boolean read) {

        if ( raws == null) {
            return null;
        }
        if ( index < 0) {
            return null;
        }
        for ( RawReader rr:raws) {
            if (rr.getIndex()==index) {
                if ( read) {
                    rr.setRead();
                }
                return rr;
            }
        }
        return null;
    }
    /*
    public void setRawReader( int index, RawReader rr) {
        raws[index] = new HardOrSoftReference(rr);
    }
    */
    public synchronized void quitter() {
        
        //Properties props = new Properties();
        if ( displayedIndex != -1) {
            props.setProperty("selectedFile", allFilesInDirectory[displayedIndex].getPath());
        }
        props.setProperty("nbCores", Integer.toString(nb_cores));
        props.setProperty("quality", Float.toString(quality));
        props.setProperty("white_balance_number", Integer.toString(wbf.getVector().size()));
        int nb = wbf.getVector().size();
        for ( int i=0; i< nb; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append("white_balance_");
            sb.append(i+1);
            props.setProperty(sb.toString(), wbf.getVector().get(i).toString());
        }
            try {
                props.store( new FileOutputStream("jraw.properties"),null);
            } catch ( FileNotFoundException e ) {
                JOptionPane.showMessageDialog(frame,"File not found");
            } catch ( IOException e) {
                JOptionPane.showMessageDialog(frame,"Error while saving config file");
            }
        System.exit(0);
    }
    
    public int getNbHardRaw() {
        return raws.size();
    }

    /**
     *
     * @return memoire consommée en mega_octets
     */
    public int getTotalHardMemory() {
        int memory = 0;
        for ( RawReader rr:raws) {
            memory += rr.getMemoryConsumption();
        }
        return memory;
    }

    public int getTotalHardMemoryExcluding(RawReader err) {
        int memory = 0;
        for ( RawReader rr:raws) {
            if ( rr != err) {
                memory += rr.getMemoryConsumption();
            }
        }
        return memory;
    }

    /**
     *
     * @return true si aucune Raw trouvee
     */
    public boolean clearOneRaw() {
        if ( raws.size() > 1) {
            raws.remove(0);
            System.gc();
            System.runFinalization();
            return false;
        }
        return true;
    }
    
    public void onClosing() {
        if ( !changing_state) {
            quitter();
        }
        changing_state = false;
    }
    
    public void onClosed() {
        changing_state = false;
    }
    
    public synchronized void setFullScreen() {
        fullScreen = true;
        if ( frame != null) {
            ancientSize = frame.getSize();        
            
            //frame.clearImage();
            frame.setVisible(false);
            frame.dispose();
            frame = null;
            //System.gc();
        }
        SwingUtilities.invokeLater(this);        
    }
    
    public void run() {
        frame = new MainJFrame(this);
        Dimension tailleEcran = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle r = new Rectangle();
        r.width = (int)tailleEcran.getWidth();
        r.height = (int)tailleEcran.getHeight();
        frame.setBounds(r);
        frame.setVisible(true);        
    }
    
    public synchronized void setNormal() {
        fullScreen = false;

        //frame.clearImage();
        if ( frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
        frame = null;
        //System.gc();
        
        frame = new MainJFrame(this);
        if ( ancientSize != null)
            frame.setSize(ancientSize);
        frame.setVisible(true);
    }
    
    public synchronized void imageIsLoaded(int noFile, RawReader rr) {

        //System.out.println("repere 200");
        if ( batchMode) {
            imageIsLoadedBatch(noFile);
            return;
        }
        //System.out.println("repere 201");
        if ( noFile == getNextDisplayedIndex()) {
            setDisplayedIndex( getNextDisplayedIndex());
            
        //System.out.println("repere 202");
            frame.setLoadedImage( displayedIndex);
        //System.out.println("repere 203");
            frame.afficheImage(true);
        //System.out.println("repere 204");
            endWaitCursor();
            nextDisplayedIndex = -1;
        //System.out.println("repere 205");
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                frame.updateInterface();
               }
            });
        }
        if ( noFile == getDisplayedIndex()) {
            frame.afficheImage(true);            
        }
        
        //System.out.println("repere 206");
        //System.out.println("repere 207");
        frame.setNextRawColor();
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                useProcessorAtMax(getNextRawIndex(displayedIndex));
            }
        });
    }

    public int getDisplayedIndex() {
        return displayedIndex;
    }
    
    void setDisplayedIndex( int rawIndex) {
        if ( rawIndex == displayedIndex)
            return;
        
        previousIndex = displayedIndex;
        
        displayedIndex = rawIndex;

    }

    public synchronized void imageIsLoadedBatch(int noFile) {
        if ( displayedIndex != noFile) {
            setDisplayedIndex( noFile);
        }
            frame.setLoadedImage( displayedIndex);
            frame.afficheImage(true);
            
            exporter(false);
            
            currentBatchNumber++;
            if ( nb_running == 0 && currentBatchNumber >= filesChooser.getSelectedFiles().length) {
                batchMode = false;
                frame.updateInterface();

                useProcessorAtMax/*Batch*/(getNextRawIndex(displayedIndex));
                endWaitCursor();
            
                return;
            }            
            if ( currentBatchNumber < filesChooser.getSelectedFiles().length) {
                useProcessorAtMax/*Batch*/( currentBatchNumber);
            }
    }

    public void memoryFull(int rawIndex) {
        memory_full = true;
        for (RawReader rr:raws) {
            if (rr.getIndex()== rawIndex) {
                raws.remove(rr);
                break;
            }
        }
        System.out.printf("memory full, index = %d\n", rawIndex);
        //System.gc();
    }

    public synchronized int getNbRunning() {
        /*
        int nb_run = 0;
        for ( RawReader rr: raws) {
            if ( !rr.hasFinished()) {
                nb_run++;
            }
        }
        return nb_run;
         * 
         */
        return nb_running;
    }
    
    public /*synchronized*/ void addRunningTask() {
        nb_running++;
        System.out.printf("nb_running = %d\n", nb_running);
    }
    
    public /*synchronized*/ void removeRunningTask() {
        nb_running--;
        System.out.printf("nb_running = %d\n", nb_running);
    }
    
    public void registerCalibrationMatrix(String vendor, String model, float[][] matrix) {

        String vendor_model = vendor + "_" + model;
        
        String str = matrix[0][0] + " "+ matrix[0][1] + " " +matrix[0][2]+" "
                 +  matrix[1][0] + " "+ matrix[1][1] + " " +matrix[1][2]+ " "
                 +  matrix[2][0] + " "+ matrix[2][1] + " " +matrix[2][2];
        
        props.setProperty(vendor_model, str);                 
    }
    
    public float[][] getCalibrationMatrix(String vendor, String model) {
        
        String vendor_model = vendor + "_" + model;
        
        String str = props.getProperty( vendor_model, "");
        
        if ( str.equals(""))
            return null;
        
        String[] numbers = str.split(" ");
        
        float[][] matrix = new float[3][3];
        
        matrix[0][0] = Float.parseFloat( numbers[0]);
        matrix[0][1] = Float.parseFloat( numbers[1]);
        matrix[0][2] = Float.parseFloat( numbers[2]);
        matrix[1][0] = Float.parseFloat( numbers[3]);
        matrix[1][1] = Float.parseFloat( numbers[4]);
        matrix[1][2] = Float.parseFloat( numbers[5]);
        matrix[2][0] = Float.parseFloat( numbers[6]);
        matrix[2][1] = Float.parseFloat( numbers[7]);
        matrix[2][2] = Float.parseFloat( numbers[8]);
        
        return matrix;
    }

    public void ouvrir() {
        
        int returnVal = chooser.showOpenDialog(frame);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            ouverture( chooser.getSelectedFile());
        }
    }
    
    void newRaws( ArrayList<RawReader> newRaws) {
        endWaitCursor();
        if ( raws != null) {
            for ( RawReader rr:raws) {
                if ( rr != null) {
                        rr.discardThread();
                }
            }
            raws.clear();
        }
        System.gc();
        System.runFinalization();
/*
        try {
            int[] dummy = new int[1024*1024*500/4];
        } catch ( OutOfMemoryError e) {
        }
 *
 */
        nb_running = 0;
        raws = newRaws;
        notStartedRaw = null;
        previousIndex = -1;
        displayedIndex = -1;
        nextDisplayedIndex = -1;
        memory_full = false;
    }

    public void setCurrentDirectory(File dir) {
        chooser.setCurrentDirectory(dir);
        filesChooser.setCurrentDirectory(dir);
    }
    
    public synchronized void ouverture(File file) {

        long startl = System.nanoTime();
        if ( frame != null) {
            if ( frame.graphicPanel != null) {
                frame.graphicPanel.invalidateImage();
            }
        }
            allFilesInDirectory = file.getParentFile().listFiles(new PlainFileFilter());
            long endl = System.nanoTime();
            double elapsed = (endl - startl)/1.0e9;
            System.out.println("listFiles(): "+ elapsed + " seconds");
            displayedIndex = -1;
            newRaws( new ArrayList<RawReader>());
            for ( int i=0; i< allFilesInDirectory.length; i++) {
                if ( allFilesInDirectory[i].equals(file)) {
                    nextDisplayedIndex = i;
                    break;
                }
            }
            setWaitCursor();
           System.out.println("You chose to open this file: " +
                file.getName());           
           if ( /*useProcessorAtMax*/takeCareOfFirstIndex( getNextDisplayedIndex())) {
               JOptionPane.showMessageDialog(null, "Mémoire insuffisante, augmentez le paramètre Xmx");
               endWaitCursor();
               return;
           }
           useProcessorAtMax(getNextDisplayedIndex());
    }
    
    public int getNextRawIndex(int selIndex) {
        
        if ( allFilesInDirectory == null) {
            return -1;
        }
        if ( allFilesInDirectory.length == 0) {
            return -1;
        }

        selIndex++;
        if ( selIndex >= allFilesInDirectory.length) {
            selIndex = 0;
        }
        
        return selIndex;
    }
    
    public synchronized void suivant() {
        System.out.println("________________________________Suivant");
        nextDisplayedIndex = getNextRawIndex(displayedIndex);

        System.out.println("Le suivant sera "+nextDisplayedIndex);
/*
        if ( raws[nextDisplayedIndex] != null) {
            raws[nextDisplayedIndex].setHard();
        }
  */
        if ( nextDisplayedIndex == displayedIndex) //deja affiche
            return;

        memory_full = false;
        //System.gc();
     
        //System.out.println("Repere 2");
        RawReader rr = getRawReader( getNextDisplayedIndex(), false);

        if ( rr == null) { // Pas encore créé
            takeCareOfFirstIndex( getNextDisplayedIndex());
            useProcessorAtMax( getNextDisplayedIndex());
            setWaitCursor();
            return;
        }
        if ( rr.hasFinished()) { // il a fini
            
            setDisplayedIndex( nextDisplayedIndex);
            frame.setLoadedImage( displayedIndex);
            frame.afficheImage(true);
            nextDisplayedIndex = -1;
        //System.out.println("Repere 3");
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                frame.setNextRawColor();
                useProcessorAtMax( getNextRawIndex(displayedIndex));
               }
            });
            return;
        //System.out.println("Repere 4");
        }
        else { // Il n'a pas fini
            setWaitCursor();
        }
    }
    public synchronized void batch() {
        int returnVal = filesChooser.showOpenDialog(frame);
        if(returnVal == JFileChooser.APPROVE_OPTION && filesChooser.getSelectedFiles().length != 0) {
            allFilesInDirectory = filesChooser.getSelectedFiles()/*[0].getParentFile().listFiles()*/;
            newRaws( /*new HardOrSoftReference[allFilesInDirectory.length]*/new ArrayList<RawReader>());
            currentBatchNumber = 0;
            batchMode = true;
            setWaitCursor();
            frame.updateInterface();
            
            useProcessorAtMax/*Batch*/( 0);
        }
    }
    
    public int getSelectedIndexFromBatch( int indexInBatch) {        
        
        int selectedIndex = -1;
        File[] files = filesChooser.getSelectedFiles();
        if ( indexInBatch >= files.length) {
            return -1;
        }
        for ( int j=0; j< allFilesInDirectory.length; j++) {
            if ( allFilesInDirectory[j].equals(files[indexInBatch])) {
                selectedIndex = j;
                break;
            }
        }
        return selectedIndex;
    }
    
    public void setWaitCursor() {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
            curs = frame.getCursor();
            frame.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            start = System.currentTimeMillis();
            disableSuivant = true;
            frame.updateInterface();
            }
        } );
    }
    public void endWaitCursor() {
        if ( curs == null) {
            return;
        }
                frame.setCursor( curs);
                long elapsed = System.currentTimeMillis() - start;
            
                System.out.printf("Time elapsed in sec: %f\n", elapsed/1000.);
                disableSuivant = false;
                //frame.updateInterface();
    }

    /** cree un RawReader si pas dans la liste
     * retourne null si le RawReader tourne ou a fini de tourner
     * @param rawIndex
     */
    public synchronized RawReader traiteImage( int rawIndex, int priority) {
        if ( raws == null || rawIndex == -1)
            return null;
                    
        RawReader rr = getRawReader( rawIndex, false);
        
        if ( rr != null) {
            if ( rr.isReadyToRun()) {
                return rr;
            }
            rr.setPriority(priority);
            return null;
        }

        if ( notStartedRaw != null && notStartedRaw.getIndex()==rawIndex) {
            return notStartedRaw;
        }
        if ( notStartedRaw == null)
            rr = new RawReader(this);
        else
            rr = notStartedRaw;
        
        if ( rawIndex >= allFilesInDirectory.length) {
            int ii=0;
        }
        rr.setFile(rawIndex,allFilesInDirectory[rawIndex]);
        rr.prepare();

        rr.setPriority( priority);
        
        notStartedRaw = rr;
        return rr;
    }

    /**
     *
     * @param firstIndex
     * @return false si succes
     */
    public synchronized boolean useProcessorAtMaxNew( final int firstIndex) {

        if ( takeCareOfFirstIndex(firstIndex)) {
            return true; // erreur
        }
        return false;
    }

    /**
     *
     * @param firstIndex
     * @return true si erreur
     */
    public boolean takeCareOfFirstIndex( final int firstIndex) {
        RawReader rr = getRawReader(firstIndex,false);
        if ( rr != null) {
            return false; // RawReader existe deja et il est lance, tout va bien
        }
        if ( notStartedRaw == null)
            rr = new RawReader(this);
        else
            rr = notStartedRaw;
        
        if ( rr.getIndex() != firstIndex) {
            rr.setFile(firstIndex, allFilesInDirectory[firstIndex]);
            rr.prepare();
        }
        if ( memoryIsFull(rr.getMemoryConsumption())) { // Cas ou le fait de le rajouter saturerait la memoire
            if ( raws.isEmpty()) {
                return true; // rien a faire
            }
            else { // Cas ou la liste contient un ou plusieurs elements
                RawReader err = getLeastImportantRawReader(firstIndex);
                err.discardThread();
                err.setFile(firstIndex, allFilesInDirectory[firstIndex]);
                err.prepare();
                while ( memoryIsFull(0) && raws.size() > 1) {
                        raws.remove(getLeastImportantRawReader(firstIndex));
                }
                if ( memoryIsFull(0)) {
                    return true; // rien a faire
                }
                else {
                    err.go();
                    return false; // On a remplace l'ancien raw par le nouveau
                }
            }
        }
        else { // On peut le rajouter sans saturer la memoire
            notStartedRaw = null;
            rr.go();
            raws.add(rr);
            return false; // Tout va bien
        }
    }

    public RawReader getLeastImportantRawReader( int firstIndex) {
        RawReader rr = null;
        int index = firstIndex;
        do {
            index = index -1;
            if ( index < 0) {
                index = allFilesInDirectory.length -1;
            }
            rr = getRawReader(index,false);
        } while( rr == null);
        return rr;
    }

    public boolean memoryIsFull(int memoryConsumption) {
        return (getTotalHardMemory()+memoryConsumption) >= (Runtime.getRuntime().maxMemory()/1024/1024 - 100);
    }

    public boolean memoryIsFullExcluding( RawReader err, int memoryConsumption) {
        return (getTotalHardMemoryExcluding(err)+memoryConsumption) >= (Runtime.getRuntime().maxMemory()/1024/1024 - 100);
    }

    /**
     * @return true si succes
     * @param firstIndex: Index de la premiere image qui est demandee
     */
    public synchronized boolean useProcessorAtMax( final int firstIndex) {

        if ( firstIndex == -1) {
            return false;
        }

        int rawIndex = firstIndex;
        while( getNbRunning() < nb_cores && !memory_full) {
            RawReader rr = traiteImage( rawIndex, Thread.MIN_PRIORITY);
            if ( rr !=null) {
                if ( memoryIsFull( rr.getMemoryConsumption()) ) {
                    RawReader err = getLeastImportantRawReader(getMoreImportantIndex());
                    if ( canBeReplaced(err)) {

                        if ( memoryIsFullExcluding(err, rr.getMemoryConsumption())) {
                            return true;
                        }
                        else {
                            err.setFile(rawIndex, allFilesInDirectory[rawIndex]);
                            err.prepare();
                            err.go();
                        }
                    }
                    else
                        return true; // On ne l'ajoute pas
                }
                else {
                    notStartedRaw = null;
                    raws.add(rr); // On l'ajoute
                    rr.go();
                }
            }
            rawIndex = getNextRawIndex( rawIndex);
            if ( rawIndex == firstIndex)
                break;
        }
        return true;
    }
    
    public boolean canBeReplaced(RawReader rr) {
        if ( rr == null) {
            return false;
        }
        if ( !rr.hasFinished()) {
            return false;
        }
        if ( !rr.hasBeenRead()) {
            return false;
        }
        if ( rr.getIndex() == displayedIndex) {
            return false;
        }
        return true;
    }

    public int getMoreImportantIndex() {
        if ( nextDisplayedIndex != -1) {
            return nextDisplayedIndex;
        }
        return displayedIndex;
    }
/*
    public void useProcessorAtMaxBatch( int batchIndex) {
        if ( batchIndex == -1 || batchIndex >= filesChooser.getSelectedFiles().length)
            return;
        
        int rawIndex = batchIndex;
        while( getNbRunning() < nb_cores) {
            RawReader rr = traiteImage( getSelectedIndexFromBatch(rawIndex), Thread.MIN_PRIORITY);
            if ( getTotalHardMemory() >= (Runtime.getRuntime().maxMemory()/1024/1024)) {
                return;
            }
            if ( rr!= null)rr.go();
            rawIndex++;
            if ( rawIndex == filesChooser.getSelectedFiles().length)
                break;
        }
    }
*/
    public File getOutputFile() {

        if ( displayedIndex < 0) {
            return null;
        }
        String inputFile = allFilesInDirectory[displayedIndex].getPath();
        if ( inputFile.endsWith(".NEF")
        || inputFile.endsWith(".nef")
                || inputFile.endsWith(".PEF")
                || inputFile.endsWith(".pef")
                || inputFile.endsWith(".CRW")
                || inputFile.endsWith(".crw")
                || inputFile.endsWith(".CR2")
                || inputFile.endsWith(".cr2")
                || inputFile.endsWith(".RAF")
                || inputFile.endsWith(".raf")
                || inputFile.endsWith(".RAW")
                || inputFile.endsWith(".raw")) {
            inputFile = inputFile.substring(0, inputFile.length()-4);
            if ( quality == PNG_QUALITY
                    || quality == PNG_X2_QUALITY
                    || quality == PNG_X4_QUALITY
                    || quality == PNG_X8_QUALITY) {
                return new File( inputFile+".png");
            }
            else {
                return new File( inputFile+".jpg");
            }
        }
        return null;
    }

    public synchronized void exporter(boolean askIfExists) {

        outputFile = getOutputFile();

        if ( outputFile == null) {
            return;
        }

        if ( outputFile.exists()) {
                if ( !askIfExists)
                    return;
                int ret_code = JOptionPane.showConfirmDialog(frame, "Le fichier existe déjà, voulez-vous l'écraser ?","JRaw",JOptionPane.YES_NO_OPTION);
                if ( ret_code ==JOptionPane.NO_OPTION)
                    return;
                outputFile.delete();
         }
         try {
                //ImageIO.write(loadedImage,"jpg", jpegFile);
                if ( quality != PNG_QUALITY
                        && quality != PNG_X2_QUALITY
                        && quality != PNG_X4_QUALITY
                        && quality != PNG_X8_QUALITY) {
                    JPEGWriter.writeJPEG(frame.getLoadedImage(), outputFile, quality);
                }
                else {
                    PNGWriter.writePNG(frame.getLoadedImage(), outputFile, quality);
                }
                //JPEGMetaData.writeJPEG(loadedImage,jpegFile);
         } catch( IOException e) {
                JOptionPane.showMessageDialog(frame, "Erreur écriture JPEG");
                e.printStackTrace();
         }
        frame.updateInterface();
    }

    public void keyPressed( KeyEvent e) {
        
        if ( e.getKeyCode() == KeyEvent.VK_F11) {
            changing_state = true;
            if ( fullScreen)
                setNormal();
            else
                setFullScreen();
        }
        if ( e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if ( fullScreen)
                changing_state = true;
                setNormal();
        }
    }

    public void keyReleased( KeyEvent e) {
        
    }
    
    public void keyTyped( KeyEvent e) {
    }
    /*
    public void increment(int memoryConsumption) {
        nb_raws_in_memory++;
        System.out.println("increment: "+ nb_raws_in_memory);

        //nettoyage(memoryConsumption);
    }
*/
    public void nettoyage(int amount) {

        while ( Runtime.getRuntime().freeMemory()/1024/1024 < amount) {
            System.out.println("Nettoyage...");
            if ( clearOneRaw()) {
                break;
            }
            System.gc();
            System.runFinalization();
        }
    }
/*    
    public void decrement(int rawIndex) {
        nb_raws_in_memory--;
        System.out.println("decrement: "+ nb_raws_in_memory);
    }
*/
    public boolean isFullScreen() {
        return fullScreen;
    }

    public synchronized int getNextDisplayedIndex() {
        return nextDisplayedIndex;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        File file = new File("C:\\Documents and Settings\\YvesFabienne\\Mes documents\\Mes images\\Nikon Transfer\\20081204_002\\20081204_221722.JPG");
        /*
        try {
            Metadata meta = JpegMetadataReader.readMetadata(file);
            
            int i=0;
        } catch ( JpegProcessingException e) {
            e.printStackTrace();
        }
        */
        /*
        try {
        JPEGReader.readJPEG(file);
        } catch( IOException e) {
            e.printStackTrace();
        }
        */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainApplication();
            }
        });
    }
    
}
