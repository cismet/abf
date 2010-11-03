/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.javaclass;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;

import java.text.MessageFormat;

import java.util.Arrays;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.JavaClassManagement;

import de.cismet.cids.jpa.entity.cidsclass.JavaClass;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.4
 */
public final class NewJavaClassWizardAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewJavaClassWizardAction.class);

    public static final String JAVACLASS_PROPERTY = "javaclassProperty"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel[] panels;

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        final DomainserverProject proj = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels(proj));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent()
        // .getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));                         // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                NewJavaClassWizardAction.class,
                "NewJavaClassWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        final Dialog dialog;
//        try
//        {
        dialog = DialogDisplayer.getDefault().createDialog(wizard);
//        }catch(final NullPointerException npe)
//        {
//            LOG.error("cids distribution corrupted", npe);
//            ErrorUtils.showErrorMessage("Die cidsDistribution verfügt nicht ü" +
//                    "ber ein lib Verzeichnis!", "cidsDistribution fehlerhaft",
//                    npe);
//            return;
//        }
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final JavaClass javaClass = (JavaClass)wizard.getProperty(
                    JAVACLASS_PROPERTY);
            try {
                proj.getCidsDataObjectBackend().store(javaClass);
            } catch (final Exception ex) {
                LOG.error("could not store javaclass", ex); // NOI18N
                ErrorManager.getDefault().notify(ex);
            }
            final JavaClassManagement jcm = proj.getLookup().lookup(JavaClassManagement.class);
            if (jcm != null) {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            jcm.refreshChildren();
                        }
                    });
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   proj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel[] getPanels(final DomainserverProject proj) {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] { new NewJavaClassWizardPanel1(proj) };
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
                    jc.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                        Integer.valueOf(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,
                        steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(
                        WizardDescriptor.PROP_AUTO_WIZARD_STYLE,
                        Boolean.TRUE);
                    // Show steps on the left side with the image on the
                    // background
                    jc.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_DISPLAYED,
                        Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED,
                        Boolean.TRUE);
                }
            }
        }
        return Arrays.copyOf(panels, panels.length);
    }

    // as long as cookieaction does not act as desired
    @Override
    protected boolean enable(final Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
        if (nodes[0].getCookie(JavaClassManagementContextCookie.class) == null) {
            return false;
        }
        return nodes[0].getCookie(DomainserverContext.class).getDomainserverProject().isConnected();
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewJavaClassWizardAction.class,
                "NewJavaClassWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
