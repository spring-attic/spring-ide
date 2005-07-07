/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.SimpleFactory;
import org.springframework.ide.eclipse.web.flow.core.internal.model.Action;
import org.springframework.ide.eclipse.web.flow.core.internal.model.ActionState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.internal.model.DecisionState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.EndState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.If;
import org.springframework.ide.eclipse.web.flow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.web.flow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.web.flow.core.internal.model.SubFlowState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.ViewState;

public class WebFlowEditorPaletteFactory {

    private static List createCategories(PaletteRoot root) {
        List categories = new ArrayList();
        categories.add(createControlGroup(root));
        categories.add(createComponentsDrawer());
        categories.add(createPropertiesDrawer());
        return categories;
    }

    private static PaletteContainer createComponentsDrawer() {

        PaletteDrawer drawer = new PaletteDrawer("Web Flow States", null);

        List entries = new ArrayList();

        CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
                "Action State", "Create a new Action State", ActionState.class,
                new SimpleFactory(ActionState.class),
                WebFlowImages.DESC_OBJS_ACTION_STATE,
                WebFlowImages.DESC_OBJS_ACTION_STATE);
        entries.add(combined);

        combined = new CombinedTemplateCreationEntry("View State",
                "Create a View State", ViewState.class, new SimpleFactory(
                        ViewState.class), WebFlowImages.DESC_OBJS_VIEW_STATE,
                WebFlowImages.DESC_OBJS_VIEW_STATE);
        entries.add(combined);

        combined = new CombinedTemplateCreationEntry("Subflow State",
                "Create a Subflow State", SubFlowState.class,
                new SimpleFactory(SubFlowState.class),
                WebFlowImages.DESC_OBJS_SUBFLOW_STATE,
                WebFlowImages.DESC_OBJS_SUBFLOW_STATE);
        entries.add(combined);

        combined = new CombinedTemplateCreationEntry("Decision State",
                "Create a Decision State", DecisionState.class,
                new SimpleFactory(DecisionState.class),
                WebFlowImages.DESC_OBJS_DECISION_STATE,
                WebFlowImages.DESC_OBJS_DECISION_STATE);
        entries.add(combined);

        combined = new CombinedTemplateCreationEntry("End State",
                "Create an End State", EndState.class, new SimpleFactory(
                        EndState.class), WebFlowImages.DESC_OBJS_END_STATE,
                WebFlowImages.DESC_OBJS_END_STATE);
        entries.add(combined);

        drawer.addAll(entries);
        return drawer;
    }

    private static PaletteContainer createControlGroup(PaletteRoot root) {
        PaletteGroup controlGroup = new PaletteGroup("Control Group");

        List entries = new ArrayList();

        ToolEntry tool = new SelectionToolEntry();
        entries.add(tool);
        root.setDefaultEntry(tool);

        tool = new MarqueeToolEntry();
        entries.add(tool);

        PaletteSeparator sep = new PaletteSeparator(WebFlowPlugin.PLUGIN_ID + 
                ".palette.sep2");
        sep
                .setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
        entries.add(sep);

        tool = new ConnectionCreationToolEntry("Transition",
                "Create a Transition",
                new SimpleFactory(StateTransition.class), WebFlowImages.DESC_OBJS_CONNECTION, 
                WebFlowImages.DESC_OBJS_ELSE_CONNECTION);
        entries.add(tool);
        tool = new ConnectionCreationToolEntry("Else Transition",
                "Create a Transition", new SimpleFactory(IfTransition.class),
                WebFlowImages.DESC_OBJS_ELSE_CONNECTION, 
                WebFlowImages.DESC_OBJS_ELSE_CONNECTION);;
        entries.add(tool);
        controlGroup.addAll(entries);
        return controlGroup;
    }

    /**
     * Creates the PaletteRoot and adds all Palette elements.
     * 
     * @return the root
     */
    public static PaletteRoot createPalette() {
        PaletteRoot flowPalette = new PaletteRoot();
        flowPalette.addAll(createCategories(flowPalette));
        return flowPalette;
    }

    private static PaletteContainer createPropertiesDrawer() {

        PaletteDrawer drawer = new PaletteDrawer("Web Flow Properties", null);

        List entries = new ArrayList();
        CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
                "Action", "Create a new Action within an Action State",
                Action.class, new SimpleFactory(Action.class),
                WebFlowImages.DESC_OBJS_ACTION, WebFlowImages.DESC_OBJS_ACTION);
        entries.add(combined);

//        combined = new CombinedTemplateCreationEntry("Attribute Mapper",
//                "Create an Attribute Mapper", AttributeMapper.class,
//                new SimpleFactory(AttributeMapper.class),
//                WebFlowImages.DESC_OBJS_ATTRIBUTE_MAPPER,
//                WebFlowImages.DESC_OBJS_ATTRIBUTE_MAPPER);
//        entries.add(combined);

        combined = new CombinedTemplateCreationEntry("If", "Create an If",
                If.class, new SimpleFactory(If.class), WebFlowImages.DESC_OBJS_IF,
                WebFlowImages.DESC_OBJS_IF);
        entries.add(combined);

        drawer.addAll(entries);
        return drawer;
    }

}