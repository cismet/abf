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

import de.cismet.cids.abf.utilities.NameValidator;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public final class NewJarWizardPanel implements WizardDescriptor.Panel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewJarWizardPanel.class);

    //~ Instance fields --------------------------------------------------------

    /**
     * The visual component that displays this panel. If you need to access the component from this class, just use
     * getComponent().
     */
    private transient NewJarVisualPanel visPanel;
    private transient WizardDescriptor wizard;
    private transient File srcDir;
    private final transient NameValidator validator;

    private final transient Set<ChangeListener> listeners;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewJarWizardPanel object.
     */
    public NewJarWizardPanel() {
        this.validator = new NameValidator(NameValidator.NAME_MEDIUM);
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    // Get the visual component for thÏe panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (visPanel == null) {
            visPanel = new NewJarVisualPanel(this);
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
        if (!validator.isValid(visPanel.getJarName())) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewJarWizardPanel.class,
                    "NewJarWizardPanel.isValid().wizard.PROP_ERROR_MESSAGE.groupNotValid"));    // NOI18N
            return false;
        }
        try {
            final File toCreate = new File(srcDir, visPanel.getJarName());
            if (toCreate.exists()) {
                wizard.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewJarWizardPanel.class,
                        "NewJarWizardPanel.isValid().wizard.PROP_ERROR_MESSAGE.groupExisits")); // NOI18N
                return false;
            }
        } catch (final Exception e) {
            LOG.warn("could not check validity", e);                                            // NOI18N
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                org.openide.util.NbBundle.getMessage(
                    NewJarWizardPanel.class,
                    "NewJarWizardPanel.isValid().wizard.PROP_ERROR_MESSAGE.nameNotValid"));     // NOI18N
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
        srcDir = FileUtil.toFile((FileObject)wizard.getProperty(
                    NewJarWizardAction.PROP_SOURCE_DIR));
        visPanel.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        ((WizardDescriptor)settings).putProperty(NewJarWizardAction.PROP_NEW_JAR_NAME, visPanel.getJarName());
    }
}
