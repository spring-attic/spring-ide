package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;

public class BeansModelUtil {

	/**
	 * Given a bean's property or constructor argument and it's value, adds any
	 * beans referenced by it's value. This value could be:
	 * <li>A RuntimeBeanReference, which bean will be added.
	 * <li>A List. This is a collection that may contain RuntimeBeanReferences
	 * which will be added.
	 * <li>A Set. May also contain RuntimeBeanReferences that will be added.
	 * <li>A Map. In this case the value may be a RuntimeBeanReference that will
	 * be added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 */
	public static void addReferencedBeansForValue(IBeansModelElement element,
												  Object value, Map refBeans) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			IBeansModelElement parent = element.getElementParent().getElementParent();
			IBean refBean = null;
			if (parent instanceof IBeansConfig) {
				refBean = ((IBeansConfig) parent).getBean(beanName);
			} else if (parent instanceof IBeansConfigSet) {
				refBean = ((IBeansConfigSet) parent).getBean(beanName);
			}
			if (refBean != null) {
				if (!refBeans.containsKey(refBean.getElementName())) {
					refBeans.put(refBean.getElementName(), refBean);
				}
				Iterator iter = refBean.getReferencedBeans().iterator();
				while (iter.hasNext()) {
					refBean = (IBean) iter.next();
					if (!refBeans.containsKey(refBean.getElementName())) {
						refBeans.put(refBean.getElementName(), refBean);
					}
				}
			}
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				addReferencedBeansForValue(element, list.get(i), refBeans);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				addReferencedBeansForValue(element, iter.next(), refBeans);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				addReferencedBeansForValue(element, map.get(iter.next()),
										   refBeans);
			}
		}
	}
}
