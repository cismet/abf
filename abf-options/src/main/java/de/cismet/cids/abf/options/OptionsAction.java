/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.options;

import org.netbeans.api.options.OptionsDisplayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class OptionsAction implements ActionListener {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        System.out.println(OptionsDisplayer.getDefault().open());
    }
}
