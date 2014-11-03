model.err = "none";

try {

   def cookie = new javax.servlet.http.Cookie('ccticket', "");
   cookie.setPath("/");
   cookie.setDomain(".craftersoftware.com");
   cookie.setMaxAge(-1);
   response.addCookie(cookie);

   def ucookie = new javax.servlet.http.Cookie('ccu', "");
   ucookie.setPath("/");
   ucookie.setDomain(".craftersoftware.com");
   ucookie.setMaxAge(-1);
   response.addCookie(ucookie);

   def acookie = new javax.servlet.http.Cookie('alf_ticket', "");
   acookie.setPath("/");
   acookie.setDomain(".craftersoftware.com");
   acookie.setMaxAge(-1);
   response.addCookie(acookie);

   def aucookie = new javax.servlet.http.Cookie('username', "");
   aucookie.setPath("/");
   aucookie.setDomain(".craftersoftware.com");
   aucookie.setMaxAge(-1);
   response.addCookie(aucookie);

   def aucookie3 = new javax.servlet.http.Cookie('alfUsername3', "");
   aucookie3.setPath("/");
   aucookie3.setDomain(".craftersoftware.com");
   aucookie3.setMaxAge(-1);
   response.addCookie(aucookie3);
}
catch(err) {
  model.err = err;
}
