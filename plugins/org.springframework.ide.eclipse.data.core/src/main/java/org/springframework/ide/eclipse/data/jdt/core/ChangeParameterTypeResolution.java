package org.springframework.ide.eclipse.data.jdt.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IMarkerResolution;
import org.springframework.ide.eclipse.data.internal.DataCorePlugin;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Terry Denney
 * @since 3.2.0
 *
 */
@SuppressWarnings("restriction")
public class ChangeParameterTypeResolution extends LinkedCorrectionProposal implements IMarkerResolution {

	private SingleVariableDeclaration param;
	
	private String paramType;
	
	private String paramTypePackage;

	public ChangeParameterTypeResolution(SingleVariableDeclaration param, String paramType, String paramTypePackage, ICompilationUnit cu) {
		super("Change Parameter Type", cu, null, 0, null);
		this.param = param;
		this.paramType = paramType;
		this.paramTypePackage = paramTypePackage;
	}

	public String getLabel() {
		return "Change parameter type to " + paramTypePackage + "." + paramType;
	}
	
	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		if (getCompilationUnit().getImport(paramTypePackage + paramType) == null) {
			ImportRewrite importRewrite = createImportRewrite(ASTResolving.findParentCompilationUnit(param));
			importRewrite.addImport(paramTypePackage + paramType);
		}
		
		AST ast = param.getAST();
		ASTRewrite astRewrite = ASTRewrite.create(ast);
		
		SimpleName newTypeName = ast.newSimpleName(paramType);
		SimpleType newType = ast.newSimpleType(newTypeName);
		astRewrite.replace(param.getType(), newType, null);
		
		return astRewrite;			
	}

	public void run(IMarker marker) {
		IDocument document = QuickfixUtils.getDocument(marker);
		if (document != null) {
			apply(document);
			try {
				marker.delete();
			} catch (CoreException e) {
				StatusHandler.log(new Status(Status.ERROR, DataCorePlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}

}
