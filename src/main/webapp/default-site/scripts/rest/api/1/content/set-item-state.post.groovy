import scripts.api.ObjectStateServices;

def result = [:]

def site = request.getParameter("site")
def path = request.getParameter("path")
def state = request.getParameter("state")
def systemprocessing = request.getParameter("systemprocessing")

/*
if (site == undefined) {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = false;
}
if (path == undefined) {
    status.code = 400;
    status.message = "Path must be provided.";
    status.redirect = true;
    valid = false;
}
if (state == undefined) {
    status.code = 400;
    status.message = "State must be provided.";
    status.redirect = true;
    valid = false;
}
*/
if ("true" == systemprocessing) {
    systemprocessing = true;
} else {
    systemprocessing = false;
}

def context = ObjectStateServices.createContext(applicationContext, request);
result.result = ObjectStateServices.setObjectState(context, site, path, state, systemprocessing);

return result 
