/*
 * FileDropTarget.java
 *
 * Created on 10 novembre 2007, 23:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package JRaw;

import JRaw.ihm.MainJFrame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author YvesFabienne
 */
public class FileDropTarget extends DropTarget {
    MainJFrame mjf;
    
    /** Creates a new instance of FileDropTarget */
    public FileDropTarget( MainJFrame mjfArg) {
        mjf = mjfArg;
    }
    
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_LINK);
        Transferable t = dtde.getTransferable();
        DataFlavor[] flavors = dtde.getCurrentDataFlavors();
        try {
            List list = (List)t.getTransferData( flavors[0]);
            File file = (File) list.get(0);
            mjf.getParentApplication().setCurrentDirectory(file.getParentFile());
            mjf.getParentApplication().ouverture(file);
        } catch ( UnsupportedFlavorException e) {
            JOptionPane.showMessageDialog(mjf,e.getMessage());
        } catch( IOException e) {
            JOptionPane.showMessageDialog(mjf,e.getMessage());            
        }
//        super.drop(dtde);
    }
}
