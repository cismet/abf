/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import org.apache.log4j.Logger;

import org.netbeans.spi.navigator.NavigatorPanel;

import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.spi.navigator.NavigatorLookupHint;

/**
 * DOCUMENT ME!
 *
 * @author   Stefan Flemming
 * @version  $Revision$, $Date$
 */
public class SatelliteNavigationPanel implements NavigatorPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(SatelliteNavigationPanel.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getDisplayHint() {
        return getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return "Class Graph Satellite Panel";
    }

    @Override
    public JComponent getComponent() {
        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc instanceof ClassDiagramTopComponent) {
            return ((ClassDiagramTopComponent)tc).getScene().createSatelliteView();
        } else {
            return new JPanel();
        }
    }

    @Override
    public void panelActivated(final Lookup context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("called panelActivated: " + context); // NOI18N
        }
    }

    @Override
    public void panelDeactivated() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("called panelDeactivated"); // NOI18N
        }
    }

    @Override
    public Lookup getLookup() {
        return null;
    }

    static final class SatelliteLookupHint implements NavigatorLookupHint {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getContentType() {
        return "cismet/satellite-wiring"; // NOI18N
    }
}
}
