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
public final class ActionConfigAttrRootNode extends ConfigAttrRootNode {

    //~ Instance fields --------------------------------------------------------

    private final transient Image actionIcon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ActionConfigAttrRootNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ActionConfigAttrRootNode(final DomainserverProject project) {
        super(Types.ACTION_TAG, project);

        setName(NbBundle.getMessage(ActionConfigAttrRootNode.class, "ActionConfigAttrRootNode.name")); // NOI18N

        actionIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "action_16.png"); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int type) {
        return actionIcon;
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }
}
