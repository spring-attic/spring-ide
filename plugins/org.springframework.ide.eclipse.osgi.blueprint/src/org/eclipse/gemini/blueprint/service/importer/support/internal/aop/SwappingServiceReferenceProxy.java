/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer.support.internal.aop;

import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.internal.util.ServiceComparatorUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;

/**
 * Synchronized, swapping {@link ServiceReference} implementation that delegates to an underlying implementation which
 * can be swapped at runtime.
 * 
 * <strong>Note:</strong> this class is thread-safe.
 * 
 * @author Costin Leau
 * 
 */
class SwappingServiceReferenceProxy implements ServiceReferenceProxy {

	private static final int HASH_CODE = SwappingServiceReferenceProxy.class.hashCode() * 13;
	
	private static final Object TIE_MONITOR = new Object();

	private ServiceReference delegate;

	synchronized ServiceReference swapDelegates(ServiceReference newDelegate) {
		Assert.notNull(newDelegate);
		ServiceReference old = this.delegate;
		this.delegate = newDelegate;

		return old;
	}

	public synchronized Bundle getBundle() {
		return (delegate == null ? null : delegate.getBundle());
	}

	public synchronized Object getProperty(String key) {
		return (delegate == null ? null : delegate.getProperty(key));
	}

	public synchronized String[] getPropertyKeys() {
		return (delegate == null ? new String[0] : delegate.getPropertyKeys());
	}

	public synchronized Bundle[] getUsingBundles() {
		return (delegate == null ? new Bundle[0] : delegate.getUsingBundles());
	}

	public synchronized boolean isAssignableTo(Bundle bundle, String className) {
		return (delegate == null ? false : delegate.isAssignableTo(bundle, className));
	}

	public synchronized ServiceReference getTargetServiceReference() {
		return delegate;
	}

	public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        if (!(obj instanceof SwappingServiceReferenceProxy)) {
            return false;
        }
        SwappingServiceReferenceProxy other = (SwappingServiceReferenceProxy) obj;
        
	    int thisHash = System.identityHashCode(this);
        int otherHash = System.identityHashCode(other);
        if (thisHash > otherHash) {
            synchronized (this) {
                synchronized (obj) {
                    return delegateEquals(other);                    
                }
            }
	    } else if (thisHash < otherHash) {
	        synchronized (obj) {
                synchronized (this) {
                    return delegateEquals(other);                    
                }
            }
	    } else {
	        synchronized (TIE_MONITOR) {
	            synchronized (this) {
	                synchronized (obj) {
	                    return delegateEquals(other);                    
	                }
	            }
	        }
	    }
	}

    public boolean delegateEquals(SwappingServiceReferenceProxy other) {
        return (delegate == null ? other.delegate == null : delegate.equals(other.delegate));
    }

	public synchronized int hashCode() {
		return HASH_CODE + (delegate == null ? 0 : delegate.hashCode());
	}

	public synchronized int compareTo(Object other) {
		if (this == other) {
			return 0;
		}
		return ServiceComparatorUtil.compare(delegate, other);
	}
}