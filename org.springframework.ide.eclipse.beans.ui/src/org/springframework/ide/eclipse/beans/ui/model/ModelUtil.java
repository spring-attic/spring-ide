package org.springframework.ide.eclipse.beans.ui.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.RuntimeBeanReference;

/**
 * Model related helper methods.
 */
public class ModelUtil {

	/**
	 * Given a bean's property or constructor argment value, adds any referenced
	 * beans. The value could be:
	 * <li>A RuntimeBeanReference, which bean will be added.
	 * <li>A List. This is a collection that may contain RuntimeBeanReferences
	 * which will be added.
	 * <li>A Set. May also contain RuntimeBeanReferences that will be added.
	 * <li>A Map. In this case the value may be a RuntimeBeanReference that will
	 * be added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 */
	public static final void addReferencedBeansForValue(INode beanParentNode,
												  Object value, List refBeans) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			addReferencedBeansForBean(beanParentNode, beanName, refBeans);
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				addReferencedBeansForValue(beanParentNode, list.get(i),
										   refBeans);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				addReferencedBeansForValue(beanParentNode, iter.next(),
										 refBeans);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				addReferencedBeansForValue(beanParentNode, map.get(iter.next()),
										 refBeans);
			}
		}
	}

	/**
	 * Adds all beans which are referenced by the specified bean to the given
	 * list.
	 * @param beanParentNode  config or config the specified bean belongs to
	 */
	public static void addReferencedBeansForBean(INode beanParentNode,
											   String beanName, List refBeans) {
		BeanNode refBean = getBean(beanParentNode, beanName);
		if (refBean != null && !refBeans.contains(refBean)) {
			refBeans.add(refBean);
			Iterator iter = refBean.getReferencedBeans().iterator();
			while (iter.hasNext()) {
				refBean = (BeanNode) iter.next();
				if (!refBeans.contains(refBean)) {
					refBeans.add(refBean);
				}
			}
		}
	}

	/**
	 * Returns the BeanNode for a given bean name from specified ConfigNode or
	 * ConfigSet.
	 * @return BeanNode or null if bean not defined
	 */
	public static final BeanNode getBean(INode node, String beanName) {
		BeanNode bean = null;
		if (node instanceof ConfigNode) {
			bean = ((ConfigNode) node).getBean(beanName);
		} else if (node instanceof ConfigSetNode) {
			bean = ((ConfigSetNode) node).getBean(beanName);
		} else if (node instanceof BeanNode) {
			bean = getBean(node.getParent(), beanName);
		}
		return bean;
	}
}
