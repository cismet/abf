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
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.io.File;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.utilities.NameValidator;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.9
 */
public final class NewStarterWizardPanel1 implements WizardDescriptor.Panel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewStarterWizardPanel1.class);

    public static final String NEW_STARTER_NAME_PROPERTY = "newStarterName";          // NOI18N
    public static final String NEW_STARTER_BASE_FILE_PROPERTY = "newStarterBaseFile"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    /**
     * The visual component that displays this panel. If you need to access the component from this class, just use
     * getComponent().
     */
    private transient NewStarterVisualPanel1 visPanel;
    private transient WizardDescriptor wizard;
    private transient LibrarySupportProject project;
    private final transient NameValidator validator;
    private transient FileObject srcDir;

    private final transient Set<ChangeListener> listeners;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewStarterWizardPanel1 object.
     */
    public NewStarterWizardPanel1() {
        this.validator = new NameValidator(NameValidator.NAME_MEDIUM);
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
            visPanel = new NewStarterVisualPanel1(this);
        }
        return visPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    LibrarySupportProject getProject() {
        return project;
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
        if (!validator.isValid(visPanel.getStarterName())) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewStarterWizardPanel1.class,
                    "NewStarterWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGED.starterNameInvalid"));    // NOI18N
            return false;
        }
        try {
            final File srcFile = FileUtil.toFile(srcDir);
            final File toCreate = new File(srcFile, visPanel.getStarterName()
                            + ".mf");                                                                      // NOI18N
            if (toCreate.exists()) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewStarterWizardPanel1.class,
                        "NewStarterWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGED.fileAlreadyExists")); // NOI18N
                return false;
            }
        } catch (final Exception e) {
            LOG.warn("could not check validity", e);                                                       // NOI18N
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewStarterWizardPanel1.class,
                    "NewJarWizardPanel.isValid().wizard.PROP_ERROR_MESSAGE.nameNotValid"));                // NOI18N
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
        project = (LibrarySupportProject)wizard.getProperty(
                NewStarterWizardAction.PROP_PROJECT);
        srcDir = (FileObject)wizard.getProperty(NewStarterWizardAction.PROP_SOURCE_DIR);
        visPanel.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        ((WizardDescriptor)settings).putProperty(
            NewStarterWizardAction.PROP_NEW_STARTER_NAME,
            visPanel.getStarterName());
        ((WizardDescriptor)settings).putProperty(
            NewStarterWizardAction.PROP_NEW_STARTER_BASE_FILE,
            visPanel.getManifestPath());
    }
}
