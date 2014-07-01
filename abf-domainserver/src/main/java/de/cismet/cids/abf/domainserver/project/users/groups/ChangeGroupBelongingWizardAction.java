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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.UserContextCookie;
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
public final class ChangeGroupBelongingWizardAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ChangeGroupBelongingWizardAction.class);

    public static final String USER_PROP = "userProperty";                    // NOI18N
    public static final String PROJECT_PROP = "projectProperty";              // NOI18N
    public static final String TOUCHED_GROUPS_PROP = "touchedGroupsProperty"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    // it is impossible to create a typed array
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] { new ChangeGroupBelongingWizardPanel1() };
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
                    jc.putClientProperty(
                        WizardDescriptor.PROP_AUTO_WIZARD_STYLE,
                        Boolean.TRUE);
                    // Show steps on the left side
                    jc.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_DISPLAYED,
                        Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }

        return Arrays.copyOf(panels, panels.length);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(
                ChangeGroupBelongingWizardAction.class,
                "ChangeGroupBelongingWizardAction.getName()returnvalue"); // NOI18N
    }

    @Override
    public String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "add_user_to_group.png"; // NOI18N
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
                DomainserverContext.class,
                UserContextCookie.class
            };
    }

    @Override
    protected void performAction(final Node[] node) {
        final User user = node[0].getCookie(UserContextCookie.class).getUser();
        final DomainserverProject project = node[0].getCookie(
                DomainserverContext.class).getDomainserverProject();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));                                 // NOI18N
        wizard.setTitle(NbBundle.getMessage(
                ChangeGroupBelongingWizardAction.class,
                "ChangeGroupBelongingWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        final Set<UserGroup> originalGroups = new HashSet<UserGroup>(user.getUserGroups());
        wizard.putProperty(USER_PROP, user);
        wizard.putProperty(PROJECT_PROP, project);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (cancelled) {
            user.setUserGroups(originalGroups);
        } else {
            UserManagement.ACTION_DISPATCHER.execute(new Runnable() {

                    @Override
                    public void run() {
                        // the api only delivers objects, no chance to achieve type safety
                        @SuppressWarnings("unchecked")
                        final List<UserGroup> groups = (List<UserGroup>)wizard.getProperty(TOUCHED_GROUPS_PROP);
                        final Backend backend = project.getCidsDataObjectBackend();
                        for (final UserGroup ug : groups) {
                            try {
                                backend.store(ug);
                                backend.store(user);
                            } catch (final Exception ex) {
                                LOG.error("could not store usergroup: " + ug.getName(), ex); // NOI18N
                                ErrorManager.getDefault().notify(ex);
                            }
                        }

                        if (DomainserverOptionsPanelController.isAutoRefresh()) {
                            project.getLookup().lookup(UserManagement.class).refreshGroups(groups);
                        }
                    }
                });
        }
    }

    @Override
    protected boolean enable(final Node[] node) {
        if (!super.enable(node)) {
            return false;
        }

        if (node[0].getCookie(UserContextCookie.class) == null) {
            return false;
        }

        final DomainserverContext context = node[0].getCookie(DomainserverContext.class);
        if (context == null) {
            return false;
        }

        return context.getDomainserverProject().isConnected();
    }
}
