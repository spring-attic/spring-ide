package org.springframework.ide.eclipse.boot.properties.editor;

import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * TODO: we should make both Spring properties and Yaml editor support implemtn
 * this interface, then test harness code could more cleanly be factored instead
 * of duplicating it.
 *
 * @author Kris De Volder
 */
public interface ICompletionEngine {

	Collection<ICompletionProposal> getCompletions(IDocument document, int offset) throws Exception;

}
