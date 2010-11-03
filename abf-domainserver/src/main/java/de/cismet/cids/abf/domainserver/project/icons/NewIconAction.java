/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.icons;

import org.apache.log4j.Logger;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

import javax.swing.JOptionPane;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.IconManagement;

import de.cismet.cids.jpa.entity.cidsclass.Icon;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.4
 */
public final class NewIconAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewIconAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                DomainserverContext.class,
                IconManagementContextCookie.class
            };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final DomainserverProject project = ((DomainserverContext)nodes[0].getCookie(DomainserverContext.class))
                    .getDomainserverProject();
        try {
            String icn = "";                                                                              // NOI18N
            while (icn != null) {
                icn = JOptionPane.showInputDialog(
                        WindowManager.getDefault().getMainWindow(),
                        org.openide.util.NbBundle.getMessage(
                            NewIconAction.class,
                            "NewIconAction.performAction(Node[]).JOptionPane.giveIconFilename.message")); // NOI18N
                if (icn == null) {
                    return;
                }
                // currently only names of length <= 32 are supported by db
                if ((icn.trim().length() > 0) && (icn.length() <= 32)) {
                    final Icon icon = new Icon();
                    icon.setFileName(icn);
                    icon.setName(icn);
                    project.getCidsDataObjectBackend().store(icon);
                    project.getLookup().lookup(IconManagement.class).refreshChildren();
                    icn = null;
                } else {
                    JOptionPane.showMessageDialog(
                        WindowManager.getDefault().getMainWindow(),
                        org.openide.util.NbBundle.getMessage(
                            NewIconAction.class,
                            "NewIconAction.performAction(Node[]).JOptionPane.givenFilenameInvalid.message"), // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            NewIconAction.class,
                            "NewIconAction.performAction(Node[]).JOptionPane.givenFilenameInvalid.title"), // NOI18N
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (final Exception e) {
            LOG.error("could not create new icon", e);                                                     // NOI18N
            ErrorManager.getDefault().notify(e);                                                           // NOI18N
        }
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewIconAction.class,
                "NewIconAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        final boolean enable = super.enable(nodes);
        if (!enable) {
            return false;
        }
        final DomainserverContext dc = nodes[0].getCookie(DomainserverContext.class);
        if (dc == null) {
            LOG.warn("domainservercontext is null, " // NOI18N
                        + "cookieaction failed again"); // NOI18N
            return false;
        }
        return dc.getDomainserverProject().isConnected();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
