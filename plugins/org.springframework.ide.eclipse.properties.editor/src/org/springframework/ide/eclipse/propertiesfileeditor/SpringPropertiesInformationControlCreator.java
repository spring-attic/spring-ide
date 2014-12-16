package org.springframework.ide.eclipse.propertiesfileeditor;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorSite;

/**
 * IInformationControlCreator for 'hover information' associated with Spring properties. This control is
 * used in two different contexts. 
 *  
 *    - tooltip info shown when hovering over a property 
 *    - side view for content assist that proposes property completions.  
 */
public class SpringPropertiesInformationControlCreator implements IInformationControlCreator {

		private IEditorSite editorSite;

		public SpringPropertiesInformationControlCreator(IEditorSite editorSite) {
			this.editorSite = editorSite;
		}

		@Override
		public IInformationControl createInformationControl(Shell parent) {
			if (BrowserInformationControl.isAvailable(parent)) {
				return new BrowserInformationControl(parent, PreferenceConstants.APPEARANCE_JAVADOC_FONT, true);
			}
			return new DefaultInformationControl(parent, true);
		}
	}
