/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.Properties;

import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.NameValidator;

import de.cismet.cids.jpa.entity.user.User;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
//TODO: proper wizard
public class NewUserWizardPanel1 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private final transient NameValidator validator;
    private final transient ChangeSupport changeSupport;
    private transient NewUserVisualPanel1 component;
    private transient String domainserverName;
    private transient WizardDescriptor wizard;
    private transient User user;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewUserWizardPanel1 object.
     */
    public NewUserWizardPanel1() {
        validator = new NameValidator(NameValidator.NAME_HIGH);
        changeSupport = new ChangeSupport(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewUserVisualPanel1(this);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDomainserverName() {
        return domainserverName;
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
    User getUser() {
        return user;
    }

    @Override
    public boolean isValid() {
        final User u = component.getUser();
        if (!validator.isValid(u.getLoginname())) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewUserWizardPanel1.class,
                    "NewUserWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.invalidLogin"));    // NOI18N
            return false;
        }
        if (!validator.isValid(u.getPassword())) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewUserWizardPanel1.class,
                    "NewUserWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.invalidPassword")); // NOI18N
            return false;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);

        return true;
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    /**
     * DOCUMENT ME!
     */
    void fireChangeEvent() {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        final DomainserverProject project = (DomainserverProject)wizard.getProperty(NewUserWizardAction.PROJECT_PROP);
        user = (User)wizard.getProperty(NewUserWizardAction.USER_PROP);

        final Properties props = project.getRuntimeProps();
        domainserverName = props.getProperty("serverName") // NOI18N
                    + " ("                                 // NOI18N
                    + props.getProperty("connection.url")  // NOI18N
                    + ")";                                 // NOI18N

        component.init();

        fireChangeEvent();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewUserWizardAction.USER_PROP, component.getUser());
    }
}
