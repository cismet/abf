/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.customizer;

import org.apache.log4j.Logger;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public final class PropertyProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(PropertyProvider.class);

    public static final String KEY_AUTORELOAD = "autoreload";                           // NOI18N
    public static final String KEY_GENERAL_KEYSTORE = "generalKeystorePath";            // NOI18N
    public static final String KEY_USE_INDIVIDUAL_KEYSTORES = "useIndividualKeystores"; // NOI18N
    public static final String KEY_GENERAL_MANIFEST = "generalManifest";                // NOI18N
    public static final String KEY_GENERAL_KEYSTORE_PW = "generalKeystorePW";           // NOI18N
    public static final String KEY_DEPLOYMENT_STRATEGY = "deploymentStrategy";          // NOI18N
    public static final String KEY_SIGN_SERVICE_URL = "signServiceUrl";                 // NOI18N

    public static final String STRATEGY_USE_LOCAL = "USE_LOCAL";               // NOI18N
    public static final String STRATEGY_USE_SIGN_SERVICE = "USE_SIGN_SERVICE"; // NOI18N

    private static PropertyProvider provider;

    //~ Instance fields --------------------------------------------------------

    private final transient FileObject propFile;

    private transient Properties properties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PropertyProvider.
     *
     * @param  propFile  DOCUMENT ME!
     */
    private PropertyProvider(final FileObject propFile) {
        this.propFile = propFile;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   propFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static synchronized PropertyProvider getInstance(final FileObject propFile) {
        if (provider == null) {
            provider = new PropertyProvider(propFile);
        }

        return provider;
    }

    /**
     * DOCUMENT ME!
     */
    public void load() {
        synchronized (this) {
            properties = new Properties();
            try {
                properties.load(new BufferedInputStream(propFile.getInputStream()));
            } catch (final Exception ex) {
                LOG.fatal("project properties could not be loaded", ex);                          // NOI18N
                ErrorManager.getDefault().annotate(ex, "project properties could not be loaded"); // NOI18N
                properties = null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void save() {
        synchronized (this) {
            assert properties != null;
            OutputStream out = null;
            final File file = FileUtil.toFile(propFile);
            try {
                out = new BufferedOutputStream(new FileOutputStream(file));
                properties.store(out, "");                                      // NOI18N
            } catch (final IOException ex) {
                final String message = "project properties could not be saved"; // NOI18N
                LOG.error(message, ex);
                ErrorManager.getDefault().annotate(ex, message);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (final IOException ex) {
                        // do nothing
                        LOG.warn("outputstream could not be closed", ex); // NOI18N
                    }
                }
                clearInternal();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String get(final String key) {
        if (properties == null) {
            load();
        }

        assert properties != null;

        return properties.getProperty(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public void put(final String key, final String value) {
        if (properties == null) {
            load();
        }

        assert properties != null;

        properties.put(key, value);
    }

    /**
     * DOCUMENT ME!
     */
    void clearInternal() {
        properties = null;
    }
}
