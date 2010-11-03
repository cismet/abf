/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.apache.log4j.Logger;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;

import javax.persistence.NoResultException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.utilities.NameValidator;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class QueryManipulationWizardPanel1 extends Observable implements WizardDescriptor.FinishablePanel,
    ChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            QueryManipulationWizardPanel1.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ChangeListener> listeners;
    private transient Query query;
    private transient WizardDescriptor wizard;
    private transient QueryManipulationVisualPanel1 component;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryManipulationWizardPanel1 object.
     */
    public QueryManipulationWizardPanel1() {
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new QueryManipulationVisualPanel1(this);
            component.addChangeListener(this);
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
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        if (!NameValidator.isValid(component.getQueryName(), NameValidator.NAME_HIGH)) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    QueryManipulationWizardPanel1.class,
                    "QueryManipulationWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.invalidQueryName"));        // NOI18N
            return false;
        }
        final Backend backend = (Backend)wizard.getProperty(
                QueryManipulationWizardAction.BACKEND_PROPERTY);
        try {
            final Query q = backend.getEntity(
                    Query.class,
                    component.getQueryName());
            if ((query == null) || !q.getId().equals(query.getId())) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        QueryManipulationWizardPanel1.class,
                        "QueryManipulationWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.queryAlreadyPresent")); // NOI18N
            }
            return false;
        } catch (final NoResultException nre) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }
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

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        query = (Query)wizard.getProperty(QueryManipulationWizardAction.QUERY_PROPERTY);
        setChanged();
        notifyObservers("readSettings"); // NOI18N
    }

    @Override
    public void storeSettings(final Object settings) {
        final WizardDescriptor wiz = (WizardDescriptor)settings;
        wiz.putProperty(QueryManipulationWizardAction.QUERY_PROPERTY,
            component.getQuery());
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isValid returns: " + isValid()); // NOI18N
        }
        fireChangeEvent();
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
