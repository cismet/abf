/*
 * SatelliteLookupHint.java
 *
 * Created on 17. Januar 2007, 12:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import org.netbeans.spi.navigator.NavigatorLookupHint;


/**
 *
 * @author hell
 */
public class SatelliteLookupHint implements NavigatorLookupHint {
    public String getContentType() {
        return "cismet/satellite-wiring";
    }
}