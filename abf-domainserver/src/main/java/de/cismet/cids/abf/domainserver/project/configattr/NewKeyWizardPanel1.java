/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.configattr;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.Component;

import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.NameValidator;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NewKeyWizardPanel1 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private NewKeyVisualPanel1 component;

    private final ChangeSupport changeSupport;
    private WizardDescriptor wizard;
    private DomainserverProject project;
    private Types type;
    private final NameValidator validator;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewKeyWizardPanel1 object.
     */
    public NewKeyWizardPanel1() {
        changeSupport = new ChangeSupport(this);
        validator = new NameValidator(NameValidator.NAME_PACKAGE);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewKeyVisualPanel1(this);
        }

        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Types getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DomainserverProject getProject() {
        return project;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        final ConfigAttrKey key = component.getKey();

        if (key.getKey().isEmpty()) {
            wizard.putProperty(
                WizardDescriptor.PROP_INFO_MESSAGE,
                NbBundle.getMessage(NewKeyWizardPanel1.class, "NewKeyWizardPanel1.isValid().infoMessage.emptyKey")); // NOI18N
            return false;
        } else if (project.getCidsDataObjectBackend().contains(key)) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(
                    NewKeyWizardPanel1.class,
                    "NewKeyWizardPanel1.isValid().errorMessage.alreadyPresent"));                                    // NOI18N
            return false;
        } else if (!validator.isValid(key.getKey())) {
            wizard.putProperty(
                WizardDescriptor.PROP_WARNING_MESSAGE,
                NbBundle.getMessage(
                    NewKeyWizardPanel1.class,
                    "NewKeyWizardPanel1.isValid().warningMessage.noPackageName"));                                   // NOI18N
            return true;
        } else {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
            wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);
            return true;
        }
    }

    @Override
    public final void addChangeListener(final ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(final ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * DOCUMENT ME!
     */
    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        project = (DomainserverProject)wizard.getProperty(NewEntryWizardPanel1.PROP_PROJECT);
        type = (Types)wizard.getProperty(NewEntryWizardPanel1.PROP_ENTRY_TYPE);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewEntryWizardPanel1.PROP_ENTRY_KEY, component.getKey());
    }
}
