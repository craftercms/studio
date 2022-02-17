/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package scripts.libs

class Cookies {

	static createCookie(name, value, domain, path, response) {
		def cookie = new javax.servlet.http.Cookie(name, value)
		cookie.setPath(path)
		cookie.setDomain(domain)
		response.addCookie(cookie)
	}

	static removeCookie(name, domain, path, response) {
		def cookie = new javax.servlet.http.Cookie(name, "XXXXXX")
		cookie.setPath(path)
		cookie.setDomain(domain)
		cookie.setMaxAge(0)
		response.addCookie(cookie)
	}

	static getCookieValue(cookieName, request) {
		def result = "UNSET"
		def cookies = request.getCookies()
		if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                def name = cookies[i].getName()
                def value = cookies[i].getValue()

                if(name == cookieName) {
                    result = value
                    break
                }
            }
        }

    	return result
	}
}
