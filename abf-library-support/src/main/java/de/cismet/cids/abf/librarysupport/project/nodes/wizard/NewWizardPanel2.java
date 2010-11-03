/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.io.File;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.utilities.NameValidator;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.7
 */
public final class NewWizardPanel2 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    /**
     * The visual component that displays this panel. If you need to access the component from this class, just use
     * getComponent().
     */
    private transient NewVisualPanel2 component;
    private transient WizardDescriptor wizard;

    private transient FileObject root;
    private transient FileObject current;
    private transient boolean isAPackage;
    private transient String ext;
    private final transient NameValidator packageValidator;
    private final transient NameValidator fileValidator;

    private final transient Set<ChangeListener> listeners;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewWizardPanel2 object.
     */
    public NewWizardPanel2() {
        packageValidator = new NameValidator(NameValidator.NAME_PACKAGE);
        fileValidator = new NameValidator(NameValidator.NAME_LOW_GERMAN);
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
            component = new NewVisualPanel2(this);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FileObject getRootDir() {
        return root;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FileObject getCurrentDir() {
        return current;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isPackage() {
        return isAPackage;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getExt() {
        return ext;
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
        boolean warn = false;
        if (isAPackage) {
            if (!packageValidator.isValid(component.getPackageName())) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewWizardPanel2.class,
                        "NewWizardPanel2.isvalid().wizard.PROP_ERROR_MESSAGE.packageNameNotValid")); // NOI18N
                warn = true;
            }
        } else {
            if (!fileValidator.isValid(component.getPackageName())) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewWizardPanel2.class,
                        "NewWizardPanel2.isvalid().wizard.PROP_ERROR_MESSAGE.nameOfFileNotValid"));  // NOI18N
                return false;
            }
        }
        final FileObject toCreate = FileUtil.toFileObject(
                new File(component.getDir()));
        if (toCreate != null) {
            if (toCreate.isValid() && toCreate.isData()) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewWizardPanel2.class,
                        "NewWizardPanel2.isValid().wizard.PROP_ERROR_MESSAGED.fileAlreadyExists"));  // NOI18N
                return false;
            } else if (toCreate.isValid() && toCreate.isFolder()) {
                // choose the appropriate reason
                if (!toCreate.canRead()) {
                    wizard.putProperty(
                        WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            NewWizardPanel2.class,
                            "NewWizardPanel2.isValid().wizard.PROP_ERROR_MESSAGED.folderExistsButCannotRead"));  // NOI18N
                } else if (!toCreate.canWrite()) {
                    wizard.putProperty(
                        WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            NewWizardPanel2.class,
                            "NewWizardPanel2.isValid().wizard.PROP_ERROR_MESSAGED.folderExistsButCannotWrite")); // NOI18N
                } else {
                    wizard.putProperty(
                        WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            NewWizardPanel2.class,
                            "NewWizardPanel2.isValid().wizard.PROP_ERROR_MESSAGED.folderAlreadyExists"));        // NOI18N
                }
                return false;
            }
        }
        if (!warn) {
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
        root = (FileObject)wizard.getProperty(NewWizardAction.PROP_ROOT_DIR);
        current = (FileObject)wizard.getProperty(NewWizardAction.PROP_CURRENT_DIR);
        isAPackage = (Boolean)wizard.getProperty(NewWizardAction.PROP_IS_PACKAGE);
        ext = (String)wizard.getProperty(NewWizardAction.PROP_EXT);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewWizardAction.PROP_NAME, component.getPackageName());
        wizard.putProperty(NewWizardAction.PROP_PATH, component.getDir());
        wizard.putProperty(NewWizardAction.PROP_PACKAGE, component.getSelectedPackage());
    }
}
