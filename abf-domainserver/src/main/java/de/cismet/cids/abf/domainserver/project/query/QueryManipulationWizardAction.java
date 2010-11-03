/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.awt.Component;
import java.awt.Dialog;

import java.text.MessageFormat;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.utilities.Refreshable;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class QueryManipulationWizardAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            QueryManipulationWizardAction.class);

    public static final String QUERY_PROPERTY = "query";                     // NOI18N
    public static final String BACKEND_PROPERTY = "backend";                 // NOI18N
    public static final String PROJECT_PROPERTIES_PROPERTY = "projectProps"; // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected WizardDescriptor.Panel[] getPanels() {
        final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {
                new QueryManipulationWizardPanel1(),
                new QueryManipulationWizardPanel2(),
                new QueryManipulationWizardPanel3(),
                new QueryManipulationWizardPanel4()
            };
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
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE,
                    Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED,
                    Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED,
                    Boolean.TRUE);
            }
        }
        return panels;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodes  DOCUMENT ME!
     * @param   wd     DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    protected void performAction(final Node[] nodes, final WizardDescriptor wd) {
        final DomainserverContext context = nodes[0].getCookie(
                DomainserverContext.class);
        final Backend b = context.getDomainserverProject().getCidsDataObjectBackend();
        final WizardDescriptor wizard = (wd == null)
            ? new WizardDescriptor(
                new WizardDescriptor.ArrayIterator(getPanels())) : wd;
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));                                               // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                QueryManipulationWizardAction.class,
                "QueryManipulationWizardAction.performAction(Node[],WizardDescriptor).wizard.title")); // NOI18N
        wizard.putProperty(BACKEND_PROPERTY, b);
        wizard.putProperty(PROJECT_PROPERTIES_PROPERTY, context.getDomainserverProject().getRuntimeProps());
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setResizable(false);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            Query q = null;
            try {
                q = (Query)wizard.getProperty(QUERY_PROPERTY);
                b.store(q);
            } catch (final Exception ex) {
                LOG.error("could not store query '"                                                    // NOI18N
                            + q.getName()
                            + "'", ex);                                                                // NOI18N
            }
            Refreshable r = nodes[0].getCookie(Refreshable.class);
            if (r == null) {
                r = nodes[0].getParentNode().getCookie(Refreshable.class);
                if (r == null) {
                    throw new IllegalStateException(
                        "refreshable cannot be null");                                                 // NOI18N
                }
            }
            r.refresh();
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length == 0)) {
            return false;
        }
        final DomainserverContext context = nodes[0].getCookie(
                DomainserverContext.class);
        if (context == null) {
            return false;
        }
        return context.getDomainserverProject().isConnected();
    }
}
