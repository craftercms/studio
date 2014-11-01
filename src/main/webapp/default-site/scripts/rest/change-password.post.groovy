def result = [:]
def email = params.email;
def password = params.password;
def confirm = params.confirmation;

if(password.equals(confirm)) {
	def url = "http://127.0.0.1:8080/alfresco/service/crafter/modules/change-user-password?email=" + email + "&password=" + password;
	def response = "";

	try {
	   response = (url).toURL().getText();

	   result.type = "success";
	   result.message = "Reset successful";
   
	 }
	 catch(err) {
	    invalidpw = true;
	    result.exception = err;
	    result.response = response;
		result.type = "error";
	    result.message = "Unable to change password";
	 }
}
return result;
