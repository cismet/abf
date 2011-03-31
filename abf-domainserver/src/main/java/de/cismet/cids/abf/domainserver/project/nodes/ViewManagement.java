/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.nodes;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import java.awt.Image;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.ClassDiagramTopComponent;
import de.cismet.cids.abf.domainserver.project.view.ViewNode;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.3
 */
public final class ViewManagement extends ProjectNode implements ConnectionListener {

    //~ Instance fields --------------------------------------------------------

    private final transient Image image;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ViewManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ViewManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
        image = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "class_management.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                ViewManagement.class,
                "ViewManagement.ViewManagement(DomainserverProject).displayName"));                  // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        return image;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return image;
    }

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        if (event.isConnected() && !event.isIndeterminate()) {
            setChildrenEDT(new ViewManagementChildren(project));
        } else {
            setChildrenEDT(Children.LEAF);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshChildren() {
        final Children ch = getChildren();
        if (ch instanceof ProjectChildren) {
            ((ProjectChildren)ch).refreshByNotify();
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class ViewManagementChildren extends ProjectChildren {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ViewManagementChildren.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ViewManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ViewManagementChildren(final DomainserverProject project) {
        super(project);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createUserNodes(final Object o) {
        if (o instanceof ViewNode) {
            return new Node[] { (ViewNode)o };
        } else {
            return new Node[] {};
        }
    }

    @Override
    protected void threadedNotify() throws IOException {
        try {
            final List<ViewNode> all = new ArrayList<ViewNode>();
            final SAXReader reader = new SAXReader();
            project.getProjectDirectory().refresh();

            final Enumeration<? extends FileObject> e = project.getProjectDirectory().getData(false);
            while (e.hasMoreElements()) {
                final FileObject fo = e.nextElement();
                if (fo.getName().startsWith("csClassView.") && fo.getExt().equalsIgnoreCase("xml")) { // NOI18N
                    final Document d = reader.read(FileUtil.toFile(fo));
                    all.add(new ViewNode(d, project));
                }
            }

            final Set openTCs = TopComponent.getRegistry().getOpened();
            for (final Object o : openTCs) {
                if (o instanceof ClassDiagramTopComponent) {
                    final Document d = ((ClassDiagramTopComponent)o).getViewDocument();
                    final ViewNode viewNode = new ViewNode(d, project);
                    if (!all.contains(viewNode)) {
                        viewNode.setTransient(true);
                        viewNode.setTopComponent((ClassDiagramTopComponent)o);
                        all.add(viewNode);
                    }
                }
            }
            Collections.sort(all);
            setKeysEDT(all);
        } catch (final Exception ex) {
            LOG.error("could not create diagrams", ex);               // NOI18N
            ErrorUtils.showErrorMessage(
                org.openide.util.NbBundle.getMessage(
                    ViewManagementChildren.class,
                    "ViewManagement.addNotify().ErrorUtils.message"), // NOI18N
                ex);
        }
    }
}
