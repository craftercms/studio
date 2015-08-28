import scripts.api.ObjectStateServices;

def result = [:]

def site = params.site;
def path = params.path;
def state = params.state;
def systemprocessing = params.systemprocessing;

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