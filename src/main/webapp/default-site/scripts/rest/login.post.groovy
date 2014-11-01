def result = [:]
def username = params.username;
def password = params.password;

def url = "http://127.0.0.1:8080/alfresco/service/api/login?u=" + username + "&pw=" + password;
def srvresponse = "";
def invalidpw = false;

 try {
   srvresponse = (url).toURL().getText();
   srvresponse = srvresponse.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
   srvresponse = srvresponse.replace("\n", "");
   srvresponse = srvresponse.replace("<ticket>", "");
   srvresponse = srvresponse.replace("</ticket>", "");

   //session.setAttribute("alfticket", srvresponse);
   def cookie = new javax.servlet.http.Cookie('ccticket', srvresponse);
   cookie.setPath("/");
   cookie.setDomain("127.0.0.1");
   response.addCookie(cookie);

   def ucookie = new javax.servlet.http.Cookie('ccu', username);
   ucookie.setPath("/");
   ucookie.setDomain("127.0.0.1");
   response.addCookie(ucookie);

   def acookie = new javax.servlet.http.Cookie('alf_ticket', srvresponse);
   acookie.setPath("/");
   acookie.setDomain("127.0.0.1");
   response.addCookie(acookie);

   def aucookie = new javax.servlet.http.Cookie('username', username);
   aucookie.setPath("/");
   aucookie.setDomain("127.0.0.1");
   response.addCookie(aucookie);
   
   def aucookie3 = new javax.servlet.http.Cookie('alfUsername3', username);
   aucookie3.setPath("/");
   aucookie3.setDomain("127.0.0.1");
   response.addCookie(aucookie3);
   
   

   result.type = "success";
   result.message = "Login successful";

}
 catch(err) {
    invalidpw = true;
    result.exception = err;
    result.response = srvresponse;
	result.type = "error";
    result.message = "Invalid user name or password";
 }

return result;