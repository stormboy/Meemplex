/*
 * @(#)MeemTypeChecker.java
 *
 * Copyright 2003 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

/* ToDo:
 * - should we instantate the wedge impl class and to a isAssignableFrom() test?
 * - Add caching?
 * 
 */
package org.openmaji.implementation.server.space.hyperspace.utility;

import java.util.Iterator;


import org.openmaji.meem.Meem;
import org.openmaji.meem.MeemPath;
import org.openmaji.meem.definition.*;
import org.openmaji.meem.filter.ExactMatchFilter;
import org.openmaji.meem.wedge.reference.Reference;
import org.openmaji.server.helper.EssentialMeemHelper;
import org.openmaji.server.utility.PigeonHole;
import org.openmaji.server.utility.TimeoutException;
import org.openmaji.system.meem.wedge.reference.ContentClient;
import org.openmaji.system.space.meemstore.MeemDefinitionClient;
import org.openmaji.system.space.meemstore.MeemStore;
import org.swzoo.log2.core.LogFactory;
import org.swzoo.log2.core.LogTools;
import org.swzoo.log2.core.Logger;

/**
 * <p>
 * ...
 * </p>
 * @author  mg
 * @version 1.0
 */
public class MeemTypeChecker {

	private static Meem meemStoreMeem = null;

	public static void setMeemStoreMeem(Meem meem) {
		meemStoreMeem = meem;
	}

	private static synchronized void getMeemStore() {
		if (meemStoreMeem == null) {
			meemStoreMeem = EssentialMeemHelper.getEssentialMeem(MeemStore.spi.getIdentifier());
		}
	}

	public static boolean checkMeem(String type, MeemPath meemPath) {
		getMeemStore();

		PigeonHole pigeonHole = new PigeonHole();
		MeemDefinitionClientImpl definitionClient = new MeemDefinitionClientImpl(pigeonHole);

		Reference meemDefinitionClientReference =
			Reference.spi.create("meemDefinitionClient", definitionClient, true, new ExactMatchFilter(meemPath));

		meemStoreMeem.addOutboundReference(meemDefinitionClientReference, true);

		try
		{
			MeemDefinition meemDefinition = (MeemDefinition) pigeonHole.get(timeout);

			if (meemDefinition != null)
			{
				return checkDefinition(type, meemDefinition);
			}
		}
		catch (TimeoutException ex) {
			LogTools.info(logger, "Timeout waiting for MeemDefinition", ex);
		}

		return false;
	}

	public static boolean checkDefinition(String type, MeemDefinition definition) {
		for (Iterator wedgeDefinitions = definition.getWedgeDefinitions().iterator(); wedgeDefinitions.hasNext();) {
			WedgeDefinition wedgeDefinition = (WedgeDefinition) wedgeDefinitions.next();

			for (Iterator facetDefinitions = wedgeDefinition.getFacetDefinitions().iterator(); facetDefinitions.hasNext();) {
				FacetDefinition facetDefinition = (FacetDefinition) facetDefinitions.next();

				if (facetDefinition.getFacetAttribute().isDirection(Direction.INBOUND)) {
					String interfaceName = facetDefinition.getFacetAttribute().getInterfaceName();

					if (interfaceName.equalsIgnoreCase(type)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static class MeemDefinitionClientImpl implements MeemDefinitionClient, ContentClient
	{
		public MeemDefinitionClientImpl(PigeonHole pigeonHole)
		{
			this.pigeonHole = pigeonHole;
		}

		public void meemDefinitionChanged(MeemPath meemPath, MeemDefinition meemDefinition)
		{
			this.meemDefinition = meemDefinition;
		}

		public void contentSent()
		{
			if (pigeonHole != null)
			{
				pigeonHole.put(meemDefinition);
				pigeonHole = null;
			}
		}

		public void contentFailed(String reason)
		{
			if (pigeonHole != null)
			{
				pigeonHole.put(null);
				pigeonHole = null;
			}
		}

		private PigeonHole pigeonHole;
		private MeemDefinition meemDefinition = null;
	}

	private static final long timeout = 60000;

	/** Logger for the class */  
	private static final Logger logger = LogFactory.getLogger();
}
