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
package org.nuxeo.ecm.user;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.mail.Composer;
import org.nuxeo.ecm.automation.core.mail.Mailer;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.user.runner.CreatePasswordResetLinkUnrestricted;
import org.nuxeo.ecm.user.runner.SearchRegistrationByResetPassKeyUnrestricted;
import org.nuxeo.ecm.user.runner.SetNewPasswordUnrestricted;
import org.nuxeo.ecm.webengine.forms.FormData;
import org.nuxeo.ecm.webengine.model.Template;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * This module provides access to resetPassword urls.
 *
 * @author ldoguin
 */
@Path("/resetPassword")
@Produces("text/html;charset=UTF-8")
@WebObject(type="RestPassword")
public class RestPassword extends ModuleRoot {

    private static final String MAIL_FROM = Framework.getProperty("mail.from");

	public static final Log log = LogFactory.getLog(RestPassword.class);

    public static String defaultRepositoryName;

    @GET
    public Object doGet() {
        Map<String, String> data = new HashMap<String, String>();
        return getView("newPasswordRequest").arg("data", data);
    }

    @POST
    @Path("sendPasswordMail")
    @Produces("text/html")
    public Object sendPasswordMail() throws ClientException {
        FormData formData = getContext().getForm();
        String email = formData.getString("EmailAddress");
        if (email == null || "".equals(email.trim())) {
            return redisplayFormWithErrorMessage("newPasswordRequest",
                    ctx.getMessage("label.registerForm.validation.email"),
                    formData);
        }
        email = email.trim();
        CreatePasswordResetLinkUnrestricted runner = new CreatePasswordResetLinkUnrestricted(
                getDefaultRepositoryName(), email);
        runner.runUnrestricted();

        String errorMessage = runner.getErrorMessage();
        if (errorMessage != null) {
            return redisplayFormWithErrorMessage("newPasswordRequest",
                    ctx.getMessage(errorMessage), formData);
        } else {
            String passwordResetLink = runner.getPasswordResetLink();
            String subject;
            Template template;
            if (ctx.getLocale().equals(Locale.FRENCH)) {
                template = getView("mail/passwordForgotten_fr");
                subject = "Nuxeo - Votre nouveau mot de passe";
            } else {
                template = getView("mail/passwordForgotten");
                subject = "Nuxeo - Your new password";
            }
            String message = template.arg("passwordResetLink",
                    passwordResetLink).render();
            try {
                sendEmail(email, subject, message);
            } catch (MessagingException e) {
                // issue while sending the mail
                log.error("Sending Registration E-Mail Error", e);
                return Response.status(500).build();
            }
            return redisplayFormWithInfoMessage("newPasswordRequest",
                    ctx.getMessage("label.sendPasswordMail.emailSent"),
                    formData);
        }
    }

    @GET
    @Path("enterNewPassword/{key}")
    @Produces("text/html")
    public Object enterNewPassword(@PathParam("key") String key)
            throws ClientException {
        SearchRegistrationByResetPassKeyUnrestricted runner = new SearchRegistrationByResetPassKeyUnrestricted(
                getDefaultRepositoryName(), key);
        runner.runUnrestricted();

        String errorMessage = runner.getErrorMessage();
        if (errorMessage != null) {
            return getView("wrongResetKey");
        } else {
            Map<String, String> data = new HashMap<String, String>();
            return getView("submitNewPassword").arg("key", key).arg("data",
                    data);
        }
    }

    @POST
    @Path("submitNewPassword")
    @Produces("text/html")
    public Object submitNewPassword() throws ClientException,
            URISyntaxException {
        FormData formData = getContext().getForm();
        String passwordKey = formData.getString("PasswordKey");
        String password = formData.getString("Password");
        String passwordConfirmation = formData.getString("PasswordConfirmation");
        if (password == null || "".equals(password.trim())) {
            return redisplayFormWithErrorMessage("submitNewPassword",
                    ctx.getMessage("label.registerForm.validation.password"),
                    formData);
        }
        if (passwordConfirmation == null
                || "".equals(passwordConfirmation.trim())) {
            return redisplayFormWithErrorMessage(
                    "submitNewPassword",
                    ctx.getMessage("label.registerForm.validation.passwordconfirmation"),
                    formData);
        }
        password = password.trim();
        passwordConfirmation = passwordConfirmation.trim();
        if (!password.equals(passwordConfirmation)) {
            return redisplayFormWithErrorMessage(
                    "submitNewPassword",
                    ctx.getMessage("label.registerForm.validation.passwordvalidation"),
                    formData);
        }

        SetNewPasswordUnrestricted runner = new SetNewPasswordUnrestricted(
                getDefaultRepositoryName(), password, passwordKey);
        runner.runUnrestricted();
        Response response = runner.getResponse();
        if (response != null) {
            return response;
        }
        String errorMessage = runner.getErrorMessage();
        if (errorMessage != null) {
            return redisplayFormWithErrorMessage("submitNewPassword",
                    ctx.getMessage(errorMessage), formData).arg("key",
                    passwordKey);
        } else {
            return redisplayFormWithInfoMessage("submitNewPassword",
                    ctx.getMessage("label.submitNewPassword.saved"), formData).arg(
                    "key", passwordKey);
        }
    }

    protected Template redisplayFormWithMessage(String messageType,
            String formName, String message, FormData data) {
        Map<String, String> savedData = new HashMap<String, String>();
        for (String key : data.getKeys()) {
            savedData.put(key, data.getString(key));
        }
        return getView(formName).arg("data", savedData).arg(messageType,
                message);
    }

    protected Template redisplayFormWithInfoMessage(String formName,
            String message, FormData data) {
        return redisplayFormWithMessage("info", formName, message, data);
    }

    protected Template redisplayFormWithErrorMessage(String formName,
            String message, FormData data) {
        return redisplayFormWithMessage("err", formName, message, data);
    }

    public void sendEmail(String email, String subject, String message)
            throws MessagingException {
        Composer cp = new Composer();
        Mailer mailer = cp.getMailer();
        Mailer.Message msg = mailer.newMessage();
        msg.setFrom(MAIL_FROM);
        msg.setSubject(subject);
        msg.setRecipient(RecipientType.TO, new InternetAddress(email));
        msg.setContent(message, "text/html");
        msg.send();
    }
    
    private String getDefaultRepositoryName() {
        if (defaultRepositoryName == null) {
            try {
                defaultRepositoryName = Framework.getService(
                        RepositoryManager.class).getDefaultRepository().getName();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return defaultRepositoryName;
    }
}
