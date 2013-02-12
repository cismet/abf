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
 * Manages a module's lifecycle. Remember that an installer is optional and often not needed at all.
 *
 * @version  $Revision$, $Date$
 */
public class Installer extends ModuleInstall {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void restored() {
        LoggingOptionsPanelController.adjustLogLevel();
    }
}
