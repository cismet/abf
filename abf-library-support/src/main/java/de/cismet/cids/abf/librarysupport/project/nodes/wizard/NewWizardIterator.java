/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import org.openide.WizardDescriptor;

import java.awt.Component;

import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.4
 */
public final class NewWizardIterator implements WizardDescriptor.Iterator {

    //~ Instance fields --------------------------------------------------------

    private transient int index;

    private transient WizardDescriptor.Panel[] panels;

    //~ Methods ----------------------------------------------------------------

    /**
     * Initialize panels representing individual wizard's steps and sets various properties for them influencing wizard
     * appearance.
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                    // new NewWizardPanel1(),
                    new NewWizardPanel2()
                };
            final String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                final Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    final JComponent jc = (JComponent)c;
                    // Sets step number of a component
                    jc.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                        Integer.valueOf(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,
                        steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(
                        WizardDescriptor.PROP_AUTO_WIZARD_STYLE,
                        Boolean.TRUE);
                    // Show steps on the left side with the image on the
                    // background
                    jc.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_DISPLAYED,
                        Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED,
                        Boolean.TRUE);
                }
            }
        }
        return Arrays.copyOf(panels, panels.length);
    }

    @Override
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return index < (getPanels().length - 1);
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(final ChangeListener l) {
        // not needed
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
        // not needed
    }
}
