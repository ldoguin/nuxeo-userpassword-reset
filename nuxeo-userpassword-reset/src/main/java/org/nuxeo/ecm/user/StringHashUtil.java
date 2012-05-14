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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringHashUtil {

	public static final String SECRET = getSecret();

	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	protected static final Log log = LogFactory.getLog(StringHashUtil.class);

	private static String getSecret() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}

	private static String toHexString(byte[] data) {
		StringBuilder buf = new StringBuilder(2 * data.length);
		for (byte b : data) {
			buf.append(HEX_DIGITS[(0xF0 & b) >> 4]);
			buf.append(HEX_DIGITS[0x0F & b]);
		}
		return buf.toString();
	}

	public static String getDigest(String S) {
		try {
			String digestInput = SECRET + S;
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(digestInput.getBytes());
			return toHexString(digest);
		} catch (Throwable t) {
			log.error("Error while computing digest", t);
			return null;
		}
	}

}
