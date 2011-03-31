/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.registry;

import org.netbeans.spi.project.ui.support.CommonProjectActions;

import org.openide.actions.FileSystemAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.awt.Image;

import java.text.MessageFormat;

import java.util.ArrayList;

import javax.swing.Action;

import de.cismet.cids.abf.registry.messaging.MessagingNode;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.nodes.ConnectAction;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class RegistryProjectNode extends AbstractNode implements ConnectionListener {

    //~ Instance fields --------------------------------------------------------

    private final transient String htmlTemplate;
    private final transient RegistryProject project;
    private final transient Image icon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RegistryProjectNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public RegistryProjectNode(final RegistryProject project) {
        super(new RegistryProjectChildren(project), new ProxyLookup(new Lookup[] { Lookups.singleton(project) }));
        this.project = project;
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER + "registry.png"); // NOI18N
        htmlTemplate = "<font color='!textText'>"                                       // NOI18N
                    + project.getProjectDirectory().getName()
                    + "</font><font color='!controlShadow'> "                           // NOI18N
                    + "[cidsRegistry] {0}</font>";                                      // NOI18N
        project.addConnectionListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        if (project.isConnectionInProgress()) {
            if (project.isConnected()) {
                return MessageFormat.format(
                        htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                            RegistryProjectNode.class,
                            "RegistryProjectNode.getHtmlDisplayName().returnvalue.disconnect"));   // NOI18N
            } else {
                return MessageFormat.format(
                        htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                            RegistryProjectNode.class,
                            "RegistryProjectNode.getHtmlDisplayName().returnvalue.connect"));      // NOI18N
            }
        } else {
            if (project.isConnected()) {
                return MessageFormat.format(
                        htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                            RegistryProjectNode.class,
                            "RegistryProjectNode.getHtmlDisplayName().returnvalue.connected"));    // NOI18N
            } else {
                return MessageFormat.format(
                        htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                            RegistryProjectNode.class,
                            "RegistryProjectNode.getHtmlDisplayName().returnvalue.disconnected")); // NOI18N
            }
        }
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(ConnectAction.class),
                CommonProjectActions.customizeProjectAction(),
                SystemAction.get(FileSystemAction.class),
                null,
                CommonProjectActions.closeProjectAction()
            };
    }

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        if (event.isIndeterminate()) {
            setDisplayName(project.getProjectDirectory().getName() + " ..."); // NOI18N
        } else {
            fireDisplayNameChange(null, null);
        }
    }

    @Override
    public String getShortDescription() {
        return FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class RegistryProjectChildren extends Children.Keys {

    //~ Instance fields --------------------------------------------------------

    private final transient RegistryProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RegistryProjectChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public RegistryProjectChildren(final RegistryProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void addNotify() {
        final ArrayList<Node> keys = new ArrayList<Node>();
        for (final DataObject data : DataFolder.findFolder(project.getProjectDirectory()).getChildren()) {
            if ("properties".equalsIgnoreCase(data.getPrimaryFile().getExt())) { // NOI18N
                keys.add(data.getNodeDelegate());
            }
        }
        final Node messaging = new MessagingNode(project);
        keys.add(messaging);
        setKeys(keys);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected Node[] createNodes(final Object key) {
        if (key instanceof Node) {
            return new Node[] { (Node)key };
        } else {
            return new Node[] {};
        }
    }
}
