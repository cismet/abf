/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import java.awt.Component;
import java.awt.Dialog;

import java.text.MessageFormat;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.permission.NodePermission;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NewCatalogNodeWizardAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewCatalogNodeWizardAction.class);

    public static final String BACKEND_PROP = "backendProperty"; // NOI18N
    public static final String CATNODE_PROP = "catNodeProperty"; // NOI18N
    public static final String PERM_PROP = "permissionProperty"; // NOI18N
    public static final String PARENT_PROP = "parentProperty";   // NOI18N
    public static final String DOMAIN_PROP = "domainProperty";   // NOI18N
    public static final String PROJECT_PROP = "projectProperty"; // NOI18N

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                DomainserverContext.class,
                CatalogNodeContextCookie.class
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        final WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                new NewCatalogNodeWizardPanel1(),
                new NodeRightPropertyWizardPanel1()
            };
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

        return panels;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewCatalogNodeWizardAction.class,
                "NewCatalogNodeWizardAction.getName().returnvalue"); // NOI18N
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
        return true;
    }

    @Override
    protected void performAction(final Node[] nodes) {
        performAction(nodes, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nodes  DOCUMENT ME!
     * @param  node   DOCUMENT ME!
     */
    protected void performAction(final Node[] nodes, final CatNode node) {
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                NewCatalogNodeWizardAction.class,
                "NewCatalogNodeWizardAction.performAction(Node[],CatNode).wizard.title")); // NOI18N
        performAction(nodes, node, wizard);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nodes   DOCUMENT ME!
     * @param  node    DOCUMENT ME!
     * @param  wizard  DOCUMENT ME!
     */
    protected void performAction(final Node[] nodes, final CatNode node,
            final WizardDescriptor wizard) {
        CatNode catNode = node;
        CatNode parent = null;
        Domain domain = null;
        List<NodePermission> perms = null;
        final DomainserverProject project = nodes[0].getCookie(
                DomainserverContext.class).getDomainserverProject();
        final Backend backend = project.getCidsDataObjectBackend();
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        if (catNode == null) {
            catNode = new CatNode();
            catNode.setIsRoot((nodes[0].getCookie(
                        NavigatorNodeManagementContextCookie.class) != null)
                        || (nodes[0].getCookie(ClassNodeManagementContextCookie.class)
                            != null));
        } else {
            perms = new LinkedList<NodePermission>(catNode.getNodePermissions());
        }
        if (!catNode.getIsRoot()) {
            if (catNode.getId() == null) {
                // a new node shall be created, so parent of new node is the one
                // that caused this action
                parent = nodes[0].getCookie(CatalogNodeContextCookie.class).getCatNode();
            } else {
                parent = nodes[0].getCookie(CatalogNodeContextCookie.class).getParent();
                domain = backend.getLinkDomain(parent, catNode);
            }
        }
        wizard.putProperty(PROJECT_PROP, project);
        wizard.putProperty(PARENT_PROP, parent);
        wizard.putProperty(BACKEND_PROP, backend);
        wizard.putProperty(CATNODE_PROP, catNode);
        wizard.putProperty(DOMAIN_PROP, domain);
        wizard.putProperty(PERM_PROP, perms);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            CatNode cn = (CatNode)wizard.getProperty(CATNODE_PROP);
            perms = (List<NodePermission>)wizard.getProperty(PERM_PROP);
            if (perms != null) {
                cn.setNodePermissions(new HashSet<NodePermission>(perms));
            }
            final Domain d = (Domain)wizard.getProperty(DOMAIN_PROP);
            try {
                if (this instanceof ModifyNodeRightsWizardAction) {
                    cn.setNodePermissions(new HashSet<NodePermission>(perms));
                    cn = backend.store(cn);
                    if ((parent != null) && (d != null)) {
                        backend.setLinkDomain(parent, cn, d);
                    }
                } else {
                    cn = backend.addNode(parent, cn, d);
                }
                if (perms != null) {
                    for (final NodePermission np : perms) {
                        np.setNode(cn);
                    }
                    cn.setNodePermissions(new HashSet<NodePermission>(perms));
                }
            } catch (final Exception ex) {
                LOG.error("could not store node with permissions", ex); // NOI18N
                ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                        NewCatalogNodeWizardAction.class,
                        "NewCatalogNodeWizardAction.performAction(Node[],CatNode).ErrorUtils.nodeSave.message"),
                    ex);                                                // NOI18N
            } finally {
                nodes[0].getCookie(Refreshable.class).refresh();
                if ((nodes[0].getCookie(NavigatorNodeManagementContextCookie.class) == null)
                            && (nodes[0].getCookie(
                                    ClassNodeManagementContextCookie.class) == null)) {
                    project.getLookup()
                            .lookup(CatalogManagement.class)
                            .addedNode(nodes[0].getCookie(
                                    CatalogNodeContextCookie.class).getCatNode());
                }
            }
        }
    }

    @Override
    protected boolean enable(final Node[] node) {
        if (!super.enable(node)) {
            return false;
        }
        return customEnable(node);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected boolean customEnable(final Node[] node) {
        final CatalogNodeContextCookie cookie = node[0].getCookie(
                CatalogNodeContextCookie.class);
        if ((cookie != null) && (cookie.getCatNode().getDynamicChildren() != null)) {
            return false;
        }
        if ((cookie != null) && cookie.getCatNode().getNodeType().equals(
                        CatNode.Type.OBJECT.getType())) {
            return false;
        }
        return node[0].getCookie(DomainserverContext.class).getDomainserverProject().isConnected();
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
}
