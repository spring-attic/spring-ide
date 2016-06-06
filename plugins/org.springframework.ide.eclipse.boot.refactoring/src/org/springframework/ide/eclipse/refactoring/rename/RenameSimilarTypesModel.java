/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.refactoring.rename;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.frameworks.core.async.FluxJdtSearch;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableSet;

import reactor.core.publisher.Mono;

/**
 * This is a 'model' for {@link RenameSimilarTypesWizard}
 * 
 * @author Kris De Volder
 */
public class RenameSimilarTypesModel {
	
	public final LiveVariable<IType> target = new LiveVariable<>();
	public final LiveVariable<String> newName = new LiveVariable<>("");

	private LiveExpression<String> targeTypeName = new LiveExpression<String>("") {
		{
			dependsOn(target);
		}
	
		@Override
		protected String compute() {
			IType type = target.getValue();
			if (type!=null) {
				return type.getFullyQualifiedName();
			}
			return "";
		}
	};

	public final ObservableSet<IType> similarTypesFound = ObservableSet.<IType>builder()
			.refresh(AsyncMode.ASYNC)
			.compute(this::findSimilarTypes)
			.build();
	
	public final LiveSetVariable<IType> similarTypesSelected = new LiveSetVariable<>();
	
	private RenameSimilarTypesRefactoring refactoring = new RenameSimilarTypesRefactoring();
	
	private LiveExpression<RefactoringStatus> refactoringStatus = new LiveExpression<RefactoringStatus>(null) {
		
		{
			dependsOn(target);
			dependsOn(newName);
		}
		
		@Override
		protected RefactoringStatus compute() {
			try {
				refactoring.setTarget(target.getValue());
				refactoring.setNewName(newName.getValue());
				return refactoring.checkInitialConditions(new NullProgressMonitor());
			} catch (Exception e) {
				Log.log(e);
				return RefactoringStatuses.fatal(e);
			}
		}
	};
	
	/**
	 * @return A livexp computing the value of refactoring.checkInitialConditions.
	 */
	public LiveExpression<RefactoringStatus> getRefactoringStatus() {
		return refactoringStatus;
	}
	
	ImmutableSet<IType> findSimilarTypes() throws Exception {
		IType t = target.getValue();
		if (t!=null) {
			return new FluxJdtSearch()
			.scope(t.getJavaProject())
			.search()
			.flatMap((match) -> {
				return Mono.just(t);
			})
			.toList()
			.map(ImmutableSet::copyOf)
			.get();
		}
		return ImmutableSet.of();
	}

	public Refactoring getRefactoring() {
		return refactoring;
	}

	public void setTarget(IType t) {
		this.target.setValue(t);
	}

	public String getDialogTitle() {
		return refactoring.getName();
	}

	public LiveExpression<String> getTargetTypeName() {
		return targeTypeName;
	}

	public void setNewName(String newName) {
		this.newName.setValue(newName);
	}


}
