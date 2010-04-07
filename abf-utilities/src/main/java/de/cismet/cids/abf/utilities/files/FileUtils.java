/*
 * FileUtils.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 5. März 2008, 09:22
 *
 */

package de.cismet.cids.abf.utilities.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mscholl
 * @version 1.3
 */
public final class FileUtils
{
    public static final int MAC_META = 1;
    
    public static final int UNIX_META = 2;
    
    public static final int WINDOWS_META = 3;
    
    public static final int ALL_META = 4;
    
    private static final String[] MAC_META_ENTRIES = {".DS_Store"}; // NOI18N
    private static final String[] UNIX_META_ENTRIES = {};
    private static final String[] WINDOWS_META_ENTRIES = {};
    
    /**
     * Creates a new instance of FileUtils
     */
    private FileUtils()
    {
    }
    
    public static boolean isMetaFile(final FileObject check)
    {
        return isMetaFile(FileUtil.toFile(check));
    }
    
    public static boolean isMetaFile(final File check)
    {
        return isMetaFile(check, getMode());
    }
    
    public static boolean isMetaFile(final FileObject check, final int mode)
    {
        return isMetaFile(FileUtil.toFile(check), mode);
    }
    
    public static boolean isMetaFile(final File check, final int mode)
    {
        return checkMeta(check.getName(), getMetaEntries(mode));
    }
    
    private static String[] getMetaEntries(final int mode)
    {
        switch(mode)
        {
            case MAC_META:
                return MAC_META_ENTRIES;
            case UNIX_META:
                return UNIX_META_ENTRIES;
            case WINDOWS_META:
                return WINDOWS_META_ENTRIES;
            case ALL_META:
                // ALL_META is default case
            default:
                final String[] allMetaEntries = new String
                        [
                            MAC_META_ENTRIES.length
                            + UNIX_META_ENTRIES.length
                            + WINDOWS_META_ENTRIES.length
                        ];
                int i = -1;
                for(final String s : MAC_META_ENTRIES)
                {
                    allMetaEntries[++i] = s;
                }
                for(final String s : UNIX_META_ENTRIES)
                {
                    allMetaEntries[++i] = s;
                }
                for(final String s : WINDOWS_META_ENTRIES)
                {
                    allMetaEntries[++i] = s;
                }
                return allMetaEntries;
        }
    }
    
    private static boolean checkMeta(final String filename, final String[] meta)
    {
        for(final String s : meta)
        {
            if(filename.equals(s))
            {
                return true;
            }
        }
        return false;
    }
    
    private static int getMode()
    {
        final String os = System.getProperty("os.name"); // NOI18N
        if(os.startsWith("Mac")) // NOI18N
        {
            return MAC_META;
        }else if(os.startsWith("Win")) // NOI18N
        {
            return WINDOWS_META;
        }else
        {
            return UNIX_META;
        }
    }

    public static String getName(final File file)
    {
        final String nameExt = file.getName();
        final int index = nameExt.lastIndexOf('.'); // NOI18N
        if(index == -1)
        {
            return nameExt;
        }else
        {
            return nameExt.substring(0, index);
        }
    }

    public static String getExt(final File file)
    {
        final String nameExt = file.getName();
        final int index = nameExt.lastIndexOf('.'); // NOI18N
        if(index == -1)
        {
            return ""; // NOI18N
        }else
        {
            return nameExt.substring(index + 1, nameExt.length());
        }
    }
    
    public static boolean containsOnlyMetaFiles(final FileObject check)
    {
        return containsOnlyMetaFiles(FileUtil.toFile(check));
    }
    
    public static boolean containsOnlyMetaFiles(final File check)
    {
        return containsOnlyMetaFiles(check, getMode());
    }
    
    public static boolean containsOnlyMetaFiles(final FileObject check, final 
            int mode)
    {
        return containsOnlyMetaFiles(FileUtil.toFile(check), mode);
    }
    
    public static boolean containsOnlyMetaFiles(final File check, final int 
            mode)
    {
        if(!check.isDirectory())
        {
            throw new IllegalArgumentException("only directories can " // NOI18N
                    + "contain files"); // NOI18N
        }
        final String[] metaEntries = getMetaEntries(mode);
        final FilesFilter filter = new FilesFilter();
        for(final File f : check.listFiles(filter))
        {
            if(!checkMeta(f.getName(), metaEntries))
            {
                return false;
            }
        }
        return true;
    }
    
    // exception thrown and not handled to avoid logger call
    // if it is necessary to use logger in this class return boolean and handle
    // exceptions
    public static void copyFile(final File inFile, final File outFile) throws
            FileNotFoundException,
            IOException
    {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try
        {
            fis = new FileInputStream(inFile);
            fos = new FileOutputStream(outFile);
            final byte[] buffer = new byte[1024];
            int i = 0;
            while((i = fis.read()) != -1)
            {
                fos.write(buffer, 0, i);
            }
        } finally
        {
            if(fis != null)
            {
                fis.close();
            }
            if(fos != null)
            {
                fos.close();
            }
        }
    }
    
    public static void copyContent(final FileObject srcDir, final FileObject
            destDir, final FileFilter filter, final boolean recursive) throws 
            FileNotFoundException,
            IOException
    {
        copyContent(FileUtil.toFile(srcDir), FileUtil.toFile(destDir), filter,
                recursive);
    }
    
    public static void copyContent(final File srcDir, final File destDir, final
            FileFilter filter, final boolean recursive) throws 
            FileNotFoundException,
            IOException
    {
        if(!srcDir.isDirectory())
        {
            throw new FileNotFoundException(
                    "you can only copy the content of a directory, " // NOI18N
                    + "for copying files use copy(File, File) instead");//NOI18N
        }
        if(destDir.exists())
        {
            if(!destDir.isDirectory())
            {
                throw new FileNotFoundException("you can only copy to" // NOI18N
                        + "a directory"); // NOI18N
            }
        }else
        {
            destDir.mkdirs();
        }
        for(final File f : srcDir.listFiles(filter))
        {
            if(f.isFile())
            {
                copyFile(f, new File(destDir, f.getName()));
            }
            if(f.isDirectory())
            {
                final File dir = new File(destDir, f.getName());
                dir.mkdir();
                if(recursive)
                {
                    copyContent(f, dir, filter, recursive);
                }
            }   
        }
    }

    public static void deleteContent(final FileObject srcDir, final boolean
            recursive) throws
            IOException
    {
        deleteContent(FileUtil.toFile(srcDir), recursive);
    }
    
    public static void deleteContent(final File srcDir, final boolean recursive)
            throws 
            IOException
    {
        if(!srcDir.isDirectory())
        {
            throw new IOException("source dir is not a directory"); // NOI18N
        }
        for(final File f : srcDir.listFiles())
        {
            if(f.isFile())
            {
                if(!f.delete())
                {
                    throw new IOException("could not delete file: " // NOI18N
                            + f.getAbsolutePath()
                            + File.pathSeparator
                            + f.getName());
                }
            }else if(f.isDirectory() && recursive)
            {
                deleteContent(f, recursive);
                if(!f.delete())
                {
                    throw new IOException("could not delete folder: " // NOI18N
                            + f.getAbsolutePath()
                            + File.pathSeparator
                            + f.getName());
                }
            }
        }
    }
    
    public static void deleteDir(final File srcDir) throws IOException
    {
        if(!srcDir.isDirectory())
        {
            throw new IOException("source dir is not a directory"); // NOI18N
        }
        deleteContent(srcDir, true);
        if(!srcDir.delete())
        {
            throw new IOException("could not delete file: " // NOI18N
                    + srcDir.getAbsolutePath()
                    + File.pathSeparator
                    + srcDir.getName());
        }
    }

    public static boolean containsClassFiles(final FileObject dir)
    {
        final Enumeration<? extends FileObject> e = dir.getData(true);
        while(e.hasMoreElements())
        {
            final FileObject fo = e.nextElement();
            if("class".equalsIgnoreCase(fo.getExt())) // NOI18N
            {
                return true;
            }
        }
        return false;
    }

    public static void extractJar(final FileObject jar, final FileObject dest,
            final FileFilter filter) throws
            IOException
    {
        extractJar(FileUtil.toFile(jar), FileUtil.toFile(dest), filter);
    }

    public static void extractJar(final File jar, final File dest,
            final FileFilter filter) throws
            IOException
    {
        if(!dest.exists())
        {
            throw new IOException("dest dir does not exist: " + dest); // NOI18N
        }
        if(!jar.exists())
        {
            throw new IOException("jar file does not exist: " + jar); // NOI18N
        }
        if(!dest.isDirectory())
        {
            throw new IOException(
                    "dest dir is not a directory: " + dest); // NOI18N
        }
        if(!dest.canWrite())
        {
            throw new IOException(
                    "cannot write to dest directory: " + dest); // NOI18N
        }
        if(!jar.canRead())
        {
            throw new IOException("cannot read jar file: " + jar); // NOI18N
        }
        final JarFile jarFile = new JarFile(jar);
        final Enumeration<JarEntry> e = jarFile.entries();
        while(e.hasMoreElements())
        {
            // every entry is a file, dirs are not "recognised"
            final JarEntry entry = e.nextElement();
            final File f = new File(dest, entry.getName());
            if(!filter.accept(f))
            {
                continue;
            }
            if(entry.isDirectory())
            {
                if(!f.mkdirs())
                {
                    throw new IOException("could not create dir: " + f);//NOI18N
                }
                continue;
            }
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try
            {
                bis = new BufferedInputStream(jarFile.getInputStream(entry));
                bos = new BufferedOutputStream(new FileOutputStream(f));
                while(bis.available() > 0)
                {
                    bos.write(bis.read());
                }
            }finally
            {
                try
                {
                    if(bis != null)
                    {
                        bis.close();
                    }
                }finally
                {
                    if(bos != null)
                    {
                        bos.close();
                    }
                }
            }
        }
    }
    
    public static final class DirectoryFilter implements FileFilter
    {
        @Override
        public boolean accept(final File file)
        {
            return file.isDirectory();
        }
    }
    
    public static final class FilesFilter implements FileFilter
    {
        @Override
        public boolean accept(final File file)
        {
            return !file.isDirectory();
        }
    }

    public static final class MetaInfFilter implements FileFilter
    {
        @Override
        public boolean accept(final File file)
        {
            return !file.getAbsolutePath()
                    .contains(File.separator + "META-INF"); // NOI18N
        }
    }
}