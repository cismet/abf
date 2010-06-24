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
import org.openide.windows.TopComponent;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.cidsclass.ClassDiagramTopComponent;
import de.cismet.cids.abf.domainserver.project.view.ViewNode;
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
        project.addConnectionListener(this);
        image = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "class_management.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(ViewManagement.class, "ViewManagement.ViewManagement(DomainserverProject).displayName"));    // NOI18N
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
    public void connectionStatusChanged(final boolean isConnected) {
        if (isConnected) {
            setChildren(new ViewManagementChildren(project));
        } else {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusIndeterminate() {
        // do nothing
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshChildren() {
        final Children ch = getChildren();
        if (ch instanceof ViewManagementChildren) {
            ((ViewManagementChildren)ch).refreshAll();
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class ViewManagementChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ViewManagementChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ViewManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ViewManagementChildren(final DomainserverProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createNodes(final Object object) {
        if (object instanceof ViewNode) {
            return new Node[] { (ViewNode)object };
        } else {
            return new Node[] {};
        }
    }

    /**
     * DOCUMENT ME!
     */
    void refreshAll() {
        addNotify();
    }

    @Override
    protected void addNotify() {
        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final List<ViewNode> all = new ArrayList<ViewNode>();
                            final SAXReader reader = new SAXReader();
                            project.getProjectDirectory().refresh();
                            for (final Enumeration<? extends FileObject> e = project.getProjectDirectory().getData(
                                                false); e.hasMoreElements();) {
                                final FileObject fo = e.nextElement();
                                if (fo.getName().startsWith("csClassView.") && fo.getExt().equalsIgnoreCase("xml")) // NOI18N
                                {
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
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        setKeys(all);
                                    }
                                });
                        } catch (final Exception ex) {
                            LOG.error("could not create diagrams", ex); // NOI18N
                            ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    ViewManagementChildren.class,
                                    "ViewManagement.addNotify().ErrorUtils.message"),       // NOI18N
                                ex);
                        }
                    }
                }, getClass().getSimpleName() + "::addNotifyRunner");   // NOI18N
        t.start();
    }
}
