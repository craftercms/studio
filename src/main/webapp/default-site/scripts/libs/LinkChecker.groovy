package scripts.libs

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 * Link Checker class 
 */
public class LinkChecker {
	static final String A_HREF_PATTERN = '<a[^>]+href=\\"(.*?)\\"[^>]*>.*?</a>'
	static final String LINK_HREF_PATTERN = '<link[^>]+href=\\"(.*?)\\"[^>]*>'
	static final String SCRIPT_HREF_PATTERN = '<script[^>]+src=\\"(.*?)\\"[^>]*>'
	static final String IMG_HREF_PATTERN = '<script[^>]+src=\\"(.*?)\\"[^>]*>'

	static String baseUrl = "http://localhost:9080"
	static String scanRootPath = "/"
	static String notificationList = ""
	static Object notificationService = null
	static Object siteService = null
	static boolean isRunning = false

	/**
	 * Test the whole site for dead links then send an email report to an list of email addresses
	 */
	static testSiteForDeadLinks(site, logger, context) {
		def report = [:]
		report.checkedLinks = []
		report.brokenLinks = []

		// this checker runs on a chron and can be invoked from a rest script
		// this checker can take a long time on large sites. 
		// DO NOT RUN, if already in progress
		if(!isRunning) {

			try {
				isRunning = true

				logger.info("======================================================================")
				logger.info(" Running link checker job")
				logger.info("======================================================================")

				notificationService = context.applicationContext.get("cstudioNotificationService") 
				siteService = context.applicationContext.get("cstudioSiteServiceSimple") 
				
				if(siteService) {
					def config = siteService.getConfiguration(site, "/site-config.xml", false)

					if(config.linkChecking != null) {
						if(config.linkChecking.baseUrl) baseUrl = config.linkChecking.baseUrl
						if(config.linkChecking.scanRootPath) baseUrl = config.linkChecking.scanRootPath
						if(config.linkChecking.notificationList) baseUrl = config.linkChecking.notificationList
					}

					testUrlForDeadLinks(baseUrl+scanRootPath, site, report, true, logger)

					sendReport(site, notificationList, report, logger)
				}
				else {
					logger.error("not able to location brean cstudioSiteServiceSimple from context '"+context+"'")
				}
			}
			finally {
				isRunning = false
			}
		}

		return report
	}

	/**
	 * Send the report via email
	 */
	static sendReport(site, notificationList, report, logger) {
		// for each link checked
		try {
			logger.info("CHECKED THE FOLLOWING LINKS ("+report.checkedLinks.size+") :")
			for(def i=0; i<report.checkedLinks.size; i++) {
				logger.info(".....: " + report.checkedLinks.get(i));
			}

			logger.info("FOUND THE FOLLOWING BROKEN LINKS ("+report.brokenLinks.size+"):")
			for(def j=0; j<report.brokenLinks.size; j++) {
				def brokenLink = report.brokenLinks.get(j)
				logger.info(".....: " + brokenLink.url + ", " + brokenLink.message)
			}

			def emailParams = [:]
			emailParams.put("report", report)
			
			notificationService.sendGenericNotification(
				site, 
				"/site/website/index.xml",   
				""+notificationList, 
				""+notificationList, 
				"brokenLinkNotice", 
				emailParams)
		}
		catch(Exception ex) {
		  logger.error("ERROR REPORTING: "+ex)
		}
	}

	/** 
	 * given a link, test it and update the report
	 * this method IS RECURSIVE for relative URLs so it will continue to test for broken sub links
	 * Also note, the report keeps track of what links were checked and this method WILL NOT RE-CHECK a link
	 * a link that has already been checked.
	 */
	static testUrlForDeadLinks(url, site, report, recurseFlag, logger) {

		if(!report.checkedLinks.contains(url)) {
			// add the link to the report so that we don't recheck the same path later
			
			report.checkedLinks.add(url)
			//logger.info("checking link (" + report.checkedLinks.size + ") : " + url)

			// test the link
			def result = testLink(url+"?crafterSite="+site, logger)

			if(result.success) { 
				
				if(recurseFlag) {
					// since we're recurising this link, get all the sub links and test them
					def links = findLinksInContent(url, result.content, logger)

					for(def i=0; i<links.size; i++) {
						def link = links.get(i)

						if(link.toLowerCase().contains("http://") || link.toLowerCase().contains("https://")) {
							// link is absolute, do not recurse on the test
							testUrlForDeadLinks(link, site, report, false, logger)
						}
						else {
							// link is relative, recurse after testing on sub links
							testUrlForDeadLinks(baseUrl+link, site, report, true, logger)
						}
					}
				}
				//else {
					// told not to recurse, nothing to do.  Inbound link was absolute
					// as long as it works we are happy.
				//}
			}
			else {
				// report failure getting url
				report.brokenLinks.add(result);
			}
		}
		//else {
			// link was already checked, nothing to do
		//} 
	}	

	/**
	 * given html or css, find the sub links
	 */
	static findLinksInContent(url, content, logger) {
		def foundReferences = []
		def urlLoweredCased = url.toLowerCase()

		if(urlLoweredCased.contains(".html") 	// page
		|| urlLoweredCased.contains(".htm")  	// page
		|| urlLoweredCased.contains(".xml")  	// page
		|| urlLoweredCased.endsWith("/")     	// page
		||!urlLoweredCased.contains(".")    	// page

		|| urlLoweredCased.contains(".css")	// css

		) {  // any of the above pages or css files should be scanned for sub links
		 	 // there is no point in scanning images etc
		 	 // PDFs, Documents and Javascript are difficult to scan and so are out of scope at the moment  	

		 	def refPatterns = [A_HREF_PATTERN, LINK_HREF_PATTERN, IMG_HREF_PATTERN, SCRIPT_HREF_PATTERN]

		 	for(def i=0; i < refPatterns.size; i++) {
		 		def refPattern = refPatterns.get(i)

		 		def matcher = (content =~ Pattern.compile(refPattern))

				if(matcher.getCount() > 0) {
					
					for(def j=0; j < matcher.getCount(); j++) {
						def referenceUrl = matcher[j][1]

						if(!referenceUrl.startsWith("#")) {
							if(!foundReferences.contains(referenceUrl)) {
								//logger.info("adding reference: '" + referenceUrl + "'")
								foundReferences.add(referenceUrl)
							}
						}
					} 					
				}
		 	}
		}

		return foundReferences
	}

	/**
	 * get the object at the URL and see what the responese code is.
	 * if possible, get the content
	 * return a report to the caller
	 */
	static testLink(url, logger) {
		def result = [:]
		result.success = false
		result.message = ""
		result.url = url
		result.isUrl = true
		result.content = ""
		result.statusCode = 0

		if(url) {

			try {
				def http = new HTTPBuilder(url)

				http.request(Method.GET, ContentType.TEXT) {
					//uri.path = path
				    //uri.query = query
				    //headers.'User-Agent' = 'Crafter CMS/link-checker'

					response.success = { resp, reader ->
						
						result.statusCode = resp.getStatus()

						if(resp.getStatus() == 200) {
							result.success = true
							result.content = reader.getText()
						}
					} // end closure
				} // end closure
			} 
			catch(groovyx.net.http.HttpResponseException ex) {
				result.message = "Response Error: " + ex
			} 
			catch(java.net.ConnectException ex) {
				result.message = "Connection Errror: " + ex
			}
			catch(Exception ex) {
				result.message = "General Error: " + ex
			}
		}
		else {
			// not a link
			result.isUrl = true
			result.message = "url is not provided (null)"
		}
	
		return result
	}
}