/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.ArrayList;

import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.NameValidator;

import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class NewCidsClassWizardPanel1 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    // TODO: eventually medium validator must not be used because only highly
    // "secure" names are provided
    private final transient NameValidator highValidator;
    private transient NewCidsClassVisualPanel1 component;
    private transient WizardDescriptor wizard;
    private transient DomainserverProject project;
    private transient CidsClass cidsClass;

    private final transient ChangeSupport changeSupport;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewCidsClassWizardPanel1 object.
     */
    public NewCidsClassWizardPanel1() {
        changeSupport = new ChangeSupport(this);
        highValidator = new NameValidator(NameValidator.SCHEMA_HIGH);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewCidsClassVisualPanel1(this);
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
    CidsClass getCidsClass() {
        return cidsClass;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        final ArrayList<String> names = new ArrayList<String>(10);
        final CidsClass cc = component.getCidsClass();
        final String ccName = cc.getName();
        final String ctName = cc.getTableName();
        if ((ccName == null) || "".equals(ccName.trim())) {                                        // NOI18N
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassWizardPanel1.class,
                    "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.nameNotValid")); // NOI18N

            return false;
        }
        if ((ctName == null) || "".equals(ctName.trim())) {                                                // NOI18N
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassWizardPanel1.class,
                    "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.tableW/ONameNotValid")); // NOI18N

            return false;
        }
        if (!highValidator.isValid(ctName)) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassWizardPanel1.class,
                    "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.tableNameNotValid")); // NOI18N

            return false;
        }
        for (final Attribute a : cc.getAttributes()) {
            final String name = a.getFieldName();
            if ((name == null) || "".equals(name.trim())) {                                                       // NOI18N
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassWizardPanel1.class,
                        "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.attrW/OFieldnameInvalid")); // NOI18N

                return false;
            }
            if (!highValidator.isValid(name)) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassWizardPanel1.class,
                        "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.attrFieldnameNotValid", // NOI18N
                        name));

                return false;
            }
            if (names.contains(name)) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassWizardPanel1.class,
                        "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.duplAttrFieldNamesInvalid")); // NOI18N

                return false;
            }
            names.add(name);
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);

        return true;
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
        project = (DomainserverProject)wizard.getProperty(NewCidsClassWizardAction.PROJECT_PROP);
        cidsClass = (CidsClass)wizard.getProperty(NewCidsClassWizardAction.CIDS_CLASS_PROP);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewCidsClassWizardAction.CIDS_CLASS_PROP, component.getCidsClass());
    }
}
