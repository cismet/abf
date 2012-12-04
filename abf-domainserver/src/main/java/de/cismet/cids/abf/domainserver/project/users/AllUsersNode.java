/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.apache.log4j.Logger;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupContextCookie;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.PropertyRefresh;

import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.11
 */
public final class AllUsersNode extends ProjectNode implements Refreshable, UserGroupContextCookie, PropertyRefresh {

    //~ Static fields/initializers ---------------------------------------------

    public static final UserGroup ALL_GROUP;

    static {
        ALL_GROUP = new UserGroup();
        ALL_GROUP.setName("all"); // NOI18N
        ALL_GROUP.setId(Integer.MIN_VALUE);
        ALL_GROUP.setUsers(null);
        ALL_GROUP.setDescription(null);
        ALL_GROUP.setDomain(null);
    }

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AllUsersNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AllUsersNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public AllUsersNode(final DomainserverProject project) {
        super(new AllUsersChildren(project), project);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "all_users.png");             // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(AllUsersNode.class, "AllUsersNode.displayName")); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] { CallableSystemAction.get(NewUserWizardAction.class) };
    }

    @Override
    public void refresh() {
        final Children c = getChildren();
        final Future<?> future = ((AllUsersChildren)c).refreshByNotify();

        try {
            future.get(30, TimeUnit.SECONDS);
            refreshProperties(false);
        } catch (final Exception ex) {
            LOG.warn("unsuccessful refresh: " + this, ex);
        }
    }

    @Override
    public UserGroup getUserGroup() {
        return ALL_GROUP;
    }

    @Override
    public void refreshProperties(final boolean forceInit) {
        for (final Node n : getChildren().getNodes()) {
            final PropertyRefresh pr = n.getCookie(PropertyRefresh.class);
            if (pr != null) {
                pr.refreshProperties(forceInit);
            }
        }
    }
}
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class AllUsersChildren extends ProjectChildren {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AllUsersChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public AllUsersChildren(final DomainserverProject project) {
        super(project);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createUserNodes(final Object o) {
        return new Node[] { new UserNode((User)o, project) };
    }

    @Override
    protected void threadedNotify() throws IOException {
        final List<User> users = project.getCidsDataObjectBackend().getAllEntities(User.class);
        Collections.sort(users, new Comparators.Users());
        setKeysEDT(users);
    }
}
