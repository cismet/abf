/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users.groups;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
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
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.UserManagementContextCookie;
import de.cismet.cids.abf.options.DomainserverOptionsPanelController;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class NewUsergroupWizardAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(NewUsergroupWizardAction.class);

    public static final String PROJECT_PROP = "projectProperty";     // NOI18N
    public static final String USERGROUP_PROP = "usergroupProperty"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel[] panels;

    //~ Methods ----------------------------------------------------------------

    /**
     * Initialize panels representing individual wizard's steps and sets various properties for them influencing wizard
     * appearance.
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                    new NewUsergroupWizardPanel1(),
                    new NewUsergroupWizardPanel2()
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
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side
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
        return NbBundle.getMessage(NewUsergroupWizardAction.class, "NewUsergroupWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "add_group.png"; // NOI18N
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
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                UserManagementContextCookie.class,
                DomainserverContext.class
            };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));                         // NOI18N
        wizard.setTitle(NbBundle.getMessage(
                NewUsergroupWizardAction.class,
                "NewUsergroupWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        wizard.putProperty(PROJECT_PROP, project);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            UserManagement.ACTION_DISPATCHER.execute(new Runnable() {

                    @Override
                    public void run() {
                        UserGroup newGroup = (UserGroup)wizard.getProperty(USERGROUP_PROP);
                        try {
                            final Backend b = project.getCidsDataObjectBackend();
                            newGroup.setPriority(b.getLowestUGPrio());
                            newGroup = b.store(newGroup);

                            for (final User u : newGroup.getUsers()) {
                                u.getUserGroups().add(newGroup);
                                b.store(u);
                            }
                        } catch (final Exception e) {
                            LOG.error("could not store new usergroup", e); // NOI18N
                            ErrorManager.getDefault().notify(e);
                        }
                        if (DomainserverOptionsPanelController.isAutoRefresh()) {
                            project.getLookup().lookup(UserManagement.class).refresh();
                        }
                    }
                });
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }
        final DomainserverContext dc = nodes[0].getCookie(DomainserverContext.class);
        if (dc == null) {
            LOG.warn("domainservercontext is null"); // NOI18N
            return false;
        }

        return dc.getDomainserverProject().isConnected();
    }
}
