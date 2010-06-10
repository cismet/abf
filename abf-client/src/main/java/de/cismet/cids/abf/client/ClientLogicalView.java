/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.netbeans.spi.project.ui.LogicalViewProvider;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class ClientLogicalView implements LogicalViewProvider {

    //~ Instance fields --------------------------------------------------------

    private final transient ClientProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClientLogicalView object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ClientLogicalView(final ClientProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Node findPath(final Node node, final Object object) {
        return null;
    }

    @Override
    public Node createLogicalView() {
        // Get the DataObject that represents it
        final DataFolder projDirDataObject = DataFolder.findFolder(project.getProjectDirectory());
        final Children children = projDirDataObject.createNodeChildren(
                new DataFilter() {

                    @Override
                    public boolean acceptDataObject(final DataObject dataObject) {
                        final FileObject fo = dataObject.getPrimaryFile();
                        final String name = fo.getName();
                        final String ext = fo.getExt();
                        // CVS Folder
                        if (
                            fo.isFolder()
                                    && ("cvs".equalsIgnoreCase(name)      // NOI18N
                                        || ClientProjectFactory.PROJECT_DIR.equalsIgnoreCase(name))) {
                            return false;
                        }
                        if (
                            fo.isFolder()
                                    || "jnlp".equalsIgnoreCase(ext)       // NOI18N
                                    || "cfg".equalsIgnoreCase(ext)        // NOI18N
                                    || "properties".equalsIgnoreCase(ext) // NOI18N
                                    || "xml".equalsIgnoreCase(ext)) {     // NOI18N
                            return true;
                        }
                        return false;
                    }
                });
        return new ClientProjectNode(new AbstractNode(children), project);
    }
}
