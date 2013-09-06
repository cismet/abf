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

import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
//TODO: proper mvc style, use abstractwizarddescriptorpanel
public class AddUsersWizardPanel implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private transient AddUsersVisualPanel component;
    private transient WizardDescriptor wizard;
    private transient UserGroup userGroup;
    private transient DomainserverProject project;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddUsersVisualPanel(this);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    UserGroup getUserGroup() {
        return userGroup;
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
        project = (DomainserverProject)wizard.getProperty(
                AddUsersWizardAction.PROJECT_PROP);
        userGroup = (UserGroup)wizard.getProperty(AddUsersWizardAction.USERGROUP_PROP);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(AddUsersWizardAction.USERGROUP_PROP, component.getUserGroup());
    }
}
