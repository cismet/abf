/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users.groups;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.user.User;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ChangeGroupBelongingWizardPanel1 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private transient ChangeGroupBelongingVisualPanel1 component;
    private transient WizardDescriptor wizard;
    private transient User user;
    private transient Backend backend;
    private transient DomainserverProject project;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ChangeGroupBelongingVisualPanel1(this);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    User getUser() {
        return user;
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
        // not needed
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
        // not needed
    }

    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        user = (User)wizard.getProperty(ChangeGroupBelongingWizardAction.USER_PROP);
        project = (DomainserverProject)wizard.getProperty(
                ChangeGroupBelongingWizardAction.PROJECT_PROP);
        backend = project.getCidsDataObjectBackend();
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(ChangeGroupBelongingWizardAction.TOUCHED_GROUPS_PROP,
            component.getTouchedGroups());
    }
}
