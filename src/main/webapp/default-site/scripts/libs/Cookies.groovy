package scripts.libs

class Cookies {

	static removeCookie(name, domain, path, response) {
		def cookie = new javax.servlet.http.Cookie(name, "XXXXXX")
		cookie.setPath(path)
		cookie.setDomain(domain)
		cookie.setMaxAge(0)
		response.addCookie(cookie)
	}
}