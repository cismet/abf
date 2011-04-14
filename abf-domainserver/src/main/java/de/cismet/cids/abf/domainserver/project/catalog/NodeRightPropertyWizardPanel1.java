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
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.permission.NodePermission;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class NodeRightPropertyWizardPanel1 implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ChangeListener> listeners;
    private transient NodeRightPropertyVisualPanel1 component;
    private transient CatNode catNode;
    private transient Backend backend;
    private transient List<NodePermission> perms;
    private transient DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NodeRightPropertyWizardPanel1 object.
     */
    public NodeRightPropertyWizardPanel1() {
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NodeRightPropertyVisualPanel1(this);
            addChangeListener(component);
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
    List<NodePermission> getPermissions() {
        return perms;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    DomainserverProject getProject() {
        return project;
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
        backend = (Backend)wizard.getProperty(NewCatalogNodeWizardAction.BACKEND_PROP);
        catNode = (CatNode)wizard.getProperty(NewCatalogNodeWizardAction.CATNODE_PROP);
        perms = (List<NodePermission>)wizard.getProperty(
                NewCatalogNodeWizardAction.PERM_PROP);
        project = (DomainserverProject)wizard.getProperty(
                NewCatalogNodeWizardAction.PROJECT_PROP);
        catNode.setProspectiveParent((CatNode)wizard.getProperty(
                NewCatalogNodeWizardAction.PARENT_PROP));
        fireChangeEvent();
    }

    @Override
    public void storeSettings(final Object settings) {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewCatalogNodeWizardAction.PERM_PROP,
            component.getPermissions());
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
