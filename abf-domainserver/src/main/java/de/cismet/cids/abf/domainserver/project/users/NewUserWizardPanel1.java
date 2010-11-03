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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.NameValidator;

import de.cismet.cids.jpa.entity.user.User;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NewUserWizardPanel1 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private final transient NameValidator validator;
    private final transient Set<ChangeListener> listeners;
    private transient NewUserVisualPanel1 component;
    private transient String domainserverName;
    private transient WizardDescriptor wizard;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewUserWizardPanel1 object.
     */
    public NewUserWizardPanel1() {
        validator = new NameValidator(NameValidator.NAME_HIGH);
        listeners = new HashSet<ChangeListener>(1);
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

    @Override
    public boolean isValid() {
        final User user = component.getUser();
        if (!validator.isValid(user.getLoginname())) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewUserWizardPanel1.class,
                    "NewUserWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.invalidLogin"));    // NOI18N
            return false;
        }
        if (!validator.isValid(user.getPassword())) {
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
    void fireChangeEvent() {
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
        final DomainserverProject project = (DomainserverProject)wizard.getProperty(NewUserWizardAction.PROJECT_PROP);
        final Properties props = project.getRuntimeProps();
        domainserverName = props.getProperty("serverName") // NOI18N
                    + " ("                                 // NOI18N
                    + props.getProperty("connection.url")  // NOI18N
                    + ")";                                 // NOI18N
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewUserWizardAction.USER_PROP, component.getUser());
    }
}
