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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Abstract runner for common method to all runners.
 *
 * @author ldoguin
 *
 */
public abstract class AbstractResetPassRunner extends UnrestrictedSessionRunner {

    protected AbstractResetPassRunner(CoreSession session) {
		super(session);
	}

    protected AbstractResetPassRunner(String repositoryName) {
		super(repositoryName);
	}

	public DocumentModel searchCorrectUserByEmail(String email)
            throws Exception {

        UserManager um = Framework.getService(UserManager.class);

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("email", email);

        DocumentModelList users = um.searchUsers(params, null);
        if (users.size() > 0) {
            // normally we should have only one registered email
            return users.get(0);
        }

        return null;
    }
}
