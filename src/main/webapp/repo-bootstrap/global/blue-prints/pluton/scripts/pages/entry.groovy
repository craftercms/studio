
def lang = request.getLocale().toString().toLowerCase().substring(0,2)
def uri =request.getRequestURI().substring(request.getContextPath().length())

//if(uri.equals("/"))
//response.sendRedirect("/"+lang)
