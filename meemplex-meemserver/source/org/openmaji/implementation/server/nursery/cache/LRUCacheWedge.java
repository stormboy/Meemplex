/*
 * @(#)LRUCacheWedge.java
 *
 * Copyright 2003 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

/* ToDo:
 * - None, yet.
 */
package org.openmaji.implementation.server.nursery.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


import org.openmaji.implementation.server.Common;
import org.openmaji.meem.Wedge;
import org.openmaji.meem.filter.Filter;
import org.openmaji.system.meem.wedge.reference.ContentProvider;
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
public class LRUCacheWedge implements Wedge, LRUCache, Cache {

	private Logger logger = LogFactory.getLogger();	
		
	LRU cache = new LRU(100);
	
    public CacheClient cacheClient;
    public final ContentProvider cacheClientProvider = new ContentProvider() {
        public void sendContent(Object target, Filter filter) {
            for (Iterator i = cache.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();

                ((CacheClient) target).hit(entry.getKey(), entry.getValue());
            }
        }
    };
	
	public synchronized void updateSize(int size) {
		cache = new LRU(size);
	}

	public synchronized void check(Object key) {
		if (cache.containsKey(key)) {
			cacheClient.hit(key, cache.get(key));
		} else {
			cacheClient.miss(key);
		}
	}

	public synchronized void put(Object key, Object value) {
		cache.put(key, value);
		LogTools.trace(logger, Common.getLogLevelVerbose(), "Cache.put() : key=" + key + ", value=" + value);
	}

	public synchronized void remove(Object key) {
		if (cache.containsKey(key)) {
			Object value = cache.remove(key);
			cacheClient.removed(key, value);
			LogTools.trace(logger, Common.getLogLevelVerbose(), "Cache.remove() : key=" + key + ", value=" + value);
		} else {
			cacheClient.removed(key, null);
		}
	}
	
	private synchronized void removeEntry(Object key) {
		remove(key);
	}

	
	private class LRU extends LinkedHashMap {
		private static final long serialVersionUID = 2634575427909499639L;
		
		int maxEntries = 10;

		public LRU(int maxEntries) {
			super(maxEntries, 0.75f, true);
			this.maxEntries = maxEntries;
		}

		protected boolean removeEldestEntry(Map.Entry eldest) {
			if (size() > maxEntries) {
				removeEntry((String)eldest.getKey());
			}
			return false;
		}
	}

}
