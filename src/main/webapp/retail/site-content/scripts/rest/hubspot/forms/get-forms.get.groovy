import groovy.json.JsonSlurper

def forms = []

try {
  def formsUrl = "https://api.hubapi.com/contacts/v1/forms?hapikey=X"

  def response = (formsUrl).toURL().getText()
  def hsForms = new JsonSlurper().parseText( response )
  def hsFormsLen = hsForms.size

  for(int i=0; i < hsFormsLen; i++) {
  	def hsForm = hsForms[i]
  	def form = [:]
  	form.id = hsForm.guid
  	form.name = hsForm.name
  	
    forms[i] = form
  }
}
catch(err) {
  forms.err = err
}

return forms
