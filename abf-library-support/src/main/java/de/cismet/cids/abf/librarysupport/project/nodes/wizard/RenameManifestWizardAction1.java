/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.awt.Component;
import java.awt.Dialog;

import java.text.MessageFormat;

import java.util.Arrays;

import javax.swing.JComponent;

import de.cismet.cids.abf.librarysupport.project.nodes.cookies.ManifestProviderCookie;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.3
 */
public final class RenameManifestWizardAction1 extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_MANIFEST = "property_Manifest";                 // NOI18N
    public static final String PROP_NEW_MANIFEST_NAME = "property_newManifestName"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;

    //~ Methods ----------------------------------------------------------------

    /**
     * Initialize panels representing individual wizard's steps and sets various properties for them influencing wizard
     * appearance.
     *
     * @return  DOCUMENT ME!
     */
    // it is impossible to create a typed array
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] { new RenameManifestWizardPanel1() };
            final String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                final Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    final JComponent jc = (JComponent)c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the
                    // background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }

        return Arrays.copyOf(panels, panels.length);
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                RenameManifestWizardAction1.class,
                "RenameManifestWizardAction1.getName().returnvalue"); // NOI18N
    }

    @Override
    public String iconResource() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final ManifestProviderCookie cookie = nodes[0].getCookie(ManifestProviderCookie.class);
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent()
        // .getName()
        wizard.putProperty(PROP_MANIFEST, cookie.getManifest());
        wizard.setTitleFormat(new MessageFormat("{0}"));                            // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                RenameManifestWizardAction1.class,
                "RenameManifestWizardAction1.performAction(Node[]).wizard.title")); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            nodes[0].setName(wizard.getProperty(PROP_NEW_MANIFEST_NAME).toString());
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1)) {
            return false;
        }

        return nodes[0].getCookie(ManifestProviderCookie.class) != null;
    }
}
