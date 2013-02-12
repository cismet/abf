/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class QueryManipulationWizardPanel4 extends Observable implements WizardDescriptor.FinishablePanel {

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ChangeListener> listeners;
    private transient QueryManipulationVisualPanel4 component;
    private transient Query query;
    private transient Properties properties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryManipulationWizardPanel4 object.
     */
    public QueryManipulationWizardPanel4() {
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new QueryManipulationVisualPanel4(this);
            addObserver(component);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Query getQuery() {
        return query;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void fireChangeEvent() {
        final Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void readSettings(final Object settings) {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        query = (Query)wizard.getProperty(QueryManipulationWizardAction.QUERY_PROPERTY);
        properties = (Properties)wizard.getProperty(
                QueryManipulationWizardAction.PROJECT_PROPERTIES_PROPERTY);
        setChanged();
        notifyObservers("readSettings"); // NOI18N
    }

    @Override
    public void storeSettings(final Object settings) {
        // not needed
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
