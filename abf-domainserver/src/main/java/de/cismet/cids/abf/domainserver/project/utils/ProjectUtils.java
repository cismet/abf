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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.permission.ClassPermission;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ProjectUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ProjectUtils.class);

    public static final String LOCAL_DOMAIN_NAME = "LOCAL";       // NOI18N
    public static final String PROP_SERVER_DOMAIN = "serverName"; // NOI18N
    public static final String PROP_ICON_DIR = "iconDirectory";   // NOI18N
    public static final String PROP_FILE_SEP = "fileSeparator";   // NOI18N

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
        try {
            // maybe use of FileObject would be nicer, but as long as one cannot
            // ensure that the iconDir does not contain . or .. the use of
            // FileObject is not recommended. That is because MasterFileObject
            // cannot handle these paths correctely at least when trying to resolve
            // a FileObject using the getFileObject method
            final File baseFile = FileUtil.toFile(project.getProjectDirectory());
            final String internalSeparator = project.getRuntimeProps().getProperty(PROP_FILE_SEP);
            final String iconDir = project.getRuntimeProps()
                        .getProperty(PROP_ICON_DIR)
                        .replace(internalSeparator, File.separator);
            final File imageFile = new File(baseFile, iconDir + File.separator + icon.getFileName());

            return ImageIO.read(imageFile);
        } catch (final Exception ex) {
            LOG.warn("image retrieval failed:" + icon.getFileName(), ex); // NOI18N

            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     * @param   p   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public static boolean isRemoteGroup(final UserGroup ug, final DomainserverProject p) {
        final Domain d = ug.getDomain();

        assert d != null : "domain cannot be null for given usergroup: " + ug; // NOI18N

        final String ugDomainName = d.getName();

        if (ugDomainName == null) {
            throw new IllegalStateException("domainname of usergroup is null: " + ug); // NOI18N
        }

        final String domainname = p.getRuntimeProps().getProperty(PROP_SERVER_DOMAIN);

        if (domainname == null) {
            throw new IllegalStateException("domainname of server is null"); // NOI18N
        }

        return !(LOCAL_DOMAIN_NAME.equals(ugDomainName) || domainname.equals(ugDomainName));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   perms  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Collection<UserGroup> getUserGroupsFromClassPermissions(final Collection<ClassPermission> perms) {
        final List<UserGroup> ugs = new ArrayList<UserGroup>();
        for (final ClassPermission perm : perms) {
            ugs.add(perm.getUserGroup());
        }

        return ugs;
    }

    // TODO: move to commons
    /**
     * DOCUMENT ME!
     *
     * @param   string  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toDBCompatibleString(final String string) {
        String rep = string;
        rep = rep.replace("ä", "ae"); // NOI18N
        rep = rep.replace("ö", "oe"); // NOI18N
        rep = rep.replace("ü", "ue"); // NOI18N
        rep = rep.replace("Ä", "AE"); // NOI18N
        rep = rep.replace("Ö", "OE"); // NOI18N
        rep = rep.replace("Ü", "UE"); // NOI18N
        rep = rep.replace("ß", "ss"); // NOI18N
        rep = rep.replace(" ", "_");  // NOI18N

        return rep;
    }
}
