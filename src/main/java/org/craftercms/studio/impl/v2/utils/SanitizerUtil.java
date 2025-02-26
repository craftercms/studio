/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class SanitizerUtil {
	private static final PolicyFactory TEXT_POLICY = new HtmlPolicyBuilder()
		.allowElements("b", "i", "u", "p")
		.toFactory();

	/**
	 * Sanitizes the given text input by removing potentially harmful content.
	 * This function ensures that the input text does not contain any malicious
	 * scripts, XSS payloads, or unsafe HTML elements.
	 *
	 * @param text The text to be sanitized.
	 * @return A sanitized version of the input text with only allowed content.
	 */
	public static String sanitizeText(String text) {
		return TEXT_POLICY.sanitize(text);
	}
}
