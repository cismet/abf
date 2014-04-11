/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.netbeans.spi.project.ui.support.CommonProjectActions;

import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.awt.Image;

import java.io.File;

import javax.swing.Action;

import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.util.Utils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.3
 */
public final class ClientProjectNode extends FilterNode {

    //~ Instance fields --------------------------------------------------------

    private final transient Image icon;
    private final transient ClientProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClientProjectNode object.
     *
     * @param  node     DOCUMENT ME!
     * @param  project  DOCUMENT ME!
     */
    public ClientProjectNode(final Node node, final ClientProject project) {
        super(
            node,
            new FilterNode.Children(node),
            // The projects system wants the project in the Node's lookup.
            // NewAction and friends want the original Node's lookup.
            // Make a merge of both
            new ProxyLookup(
                new Lookup[] {
                    Lookups.singleton(project),
                    node.getLookup()
                }));
        this.project = project;
        this.icon = ImageUtilities.loadImage(ClientProjectNode.class.getPackage().getName().replaceAll("\\.", "/") // NOI18N
                        + "/client.png");                                             // NOI18N
        setDisplayName(project.getProjectDirectory().getName() + " [cidsClient]");    // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        final PropertyProvider provider = PropertyProvider.getInstance(project.getProjectProperties());
        final String path = Utils.getPath(provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE));
        if ((path == null) || path.trim().equals(""))                                   // NOI18N
        {
            final Image badge = ImageUtilities.loadImage(
                    ClientProjectNode.class.getPackage().getName().replaceAll("\\.", "/")
                            + "/warningBadge.gif");                                     // NOI18N
            setShortDescription(NbBundle.getMessage(
                    ClientProjectNode.class,
                    "ClientProjectNode.getIcon(int).shortDescription.keystoreNotSet")); // NOI18N

            return ImageUtilities.mergeImages(icon, badge, 3, 3);
        }
        final File ks = new File(path);
        if (!ks.exists() || !ks.isFile() || !ks.canRead()) {
            final Image badge = ImageUtilities.loadImage(
                    ClientProjectNode.class.getPackage().getName().replaceAll("\\.", "/")
                            + "/warningBadge.gif");                                            // NOI18N
            setShortDescription(NbBundle.getMessage(
                    ClientProjectNode.class,
                    "ClientProjectNode.getIcon(int).shortDescription.givenKeystoreUnusable")); // NOI18N

            return ImageUtilities.mergeImages(icon, badge, 3, 3);
        }

        setShortDescription(FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath());

        return icon;
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(final boolean context) {
        return new Action[] {
                CommonProjectActions.customizeProjectAction(),
                null,
                CommonProjectActions.closeProjectAction()
            };
    }
}
