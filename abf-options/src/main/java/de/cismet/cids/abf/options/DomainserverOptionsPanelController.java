/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.options;

import org.netbeans.spi.options.OptionsPanelController;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@OptionsPanelController.SubRegistration(
    location = "ABF",
    displayName = "#AdvancedOption_DisplayName_Domainserver",
    keywords = "#AdvancedOption_Keywords_Domainserver",
    keywordsCategory = "ABF/Domainserver",
    position = 100
)
public final class DomainserverOptionsPanelController extends OptionsPanelController {

    //~ Instance fields --------------------------------------------------------

    private DomainserverPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        getPanel().store();
        changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private DomainserverPanel getPanel() {
        if (panel == null) {
            panel = new DomainserverPanel(this);
        }

        return panel;
    }

    /**
     * DOCUMENT ME!
     */
    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }

        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
