/*
 * JavaClassTypePropertyEditor.java, encoding: UTF-8
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
 * Created on 26. November 2007, 10:42
 *
 */

package de.cismet.cids.abf.domainserver.project.javaclass;

import de.cismet.cids.abf.domainserver.project.utils.Renderers;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Field;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.apache.log4j.Logger;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class JavaClassTypePropertyEditor extends PropertyEditorSupport
        implements
        InplaceEditor.Factory,
        ExPropertyEditor
{
    private static final transient Logger LOG = Logger.getLogger(
            JavaClassTypePropertyEditor.class);
    
    private transient JavaClassTypeInplaceEditor editor;
    
    @Override
    public InplaceEditor getInplaceEditor()
    {
        if(editor == null)
        {
            editor = new JavaClassTypeInplaceEditor();
        }
        return editor;
    }
    
    @Override
    public void attachEnv(final PropertyEnv propertyEnv)
    {
        propertyEnv.registerInplaceEditorFactory(this);
    }
    
    final class JavaClassTypeInplaceEditor implements InplaceEditor
    {
        private final transient JComboBox cboBox;
        private transient PropertyEditor editor;
        private transient PropertyModel model;
        
        JavaClassTypeInplaceEditor()
        {
            final Field[] fields = JavaClass.Type.class.getFields();
            final String[] types = new String[fields.length];
            try
            {
                for(int i = 0; i < fields.length; ++i)
                {
                    final Object o = fields[i].get(new Object());
                    types[i] = ((JavaClass.Type)o).getType();
                }
            }catch(final IllegalArgumentException ex)
            {
                LOG.error("could not add type to typearray", ex); // NOI18N
            }catch(final IllegalAccessException ex)
            {
                LOG.error("could not add type to typearray", ex); // NOI18N
            }
            cboBox = new JComboBox(types);
            cboBox.setRenderer(new Renderers.JavaClassTypeRenderer());
        }
        
        @Override
        public void connect(final PropertyEditor propertyEditor, final 
                PropertyEnv propertyEnv)
        {
            editor = propertyEditor;
            reset();
        }
        
        @Override
        public JComponent getComponent()
        {
            return cboBox;
        }
        
        @Override
        public void clear()
        {
            // nothing to clear
        }
        
        @Override
        public Object getValue()
        {
            return cboBox.getSelectedItem();
        }
        
        @Override
        public void setValue(final Object object)
        {
            if(object != null)
            {
                cboBox.setSelectedItem(object);
            }
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
                    cboBox.setSelectedItem(o);
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
            return component.equals(cboBox) || cboBox.isAncestorOf(component);
        }
    }
}