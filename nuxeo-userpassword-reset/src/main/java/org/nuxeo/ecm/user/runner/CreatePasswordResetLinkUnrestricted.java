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
package org.nuxeo.ecm.user.runner;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.user.StringHashUtil;
import org.nuxeo.runtime.api.Framework;

/**
 * Creates a key for the resetPassword demand and store it.
 * 
 * @author ldoguin
 * 
 */
public class CreatePasswordResetLinkUnrestricted extends UnrestrictedSessionRunner {

	public static final Log log = LogFactory
			.getLog(CreatePasswordResetLinkUnrestricted.class);

	private String email;

	private String errorMessage;

	private String passwordResetLink;

	public CreatePasswordResetLinkUnrestricted(String defaultRepositoryName,
			String email) {
		super(defaultRepositoryName);
		this.email = email;
	}

	public String getPasswordResetLink() {
		return passwordResetLink;
	}

	@Override
	public void run() throws ClientException {
		DocumentModel userModel = searchCorrectUserByEmail(email);
		if (userModel == null) {
            // user associated with this email does not exist.
            errorMessage = "label.askResetPassForm.registrationnotfound";
            return;
		}
		String key = StringHashUtil.getDigest(email
				+ Calendar.getInstance().getTimeInMillis());
		passwordResetLink = Framework.getProperty("nuxeo.url")
				+ "/site/resetPassword/enterNewPassword/" + key;
		DirectoryService ds;
		try {
			ds = Framework.getService(DirectoryService.class);
		} catch (Exception e) {
			throw new RuntimeException("Could not find DirectoryService", e);
		}
		Session session = ds.open("resetPasswordKeys");
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		fieldMap.put("passwordResetKey", key);
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String simpleDate = sdf.format(new Date());
		fieldMap.put("creationDate", simpleDate);
		fieldMap.put("email", email);
		session.createEntry(fieldMap);
		session.close();
	}

	public String getErrorMessage() {
		return errorMessage;
	}

    public DocumentModel searchCorrectUserByEmail(String email) {
    	try{
        UserManager um = Framework.getService(UserManager.class);

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("email", email);

        DocumentModelList users = um.searchUsers(params, null);
        if (users.size() > 0) {
            // normally we should have only one registered email
            return users.get(0);
        }
    	} catch (Exception e) {
    		throw new RuntimeException("could not get userManager", e);
    	}
        return null;
    }
}
