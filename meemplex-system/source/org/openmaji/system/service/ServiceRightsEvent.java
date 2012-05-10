/*
 * Copyright 2005 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

package org.openmaji.system.service;

import org.openmaji.meem.MeemPath;

/**
 * <p>The <code>ServiceRightsEvent</code> interface defines the messages that are sent across a
 * number of facets defining the connecctivity status of components to services.</p>
 * 
 * <p>Instances of these events are generated by the service license manager code. Device
 * client code should provide mechansisms to handle these events and take appropriate 
 * action to either prompt end users for information, and enforce the expiry of rights.</p>
 */

public interface ServiceRightsEvent {

	/** Status event type indicating that the license to the given 
	 * content has expired, and that the device client should cease to 
	 * provide service for the given content */
	
	public static final int SERVICE_RIGHTS_EXPIRED = 1;

	/** Status event type indicating that the license to the given 
	 * service has been connected, and may allow access to the given content. */
	
	public static final int SERVICE_CONNECTED = 2;
		
	/** Status event type informing the client component that the service license 
	 * manager has sucessfully relinquished the rights to the given 
	 * service, and that the device client should cease to provide service
	 * for the given content. */  
	
	public static final int SERVICE_DISCONNECTED = 3;
	
	/** Status event type informing the client component that the service has been
	 * upgraded. The new serviceID is provided in the event. The device client
	 * may use this information to modulate access to given content.*/
	
	public static final int SERVICE_UPGRADED = 4;
	
	/** Status event type informing the client component that the service has been
	 * downgraded. The new serviceID is provided in the event.The device client
	 * may use this information to modulate access to given content.*/
	
	public static final int SERVICE_DOWNGRADED = 5;
	
	/** Status event type informing the client component that the service has been 
	 * unbound from this component, and that the device client should cease to 
	 * provide service for the given content */

	public static final int SERVICE_UNBOUND = 6;
	
	/** Status event type informing the client component that an existing service
	 * has been rebound to this component, and that the device client may allow access
	 * to the given content. */
	
	public static final int SERVICE_REBOUND = 7;

	/**
	 * The service has been authorised for use.  The client must "commit" to
	 * the service when the service is being used.
	 */
	
	public static final int SERVICE_AUTHORISED = 8;
	
	/** Status event type informing the client component that the service license 
	 * manager has just renewed the lease for the given service, and that the device
	 * cleint may continue to provide access to the given content. */
	
	public static final int SERVICE_LEASE_RENEWED = 11;
	
	/** Error event type informing the client component that the service license manager has 
	 * a component client(s) already bound to the given service and user, and that connection
	 * to the service was not successful. The device client component should not provide
	 * access to the given content. */ 
	
	public static final int SERVICE_CONNECTION_LIMIT_EXCEEDED = 101;

	/** Error event type informing the client component that an unspecified error occured 
	 * while attempting to connect, unbind or rebind to the service. The device client component should 
	 * not provide access to the given content. */ 
	
	public static final int SERVICE_CONNECTION_ERROR = 102;
	
	/** Error event type informing the client component that the requested service is not
	 * activated for the user, and thus could not be connected. The device client component should not provide
	 * access to the given content. */ 
	
	public static final int SERVICE_NOT_ACTIVATED = 103;
			
	/** Error event type informing the client component that the service license 
	 * manager has failed to authenticate with the remote license 
	 * provider, and thus could not be connected. The device client component should not provide
	 * access to the given content. */
	
	public static final int SERVICE_AUTHENTICATION_FAILURE = 104;
	
	/** Error event informing the client that the service license 
	 * manager has been unable to locate the service and/or license for that service. 
	 * The device client component should not provide access to the given content. */
	
	public static final int SERVICE_NOT_FOUND = 105;

	/** Error event type informing the device client that the service license 
	 * manager has encountered an unexpected error. The device client component should cease
	 * to provide access to the given content. */
	
	public static final int SERVICE_GENERAL_ERROR = 106;
	
	/** Monitoring event which is used by the licnese system to represent a 
	 * heartbeat, and verifies that the licensed component is still alive
	 * and consuming a given license. */
	
	public static final int SERVICE_HEARTBEAT = 201;

	/** 
	 * Returns a unique sequence id of the event (automatically generated)
	 * 
	 * @return The event sequence id
	 */

	public long getEventId();

	/** 
	 * Returns one of the predefined static event types defined in the
	 * <code>RightsEvent</code> interface. 
	 * 
	 * @return An event code relating to a predefined event type 
	 */

	public int getEventType();

	/** 
	 * Returns the type of service this event relates to.
	 * 
	 * @return A string instance identifer. Never null.
	 */
	
	public String getServiceId();

	/** 
	 * Returns the instanceID object was optionally registered along 
	 * with the event listener.
	 * 
	 * @return An object instance identifer which implements the equals
	 * method for comparison, or null.
	 */
	
	public MeemPath getInstanceId();

}