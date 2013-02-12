/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 mscholl
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cids.abf.librarysupport.project.nodes.actions;

import org.apache.log4j.Logger;

import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;

import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

import java.awt.EventQueue;
import java.awt.Frame;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.JOptionPane;

import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class InstallAsMavenArtifactAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(InstallAsMavenArtifactAction.class);

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2193235635211857667L;

    public static final String DEFAULT_GROUPID = "de.cismet.cids.local"; // NOI18N
    public static final String DEFAULT_VERSION = "1.0";                  // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final FutureTask<Frame> mainFrameFuture;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new InstallAsMavenArtifactAction object.
     */
    public InstallAsMavenArtifactAction() {
        mainFrameFuture = new FutureTask<Frame>(new Callable<Frame>() {

                    @Override
                    public Frame call() throws Exception {
                        return WindowManager.getDefault().getMainWindow();
                    }
                });
        EventQueue.invokeLater(mainFrameFuture);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[] { SourceContextCookie.class };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        Frame mainFrame;
        try {
            mainFrame = mainFrameFuture.get();
        } catch (final Exception ex) {
            LOG.warn("could not retrieve main frame", ex); // NOI18N
            mainFrame = null;
        }

        final Node node = nodes[0];
        final DeployInformation deployInfo = DeployInformation.getDeployInformation(node);

        final File artifact = new File(deployInfo.getDestFilePath());
        if (!artifact.exists()) {
            JOptionPane.showMessageDialog(
                mainFrame,
                NbBundle.getMessage(
                    InstallAsMavenArtifactAction.class,
                    "InstallAsMavenArtifactAction.performAction(Node).optionPane1.message"), // NOI18N
                NbBundle.getMessage(
                    InstallAsMavenArtifactAction.class,
                    "InstallAsMavenArtifactAction.performAction(Node).optionPane1.title"), // NOI18N
                JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        final MavenArtifactEntryPanel artifactPanel = new MavenArtifactEntryPanel();
        artifactPanel.setGroupIdSuffix(artifact.getParentFile().getName().replace("local", "")); // NOI18N
        artifactPanel.setArtifactId(artifact.getName().substring(0, artifact.getName().length() - 4));
        artifactPanel.setVersion(DEFAULT_VERSION);

        final int answer = JOptionPane.showConfirmDialog(
                mainFrame,
                artifactPanel,
                NbBundle.getMessage(
                    InstallAsMavenArtifactAction.class,
                    "InstallAsMavenArtifactAction.performAction(Node).optionPane2.title"), // NOI18N
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (JOptionPane.CANCEL_OPTION == answer) {
            return;
        }

        final Properties properties = new Properties();
        properties.setProperty("file", artifact.getAbsolutePath());          // NOI18N
        properties.setProperty("groupId", artifactPanel.getGroupId());       // NOI18N
        properties.setProperty("artifactId", artifactPanel.getArtifactId()); // NOI18N
        properties.setProperty("version", artifactPanel.getVersion());       // NOI18N
        properties.setProperty("packaging", "jar");                          // NOI18N
        properties.setProperty("updateReleaseInfo", "true");                 // NOI18N

        final List<String> goals = new ArrayList<String>(1);
        goals.add("install:install-file"); // NOI18N

        final BeanRunConfig config = new BeanRunConfig();
        config.setExecutionName(NbBundle.getMessage(
                InstallAsMavenArtifactAction.class,
                "InstallAsMavenArtifactAction.performAction(Node).config.executionName"));   // NOI18N
        config.setExecutionDirectory(artifact.getParentFile());
        config.setTaskDisplayName(NbBundle.getMessage(
                InstallAsMavenArtifactAction.class,
                "InstallAsMavenArtifactAction.performAction(Node).config.taskDisplayName")); // NOI18N
        config.setGoals(goals);
        config.setProperties(properties);

        RunUtils.executeMaven(config);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(
                InstallAsMavenArtifactAction.class,
                "InstallAsMavenArtifactAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
