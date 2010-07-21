/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import java.awt.Component;
import java.awt.Dialog;

import java.text.MessageFormat;

import java.util.Arrays;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class EditRightsWizardAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 4843357411059143935L;
    private static final transient Logger LOG = Logger.getLogger(EditRightsWizardAction.class);

    public static final String PROP_ARRAY_CIDSCLASSES = "__property_array_cidsclasses__"; // NOI18N
    public static final String PROP_BACKEND = "__property_backend__";                     // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel[] getPanels() {
        final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] { new EditRightsWizardPanel1() };
        final String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            final Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) {
                // assume Swing components
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

        return Arrays.copyOf(panels, panels.length);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EditRightsWizardAction.class, "EditRightsWizardAction.getName().returnvalue"); // NOI18N
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
    protected int mode() {
        return MODE_ALL;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[] {
                DomainserverContext.class,
                CidsClassContextCookie.class
            };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final CidsClass[] classes = new CidsClass[nodes.length];
        for (int i = 0; i < nodes.length; ++i) {
            classes[i] = nodes[i].getCookie(CidsClassContextCookie.class).getCidsClass();
        }
        final Backend backend = nodes[0].getCookie(DomainserverContext.class)
                    .getDomainserverProject()
                    .getCidsDataObjectBackend();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        wizard.setTitleFormat(new MessageFormat("{0}"));                       // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                EditRightsWizardAction.class,
                "EditRightsWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        wizard.putProperty(PROP_ARRAY_CIDSCLASSES, classes);
        wizard.putProperty(PROP_BACKEND, backend);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        // NOTE: currently all changes are reflected directly into the class
        // objects
        if (!cancelled) {
            final CidsClass[] success = new CidsClass[classes.length];
            try {
                for (int i = 0; i < classes.length; ++i) {
                    success[i] = backend.store(classes[i]);
                }
            } catch (final Exception e) {
                LOG.error("could not store classes", e);                                           // NOI18N
                final StringBuffer successful = new StringBuffer();
                for (int i = 0; (i < success.length) && (success[i] != null); ++i) {
                    successful.append("\n\t").append(success[i].toString());                       // NOI18N
                }
                ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                                EditRightsWizardAction.class,
                                "EditRightsWizardAction.performAction(Node[]).ErrorUtils.message") // NOI18N
                            + successful.toString(),
                    org.openide.util.NbBundle.getMessage(
                        EditRightsWizardAction.class,
                        "EditRightsWizardAction.performAction(Node[]).ErrorUtils.title"),          // NOI18N
                    e);
            }
        }
        for (final Node node : nodes) {
            node.getCookie(Refreshable.class).refresh();
        }
    }
}
