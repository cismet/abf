/*
 * NodeTypePropertyEditor.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 7. November 2007, 16:15
 *
 */

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.project.utils.Renderers;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class NodeTypePropertyEditor extends PropertyEditorSupport
        implements
        InplaceEditor.Factory,
        ExPropertyEditor
{
    private transient InplaceEditor editor;
    
    @Override
    public InplaceEditor getInplaceEditor()
    {
        if(editor == null)
        {
            editor = new NodeTypeInplaceEditor();
        }
        return editor;
    }

    @Override
    public void attachEnv(final PropertyEnv propertyEnv)
    {
        propertyEnv.registerInplaceEditorFactory(this);
    }
    
    private static class NodeTypeInplaceEditor implements InplaceEditor
    {
        private final transient JComboBox box;
        private transient PropertyEditor editor;
        private transient PropertyModel model;
        
        public NodeTypeInplaceEditor()
        {
            box = new JComboBox(new Object[] 
            {
                CatNode.Type.CLASS.getType(),
                CatNode.Type.OBJECT.getType(),
                CatNode.Type.ORG.getType()
            });
            box.setRenderer(new Renderers.NodeTypeRenderer());
        }
        
        @Override
        public void connect(final PropertyEditor propertyEditor,
                final PropertyEnv env)
        {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent()
        {
            return box;
        }

        @Override
        public void clear()
        {
            // not needed
        }

        @Override
        public Object getValue()
        {
            return box.getSelectedItem();
        }

        @Override
        public void setValue(final Object object)
        {
            box.setSelectedItem(object);
        }

        @Override
        public boolean supportsTextEntry()
        {
            return false;
        }

        @Override
        public void reset()
        {
            if(editor != null)
            {
                final Object o = editor.getValue();
                if(o != null)
                {
                    box.setSelectedItem(o);
                }
            }
        }

        @Override
        public void addActionListener(final ActionListener actionListener)
        {
            // not needed
        }

        @Override
        public void removeActionListener(final ActionListener actionListener)
        {
            // not needed
        }

        @Override
        public KeyStroke[] getKeyStrokes()
        {
            return new KeyStroke[0];
        }

        @Override
        public PropertyEditor getPropertyEditor()
        {
            return editor;
        }

        @Override
        public PropertyModel getPropertyModel()
        {
            return model;
        }

        @Override
        public void setPropertyModel(final PropertyModel propertyModel)
        {
            this.model = propertyModel;
        }

        @Override
        public boolean isKnownComponent(final Component component)
        {
            return box.equals(component) || box.isAncestorOf(component);
        }
    }
}