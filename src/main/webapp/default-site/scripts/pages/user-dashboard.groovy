def result = [:]
def email = params.username;
def ticket = "";

try {

  def cookies = request.getCookies();
  
  for (int i = 0; i < cookies.length; i++) {
    def name = cookies[i].getName(); 
    def value = cookies[i].getValue();
    
    if(name == "ccticket") {
      ticket = value;
      break;
    }
  }

  model.ticket = ticket;

  
}
catch(err) {
  model.err = err;
}