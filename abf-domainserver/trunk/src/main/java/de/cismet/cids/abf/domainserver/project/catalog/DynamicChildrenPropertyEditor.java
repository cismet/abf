/*
 * DynamicChildrenPropertyEditor.java, encoding: UTF-8
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
 * Created on 13. November 2007, 18:41
 *
 */

package de.cismet.cids.abf.domainserver.project.catalog;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

/**
 *
 * @author martin.scholl@cismet.de
 */
public class DynamicChildrenPropertyEditor extends PropertyEditorSupport
        implements
        ExPropertyEditor
{
    private transient boolean editable;
    private transient PropertyEnv env;
    
    @Override
    public void setAsText(final String s)
    {
        if("null".equals(s) && getValue() == null) // NOI18N
        {
            return;
        }
        setValue(s);
    }

    @Override
    public void attachEnv(final PropertyEnv env)
    {
        this.env = env;
        final FeatureDescriptor desc = env.getFeatureDescriptor();
        if(desc instanceof Node.Property)
        {
            final Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
        }
    }
    
    @Override
    public boolean supportsCustomEditor()
    {
        return true;
    }

    @Override
    public Component getCustomEditor()
    {
        final Object value = getValue();
        final String dc = (value == null) ? "" : value.toString();
        return new DynamicChildrenSimpleEditor(dc, editable);
    }
}
