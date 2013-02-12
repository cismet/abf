/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.List;

import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NewUserWizardPanel2 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private transient NewUserVisualPanel2 component;
    private transient DomainserverProject project;
    private transient List<UserGroup> userGroups;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewUserVisualPanel2(this);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    DomainserverProject getProject() {
        return project;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<UserGroup> getUserGroups() {
        return userGroups;
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
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        project = (DomainserverProject)wizard.getProperty(NewUserWizardAction.PROJECT_PROP);
        userGroups = (List)wizard.getProperty(NewUserWizardAction.USERGROUP_PROP);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewUserWizardAction.USERGROUP_PROP, component.getUserGroups());
    }
}
