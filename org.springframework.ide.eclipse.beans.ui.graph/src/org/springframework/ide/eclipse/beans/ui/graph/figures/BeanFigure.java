/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphImages;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.model.ConstructorArgument;
import org.springframework.ide.eclipse.beans.ui.graph.model.Property;

public class BeanFigure extends Figure {

	public static final Color COLOR = new Color(null, 255, 255, 206);

	protected Bean bean;

	public BeanFigure(Bean bean) {
		this.bean = bean;
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);
		setBorder(new ShadowedLineBorder(ColorConstants.black, 1, 4));
		setBackgroundColor(COLOR);
		setOpaque(true);

		// Prepare child figures
		add(createLabel(bean));
		if (bean.hasConstructorArguments()) {
			add(createConstructorArguments(bean));
		}
		if (bean.hasProperties()) {
			add(createProperties(bean));
		}
	}

	protected Label createLabel(Bean bean) {
		Label label = new Label();
		label.setText(bean.getName());
		if (bean.isRootBean()) {
			label.setIcon(BeansGraphImages.getImage(
										  BeansGraphImages.IMG_OBJS_ROOT_BEAN));
			label.setToolTip(new Label("Class: " + bean.getClassName()));
		} else {
			label.setIcon(BeansGraphImages.getImage(
										 BeansGraphImages.IMG_OBJS_CHILD_BEAN));
			label.setToolTip(new Label("Parent: " + bean.getParentName()));
		}
		return label;
	}

	protected ConstructorArgumentFigure createConstructorArguments(Bean bean) {
		ConstructorArgument[] cargs = bean.getConstructorArguments();
		ConstructorArgumentFigure figure = new ConstructorArgumentFigure();
		for (int i = 0; i < cargs.length; i++) {
			ConstructorArgument carg = cargs[i];
			Label label = new Label(carg.getName());
//			if (carg.isBeanReference()) {
				label.setIcon(BeansGraphImages.getImage(
										BeansGraphImages.IMG_OBJS_CONSTRUCTOR));
				label.setToolTip(new Label("Value: " +
										   carg.getNode().getValue()));
//			} else {
//			}
			figure.add(label);
		}
		return figure;
	}

	protected PropertiesFigure createProperties(Bean bean) {
		Property[] props = bean.getProperties();
		PropertiesFigure properties = new PropertiesFigure();
		for (int i = 0; i < props.length; i++) {
			Property prop = props[i];
			Label label = new Label(prop.getName());
//			if (prop.isBeanReference()) {
				label.setIcon(BeansGraphImages.getImage(
										   BeansGraphImages.IMG_OBJS_PROPERTY));
				label.setToolTip(new Label("Value: " +
												prop.getNode().getValue()));
//			} else {
//			}
			properties.add(label);
		}
		return properties;
	}

	protected void paintFigure(Graphics graphics) {
		if (isOpaque()) {
			graphics.fillRectangle(getClientArea());
		}
	}

	public String toString() {
		Rectangle rect = getBounds();
		return "BeanFigure '" + bean.getName() + "': x=" + rect.x + ", y=" +
			   rect.y + ", width=" + rect.width + ", height=" + rect.height;
	}
}
