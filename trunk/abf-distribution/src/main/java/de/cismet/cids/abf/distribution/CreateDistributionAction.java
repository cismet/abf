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
package de.cismet.cids.abf.distribution;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class CreateDistributionAction implements ActionListener {

    //~ Static fields/initializers ---------------------------------------------

// private static final transient Logger LOG = Logger.getLogger(CreateDistributionAction.class);

    public static final String PROP_MAVEN_PROJECT = "__maven_project__";                 // NOI18N
    public static final String PROP_CHOSEN_APPS = "__chosen_applications__";             // NOI18N
    public static final String PROP_APP_CUSTOM_ASSIGNMENT = "__app_custom_assignment__"; // NOI18N
    public static final String PROP_CHOSEN_OUTDIR = "__chosen_output_dir__";             // NOI18N

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MavenProject mavenProject;
        try {
            final NbMavenProjectFactory factory = new NbMavenProjectFactory();
            final NbMavenProjectImpl projectImpl = (NbMavenProjectImpl)factory.loadProject(
                    FileUtil.toFileObject(
                        new File("/Users/mscholl/svnwork/central/de/cismet/cids/generate-dist/trunk")),
                    null);
            mavenProject = projectImpl.getOriginalMavenProject();
        } catch (final IOException ex) {
            final String message = "Cannot load maven project"; // NOI18N
//            LOG.error(message, ex);
            ErrorManager.getDefault().annotate(ex, message);
            return;
        }

        final WizardDescriptor.Iterator iterator = new EditDistributionWizardIterator();
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})")); // NOI18N
        wizardDescriptor.setTitle("Create cids Distribution");
        wizardDescriptor.putProperty(PROP_MAVEN_PROJECT, mavenProject);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final Map<Artifact, Set<Artifact>> apps = (Map<Artifact, Set<Artifact>>)wizardDescriptor.getProperty(
                    PROP_APP_CUSTOM_ASSIGNMENT);
            final MavenProject mvn = new MavenProject();
            mvn.setDependencyArtifacts(apps.keySet());
            mavenProject.setDependencyArtifacts(apps.keySet());
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(new File("/Users/mscholl/Desktop/distributionDescriptor.xml")));
                EmbedderFactory.getProjectEmbedder().writeModel(bw, mvn.getModel());
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (final Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            for (final Artifact app : apps.keySet()) {
            }
        }
    }
}
