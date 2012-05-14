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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Set the new password for the user.
 *
 * @author ldoguin
 *
 */
public class SetNewPasswordUnrestricted extends UnrestrictedSessionRunner {

    public static final Log log = LogFactory.getLog(SetNewPasswordUnrestricted.class);

    private Response response;

    private String passwordKey;

    private String password;

    private String errorMessage;

    public SetNewPasswordUnrestricted(String defaultRepositoryName,
            String password, String passwordKey) {
        super(defaultRepositoryName);
        this.password = password;
        this.passwordKey = passwordKey;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public void run() throws ClientException {

        try {
            SearchRegistrationByResetPassKeyUnrestricted runner = new SearchRegistrationByResetPassKeyUnrestricted(
                    session, passwordKey);
            runner.run();
            DocumentModel registration = runner.getRegistration();
            if (registration == null) {
                // No key found
                errorMessage = "label.resetPassForm.registrationnotfound";
                return;
            }
            String email = registration.getProperty("resetPasswordKeys:email").getValue(
                    String.class);

            UserManager userManager = null;
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                log.error("Could not find UserManager service.", e);
                response = Response.status(500).build();
                return;
            }
            DocumentModel userModel = searchCorrectUserByEmail(email);
            userModel.setPropertyValue("user:password", password);
            userManager.updateUser(userModel);

    		DirectoryService ds;
    		try {
    			ds = Framework.getService(DirectoryService.class);
    		} catch (Exception e) {
    			throw new RuntimeException("Could not find DirectoryService", e);
    		}
    		Session session = ds.open("resetPasswordKeys");
    		session.deleteEntry(passwordKey);
    		session.close();
        } catch (Exception e) {
            log.error("Error while reseting user password", e);
            response = Response.status(500).build();
        }
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

    public String getErrorMessage() {
        return errorMessage;
    }

}
