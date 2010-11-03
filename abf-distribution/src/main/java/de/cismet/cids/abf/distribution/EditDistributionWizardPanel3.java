/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.distribution;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.io.File;

import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class EditDistributionWizardPanel3 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private final transient ChangeSupport changeSupport;

    private transient WizardDescriptor wizard;
    private transient EditDistributionVisualPanel3 component;
    private transient String createdPath;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new EditDistributionWizardPanel3 object.
     */
    public EditDistributionWizardPanel3() {
        changeSupport = new ChangeSupport(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new EditDistributionVisualPanel3(this);
        }

        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getCreatedPath() {
        return createdPath;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        final String path = component.getCreatedDir();

        if (path.isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Invalid folder name");

            return false;
        }

        final File file = new File(path);
        if (file.exists()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Folder already exists");

            return false;
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
        createdPath = (String)wizard.getProperty(CreateDistributionAction.PROP_CHOSEN_OUTDIR);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(CreateDistributionAction.PROP_CHOSEN_OUTDIR, component.getCreatedDir());
    }
}
