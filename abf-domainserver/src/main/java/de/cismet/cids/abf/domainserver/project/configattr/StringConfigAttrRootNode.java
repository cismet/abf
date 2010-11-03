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
public final class StringConfigAttrRootNode extends ConfigAttrRootNode {

    //~ Instance fields --------------------------------------------------------

    private final transient Image kvIcon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StringConfigAttrRootNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public StringConfigAttrRootNode(final DomainserverProject project) {
        super(Types.CONFIG_ATTR, project);

        setName(NbBundle.getMessage(StringConfigAttrRootNode.class, "StringConfigAttrRootNode.name")); // NOI18N

        kvIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "keyvalue_16.png"); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int type) {
        return kvIcon;
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }
}
