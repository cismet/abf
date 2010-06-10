/*
 * LibrarySupportProjectCustomizer.java, encoding: UTF-8
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
 * Created on 25. August 2007, 14:24
 *
 */

package de.cismet.cids.abf.librarysupport.project.customizer;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProjectNode;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 *
 * @author mscholl
 * @version 1.4
 */
public final class LibrarySupportProjectCustomizer implements CustomizerProvider
{
    private static final String GENERAL_CATEGORY = "general"; // NOI18N
    private static final String DEPLOY_CATEGORY = "deploy"; // NOI18N
    private static final String DEPLOY_KEYSTORE = "deployKeystore"; // NOI18N

    private final transient LibrarySupportProject project;
    
    private transient ProjectCustomizer.Category[] categories;
    private transient ProjectCustomizer.CategoryComponentProvider panelProvider;
    private transient ManifestVisualPanel manifestVis;
    private transient KeystoreVisualPanel keystoreVis;
    
    /** Creates a new instance of LibrarySupportProjectCustomizer */
    public LibrarySupportProjectCustomizer(final LibrarySupportProject project)
    {
        this.project = project;
    }

    private void init()
    {
        final ProjectCustomizer.Category deployKeystore = ProjectCustomizer.
                Category.create(
                DEPLOY_KEYSTORE,
                org.openide.util.NbBundle
                    .getMessage(LibrarySupportProjectCustomizer.class, 
                    "Lbl_keystore"), // NOI18N
                null, // TODO: provide icon
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deployManifest = ProjectCustomizer.
                Category.create(
                DEPLOY_KEYSTORE,
                org.openide.util.NbBundle
                    .getMessage(LibrarySupportProjectCustomizer.class, 
                    "Lbl_manifest"), // NOI18N
                null, // TODO: provide icon
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category[] deployCategories =
                new ProjectCustomizer.Category[2];
        deployCategories[0] = deployKeystore;
        deployCategories[1] = deployManifest;
        final ProjectCustomizer.Category general = ProjectCustomizer.Category.
                create(
                GENERAL_CATEGORY,
                org.openide.util.NbBundle
                    .getMessage(LibrarySupportProjectCustomizer.class,
                    "Lbl_general"), // NOI18N
                null, // TODO: provide icon)
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deploy = ProjectCustomizer.Category.
                create(
                DEPLOY_CATEGORY,
                org.openide.util.NbBundle
                    .getMessage(LibrarySupportProjectCustomizer.class,
                    "Lbl_incorporate"), // NOI18N
                null, //TODO: provide icon
                deployCategories);
        categories = new ProjectCustomizer.Category[2];
        categories[0] = general;
        categories[1] = deploy;
        final Map panels = new HashMap(2);
        final GeneralVisualPanel generalVis = new GeneralVisualPanel();
        final DeployVisualPanel deployVis = new DeployVisualPanel();
        keystoreVis = new KeystoreVisualPanel(project);
        manifestVis = new ManifestVisualPanel(project);
        panels.put(general, generalVis);
        panels.put(deploy, deployVis);
        panels.put(deployKeystore, keystoreVis);
        panels.put(deployManifest, manifestVis);
        panelProvider = new PanelProvider(panels);
    }
    
    @Override
    public void showCustomizer()
    {
        init();
        final OptionListener okListener = new OptionListener();
        final Dialog dialog = ProjectCustomizer.createCustomizerDialog(
                categories, panelProvider, GENERAL_CATEGORY, okListener, null);
        dialog.addWindowListener(okListener);
        final String title = org.openide.util.NbBundle
                .getMessage(LibrarySupportProjectCustomizer.class,
                "Lbl_projectProperties") + ProjectUtils // NOI18N
                .getInformation(project).getDisplayName();
        dialog.setTitle(title);
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    private final class OptionListener extends WindowAdapter implements 
            ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
            final PropertyProvider provider = PropertyProvider.getInstance(
                    project.getProjectProperties());
            final String mainKeystore = keystoreVis.getMainKeystore();
            final String mainKeystorePW = keystoreVis.getPassword();
            final String basicManPath = manifestVis.getBasicManifestField().
                    getText();
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE, mainKeystore);
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE_PW,
                    mainKeystorePW);
            provider.put(PropertyProvider.KEY_GENERAL_MANIFEST, basicManPath);
            provider.save();
        }
        
        @Override
        public void windowClosed(final WindowEvent we)
        {
            PropertyProvider.getInstance(project.getProjectProperties()).
                    clearInternal();
            project.getLookup().lookup(LibrarySupportProjectNode.class).
                    firePropertiesChange();
        }
    }
}
