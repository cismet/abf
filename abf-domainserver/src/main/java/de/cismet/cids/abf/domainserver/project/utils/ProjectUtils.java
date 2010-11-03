/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.utils;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileUtil;

import java.awt.Image;

import java.io.File;

import javax.imageio.ImageIO;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ProjectUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            ProjectUtils.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProjectUtils object.
     */
    private ProjectUtils() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   icon     DOCUMENT ME!
     * @param   project  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Image getImageForIconAndProject(final Icon icon, final DomainserverProject project) {
        String iconDir = project.getRuntimeProps().get("iconDirectory").toString(); // NOI18N
        // maybe use of FileObject would be nicer, but as long as one cannot
        // ensure that the iconDir does not contain . or .. the use of
        // FileObject is not recommended. That is because MasterFileObject
        // cannot handle these paths correctely at least when trying to resolve
        // a FileObject using the getFileObject method
        final File baseFile = FileUtil.toFile(project.getProjectDirectory());
        final String internalSeparator = project.getRuntimeProps().get(
                "fileSeparator").toString();   // NOI18N
        iconDir = iconDir.replace(internalSeparator, File.separator);
        final File imageFile = new File(baseFile, iconDir + File.separator
                        + icon.getFileName());
        try {
            return ImageIO.read(imageFile);
        } catch (final Exception ex) {
            LOG.warn("image retrieval failed:" // NOI18N
                        + icon.getFileName(), ex);
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     * @param   p   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isRemoteGroup(final UserGroup ug, final DomainserverProject p) {
        final Domain d = ug.getDomain();
        final String domainname = p.getRuntimeProps().getProperty(
                "serverName");                         // NOI18N
        return !(d.getName().equalsIgnoreCase("LOCAL") // NOI18N
                        || d.getName().equalsIgnoreCase(domainname));
    }
}
