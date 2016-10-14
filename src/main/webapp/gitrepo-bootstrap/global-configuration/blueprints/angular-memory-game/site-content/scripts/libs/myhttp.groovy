package scripts.libs

import groovy.json.JsonSlurper;

class MyHTTP {

    def postToJSON(request) {
	StringBuilder sb = new StringBuilder();
    	BufferedReader br = request.getReader();

    	String str;
    	while( (str = br.readLine()) != null ){
        	sb.append(str);
    	}

    	def slurper = new JsonSlurper()
    	def postData  = slurper.parseText(""+sb);

	return postData;
    }
}
