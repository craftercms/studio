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
