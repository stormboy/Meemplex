/*
 * @(#)MeemTypeInclusionFilter.java
 * Created on 27/02/2004
 * Copyright 2003 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

package org.openmaji.implementation.tool.eclipse.filters;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.openmaji.implementation.tool.eclipse.client.MeemClientProxy;


/**
 * <code>MeemTypeInclusionFilter</code>.
 * <p>
 * @author Kin Wong
 */
public class MeemTypeInclusionFilter extends MeemTypeFilter {
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(getClasses() == null) return false;
		if(!(element instanceof IAdaptable)) return false;
		IAdaptable adaptable = (IAdaptable)element;
		Set inclusions = getClasses();
		MeemClientProxy proxy = 
			(MeemClientProxy)adaptable.getAdapter(MeemClientProxy.class);

		if(proxy == null) return false;
		Iterator it = inclusions.iterator();
		while(it.hasNext()) {
			Class clazz = (Class)it.next();
			if(proxy.isA(clazz)) return true;
		}
		return false;
	}
}
