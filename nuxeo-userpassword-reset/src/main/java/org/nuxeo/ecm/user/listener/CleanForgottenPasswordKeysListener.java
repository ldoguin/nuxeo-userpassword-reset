/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ldoguin
 * 
 */
package org.nuxeo.ecm.user.listener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.user.runner.CreatePasswordResetLinkUnrestricted;
import org.nuxeo.runtime.api.Framework;

/**
 * Remove all forgottenPassword keys created the day before.
 * 
 * @author ldoguin
 * 
 */
public class CleanForgottenPasswordKeysListener implements EventListener {

	public static final Log log = LogFactory
			.getLog(CleanForgottenPasswordKeysListener.class);

    @Override
    public void handleEvent(Event event) throws ClientException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);
		String simpleDate = sdf.format(yesterday.getTime());
		DirectoryService ds;
		try {
			ds = Framework.getService(DirectoryService.class);
		} catch (Exception e) {
			throw new RuntimeException("Could not find DirectoryService", e);
		}
		Session session = ds.open("resetPasswordKeys");
		Map<String, Serializable> filter = new HashMap<String, Serializable>();
		filter.put("creationDate", simpleDate);
		DocumentModelList keysToRemove = session.query(filter);
		for (DocumentModel key : keysToRemove) {
			session.deleteEntry(key);
		}
		session.close();
    }
}
