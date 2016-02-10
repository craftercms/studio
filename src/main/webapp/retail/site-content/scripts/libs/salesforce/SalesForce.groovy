package scripts.libs.salesforce
import groovy.json.JsonSlurper;
 
class SalesForce {
	def QUERY_ACTIVE_CAMPAIGN = "select Name, Id, endDate from Campaign where CALENDAR_YEAR(endDate) >= 1900"
	def QUERY_CAMPAIGN_MEMBER = "select Id, lastModifiedDate, campaignId, contactId from CampaignMember "


	def USERNAME = "X"
	def PASSWORD = "X"

	/**
	 * get activeCampaigns
	 */
	def getActiveCampaigns(applicationContext) {
		def salesforceService = applicationContext.get("SalesforceService")
		def records = salesforceService.executeQuery(QUERY_ACTIVE_CAMPAIGN, USERNAME, PASSWORD)
		def recordCount = records.length;
		def results = [];

		for (int i = 0; i < recordCount; i++) {
			def record = records[i]
			def result = [:]
			result.name = record.name
			result.id = record.id
			result.endDate = record.endDate	           

			results[i] = result;
		}

		return results;
	}

	/**
	 * get oppertunities that were influenced by campaigns
	 */
	def getCampaignInfluencedOpportunities(applicationContext) {
		def x ='[{"id":"701i0000000dEKTAA2","name":"User Conference - Jun 17-19, 2002","contactCount":0,"oppertunities":[{"id":"no-oppertunity","name":"no oppertunity","modifyDate":"XYX","isClosed":false,"stageName":"none","probability":0.0,"contact":{"id":"003i000000T9WGSAA3","firstName":"Sean","lastName":"Forbes","role":"Decision Maker"}}]},{"id":"701i0000000ouLEAAY","name":"abc","contactCount":0,"oppertunities":[{"id":"no-oppertunity","name":"no oppertunity","modifyDate":"XYX","isClosed":false,"stageName":"none","probability":0.0,"contact":{"id":"003i000000T9WGVAA3","firstName":"Andy","lastName":"Young","role":"Influencer"}}]},{"id":"701i0000000ouLEAAY","name":"abc","contactCount":0,"oppertunities":[{"id":"no-oppertunity","name":"no oppertunity","modifyDate":"XYX","isClosed":false,"stageName":"none","probability":0.0,"contact":{"id":"003i000000T9WGeAAN","firstName":"Ashley","lastName":"James","role":"Decision Maker"}}]},{"id":"701i0000000dEKUAA2","name":"DM Campaign to Top Customers - Nov 12-23, 2001","contactCount":1,"oppertunities":[{"id":"006i000000ESIxmAAH","name":"Burlington Textiles Weaving Plant Generator","modifyDate":"Dec 31, 2013 2:41:29 PM","isClosed":false,"stageName":"Proposal/Price Quote","probability":75.0,"contact":{"id":"003i000000T9WGWAA3","firstName":"Tim","lastName":"Barr"}}]},{"id":"701i0000000ouLEAAY","name":"abc","contactCount":1,"oppertunities":[{"id":"006i000000ElSyUAAV","name":"Pyramid Construction New CMS","modifyDate":"Dec 31, 2013 2:52:26 PM","isClosed":false,"stageName":"Proposal/Price Quote","probability":75.0,"contact":{"id":"003i000000T9WGUAA3","firstName":"Pat","lastName":"Stumuller","role":"Decision Maker"}}]},{"id":"701i0000000ouLEAAY","name":"abc","contactCount":1,"oppertunities":[{"id":"006i000000ESIxmAAH","name":"Burlington Textiles Weaving Plant Generator","modifyDate":"Dec 31, 2013 2:41:29 PM","isClosed":false,"stageName":"Proposal/Price Quote","probability":75.0,"contact":{"id":"003i000000T9WGXAA3","firstName":"John","lastName":"Bond"}}]},{"id":"701i0000000CoPFAA0","name":"The big deal","contactCount":1,"oppertunities":[{"id":"006i000000ElSEXAA3","name":"CMS Upgrade","modifyDate":"Dec 31, 2013 2:48:27 PM","isClosed":false,"stageName":"Value Proposition","probability":50.0,"contact":{"id":"003i000000T9WGfAAN","firstName":"Tom","lastName":"Ripley"}}]},{"id":"701i0000000ouLEAAY","name":"abc","contactCount":1,"oppertunities":[{"id":"006i000000ESIxmAAH","name":"Burlington Textiles Weaving Plant Generator","modifyDate":"Dec 31, 2013 2:41:29 PM","isClosed":false,"stageName":"Proposal/Price Quote","probability":75.0,"contact":{"id":"003i000000T9WGWAA3","firstName":"Tim","lastName":"Barr"}}]},{"id":"701i0000000dEKVAA2","name":"International Electrical Engineers Assoc. Conf 2013","contactCount":1,"oppertunities":[{"id":"006i000000ESIxmAAH","name":"Burlington Textiles Weaving Plant Generator","modifyDate":"Dec 31, 2013 2:41:29 PM","isClosed":false,"stageName":"Proposal/Price Quote","probability":75.0,"contact":{"id":"003i000000T9WGWAA3","firstName":"Tim","lastName":"Barr"}}]},{"id":"701i0000000t0BqAAI","name":"Demin Days","contactCount":1,"oppertunities":[{"id":"006i000000ElSyUAAV","name":"Pyramid Construction New CMS","modifyDate":"Dec 31, 2013 2:52:26 PM","isClosed":false,"stageName":"Proposal/Price Quote","probability":75.0,"contact":{"id":"003i000000T9WGUAA3","firstName":"Pat","lastName":"Stumuller","role":"Decision Maker"}}]},{"id":"701i0000000CoPFAA0","name":"The big deal","contactCount":1,"oppertunities":[{"id":"006i000000ESIxmAAH","name":"Burlington Textiles Weaving Plant Generator","modifyDate":"Dec 31, 2013 2:41:29 PM","isClosed":false,"stageName":"Proposal/Price Quote","probability":75.0,"contact":{"id":"003i000000T9WGWAA3","firstName":"Tim","lastName":"Barr"}}]}]';
 		def slurper = new JsonSlurper()
 		def result = slurper.parseText(x)

		return result;
	}

	def realfunction(x) {
		def results = [];
		def salesforceService = applicationContext.get("SalesforceService")
		def campaignMemberRecords = salesforceService.executeQuery(QUERY_CAMPAIGN_MEMBER, USERNAME, PASSWORD) 

		def campaignMemberRecordsCount = campaignMemberRecords.length;

		for (int i = 0; i < campaignMemberRecordsCount; i++) {
			def campaignMemberRecord = campaignMemberRecords[i]
			
			def campaignRecord = salesforceService.executeQuery("select Id, name from Campaign where id = '" + campaignMemberRecord.campaignId + "'", USERNAME, PASSWORD)[0]

			def result = [:];
			result.id = campaignRecord.id;
			result.name = campaignRecord.name;
			result.contactCount = 0;
			result.oppertunities = [];

			def opportunityContactRoleRecords = salesforceService.executeQuery("select Id, opportunityId, role from OpportunityContactRole where contactId = '" + campaignMemberRecord.contactId + "'", USERNAME, PASSWORD)
			
			if(opportunityContactRoleRecords != null) {
				result.contactCount = opportunityContactRoleRecords.length;
			
				for (int j = 0; j < opportunityContactRoleRecords.length; j++) {
					def opportunityContactRoleRecord = opportunityContactRoleRecords[j]
					def oppertunityRecord = salesforceService.executeQuery("select Id, name, lastModifiedDate, isClosed, stageName, probability from Opportunity where id = '" + opportunityContactRoleRecord.opportunityId + "'", USERNAME, PASSWORD)[0]
					def oppertunity = [:]
					oppertunity.id = oppertunityRecord.id
		        	oppertunity.name = oppertunityRecord.name
		        	oppertunity.modifyDate = oppertunityRecord.lastModifiedDate.getTime() //string("MMM/dd/yyyy")
					oppertunity.isClosed = oppertunityRecord.isClosed
					oppertunity.stageName = oppertunityRecord.stageName
					oppertunity.probability = oppertunityRecord.probability
					oppertunity.contact = [:];

					def contactRecord = salesforceService.executeQuery("select Id, firstName, lastName from Contact where id = '" + campaignMemberRecord.contactId + "'", USERNAME, PASSWORD)[0]
					oppertunity.contact.id = contactRecord.id
					oppertunity.contact.firstName = contactRecord.firstName
					oppertunity.contact.lastName = contactRecord.lastName
					//oppertunity.contact.role = (contactRecord.role != null) ? contactRecord.role : "unknown"

					result.oppertunities[j] = oppertunity
				}  
			}
			else {
				def oppertunity = [:]
				oppertunity.id = "no-oppertunity"
	        	oppertunity.name = "no oppertunity"
	        	oppertunity.modifyDate = "XYX"
				oppertunity.isClosed = false
				oppertunity.stageName = "none"
				oppertunity.probability = 0.0
				oppertunity.contact = [:];

				def contactRecord = salesforceService.executeQuery("select Id, firstName, lastName from Contact where id = '" + campaignMemberRecord.contactId + "'", USERNAME, PASSWORD)[0]
				oppertunity.contact.id = contactRecord.id
				oppertunity.contact.firstName = contactRecord.firstName
				oppertunity.contact.lastName = contactRecord.lastName
				oppertunity.contact.role = "unknown"

				result.oppertunities[0] = oppertunity
			}


			results[i] = result;
		}

		return results;  
	}
}
