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

import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.frameworks.core.async.FluxJdtSearch;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSets;
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

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");
	private static void debug(String string) {
		System.out.println(string);
	}

	public final LiveVariable<String> oldName = new LiveVariable<>("");
	public final LiveVariable<String> newName = new LiveVariable<>("");

	public final ObservableSet<IType> foundTypes = ObservableSet.<IType>builder()
	.refresh(AsyncMode.ASYNC)
	.compute(this::findSimilarTypes)
	.build();
	{
		foundTypes.dependsOn(oldName);
	}

	public final LiveSetVariable<IType> checkBoxStates = new LiveSetVariable<>();
	public final ObservableSet<IType> selectedTypes = LiveSets.intersection(foundTypes, checkBoxStates);
	{
		if (DEBUG) {
			checkBoxStates.addListener((exp, ts) -> {
				debug("Checked: "+ts.stream().map(IType::getElementName).collect(Collectors.toList()));
			});

			selectedTypes.addListener((exp, ts) -> {
				debug("Selected: "+ts.stream().map(IType::getElementName).collect(Collectors.toList()));
			});
		}

	}

	private RenameSimilarTypesRefactoring refactoring = new RenameSimilarTypesRefactoring();

	private LiveExpression<RefactoringStatus> refactoringStatus = new LiveExpression<RefactoringStatus>(null) {

		{
			dependsOn(oldName);
			dependsOn(newName);
			dependsOn(foundTypes);
			dependsOn(selectedTypes);
		}

		@Override
		protected RefactoringStatus compute() {
			try {
				refactoring.setOldName(oldName.getValue());
				refactoring.setNewName(newName.getValue());
				refactoring.setFoundTypes(foundTypes.getValue());
				refactoring.setSelectedTypes(selectedTypes.getValue());
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
		String name = oldName.getValue();
		if (StringUtils.hasText(name) && (!hasWildCards(name))) {
			return new FluxJdtSearch()
			.scope(FluxJdtSearch.workspaceScope(/*includeBinaries=*/false))
			.pattern(toTypePattern(name+"*"))
			.search()
			.flatMap((match) -> {
				Object e = match.getElement();
				if (e instanceof IType) {
					return Mono.just((IType)e);
				}
				return Mono.empty();
			})
			.toList()
			.map(ImmutableSet::copyOf)
			.get();
		}
		return ImmutableSet.of();
	}

	private boolean hasWildCards(String name) {
		return name.contains("?") || name.contains("*");
	}

	public Refactoring getRefactoring() {
		return refactoring;
	}

	public String getDialogTitle() {
		return refactoring.getName();
	}

	public void setNewName(String newName) {
		this.newName.setValue(newName);
	}

	private static SearchPattern toTypePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.TYPE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public void setOldName(String oldName) {
		this.oldName.setValue(oldName);
	}


}
