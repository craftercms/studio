/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.impl.v1.util;

import org.springframework.security.crypto.codec.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SessionTokenUtils {

    public static String createToken(String username, int sessionTimeout) {
        long expires = System.currentTimeMillis() + 1000L * 60 * sessionTimeout;

        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(username);
        tokenBuilder.append(":");
        tokenBuilder.append(expires);
        tokenBuilder.append(":");
        tokenBuilder.append(computeSignature(username, expires));

        return tokenBuilder.toString();
    }


    public static String computeSignature(String username, long expires) {
        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(username);
        signatureBuilder.append(":");
        signatureBuilder.append(expires);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(Hex.encode(digest.digest(signatureBuilder.toString().getBytes())));
    }


    public static String getUserNameFromToken(String authToken) {
        if (null == authToken) {
            return null;
        }

        String[] parts = authToken.split(":");
        return parts[0];
    }


    public static boolean validateToken(String authToken, String username) {
        String[] parts = authToken.split(":");
        long expires = Long.parseLong(parts[1]);
        String signature = parts[2];

        if (expires < System.currentTimeMillis()) {
            return false;
        }

        return signature.equals(computeSignature(username, expires));
    }
}
