/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.broker;

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
public class BrokerLogicalView implements LogicalViewProvider {

    //~ Instance fields --------------------------------------------------------

    private final transient BrokerProject project;
    private transient volatile BrokerProjectNode view;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BrokerLogicalView object.
     *
     * @param  project  DOCUMENT ME!
     */
    public BrokerLogicalView(final BrokerProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Node findPath(final Node node, final Object object) {
        return null;
    }

    @Override
    public Node createLogicalView() {
        if (view == null) {
            synchronized (this) {
                if (view == null) {
                    // Get the DataObject that represents it
                    final DataFolder projDirDataObject = DataFolder.findFolder(project.getProjectDirectory());
                    final Children children = projDirDataObject.createNodeChildren(
                            new DataFilter() {

                                @Override
                                public boolean acceptDataObject(final DataObject dataObject) {
                                    // TODO: shall this filter only accept properties files????
                                    final FileObject fo = dataObject.getPrimaryFile();
                                    final String name = fo.getName();
                                    // CVS Folder
                                    if (fo.isFolder()
                                                && ("cvs".equalsIgnoreCase(name)      // NOI18N
                                                    || BrokerProjectFactory.PROJECT_DIR.equalsIgnoreCase(name))) {
                                        return false;
                                    }
                                    if ("properties".equalsIgnoreCase(fo.getExt())) { // NOI18N
                                        return true;
                                    }

                                    return false;
                                }
                            });
                    view = new BrokerProjectNode(new AbstractNode(children), project);
                }
            }
        }

        return view;
    }
}
