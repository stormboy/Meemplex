/*
 * @(#)RequestSuspendedException.java
 *
 * Copyright 2004 by Majitek Limited.  All Rights Reserved.
 *
 * This software is the proprietary information of Majitek Limited.
 * Use is subject to license terms.
 */
package org.openmaji.system.request;

/**
 * @author mg
 */
public class RequestSuspendedException extends Exception {
	private static final long serialVersionUID = 534540102928363464L;

	public RequestSuspendedException(String message) {
		super(message);
	}
}
