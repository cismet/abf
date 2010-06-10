/*
 * SatelliteNavigationPanel.java
 *
 * Created on 17. Januar 2007, 12:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import de.cismet.cids.abf.domainserver.project.cidsclass.ClassDiagramTopComponent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Stefan Flemming
 */
public class SatelliteNavigationPanel implements NavigatorPanel {
    
    /** Creates a new instance of SatelliteNavigationPanel no argument required */
    public SatelliteNavigationPanel() {
        
    }
    
    public String getDisplayHint() {
        return "SSSSSS";//return NbBundle.getMessage(SatelliteNavigationPanel.class, "HINT_SatelliteNavigationPanel");
    }
    
    public String getDisplayName() {
        return "SSSSSS";//NbBundle.getMessage(SatelliteNavigationPanel.class, "CTL_SatelliteNavigationPanel");
    }
    
    public JComponent getComponent() {
        TopComponent tc=TopComponent.getRegistry().getActivated();
        if (tc instanceof ClassDiagramTopComponent) {
            return ((ClassDiagramTopComponent)tc).getScene().createSatelliteView();
        }
        else {
            return new JPanel();
        }
    }
    
    public void panelActivated(Lookup context) {
    }
    
    public void panelDeactivated() {
    }
    
    public Lookup getLookup() {
        return null;
    }
    
}