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
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.Component;

import java.util.List;

import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class EditDistributionWizardPanel1 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private final transient ChangeSupport changeSupport;

    private transient EditDistributionVisualPanel1 component;
    private transient MavenProject mavenProject;
    private transient List<Artifact> selectedAppNames;
    private transient WizardDescriptor wizard;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new EditDistributionWizardPanel1 object.
     */
    public EditDistributionWizardPanel1() {
        changeSupport = new ChangeSupport(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new EditDistributionVisualPanel1(this);
        }

        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx(SampleWizardPanel1.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MavenProject getMavenProject() {
        return mavenProject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Artifact> getSelectedAppNames() {
        return selectedAppNames;
    }

    @Override
    public boolean isValid() {
        boolean valid = true;
        if (component.getSelectedApps().isEmpty()) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(
                    EditDistributionWizardPanel1.class,
                    "EditDistributionWizardPanel1.isValid().noItemSelected"));
            valid = false;
        } else {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
            valid = true;
        }

        return valid;
    }

    @Override
    public final void addChangeListener(final ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(final ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * DOCUMENT ME!
     */
    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        mavenProject = (MavenProject)wizard.getProperty(CreateDistributionAction.PROP_MAVEN_PROJECT);
        selectedAppNames = (List<Artifact>)wizard.getProperty(CreateDistributionAction.PROP_CHOSEN_APPS);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(CreateDistributionAction.PROP_CHOSEN_APPS, component.getSelectedApps());
    }
}
