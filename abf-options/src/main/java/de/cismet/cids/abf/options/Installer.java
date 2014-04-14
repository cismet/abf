/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.options;

import org.openide.modules.ModuleInstall;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Installer extends ModuleInstall {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void restored() {
        GeneralOptionsPanelController.adjustLogLevel();
    }
}
