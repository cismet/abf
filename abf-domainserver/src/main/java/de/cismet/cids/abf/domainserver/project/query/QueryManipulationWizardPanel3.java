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
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class QueryManipulationWizardPanel3 extends Observable implements WizardDescriptor.FinishablePanel {

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ChangeListener> listeners;
    private transient QueryManipulationVisualPanel3 component;
    private transient Query query;
    private transient Backend backend;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryManipulationWizardPanel3 object.
     */
    public QueryManipulationWizardPanel3() {
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new QueryManipulationVisualPanel3(this);
            addObserver(component);
        }
        return component;
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
    public Backend getBackend() {
        return backend;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
            // If it depends on some condition (form filled out...), then:
            // return someCondition();
            // and when this condition changes (last form field filled in...) then:
            // fireChangeEvent();
            // and uncomment the complicated stuff below.
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
        backend = (Backend)wizard.getProperty(QueryManipulationWizardAction.BACKEND_PROPERTY);
        setChanged();
        notifyObservers("readSettings"); // NOI18N
    }

    @Override
    public void storeSettings(final Object settings) {
        ((WizardDescriptor)settings).putProperty(QueryManipulationWizardAction.QUERY_PROPERTY, component.getQuery());
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
