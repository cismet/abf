/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * This class represents a node anchor. The anchor could be assign by multiple connection widgets. For each usage the
 * anchor resolves a different possition. The position are resolved at the top and the bottom of the widget where the
 * anchor is attached to.
 *
 * @author   David Kaspar
 * @version  $Revision$, $Date$
 */
public class NodeAnchor extends Anchor {

    //~ Static fields/initializers ---------------------------------------------

    private static final int PIN_GAP = 8;

    //~ Instance fields --------------------------------------------------------

    private boolean requiresRecalculation = true;

    private HashMap<Entry, Result> results = new HashMap<Entry, Result>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a node anchor.
     *
     * @param  widget  the node widget where the anchor is attached to
     */
    public NodeAnchor(final Widget widget) {
        super(widget);
        assert widget != null;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Notifies when an entry is registered.
     *
     * @param  entry  the registered entry
     */
    @Override
    protected void notifyEntryAdded(final Entry entry) {
        requiresRecalculation = true;
    }

    /**
     * Notifies when an entry is unregistered.
     *
     * @param  entry  the unregistered entry
     */
    @Override
    protected void notifyEntryRemoved(final Entry entry) {
        results.remove(entry);
        requiresRecalculation = true;
    }

    /**
     * DOCUMENT ME!
     */
    private void recalculate() {
        if (!requiresRecalculation) {
            return;
        }

        final Widget widget = getRelatedWidget();
        final Point relatedLocation = getRelatedSceneLocation();

        final HashMap<Entry, Float> topmap = new HashMap<Entry, Float>();
        final HashMap<Entry, Float> bottommap = new HashMap<Entry, Float>();

        for (final Entry entry : getEntries()) {
            final Point oppositeLocation = getOppositeSceneLocation(entry);
            final int dy = oppositeLocation.y - relatedLocation.y;
            final int dx = oppositeLocation.x - relatedLocation.x;

            if (dy > 0) {
                bottommap.put(entry, (float)dx / (float)dy);
            } else if (dy < 0) {
                topmap.put(entry, (float)-dx / (float)dy);
            } else {
                topmap.put(entry, (dx < 0) ? Float.MAX_VALUE : Float.MIN_VALUE);
            }
        }

        final Entry[] topList = toArray(topmap);
        final Entry[] bottomList = toArray(bottommap);

        final Rectangle bounds = widget.convertLocalToScene(widget.getBounds());

        int y = bounds.y;
        int len = topList.length;

        for (int a = 0; a < len; a++) {
            final Entry entry = topList[a];
            final int x = bounds.x + ((a + 1) * bounds.width / (len + 1));
            results.put(entry, new Result(new Point(x, y - PIN_GAP), Direction.TOP));
        }

        y = bounds.y + bounds.height;
        len = bottomList.length;

        for (int a = 0; a < len; a++) {
            final Entry entry = bottomList[a];
            final int x = bounds.x + ((a + 1) * bounds.width / (len + 1));
            results.put(entry, new Result(new Point(x, y + PIN_GAP), Direction.BOTTOM));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   map  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Entry[] toArray(final HashMap<Entry, Float> map) {
        final Set<Entry> keys = map.keySet();
        final Entry[] entries = keys.toArray(new Entry[keys.size()]);
        Arrays.sort(entries, new Comparator<Entry>() {

                @Override
                public int compare(final Entry o1, final Entry o2) {
                    final float f = map.get(o1) - map.get(o2);
                    if (f > 0.0f) {
                        return 1;
                    } else if (f < 0.0f) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        return entries;
    }

    /**
     * Computes a result (position and direction) for a specific entry.
     *
     * @param   entry  the entry
     *
     * @return  the calculated result
     */
    @Override
    public Result compute(final Entry entry) {
        recalculate();
        return results.get(entry);
    }
}
