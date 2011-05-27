/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes;

import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.awt.Image;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public final class ExtManagement extends FilterNode {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExtManagement object.
     *
     * @param  project  DOCUMENT ME!
     * @param  n        DOCUMENT ME!
     */
    public ExtManagement(final LibrarySupportProject project, final Node n) {
        super(
            n,
            new FilterNode.Children(n),
            new ProxyLookup(
                new Lookup[] { Lookups.singleton(project), n.getLookup() }));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getOpenedIcon(final int i) {
        return getIcon(i);
    }

    @Override
    public Image getIcon(final int i) {
        return ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER + "extern_16.png"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExtManagement.class, "ExtManagement.getName().returnvalue"); // NOI18N
    }
}
