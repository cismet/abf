/*
 * JarHandler.java, encoding: UTF-8
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
 * Created on 13. Juli 2007, 14:18
 *
 */

package de.cismet.cids.abf.librarysupport;

import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author mscholl
 * @version 1.4
 */
public final class JarHandler
{
    public static final String ANT_TARGET_UNJAR = "extractJar"; // NOI18N
    public static final String ANT_TARGET_DELETE = "deleteDir"; // NOI18N
    public static final String ANT_TARGET_DEPLOY_JAR = "deployJar"; // NOI18N
    public static final String ANT_TARGET_DEPLOY_ALL_JARS = 
            "deployAllJars"; // NOI18N
    public static final String ANT_TARGET_DEPLOY_CHANGED_JARS = 
            "deployChangedJars"; // NOI18N
    
    private static final String TMP_DIR_ADDITION = "_abf_tmp_"; // NOI18N
    private static final String TMP_BUILD_FILE = "__tmp_build.xml"; // NOI18N
    private static final String ANT_PROPERTY_FILE =
            "/de/cismet/cids/abf/librarysupport/ant.properties"; // NOI18N
    
    private static final transient Logger LOG = Logger.getLogger(
            JarHandler.class);
    
    private JarHandler() {}
    
    public static FileObject extractJar(final FileObject buildXML, 
            final FileObject jarToExtract, final FileObject target) throws
            IOException
    {
        final FileObject extractDir;
        if(target != null && target.isValid() && target.isFolder())
        {
            extractDir = target;
        }else
        {
            extractDir = jarToExtract.getParent().createFolder(jarToExtract.
                    getName() + TMP_DIR_ADDITION + System.currentTimeMillis());
        }
        final Properties p = new Properties();
        if(jarToExtract != null)
        {
            p.put("jar.sourcefile", FileUtil.toFile(jarToExtract) // NOI18N
                    .getAbsolutePath());
        }
        p.put("jar.tmpdir", // NOI18N
                FileUtil.toFile(extractDir).getAbsolutePath());
        final ExecutorTask task = ActionUtils.runTarget(
                buildXML, new String[]{ANT_TARGET_UNJAR}, p);
        task.waitFinished();
        if(task.result() != 0)
        {
            LOG.error("could not extract jar '" // NOI18N
                    + jarToExtract.getName()
                    + "' to temporary directory '" // NOI18N
                    + extractDir.getPath()
                    + "', cleaning up"); // NOI18N
            if(target == null || !target.isValid() || !target.isFolder())
            {
                removeDir(buildXML, extractDir);
            }
            throw new IOException("could not extract jar '" // NOI18N
                    + jarToExtract.getName()
                    + "' to temporary directory '" // NOI18N
                    + extractDir.getPath()
                    + "', cleaning up"); // NOI18N
        }
        return extractDir;
    }
    
    public static void removeDir(final FileObject buildXML, final FileObject 
            dirToRemove) throws 
            IOException
    {
        final Properties p = new Properties();
        p.load(ClassLoader.getSystemClassLoader().getResourceAsStream(
                ANT_PROPERTY_FILE));
        p.put("jar.tmpdir", // NOI18N
                FileUtil.toFile(dirToRemove).getAbsolutePath());
        final ExecutorTask task = ActionUtils.runTarget(buildXML, new String[]{
                ANT_TARGET_DELETE}, p);
        task.waitFinished();
        if(task.result() != 0)
        {
            LOG.error("removal of dir '" // NOI18N
                    + dirToRemove.getPath()
                    + "' failed"); // NOI18N
            throw new IOException("removal of dir '" // NOI18N
                    + dirToRemove.getPath()
                    + "' failed"); // NOI18N
        }
    }
    
    public static void cleanup(final FileObject buildXML, final FileObject 
            workDir) throws
            IOException
    {
        for(FileObject f : workDir.getChildren())
        {
            if(f.getName().contains(TMP_DIR_ADDITION))
            {
                removeDir(buildXML, f);
            }
        }
    }
    
    public static void deployJar(final DeployInformation info) throws
            IOException
    {
        assert info.getBuildXML() != null;
        final Properties p = new Properties();
        final URL url = JarHandler.class.getResource(ANT_PROPERTY_FILE);
        p.load(url.openStream());
        if(info.getKeystore() != null)
        {
            p.put("jar.sign.keystorepath", // NOI18N
                    FileUtil.toFile(info.getKeystore()).getAbsolutePath());
        }
        if(info.getAlias() != null)
        {
            p.put("jar.sign.alias", info.getAlias()); // NOI18N
        }
        if(info.getStorepass() != null)
        {
            p.put("jar.sign.storepass", info.getStorepass()); // NOI18N
        }
        if(info.getSourceDir() != null)
        {
            p.put("jar.sourcepath", // NOI18N
                    FileUtil.toFile(info.getSourceDir()).getAbsolutePath());
        }
        if(info.getDestFilePath() != null)
        {
            p.put("jar.destfile", info.getDestFilePath()); // NOI18N
        }
        if(info.getManifest() != null)
        {
            p.put("jar.manifest", // NOI18N
                    FileUtil.toFile(info.getManifest()).getAbsolutePath());
        }
        final ExecutorTask task = ActionUtils.runTarget(info.getBuildXML(), 
                new String[]{ANT_TARGET_DEPLOY_JAR}, p);
        task.waitFinished();
        if(task.result() != 0)
        {
            LOG.error("deploy jar failed: " + info.getDestFilePath()); // NOI18N
            throw new IOException("deploy jar failed: " // NOI18N
                    + info.getDestFilePath());
        }
    }
    
    public static void deployAllJars(final List<DeployInformation> infos, final 
            String targetName) throws
            IOException
    {
        if(infos.size() < 1)
        {
            return;
        }
        final Properties p = new Properties();
        final URL url = JarHandler.class.getResource(ANT_PROPERTY_FILE);
        p.load(url.openStream());
        if(infos.get(0).getKeystore() != null)
        {
            p.put("jar.sign.keystorepath", FileUtil.toFile( // NOI18N
                    infos.get(0).getKeystore()).getAbsolutePath());
        }
        if(infos.get(0).getAlias() != null)
        {
            p.put("jar.sign.alias", infos.get(0).getAlias()); // NOI18N
        }
        if(infos.get(0).getStorepass() != null)
        {
            p.put("jar.sign.storepass", infos.get(0).getStorepass()); // NOI18N
        }
        final Document buildDoc = XMLUtil.createDocument(
                "project", null, null, null); // NOI18N
        final Element target = buildDoc.createElement("target"); // NOI18N
        target.setAttribute("name", targetName); // NOI18N
        buildDoc.getDocumentElement().appendChild(target);
        for(final DeployInformation info : infos)
        {
            final Element jar = buildDoc.createElement("jar"); // NOI18N
            jar.setAttribute("destfile", info.getDestFilePath()); // NOI18N
            if(info.getSourceDir() != null)
            {
                jar.setAttribute("basedir", FileUtil.toFile( // NOI18N
                        info.getSourceDir()).getAbsolutePath());
            }
            if(info.getManifest() != null)
            {
                jar.setAttribute("manifest", FileUtil.toFile( // NOI18N
                        info.getManifest()).getAbsolutePath());
            }
            target.appendChild(jar);
            final Element sign = buildDoc.createElement("signjar"); // NOI18N
            sign.setAttribute("jar", info.getDestFilePath()); // NOI18N
            sign.setAttribute("alias", "${jar.sign.alias}"); // NOI18N
            sign.setAttribute("storepass", "${jar.sign.storepass}"); // NOI18N
            sign.setAttribute("keystore", "${jar.sign.keystorepath}"); // NOI18N
            target.appendChild(sign);
        }
        final File outFile = new File(FileUtil.toFile(infos.get(0).getBuildXML(
                ).getParent()), TMP_BUILD_FILE);
        if(outFile.exists() && !outFile.delete())
        {
            LOG.error("outfile could not be deleted"); // NOI18N
            throw new IOException("outfile '" // NOI18N
                    + outFile.getAbsolutePath()
                    + "' could not be deleted"); // NOI18N
        }
        final BufferedOutputStream bos = new BufferedOutputStream(new 
                FileOutputStream(outFile));
        try
        {
            XMLUtil.write(buildDoc, bos, "UTF-8"); // NOI18N
        } catch (final IOException ex)
        {
            LOG.error("could not write tmp build file", ex); // NOI18N
            throw ex;
        } finally
        {
            bos.close();
        }
        final ExecutorTask task = ActionUtils.runTarget(FileUtil.toFileObject(
                outFile), new String[]{targetName}, p);
        task.waitFinished();
        if(task.result() != 0)
        {
            task.stop();
            LOG.error("deploy all jars failed"); // NOI18N
            throw new IOException("deploy all jars failed"); // NOI18N
        }
        if(outFile.exists() && !outFile.delete())
        {
            LOG.error("outfile could not be deleted"); // NOI18N
            throw new IOException("outfile '" // NOI18N
                    + outFile.getAbsolutePath()
                    + "' could not be deleted"); // NOI18N
        }
    }
}