/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.model.ConstructorArgument;
import org.springframework.ide.eclipse.beans.ui.graph.model.Property;

/**
 * A Figure representating a bean
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeanFigure extends Figure {

	public static final Color COLOR = new Color(null, 255, 255, 206);

	public static final int MAX_NAME_LENGTH = 20;

	protected Bean bean;

	private IFigure contents;

	private Label label;

	private ConstructorArgumentFigure constructorArgumentFigure;

	private PropertiesFigure propertiesFigure;

	public BeanFigure(Bean bean) {
		this.bean = bean;
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);
		setBorder(new ShadowedLineBorder(ColorConstants.black, 1, 4));
		setOpaque(true);

		// Prepare child figures
		this.contents = new Figure();
		label = createLabel(bean);
		add(label);
		if (bean.hasConstructorArguments()) {
			constructorArgumentFigure = createConstructorArguments(bean);
			add(constructorArgumentFigure);
		}
		if (bean.hasProperties()) {
			propertiesFigure = createProperties(bean);
			add(propertiesFigure);
		}
		
		// Add the inner child beans
		if (bean.getInnerBeans().length > 0) {
			createInnerBeans(bean);
		}
		add(contents);
	}

	protected void createInnerBeans(Bean bean) {
		contents.setLayoutManager(new ToolbarLayout());
		contents.setBorder(new MarginBorder(new Insets(8, 11, 8, 8)));
		for (Bean innerBean : bean.getInnerBeans()) {
			contents.add(new BeanFigure(innerBean));
		}
	}

	protected Label createLabel(Bean bean) {
		Label label = new Label();
		label.setText(bean.getName());
		if (bean.isRootBean()) {
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(bean.getBean()));
			if (bean.getClassName() != null) {
				label.setToolTip(new Label("Class: " + bean.getClassName()));
			}
			else {
				// TODO set tooltip for abstract beans, bean factories, ...
				label.setToolTip(new Label("Class: <no class specified>"));
			}
		}
		else if (bean.isChildBean()) {
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(bean.getBean()));
			label.setToolTip(new Label("Parent: " + bean.getParentName()));
		}
		else {
			// FIXME Handle factory beans
			// label.setIcon(BeansGraphImages.getImage(
			// BeansGraphImages.IMG_OBJS_CHILD_BEAN));
			// label.setToolTip(new Label("Parent: " + bean.getParentName()));
		}
		return label;
	}

	protected ConstructorArgumentFigure createConstructorArguments(Bean bean) {
		ConstructorArgument[] cargs = bean.getConstructorArguments();
		ConstructorArgumentFigure figure = new ConstructorArgumentFigure();
		for (ConstructorArgument carg : cargs) {
			String name = carg.getName();
			Label label = new Label();

//			// Display a truncated element name if necessary
//			if (name.length() > MAX_NAME_LENGTH) {
//				label.setText(name.substring(0, MAX_NAME_LENGTH) + "...");
//				label.setToolTip(new Label(name));
//			}
//			else {
				label.setText(name);
				Object value = carg.getBeanConstructorArgument().getValue();
				label.setToolTip(new Label(createToolTipForValue(value)));
//			}
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(
					carg.getBeanConstructorArgument()));
			figure.add(label);
		}
		return figure;
	}

	protected PropertiesFigure createProperties(Bean bean) {
		Property[] props = bean.getProperties();
		PropertiesFigure properties = new PropertiesFigure();
		for (Property prop : props) {
			Label label = new Label(prop.getName());
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(prop.getBeanProperty()));
			Object value = prop.getBeanProperty().getValue();
			label.setToolTip(new Label(createToolTipForValue(value)));
			properties.add(label);
		}
		return properties;
	}

	private String createToolTipForValue(Object value) {
		StringBuffer toolTip = new StringBuffer("Value: ");
		if (value == null) {
			toolTip.append("NULL");
		}
		else if (value instanceof RuntimeBeanReference) {
			toolTip.append('<');
			toolTip.append(((RuntimeBeanReference) value).getBeanName());
			toolTip.append('>');
		}
		else if (value instanceof BeanDefinitionHolder) {
			toolTip.append('{');
			toolTip.append(((BeanDefinitionHolder) value).getBeanName());
			toolTip.append('}');
		}
		else {
			toolTip.append(value.toString());
		}
		return toolTip.toString();
	}

	@Override
	public String toString() {
		Rectangle rect = getBounds();
		return "BeanFigure '" + bean.getName() + "': x=" + rect.x + ", y=" + rect.y + ", width="
				+ rect.width + ", height=" + rect.height;
	}

	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
		Rectangle r = super.getBounds();
		g.setAntialias(SWT.ON);
		g.setBackgroundColor(COLOR);
		g.fillRectangle(r.x, r.y, 5, r.height - 5);
		g.fillRectangle(r.right() - 9, r.y, 5, r.height - 5);
		g.fillRectangle(r.x, r.bottom() - 9, r.width - 5, 5);
		g.fillRectangle(r.x, r.y, r.width - 5, getBeanSize());
	}

	public int getBeanSize() {
		int size = label.getPreferredSize().height;
		if (constructorArgumentFigure != null) {
			size += constructorArgumentFigure.getPreferredSize().height;
		}
		if (propertiesFigure != null) {
			size += propertiesFigure.getPreferredSize().height;
		}
		return size + 1;
	}

	public IFigure getContents() {
		return contents;
	}

}
