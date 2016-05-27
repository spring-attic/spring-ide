import java.util.Iterator;

import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.template.java.SWTContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.boot.templates.BootContextType;

public class BootTemplatesActivator implements BundleActivator {


//	/**
//	 * Registers the given Java template context.
//	 *
//	 * @param registry the template context type registry
//	 * @param id the context type id
//	 * @param parent the parent context type
//	 * @since 3.4
//	 */
//	private static void registerJavaContext(ContributionContextTypeRegistry registry, String id, TemplateContextType parent) {
//		TemplateContextType contextType= registry.getContextType(id);
//		@SuppressWarnings("unchecked")
//		Iterator<TemplateVariableResolver> iter= parent.resolvers();
//		while (iter.hasNext()) {
//			contextType.addResolver(iter.next());
//		}
//	}

	@SuppressWarnings("restriction")
	@Override
	public void start(BundleContext context) throws Exception {
//		ContributionContextTypeRegistry registry = (ContributionContextTypeRegistry) JavaPlugin.getDefault().getTemplateContextRegistry();
//		TemplateContextType all_contextType= registry.getContextType(JavaContextType.ID_ALL);
//		registerJavaContext(registry, BootContextType.ID_ALL, all_contextType);
//		all_contextType= registry.getContextType(BootContextType.ID_ALL);
//		registerJavaContext(registry, BootContextType.ID_MEMBERS, all_contextType);
//		registerJavaContext(registry, BootContextType.ID_STATEMENTS, all_contextType);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
