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

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

import java.util.List;

import de.cismet.cids.abf.domainserver.project.cidsclass.CidsAttributeNode;

/**
 * This class represents a pin widget in the VMD plug-in.
 *
 * @author   David Kaspar
 * @version  $Revision$, $Date$
 */
public class AttributeWidget extends Widget {

    //~ Instance fields --------------------------------------------------------

    private CidsAttributeNode cidsAttributeNode;
    private LabelWidget nameWidget;
    private GlyphSetWidget glyphsWidget;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a pin widget.
     *
     * @param  scene  the scene
     */
    public AttributeWidget(final Scene scene) {
        super(scene);
        setBorder(ClassNodeWidget.BORDER);
        setBackground(ClassNodeWidget.COLOR_SELECTED);
        setOpaque(false);
        setLayout(LayoutFactory.createHorizontalLayout(LayoutFactory.SerialAlignment.CENTER, 8));
        addChild(nameWidget = new LabelWidget(scene));
        addChild(glyphsWidget = new GlyphSetWidget(scene));

        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Called to notify about the change of the widget state.
     *
     * @param  previousState  the previous state
     * @param  state          the new state
     */
    @Override
    protected void notifyStateChanged(final ObjectState previousState, final ObjectState state) {
        setOpaque(state.isSelected());
        setBorder((state.isFocused() || state.isHovered()) ? ClassNodeWidget.BORDER_HOVERED : ClassNodeWidget.BORDER);
//        LookFeel lookFeel = getScene ().getLookFeel ();
//        setBorder (BorderFactory.createCompositeBorder (BorderFactory.createEmptyBorder (8, 2), lookFeel.getMiniBorder (state)));
//        setForeground (lookFeel.getForeground (state));
    }

    /**
     * Returns a pin name widget.
     *
     * @return  the pin name widget
     */
    public Widget getPinNameWidget() {
        return nameWidget;
    }

    /**
     * Sets a pin name.
     *
     * @param  name  the pin name
     */
    public void setPinName(final String name) {
        nameWidget.setLabel(name);
    }

    /**
     * Returns a pin name.
     *
     * @return  the pin name
     */
    public String getPinName() {
        return nameWidget.getLabel();
    }

    /**
     * Sets pin glyphs.
     *
     * @param  glyphs  the list of images
     */
    public void setGlyphs(final List<Image> glyphs) {
        glyphsWidget.setGlyphs(glyphs);
    }

    /**
     * Sets all pin properties at once.
     *
     * @param  name    the pin name
     * @param  glyphs  the pin glyphs
     */
    public void setProperties(final String name, final List<Image> glyphs) {
        setPinName(name);
        glyphsWidget.setGlyphs(glyphs);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsAttributeNode getCidsAttributeNode() {
        return cidsAttributeNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsAttributeNode  DOCUMENT ME!
     */
    public void setCidsAttributeNode(final CidsAttributeNode cidsAttributeNode) {
        this.cidsAttributeNode = cidsAttributeNode;
    }
}
