/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.configattr;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.Image;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class XMLConfigAttrRootNode extends ConfigAttrRootNode {

    //~ Instance fields --------------------------------------------------------

    private final transient Image xmlIcon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new XMLConfigAttrRootNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public XMLConfigAttrRootNode(final DomainserverProject project) {
        super(Types.XML_ATTR, project);

        setName(NbBundle.getMessage(XMLConfigAttrRootNode.class, "XMLConfigAttrRootNode.name")); // NOI18N

        xmlIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "xmlfile_16.png"); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int type) {
        return xmlIcon;
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }
}
