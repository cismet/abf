/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import java.util.Arrays;
import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public final class SimpleManifest {

    //~ Instance fields --------------------------------------------------------

    private String manifestName;
    private String manifestPath;
    private String[] classPath;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleManifest object.
     *
     * @param  manifestName  DOCUMENT ME!
     * @param  manifestPath  DOCUMENT ME!
     * @param  classPath     DOCUMENT ME!
     */
    public SimpleManifest(final String manifestName, final String manifestPath, final String[] classPath) {
        setManifestName(manifestName);
        this.manifestPath = manifestPath;
        this.classPath = Arrays.copyOf(classPath, classPath.length);
    }

    /**
     * Creates a new SimpleManifest object.
     *
     * @param  manifestName  DOCUMENT ME!
     * @param  manifestPath  DOCUMENT ME!
     * @param  classPath     DOCUMENT ME!
     */
    public SimpleManifest(final String manifestName, final String manifestPath, final Collection<String> classPath) {
        this.manifestPath = manifestPath;
        setManifestName(manifestName);
        setClassPath(classPath);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getManifestName() {
        return manifestName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  manifestName  DOCUMENT ME!
     */
    public void setManifestName(final String manifestName) {
        this.manifestName = manifestName.substring(0, 1).toUpperCase() + manifestName.substring(1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getClassPath() {
        return Arrays.copyOf(classPath, classPath.length);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classPath  DOCUMENT ME!
     */
    public void setClassPath(final String[] classPath) {
        this.classPath = Arrays.copyOf(classPath, classPath.length);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classPath  DOCUMENT ME!
     */
    public void setClassPath(final Collection<String> classPath) {
        this.classPath = new String[classPath.size()];
        classPath.toArray(this.classPath);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getManifestPath() {
        return manifestPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  manifestPath  DOCUMENT ME!
     */
    public void setManifestPath(final String manifestPath) {
        this.manifestPath = manifestPath;
    }

    @Override
    public String toString() {
        return manifestName + " :: " + manifestPath; // NOI18N
    }
}
