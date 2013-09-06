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

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * DOCUMENT ME!
 *
 * @author   David Kaspar
 * @version  $Revision$, $Date$
 */
class NodeBorder implements Border {

    //~ Static fields/initializers ---------------------------------------------

// static final Color COLOR_BORDER = new Color (0xBACDF0); //Hellblau
// private static final Insets INSETS = new Insets (1, 1, 1, 1);
////    private static final Color COLOR0 = new Color (169, 197, 235);
//    private static final Color COLOR1 = new Color (221, 235, 246);  //Hellerblau
//    private static final Color COLOR2 = new Color (255, 255, 255); // Wei�
//    private static final Color COLOR3 = new Color (214, 235, 255); //
//    private static final Color COLOR4 = new Color (241, 249, 253); //noch Hellerblau
//    private static final Color COLOR5 = new Color (255, 255, 255);

    static final Color COLOR_BORDER = java.awt.SystemColor.controlShadow;
    private static final Insets INSETS = new Insets(1, 1, 1, 1);
    private static final Color COLOR1 = java.awt.SystemColor.control;
    private static final Color COLOR2 = new Color(255, 255, 255); // Wei�
    private static final Color COLOR3 = java.awt.SystemColor.control;
    private static final Color COLOR4 = java.awt.SystemColor.controlHighlight;
    private static final Color COLOR5 = new Color(255, 255, 255);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NodeBorder object.
     */
    NodeBorder() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Insets getInsets() {
        return INSETS;
    }

    @Override
    public void paint(final Graphics2D gr, final Rectangle bounds) {
        final Shape previousClip = gr.getClip();
        gr.clip(new RoundRectangle2D.Float(bounds.x, bounds.y, bounds.width, bounds.height, 4, 4));

        drawGradient(gr, bounds, COLOR1, COLOR2, 0f, 0.3f);
        drawGradient(gr, bounds, COLOR2, COLOR3, 0.3f, 0.764f);
        drawGradient(gr, bounds, COLOR3, COLOR4, 0.764f, 0.927f);
        drawGradient(gr, bounds, COLOR4, COLOR5, 0.927f, 1f);

        gr.setColor(COLOR_BORDER);
        gr.draw(new RoundRectangle2D.Float(
                bounds.x
                        + 0.5f,
                bounds.y
                        + 0.5f,
                bounds.width
                        - 1,
                bounds.height
                        - 1,
                4,
                4));

        gr.setClip(previousClip);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gr      DOCUMENT ME!
     * @param  bounds  DOCUMENT ME!
     * @param  color1  DOCUMENT ME!
     * @param  color2  DOCUMENT ME!
     * @param  y1      DOCUMENT ME!
     * @param  y2      DOCUMENT ME!
     */
    private void drawGradient(final Graphics2D gr,
            final Rectangle bounds,
            final Color color1,
            final Color color2,
            float y1,
            float y2) {
        y1 = bounds.y + (y1 * bounds.height);
        y2 = bounds.y + (y2 * bounds.height);
        gr.setPaint(new GradientPaint(bounds.x, y1, color1, bounds.x, y2, color2));
        gr.fill(new Rectangle.Float(bounds.x, y1, bounds.x + bounds.width, y2));
    }

    @Override
    public boolean isOpaque() {
        return true;
    }
}
