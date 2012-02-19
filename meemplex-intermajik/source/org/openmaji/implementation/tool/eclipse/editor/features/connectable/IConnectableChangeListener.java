/*
 * @(#)IConnectableChangeListener.java
 * Created on 21/10/2003
 * Copyright 2003 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

package org.openmaji.implementation.tool.eclipse.editor.features.connectable;

/**
 * <code>IConnectableChangeListener</code>.
 * <p>
 * @author Kin Wong
 */
public interface IConnectableChangeListener {
	void sourceChanged();
	void targetChanged();
}
