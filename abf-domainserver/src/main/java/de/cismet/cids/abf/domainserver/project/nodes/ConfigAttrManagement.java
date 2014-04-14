/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.nodes;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.configattr.ActionConfigAttrRootNode;
import de.cismet.cids.abf.domainserver.project.configattr.StringConfigAttrRootNode;
import de.cismet.cids.abf.domainserver.project.configattr.XMLConfigAttrRootNode;
import de.cismet.cids.abf.options.DomainserverOptionsPanelController;
import de.cismet.cids.abf.utilities.Refreshable;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ConfigAttrManagement extends ProjectNode implements Refreshable {

    //~ Instance fields --------------------------------------------------------

    private final transient Image keysIcon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigAttrManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ConfigAttrManagement(final DomainserverProject project) {
        super(new ConfigAttrManagementChildren(project), project);

        setName(NbBundle.getMessage(ConfigAttrManagement.class, "ConfigAttrManagement.name")); // NOI18N

        keysIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "keys_16.png"); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int type) {
        return keysIcon;
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(final boolean context) {
        if (DomainserverOptionsPanelController.isAutoRefresh()) {
            return super.getActions(context);
        } else {
            return new Action[] { CallableSystemAction.get(RefreshAction.class) };
        }
    }

    @Override
    public void refresh() {
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    final Node[] children = getChildren().getNodes(false);
                    for (final Node childNode : children) {
                        final Refreshable childRefreshable = childNode.getCookie(Refreshable.class);
                        if (childRefreshable != null) {
                            childRefreshable.refresh();
                        }
                    }
                }
            };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class ConfigAttrManagementChildren extends Children.Keys {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CatalogManagementChildren object.
         *
         * @param  project  DOCUMENT ME!
         */
        public ConfigAttrManagementChildren(final DomainserverProject project) {
            setKeys(
                new Object[] {
                    new StringConfigAttrRootNode(project),
                    new ActionConfigAttrRootNode(project),
                    new XMLConfigAttrRootNode(project)
                });
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createNodes(final Object key) {
            return new Node[] { (Node)key };
        }
    }
}
