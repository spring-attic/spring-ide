/**
 * 
 */
package org.springframework.ide.eclipse.beans.ui.wizards.wizards.pages;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.IdRefModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.ListModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.MapEntryModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.MapModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.PropModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.PropertyModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.PropsModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.RefModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.SetModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.ValueModelItem;

class PropertiesListLabelProvider extends LabelProvider {

	private Image fPropertyImage;

	private Image fValueImage;

	private Image fRefImage;
	
	private Image fIdRefImage;

	private Image fMapImage;

	private Image fListImage;

	private Image fPropsImage;

	private Image fSetImage;

	private Image fMapEntryImage;

	private Image fPropImage;

	public PropertiesListLabelProvider() {
		super();
		fPropertyImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		fValueImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_VALUE);
		fRefImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN_REF);
		//FIXME find a proper image for an idRef
		fIdRefImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN_REF);
		fMapImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
		fListImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
		fPropsImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
		fSetImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
		//FIXME find a proper image for a map entry and for a prop
		fMapEntryImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_DESCRIPTION);
		fPropImage = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_DESCRIPTION);
	}

	public Image getImage(Object element) {
		Image result = null;
		if (element instanceof PropertyModelItem) {
			result = fPropertyImage;
		}
		if (element instanceof ValueModelItem) {
			result = fValueImage;
		}
		if (element instanceof RefModelItem) {
			result = fRefImage;
		}
		if (element instanceof IdRefModelItem) {
			result = fIdRefImage;
		}
		if (element instanceof ListModelItem) {
			result = fListImage;
		}
		if (element instanceof MapModelItem) {
			result = fMapImage;
		}
		if (element instanceof SetModelItem) {
			result = fSetImage;
		}
		if (element instanceof PropsModelItem) {
			result = fPropsImage;
		}
		if (element instanceof MapEntryModelItem) {
			result = fMapEntryImage;
		}
		if (element instanceof PropModelItem) {
			result = fPropImage;
		}
		return result;
	}

	public String getText(Object element) {
		String result = "Undetermined content";
		if (element instanceof PropertyModelItem) {
			PropertyModelItem propertyModelItem = ((PropertyModelItem) element);
			if (propertyModelItem.getType() != null) {
				String typeName = propertyModelItem.getType().getElementName();
				result = propertyModelItem.getName() + "  <" + typeName + ">";
			} else {
				if (propertyModelItem.isPrimitive()) {
					result =propertyModelItem.getName()+ "<" + propertyModelItem.getPrimitiveTypeName() + ">";
				} else {
					result = propertyModelItem.getName()+"<???>";
				}
			}
		}
		if (element instanceof ValueModelItem) {
			ValueModelItem valueModelItem = ((ValueModelItem) element);
			result = valueModelItem.getValue();
		}
		if (element instanceof RefModelItem) {
			RefModelItem refModelItem=((RefModelItem)element);
			result=refModelItem.getBeanId();
		}
		if (element instanceof IdRefModelItem) {
			IdRefModelItem idRefModelItem=((IdRefModelItem)element);
			result=idRefModelItem.getBeanId();
		}
		if (element instanceof ListModelItem) {
			result = "<list>";
		}
		if (element instanceof MapModelItem) {
			result = "<map>";
		}
		if (element instanceof SetModelItem) {
			result = "<set>";
		}
		if (element instanceof PropsModelItem) {
			result = "<props";
		}
		if (element instanceof MapEntryModelItem) {
			MapEntryModelItem mapEntryModelItem=((MapEntryModelItem)element);
			result=mapEntryModelItem.getKeyValue();
		}
		if (element instanceof PropModelItem) {
			PropModelItem propModelItem=((PropModelItem)element);
			result=propModelItem.getKey()+"="+propModelItem.getValue();
		}
		return result;
	}

}