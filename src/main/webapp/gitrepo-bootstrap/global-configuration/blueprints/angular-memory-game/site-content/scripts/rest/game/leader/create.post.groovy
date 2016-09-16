import scripts.libs.MyDAO;
import scripts.libs.MyHTTP;
import groovy.json.JsonSlurper;

def dao = new MyDAO();
dao.init();
def status = [success:true, msg:""];

try {
    def postData = new MyHTTP().postToJSON(request);

    def name = postData.name; 
    def moves = postData.moves;
    def pairs = postData.cardCount;
    
    dao.createLeader(name, moves, pairs);
}
catch(err) {
	status.msg = ""+err;
	success: false;
}

return status;
