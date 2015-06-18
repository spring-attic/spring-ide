package org.springframework.ide.eclipse.boot.dash.livexp;

import java.util.Collections;
import java.util.Set;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * A LiveSet (i.e. LiveExp<Set<T>>) which is defined as mirroring the
 * contents of a delegate.
 * <p>
 * This delegate is initially null and can be set repeatedly.
 * <p>
 * When delegate is null the DelegatingSet will be empty, when set
 * to point to non-null LiveSet then its contents will be equal to the
 * contents of the delegate.
 *
 * @author Kris De Volder
 */
public class DelegatingLiveSet<T> extends LiveExpression<Set<T>> {

	@SuppressWarnings("unchecked")
	public DelegatingLiveSet() {
		super(Collections.EMPTY_SET);
	}

	private LiveExpression<Set<T>> delegate = null;
	private ValueListener<Set<T>> delegateListener = new ValueListener<Set<T>>() {
		public void gotValue(LiveExpression<Set<T>> exp, Set<T> value) {
			refresh();
		}
	};

	@Override
	protected Set<T> compute() {
		if (delegate==null) {
			return Collections.emptySet();
		} else {
			return delegate.getValue();
		}
	}

	public synchronized void setDelegate(LiveExpression<Set<T>> newDelegate) {
		LiveExpression<Set<T>> oldDelegate = this.delegate;
		this.delegate = newDelegate;
		if (oldDelegate==newDelegate) {
			return;
		} else if (oldDelegate!=null) {
			oldDelegate.removeListener(delegateListener);
			if (newDelegate==null) {
				//trigger a refresh because the delegate changed and the newDelegate won't trigger one
				refresh();
			} else {
				newDelegate.addListener(delegateListener);
			}
		}
	}

}
