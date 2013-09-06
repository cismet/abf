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

import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class QueryManipulationWizardPanel2 extends Observable implements WizardDescriptor.FinishablePanel {

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ChangeListener> listeners;
    private transient QueryManipulationVisualPanel2 component;
    private transient Query query;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryManipulationWizardPanel2 object.
     */
    public QueryManipulationWizardPanel2() {
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new QueryManipulationVisualPanel2(this);
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

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
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
