/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.options.DomainserverOptionsPanelController;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.13
 */
public final class CopyUserWizardAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CopyUserWizardAction.class);

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel[] panels;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                    new NewUserWizardPanel1(),
                    new NewUserWizardPanel2()
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
        return NbBundle.getMessage(CopyUserWizardAction.class, "CopyUserWizardAction.getName().name"); // NOI18N
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
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { DomainserverContext.class, UserContextCookie.class };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final DomainserverContext cookie = nodes[0].getCookie(DomainserverContext.class);
        final UserContextCookie ucc = nodes[0].getCookie(UserContextCookie.class);

        final DomainserverProject project = cookie.getDomainserverProject();
        final User u = ucc.getUser();
        final List<UserGroup> userGroups = new ArrayList<UserGroup>(u.getUserGroups());

        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));                     // NOI18N
        wizard.setTitle(NbBundle.getMessage(
                CopyUserWizardAction.class,
                "CopyUserWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        wizard.putProperty(NewUserWizardAction.PROJECT_PROP, project);

        final User user = new User();
        user.setAdmin(u.isAdmin());
        user.setLoginname(u.getLoginname()
                    + NbBundle.getMessage(
                        CopyUserWizardAction.class,
                        "CopyUserWizardAction.performAction(Node[]).user.copySuffix"));
        user.setUserGroups(u.getUserGroups());
        user.setPassword(null);

        wizard.putProperty(NewUserWizardAction.USER_PROP, user);
        wizard.putProperty(NewUserWizardAction.USERGROUP_PROP, userGroups);

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();

        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            UserManagement.ACTION_DISPATCHER.execute(new Runnable() {

                    @Override
                    public void run() {
                        User newUser = (User)wizard.getProperty(NewUserWizardAction.USER_PROP);

                        // TODO: add attributes
                        final List<UserGroup> ugs = (List<UserGroup>)wizard.getProperty(
                                NewUserWizardAction.USERGROUP_PROP);
                        final Backend backend = project.getCidsDataObjectBackend();
                        try {
                            newUser = backend.store(newUser);
                            for (final UserGroup ug : ugs) {
                                ug.getUsers().add(newUser);
                                try {
                                    backend.store(ug);
                                } catch (final Exception e) {
                                    LOG.error("could not store usergroup: " + ug.getName(), e); // NOI18N
                                }

                                try {
                                    for (final UserGroup group : u.getUserGroups()) {
                                        if (group.equals(ug)) {
                                            final List<ConfigAttrEntry> caes = backend.getEntries(
                                                    group.getDomain(),
                                                    group,
                                                    u,
                                                    project.getRuntimeProps().getProperty("serverName"), // NOI18N
                                                    false);
                                            for (final ConfigAttrEntry cae : caes) {
                                                final ConfigAttrEntry clone = new ConfigAttrEntry();
                                                clone.setDomain(cae.getDomain());
                                                clone.setKey(cae.getKey());
                                                clone.setType(cae.getType());
                                                clone.setUser(newUser);
                                                clone.setUsergroup(group);
                                                clone.setValue(cae.getValue());

                                                backend.storeEntry(clone);
                                            }
                                        }
                                    }
                                } catch (final Exception e) {
                                    LOG.error(
                                        "could not store config attr entries for user in usergroup: [user="
                                                + newUser
                                                + "|ug="
                                                + ug
                                                + "]",
                                        e);
                                    ErrorManager.getDefault().notify(e);
                                }
                            }
                        } catch (final Exception e) {
                            LOG.error("could not store new user: " + newUser.getLoginname(), e); // NOI18N
                            ErrorManager.getDefault().notify(e);
                        } finally {
                            if (DomainserverOptionsPanelController.isAutoRefresh()) {
                                project.getLookup().lookup(UserManagement.class).refreshGroups(ugs);
                            }
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

        return node[0].getCookie(DomainserverContext.class).getDomainserverProject().isConnected();
    }
}
