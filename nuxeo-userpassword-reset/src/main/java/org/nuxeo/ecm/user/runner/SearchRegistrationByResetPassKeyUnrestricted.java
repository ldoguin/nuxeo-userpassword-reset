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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

/**
 * Search a registration with the given resetPasswordKey.
 *
 * @author ldoguin
 *
 */
public class SearchRegistrationByResetPassKeyUnrestricted extends UnrestrictedSessionRunner {

    public static final Log log = LogFactory.getLog(SearchRegistrationByResetPassKeyUnrestricted.class);

    private String passwordKey;

    private String errorMessage;

    private DocumentModel registration;

    public SearchRegistrationByResetPassKeyUnrestricted(CoreSession session,
            String passwordKey) {
        super(session);
        this.passwordKey = passwordKey;
    }

    public SearchRegistrationByResetPassKeyUnrestricted(String repositoryName,
            String passwordKey) {
        super(repositoryName);
        this.passwordKey = passwordKey;
    }

    @Override
    public void run() throws ClientException {
		DirectoryService ds;
		try {
			ds = Framework.getService(DirectoryService.class);
		} catch (Exception e) {
			throw new RuntimeException("Could not find DirectoryService", e);
		}
		Session session = ds.open("resetPasswordKeys");
		DocumentModel entry = session.getEntry(passwordKey);
		session.close();
        if (entry == null) {
            // No key found
            errorMessage = "label.resetPassForm.registrationnotfound";
            return;
        }
        registration = entry;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DocumentModel getRegistration() {
        return registration;
    }

}
