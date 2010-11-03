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
import org.apache.maven.project.MavenProject;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.List;

import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class EditDistributionWizardPanel2 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    /**
     * The visual component that displays this panel. If you need to access the component from this class, just use
     * getComponent().
     */
    private transient EditDistributionVisualPanel2 component;
    private transient MavenProject mavenProject;
    private transient List<Artifact> apps;

    //~ Methods ----------------------------------------------------------------

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new EditDistributionVisualPanel2(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public final void addChangeListener(final ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(final ChangeListener l) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MavenProject getMavenProject() {
        return mavenProject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<Artifact> getApps() {
        return apps;
    }

    @Override
    public void readSettings(final Object settings) {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        mavenProject = (MavenProject)wizard.getProperty(CreateDistributionAction.PROP_MAVEN_PROJECT);
        apps = (List<Artifact>)wizard.getProperty(CreateDistributionAction.PROP_CHOSEN_APPS);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        wizard.putProperty(CreateDistributionAction.PROP_APP_CUSTOM_ASSIGNMENT, component.getCustomSelection());
    }
}
