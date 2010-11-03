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
import org.openide.util.HelpCtx;

import java.awt.Component;

import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.3
 */
public class AddFilesWizardPanel1 implements WizardDescriptor.Panel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String CHOOSEN_PACKAGE = "choosenPackage"; // NOI18N
    public static final String CHOOSEN_FILES = "choosenFiles";     // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient FileObject root;
    private final transient FileObject current;

    /**
     * The visual component that displays this panel. If you need to access the component from this class, just use
     * getComponent().
     */
    private transient AddFilesVisualPanel1 visPanel;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AddFilesWizardPanel1 object.
     *
     * @param  root     DOCUMENT ME!
     * @param  current  DOCUMENT ME!
     */
    public AddFilesWizardPanel1(final FileObject root, final FileObject current) {
        this.root = root;
        this.current = current;
    }

    //~ Methods ----------------------------------------------------------------

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (visPanel == null) {
            visPanel = new AddFilesVisualPanel1(root, current);
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
        // If it is always OK to press Next or Finish, then:
        return true;
            // If it depends on some condition (form filled out...), then:
            // return someCondition();
            // and when this condition changes (last form field filled in...) then:
            // fireChangeEvent();
            // and uncomment the complicated stuff below.
    }

    @Override
    public final void addChangeListener(final ChangeListener l) {
        // not needed
    }

    @Override
    public final void removeChangeListener(final ChangeListener l) {
        // not needed
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(final Object settings) {
        // not needed
    }

    @Override
    public void storeSettings(final Object settings) {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        wizard.putProperty(CHOOSEN_FILES, visPanel.getChoosenFiles());
        wizard.putProperty(CHOOSEN_PACKAGE, visPanel.getChoosenPackage());
    }
}
