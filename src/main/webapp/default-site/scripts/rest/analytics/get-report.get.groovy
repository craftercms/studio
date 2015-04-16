import groovy.json.JsonSlurper;
import scripts.libs.Cookies

/**
 * make service call for report configuration
 * @param site
 * @param configId
 * @returns
 */
def analyticsConfigFn =  { site, configId, alfrescoUrl, ticket ->
  def serviceUrl = alfrescoUrl + "/service/cstudio/site/get-configuration"
  serviceUrl += "?site=" + site
  serviceUrl += "&path=" + configId
  serviceUrl += "&alf_ticket=" + ticket

  def respJson = serviceUrl.toURL().getText()
  def result = new JsonSlurper().parseText( respJson )  
  return result;
}

/**
 * lookup the report from the configuration
 * @param webPropertyId
 * @param reportId
 * @param config
 * @returns
 */
def lookupReportFn = { webPropertyId, reportId, config ->
  def property;
  def report;
  if (config.sites.site != null ) { // array of 1
    config.sites = [ config.sites.site ];
  }

  for (def i = 0; i < config.sites.size; i++) {
    def cSite = config.sites[i];

    if (cSite.webPropertyId == webPropertyId) {
      property = cSite;
      break;
    }
  }
  if (property) {
  //  if (property.reports.report != null) { // array of 1
  //    property.reports = [ property.reports.report ];
  //  }

    for (def j = 0; j < property.reports.size; j++) {
      def cReport = property.reports[j];
// throw new Exception(reportId)
      if (cReport.reportId == reportId) {


        report = cReport;
        break;
      }
    }
  }

  return report;
};



def serverProperties = applicationContext.get("studio.crafter.properties")
def analyticsService = applicationContext.get("crafter.analyticsService")
def alfrescoUrl = serverProperties["alfrescoUrl"]  
def ticket = Cookies.getCookieValue("ccticket", request)

// get parameter values
def siteId = params.site
def webPropertyId = params.webPropertyId
def reportId = params.reportId
def filter = params.filter

def result = [:]

//get the analytics configuration for the website
def analyticsConfig = analyticsConfigFn(siteId, "/analytics/report-config.xml", alfrescoUrl, ticket)

def username = analyticsConfig.credentials.username
def password = analyticsConfig.credentials.password
def report = lookupReportFn(webPropertyId, reportId, analyticsConfig)

// prepare the query
def query = analyticsService.createQuery()
query.setUsername(username)
query.setPassword(password)
query.setTableId(report.tableId)
query.setStartDate(report.startDate)
query.setEndDate(report.endDate)

//add dimensions
def dimensions = report.query.dimensions.split(",")
for (def l = 0; l < dimensions.length; l++) {
  query.addDimension(dimensions[l])
}

// add metrics
def metrics = report.query.metrics.split(",");
for (def k = 0; k < metrics.length; k++) {
  query.addMetric(metrics[k]);
}


if(filter) {
	filter = filter.replace(".eq.", "==");
	query.setFilters(filter);
}
// set sort
//query.setSort(report.query.sort);

// maker the query
def queryResults = analyticsService.query(query);

// build the service response
//result.username = username;
//result.password = password;
result.tableId = report.tableId;
result.startDate = report.startDate;
result.endDate = report.endDate;
result.queryResults = queryResults;
result.visualizationCode = [:]
result.visualizationCode.library = report.presentation.library
result.visualizationCode.controller = report.presentation.controller 

return result;