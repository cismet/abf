/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import java.awt.Component;

import java.beans.PropertyEditorSupport;

import java.util.List;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ConflictingCfgAttrPropertyEditor extends PropertyEditorSupport {

    //~ Instance fields --------------------------------------------------------

    private final transient ConflictingCfgAttrPropertyViewerPanel viewer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConflictingCfgAttrPropertyEditor object.
     *
     * @param  mainEntry              DOCUMENT ME!
     * @param  mainEntryOriginUgName  DOCUMENT ME!
     * @param  conflictingEntries     DOCUMENT ME!
     */
    public ConflictingCfgAttrPropertyEditor(final ConfigAttrEntry mainEntry,
            final String mainEntryOriginUgName,
            final List<Object[]> conflictingEntries) {
        this.viewer = new ConflictingCfgAttrPropertyViewerPanel(mainEntry, mainEntryOriginUgName, conflictingEntries);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getCustomEditor() {
        return viewer;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }
}
