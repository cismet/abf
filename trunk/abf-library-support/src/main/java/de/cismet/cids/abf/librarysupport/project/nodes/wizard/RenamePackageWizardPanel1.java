/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import org.apache.log4j.Logger;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

import java.awt.Component;

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
public final class RenamePackageWizardPanel1 implements WizardDescriptor.Panel, ChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            RenamePackageWizardPanel1.class);

    public static final String NEW_PACKAGE_NAME_PROPERTY = "newPackageName"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    /**
     * The visual component that displays this panel. If you need to access the component from this class, just use
     * getComponent().
     */
    private transient RenamePackageVisualPanel1 visPanel;
    private final transient FileObject root;
    private final transient FileObject current;
    private transient WizardDescriptor wizard;
    private final transient NameValidator validator;

    private final transient Set<ChangeListener> listeners;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RenamePackageWizardPanel1 object.
     *
     * @param  root     DOCUMENT ME!
     * @param  current  DOCUMENT ME!
     */
    public RenamePackageWizardPanel1(final FileObject root, final FileObject current) {
        this.root = root;
        this.current = current;
        validator = new NameValidator(NameValidator.NAME_PACKAGE);
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (visPanel == null) {
            visPanel = new RenamePackageVisualPanel1(root, current);
            visPanel.addChangeListener(this);
        }
        return visPanel;
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
        if (validator.isValid(visPanel.getPackage())) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    RenamePackageWizardPanel1.class,
                    "RenamePackageVisualPanel1.isvalid().wizard.PROP_ERROR_MESSAGE.packageNameNotValid"));             // NOI18N
            return false;
        }
        final String relPath = visPanel.getPackage().replace(".", "/");                                                // NOI18N
        try {
            final FileObject toCreate = root.getFileObject(relPath);
            if (toCreate != null) {
                if (toCreate.equals(current)) {
                    wizard.putProperty(
                        WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            RenamePackageWizardPanel1.class,
                            "RenamePackageWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.noChangesMade"));           // NOI18N
                    return false;
                }
                if (toCreate.isValid() && toCreate.isData()) {
                    wizard.putProperty(
                        WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            RenamePackageWizardPanel1.class,
                            "RenamePackageWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.normalFileAlreadyExists")); // NOI18N
                    return false;
                } else if (toCreate.isValid() && toCreate.isFolder()) {
                    // choose the appropriate reason
                    if (!toCreate.canRead()) {
                        wizard.putProperty(
                            WizardDescriptor.PROP_ERROR_MESSAGE,
                            org.openide.util.NbBundle.getMessage(
                                RenamePackageWizardPanel1.class,
                                "RenamePackageVisualPanel1.isvalid().wizard.PROP_ERROR_MESSAGE.folderExistsButCannotRead"));  // NOI18N
                    } else if (!toCreate.canWrite()) {
                        wizard.putProperty(
                            WizardDescriptor.PROP_ERROR_MESSAGE,
                            org.openide.util.NbBundle.getMessage(
                                RenamePackageWizardPanel1.class,
                                "RenamePackageVisualPanel1.isvalid().wizard.PROP_ERROR_MESSAGE.folderExistsButCannotWrite")); // NOI18N
                    } else {
                        wizard.putProperty(
                            WizardDescriptor.PROP_ERROR_MESSAGE,
                            org.openide.util.NbBundle.getMessage(
                                RenamePackageWizardPanel1.class,
                                "RenamePackageVisualPanel1.isvalid().wizard.PROP_ERROR_MESSAGE.folderAlreadyExists"));        // NOI18N
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    RenamePackageWizardPanel1.class,
                    "RenamePackageVisualPanel1.isvalid().wizard.PROP_ERROR_MESSAGE.invalidName"));                            // NOI18N
            return false;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
    }

    @Override
    public void storeSettings(final Object settings) {
        ((WizardDescriptor)settings).putProperty(NEW_PACKAGE_NAME_PROPERTY,
            visPanel.getPackage());
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
    public void stateChanged(final ChangeEvent changeEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isValid returns " + isValid()); // NOI18N
        }
        fireChangeEvent();
    }
}
