/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.util;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import java.io.File;
import java.io.FileNotFoundException;

import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.ManifestProviderCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;

import de.cismet.tools.PasswordEncrypter;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.6
 */
public final class DeployInformation {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            DeployInformation.class);

    //~ Instance fields --------------------------------------------------------

    private FileObject buildXML;
    private FileObject sourceDir;
    private FileObject keystore;
    private FileObject manifest;
    private String destFilePath;
    private String alias;
    private String storepass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeployInformation object.
     *
     * @param  buildXML      DOCUMENT ME!
     * @param  sourceDir     DOCUMENT ME!
     * @param  destFilePath  DOCUMENT ME!
     * @param  keystore      DOCUMENT ME!
     * @param  alias         DOCUMENT ME!
     * @param  storepass     DOCUMENT ME!
     * @param  manifest      DOCUMENT ME!
     */
    public DeployInformation(
            final FileObject buildXML,
            final FileObject sourceDir,
            final String destFilePath,
            final FileObject keystore,
            final String alias,
            final String storepass,
            final FileObject manifest) {
        this.buildXML = buildXML;
        this.sourceDir = sourceDir;
        this.keystore = keystore;
        this.manifest = manifest;
        this.destFilePath = destFilePath;
        this.alias = alias;
        this.storepass = storepass;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getBuildXML() {
        return buildXML;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buildXML  DOCUMENT ME!
     */
    public void setBuildXML(final FileObject buildXML) {
        this.buildXML = buildXML;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getSourceDir() {
        return sourceDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sourceDir  DOCUMENT ME!
     */
    public void setSourceDir(final FileObject sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getKeystore() {
        return keystore;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  keystore  DOCUMENT ME!
     */
    public void setKeystore(final FileObject keystore) {
        this.keystore = keystore;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getManifest() {
        return manifest;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  manifest  DOCUMENT ME!
     */
    public void setManifest(final FileObject manifest) {
        this.manifest = manifest;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDestFilePath() {
        return destFilePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  destFilePath  DOCUMENT ME!
     */
    public void setDestFilePath(final String destFilePath) {
        this.destFilePath = destFilePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAlias() {
        return alias;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  alias  DOCUMENT ME!
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStorepass() {
        return storepass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  storepass  DOCUMENT ME!
     */
    public void setStorepass(final String storepass) {
        this.storepass = storepass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DeployInformation getDeployInformation(final Node n) {
        try {
            final SourceContextCookie sourceCookie = n.getCookie(SourceContextCookie.class);
            final LibrarySupportContextCookie libCC = n.getCookie(LibrarySupportContextCookie.class);
            final ManifestProviderCookie manifestProvider = n.getCookie(ManifestProviderCookie.class);
            final FileObject manifest;
            if (manifestProvider == null) {
                manifest = libCC.getLibrarySupportContext().getDefaultManifest();
            } else {
                manifest = manifestProvider.getManifest();
            }
            final FileObject srcFile = sourceCookie.getSourceObject();
            final File destFilePath = FileUtil.toFile(srcFile.getParent().getParent().getParent());
            final PropertyProvider provider = PropertyProvider.getInstance(
                    libCC.getLibrarySupportContext().getProjectProperties());
            final FileObject keystore = FileUtil.toFileObject(new File(
                        provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE)));
            final String passwd = PasswordEncrypter.decryptString(provider.get(
                        PropertyProvider.KEY_GENERAL_KEYSTORE_PW));
            final String destFile = destFilePath.getAbsolutePath()
                        + System.getProperty("file.separator") // NOI18N
                        + srcFile.getName() + ".jar";          // NOI18N

            return new DeployInformation(
                    libCC.getLibrarySupportContext().getBuildXML(),
                    srcFile,
                    destFile,
                    keystore,
                    null,
                    passwd,
                    manifest);
        } catch (final FileNotFoundException ex) {
            LOG.error("could not create deploy information", ex); // NOI18N
            return null;
        }
    }
}
