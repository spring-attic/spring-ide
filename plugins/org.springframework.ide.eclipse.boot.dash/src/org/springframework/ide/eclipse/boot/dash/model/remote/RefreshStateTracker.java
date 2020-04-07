package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.OnDispose;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class RefreshStateTracker {

	private final Map<String, RefreshState> map = new HashMap<>();

	private final Set<String> inProgress = new HashSet<>();

	public RefreshStateTracker(OnDispose owner) {
		owner.onDispose(d -> refreshState.dispose());
	}

	public final LiveExpression<RefreshState> refreshState = new LiveExpression<RefreshState>(RefreshState.READY) {
		protected RefreshState compute() {
			RefreshState merged = RefreshState.READY;
			synchronized (RefreshStateTracker.this) {
				for (RefreshState s : map.values()) {
					merged = RefreshState.merge(merged, s);
				}
			}
			return merged;
		}
	};

	public <T> T call(String busyMessage, Callable<T> callable) throws Exception {
		start(busyMessage);
		try {
			T success = callable.call();
			success(busyMessage);
			return success;
		} catch (Throwable e) {
			if (ExceptionUtil.isWarning(e)) {
				warn(busyMessage, ExceptionUtil.getMessage(e));
			} else {
				error(busyMessage, ExceptionUtil.getMessage(e));
			}
			throw ExceptionUtil.exception(e);
		}
	}

	private void warn(String busyMessage, String warningMessage) {
		Assert.isLegal(inProgress.remove(busyMessage));
		map.put(busyMessage, RefreshState.warning(warningMessage));
		refreshState.refresh();
	}

	private synchronized void error(String busyMessage, String errorMessage) {
		Assert.isLegal(inProgress.remove(busyMessage));
		map.put(busyMessage, RefreshState.error(errorMessage));
		refreshState.refresh();
	}

	private synchronized void success(String busyMessage) {
		Assert.isLegal(inProgress.remove(busyMessage));
		map.remove(busyMessage);
		refreshState.refresh();
	}

	private synchronized void start(String busyMessage) {
		Assert.isLegal(inProgress.add(busyMessage));
		map.put(busyMessage, RefreshState.loading(busyMessage));
		refreshState.refresh();
	}

	public <T> CompletableFuture<T> callAsync(String busyMessage, Callable<T> callable) {
		CompletableFuture<T> result = new CompletableFuture<>();
		JobUtil.runInJob(busyMessage, (mon) -> {
			try {
				result.complete(this.call(busyMessage, callable));
			} catch (Throwable e) {
				result.completeExceptionally(e);
			}
		});
		return result;
	}

}
