package org.springframework.ide.eclipse.quickfix.hypelrinks;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

public class AutowireHyperlinkDetector extends JavaElementHyperlinkDetector {

	@Override
	protected void addHyperlinks(List<IHyperlink> hyperlinksCollector, final IRegion wordRegion,
			SelectionDispatchAction openAction, IJavaElement element, boolean qualify, JavaEditor editor) {

		if (element instanceof ILocalVariable) {
			ILocalVariable localVariable = (ILocalVariable) element;
			IJavaElement parent = localVariable.getParent();
			if (parent instanceof IMethod) {
				IMethod parentMethod = (IMethod) parent;
				if (parentMethod.getAnnotation("Autowired") != null) {
					String typeSignature = localVariable.getTypeSignature();
					addHyperlinksHelper(typeSignature, localVariable, hyperlinksCollector);
				}
			}
		}
		else if (element instanceof IField) {
			IField field = (IField) element;
			if (field.getAnnotation("Autowired") != null) {
				try {
					String typeSignature = field.getTypeSignature();
					addHyperlinksHelper(typeSignature, field, hyperlinksCollector);
				}
				catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (element instanceof IType) {
			IType type = (IType) element;
			if (!type.getElementName().equals("Autowired")) {
				return;
			}
		}

		IType type = getParentType(element);
		if (type != null) {
			String typeName = type.getFullyQualifiedName();
			addHyperlinksHelper(typeName, element.getJavaProject().getProject(), hyperlinksCollector);
		}

	}

	private void addHyperlinksHelper(String typeSignature, IJavaElement element, List<IHyperlink> hyperlinksCollector) {
		String typeName = Signature.toString(typeSignature);
		try {
			IType type = getParentType(element);
			if (type != null) {
				String[][] qualifiedTypeNames = type.resolveType(typeName);
				for (String[] typeNameSegments : qualifiedTypeNames) {
					StringBuilder qualifiedTypeName = new StringBuilder();
					for (String typeNameSegment : typeNameSegments) {
						if (qualifiedTypeName.length() > 0) {
							qualifiedTypeName.append(".");
						}
						qualifiedTypeName.append(typeNameSegment);
					}

					addHyperlinksHelper(qualifiedTypeName.toString(), element.getJavaProject().getProject(),
							hyperlinksCollector);
				}
			}
		}
		catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addHyperlinksHelper(final String typeName, final IProject project,
			final List<IHyperlink> hyperlinksCollector) {
		IBeansModel model = BeansCorePlugin.getModel();
		IBeansProject springProject = model.getProject(project);
		Set<IBeansConfig> configs = springProject.getConfigs();

		// IBeansConfig config = BeansConfigFactory.create(springProject,
		// BeansConfigFactory.JAVA_CONFIG_TYPE + typeName,
		// IBeansConfig.Type.AUTO_DETECTED);

		for (IBeansConfig config : configs) {
			final AutowireDependencyProvider autowireDependencyProvider = new AutowireDependencyProvider(config, config);
			final String[][] beanNamesWrapper = new String[1][];

			try {
				IProjectClassLoaderSupport classLoaderSupport = JdtUtils.getProjectClassLoaderSupport(
						project.getProject(), BeansCorePlugin.getClassLoader());
				autowireDependencyProvider.setProjectClassLoaderSupport(classLoaderSupport);
				classLoaderSupport.executeCallback(new IProjectClassLoaderAwareCallback() {
					public void doWithActiveProjectClassLoader() throws Throwable {
						beanNamesWrapper[0] = autowireDependencyProvider.getBeansForType(typeName);
					}
				});
			}
			catch (Throwable e) {
				BeansCorePlugin.log(e);
			}

			String[] beanNames = beanNamesWrapper[0];
			for (final String beanName : beanNames) {
				IBean bean = autowireDependencyProvider.getBean(beanName);
				final IResource resource = bean.getElementResource();
				final int line = bean.getElementStartLine();
				if (resource instanceof IFile) {
					hyperlinksCollector.add(new IHyperlink() {

						public void open() {
							SpringUIUtils.openInEditor((IFile) resource, line);
						}

						public String getTypeLabel() {
							// TODO Auto-generated method stub
							return null;
						}

						public String getHyperlinkText() {
							return beanName;
						}

						public IRegion getHyperlinkRegion() {
							return new IRegion() {

								public int getOffset() {
									// TODO Auto-generated method stub
									return 0;
								}

								public int getLength() {
									// TODO Auto-generated method stub
									return 0;
								}
							};
						}
					});
				}
			}
		}
		// autowireDependencyProvider.getBeansForType()
	}

	private IType getParentType(IJavaElement element) {
		if (element == null) {
			return null;
		}

		if (element instanceof IType) {
			return (IType) element;
		}

		return getParentType(element.getParent());
	}

}
