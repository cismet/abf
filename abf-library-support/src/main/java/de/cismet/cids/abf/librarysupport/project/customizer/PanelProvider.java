/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.customizer;

import org.netbeans.spi.project.ui.support.ProjectCustomizer;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public final class PanelProvider implements ProjectCustomizer.CategoryComponentProvider {

    //~ Instance fields --------------------------------------------------------

    private final transient Map panels;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PanelProvider object.
     *
     * @param  panels  DOCUMENT ME!
     */
    public PanelProvider(final Map panels) {
        this.panels = panels;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public JComponent create(final ProjectCustomizer.Category category) {
        final JComponent panel = (JComponent)panels.get(category);
        return (panel == null) ? new JPanel() : panel;
    }
}
