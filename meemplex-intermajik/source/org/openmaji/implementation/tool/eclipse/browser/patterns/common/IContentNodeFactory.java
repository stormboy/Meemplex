/*
 * @(#)IContentNodeFactory.java
 * Created on 18/05/2004
 * Copyright 2003 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

package org.openmaji.implementation.tool.eclipse.browser.patterns.common;

import org.openmaji.implementation.tool.eclipse.hierarchy.nodes.Node;

/**
 * <code>IContentNodeFactory</code>.
 * <p>
 * @author Kin Wong
 */
public interface IContentNodeFactory {
	Node createContentNode(Node node);
}
