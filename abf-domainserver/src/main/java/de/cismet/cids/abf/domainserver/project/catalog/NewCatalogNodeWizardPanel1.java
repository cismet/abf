/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.utilities.NameValidator;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.common.Domain;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class NewCatalogNodeWizardPanel1 extends Observable implements WizardDescriptor.Panel,
    WizardDescriptor.FinishablePanel {

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ChangeListener> listeners;
    private transient NewCatalogNodeVisualPanel1 component;
    private transient CatNode catNode;
    private transient Domain domain;
    private transient Backend backend;
    private transient WizardDescriptor wizard;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewCatalogNodeWizardPanel1 object.
     */
    public NewCatalogNodeWizardPanel1() {
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewCatalogNodeVisualPanel1(this);
            addObserver(component);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Backend getBackend() {
        return backend;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CatNode getCatNode() {
        return catNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Domain getLinkDomain() {
        return domain;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        final String name = component.getNodeName();
        if (NameValidator.isValid(name, NameValidator.NAME_LOW_GERMAN)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        } else {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewCatalogNodeWizardPanel1.class,
                    "NewCatalogNodeWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.nodeNameNotValid")); // NOI18N
            return false;
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     */
    void nameChanged() {
        fireChangeEvent();
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
        wizard = (WizardDescriptor)settings;
        backend = (Backend)wizard.getProperty(NewCatalogNodeWizardAction.BACKEND_PROP);
        catNode = (CatNode)wizard.getProperty(NewCatalogNodeWizardAction.CATNODE_PROP);
        domain = (Domain)wizard.getProperty(NewCatalogNodeWizardAction.DOMAIN_PROP);
        fireChangeEvent();
        setChanged();
        notifyObservers();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewCatalogNodeWizardAction.CATNODE_PROP, component.getCatNode());
        wizard.putProperty(NewCatalogNodeWizardAction.DOMAIN_PROP, component.getLinkDomain());
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
