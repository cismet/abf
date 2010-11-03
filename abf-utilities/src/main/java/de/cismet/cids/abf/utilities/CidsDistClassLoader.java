/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This Class is able the load all classes of libraries in a cidsDistribution. Additionally it has a class cache to
 * check if a class can generally be loaded or if the class can be used in a given library context.
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public final class CidsDistClassLoader extends URLClassLoader {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsDistClassLoader.class);

    private static final transient Map<FileObject, CidsDistClassLoader> INSTANCES =
        new HashMap<FileObject, CidsDistClassLoader>(5);

    private static final String EXT_CLASS = ".class"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient FileObject distRoot;
    private final transient Map<String, String[]> classCache;

    private transient Map<String, String[]> map;
    private transient String part;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CidsDistClassLoader.
     *
     * @param  distRoot  a FileObject point to a "cidsDistribtion" dir
     */
    private CidsDistClassLoader(final FileObject distRoot) {
        super(new URL[] {});
        this.distRoot = distRoot;
        classCache = new HashMap<String, String[]>(30);
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Initializes the ClassLoader by loading all jars of the corresponding cidsDistribution.
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void init() {
        final FileObject libFO = distRoot.getFileObject("lib");          // NOI18N
        if (libFO == null) {
            throw new IllegalStateException("could not locate lib dir"); // NOI18N
        }
        FileObject srcFO = libFO.getFileObject("int");                   // NOI18N
        // use this if else blocks to be able to throw appropriate log message
        if (srcFO == null) {
            LOG.warn("could not locate int dir, int libraries not loaded");     // NOI18N
        } else {
            loadJars(srcFO);
        }
        srcFO = libFO.getFileObject("ext");                                     // NOI18N
        if (srcFO == null) {
            LOG.warn("could not locate ext dir, ext libraries not loaded");     // NOI18N
        } else {
            loadJars(srcFO);
        }
        srcFO = libFO.getFileObject("local");                                   // NOI18N
        if (srcFO == null) {
            LOG.warn("could not locate local dir, local libraries not loaded"); // NOI18N
        } else {
            loadJars(srcFO);
        }
    }

    /**
     * Loads the classes of all jars in the given library dir into memory of the ClassLoader.
     *
     * @param  srcFO  a FileObject pointing to a library dir
     */
    private void loadJars(final FileObject srcFO) {
        for (final Enumeration e = srcFO.getData(false); e.hasMoreElements();) {
            final FileObject fo = (FileObject)e.nextElement();
            if (fo.getExt().equals("jar") || fo.getExt().equals("zip"))     // NOI18N
            {
                try {
                    addURL(fo.getURL());
                    addToCache(fo);
                } catch (final FileStateInvalidException ex) {
                    LOG.error("could not add file " + fo.getNameExt(), ex); // NOI18N
                }
            }
        }
    }

    /**
     * Adds a jar and its classes to the classCache.
     *
     * @param   srcFO  a FileObject pointing to a jarfile
     *
     * @throws  CacheException  DOCUMENT ME!
     */
    private void addToCache(final FileObject srcFO) {
        final File srcFile = FileUtil.toFile(srcFO);
        final String jarFile = srcFile.getAbsolutePath();
        final JarFile jar;
        try {
            jar = new JarFile(srcFile, false);
        } catch (final IOException ex) {
            final String message = "could not open jar file: " + srcFile.getPath(); // NOI18N

            LOG.error(message, ex);
            throw new CacheException(message, ex);
        }
        assert jar != null;
        final ArrayList<String> list = new ArrayList<String>(jar.size() / 2);
        for (final Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
            final JarEntry entry = e.nextElement();
            if (entry.getName().endsWith(EXT_CLASS)) {
                final String binaryName = entry.getName().replace('/', '.'); // NOI18N
                list.add(binaryName);
            }
        }
        classCache.put(jarFile, list.toArray(new String[list.size()]));
    }

    /**
     * Loads the class with the given binary name using the context jarClassPath.
     *
     * @param   name          binary name of the class that shall be check for loadability
     * @param   jarClassPath  all absolute paths to jar/zip files that will be in the class path used by the application
     *                        or null if a general check shall be performed where all libraries will be searched for the
     *                        given class name
     *
     * @return  the loaded Class Object
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Class loadClass(final String name, final String[] jarClassPath) {
        throw new UnsupportedOperationException("not implemented yet"); // NOI18N
    }

    /**
     * Computes the hits for a given partial String of a class name. case-sensitive
     *
     * @param   part  any String
     *
     * @return  a HashMap containing all names of jars that contain classes, that contain the String part, as keys and
     *          the binary class names as values
     */
    public Map<String, String[]> getHits(final String part) {
        this.part = part;
        if (part.isEmpty()) // NOI18N
        {
            return classCache;
        }
        computeHits();
        return map;
    }

    /**
     * Computes the hits for a given String part. case-sensitive
     */
    private void computeHits() {
        if (Thread.interrupted()) {
            return;
        }
        map = new HashMap<String, String[]>();
        final ArrayList<String> list = new ArrayList<String>(5);
        for (final String jar : classCache.keySet()) {
            if (Thread.interrupted()) {
                return;
            }
            list.clear();
            for (final String clazz : classCache.get(jar)) {
                if (Thread.interrupted()) {
                    return;
                }
                if (clazz.contains(part)) {
                    list.add(clazz.substring(0, clazz.indexOf(EXT_CLASS)));
                }
            }
            if (!list.isEmpty()) {
                map.put(jar, list.toArray(new String[list.size()]));
            }
        }
    }

    /**
     * Checks whether a given class could be found and loaded by this loader instance.
     *
     * @param   name          binary name of the class that shall be check for loadability
     * @param   jarClassPath  all absolute paths to jar/zip files that will be in the class path used by the application
     *                        or null if a general check shall be performed where all libraries will be searched for the
     *                        given class name
     *
     * @return  true if class was found in any library (in the application context, if jarClassPath was not null), false
     *          otherwise
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public boolean isLoadable(final String name, final String[] jarClassPath) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");                           // NOI18N
        }
        final String[] jcp;
        if (jarClassPath == null) {
            jcp = classCache.keySet().toArray(new String[0]);
        } else {
            jcp = jarClassPath;
        }
        final String className;
        if (name.endsWith(EXT_CLASS)) {
            className = name;
        } else {
            className = name + EXT_CLASS;
        }
        for (final String jar : jcp) {
            final String[] classes = classCache.get(jar);
            if (classes == null) {
                LOG.warn(
                    "you provided a jar in the jarClassPath array that could not be found in the " // NOI18N
                            + "cids distribution libraries");                                      // NOI18N
                break;
            }
            for (final String clazz : classes) {
                if (className.equals(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Reloads libraries of the associated cids distribution.
     *
     * @param   loader  the CidsDistClassLoader that shall be reloaded
     *
     * @return  the new classloader or null if the given cl is not managed by this class (what should not happen though)
     */
    public CidsDistClassLoader fullReload(final CidsDistClassLoader loader) {
        for (final FileObject fo : INSTANCES.keySet()) {
            if (INSTANCES.get(fo).equals(loader)) {
                final CidsDistClassLoader newLoader = new CidsDistClassLoader(fo);
                INSTANCES.remove(fo);
                INSTANCES.put(fo, newLoader);
                return newLoader;
            }
        }
        return null;
    }

    /**
     * Creates a new instance of CidsDistClassLoader if it does not exist for the given cidsDistribution dir or returns
     * the existing one.
     *
     * @param   distRoot  the FileObject pointing to the "cidsDistribution" dir
     *
     * @return  the classloader associated with the given cids distribution
     */
    // synchronization is essential
    public static synchronized CidsDistClassLoader getInstance(final FileObject distRoot) {
        CidsDistClassLoader loader = INSTANCES.get(distRoot);
        if (loader == null) {
            loader = new CidsDistClassLoader(distRoot);
            INSTANCES.put(distRoot, loader);
        }
        return loader;
    }

    /**
     * Checks whether a CidsDistClassLoader instance has already been created for the given cidsDistribution dir.
     *
     * @param   distRoot  the FileObject pointing to the "cidsDistribution" dir
     *
     * @return  true if there already exists an instance, false otherwise
     */
    public static boolean isLoaded(final FileObject distRoot) {
        return INSTANCES.get(distRoot) != null;
    }
}
