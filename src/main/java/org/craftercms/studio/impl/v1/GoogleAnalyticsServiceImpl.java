/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1;


import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.*;
import com.google.gdata.data.analytics.*;
import com.google.gdata.util.*;
import org.craftercms.studio.api.v1.service.AnalyticsQuery;
import org.craftercms.studio.api.v1.to.AnalyticsReportTO;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class GoogleAnalyticsServiceImpl implements org.craftercms.studio.api.v1.service.AnalyticsService {
  public static final String ACCOUNTS_URL = "https://www.google.com/analytics/feeds/accounts/default";
  public static final String DATA_URL = "https://www.google.com/analytics/feeds/data";

  // Visitors
  public static final String DIMENSION_VISITOR_TYPE = "ga:visitorType";
  public static final String DIMENSION_VISITOR_COUNT = "ga:visitorCount";
  public static final String DIMENSION_DAYS_SINCE_LAST_VISIT = "ga:daysSinceLastVisit";
  public static final String DIMENSION_USER_DEFINED_VALUE = "ga:userDefinedValue";

  // Session
  public static final String DIMENSION_VISIT_LENGTH = "ga:visitLength";

  // Campaign
  public static final String DIMENSION_REFERRAL_PATH = "ga:referralPath";
  public static final String DIMENSION_CAMPAIGN = "ga:campaign";
  public static final String DIMENSION_SOURCE = "ga:source";
  public static final String DIMENSION_MEDIUM = "ga:medium";
  public static final String DIMENSION_KEYWORD = "ga:keyword";
  public static final String DIMENSION_AD_CONTENT = "ga:adContent";

  // AdWords
  public static final String DIMENSION_AD_GROUP = "ga:adGroup";
  public static final String DIMENSION_AD_SLOT = "ga:adSlot";
  public static final String DIMENSION_AD_SLOT_POSITION = "ga:adSlotPosition";
  public static final String DIMENSION_AD_DISTRIBUTION_NETWORK = "ga:adDistributionNetwork";
  public static final String DIMENSION_AD_MATCH_TYPE = "ga:adMatchType";
  public static final String DIMENSION_AD_MATCHED_QUERY = "ga:adMatchedQuery";
  public static final String DIMENSION_AD_PLACEMENT_DOMAIN = "ga:adPlacementDomain";
  public static final String DIMENSION_AD_PLACEMENT_URL = "ga:adPlacementUrl";
  public static final String DIMENSION_AD_FORMAT = "ga:adFormat";
  public static final String DIMENSION_AD_TARGETING_TYPE = "ga:adTargetingType";
  public static final String DIMENSION_AD_TARGETING_OPTION = "ga:adTargetingOption";
  public static final String DIMENSION_AD_DISPLAY_URL = "ga:adDisplayUrl";
  public static final String DIMENSION_AD_DESTINATION_URL = "ga:adDestinationUrl";
  public static final String DIMENSION_ADWORDS_CUSTOMER_ID = "ga:adwordsCustomerId";
  public static final String DIMENSION_ADWORDS_CAMPAIGN_ID = "ga:adwordsCampaignId";
  public static final String DIMENSION_ADWORDS_AD_GROUP_ID = "ga:adwordsAdGroupId";
  public static final String DIMENSION_ADWORDS_CREATIVE_ID = "ga:adwordsCreativeId";
  public static final String DIMENSION_ADWORDS_CRITERIA_ID = "ga:adwordsCriteriaId";

  // System
  public static final String DIMENSION_BROWSER = "ga:browser";
  public static final String DIMENSION_BROWSER_VERSION = "ga:browserVersion";
  public static final String DIMENSION_OPERATING_SYSTEM = "ga:operatingSystem";
  public static final String DIMENSION_OPERATING_SYSTEM_VERSION = "ga:operatingSystemVersion";
  public static final String DIMENSION_FLASH_VERSION = "ga:flashVersion";
  public static final String DIMENSION_JAVA_ENABLED = "ga:javaEnabled";
  public static final String DIMENSION_IS_MOBILE = "ga:isMobile";
  public static final String DIMENSION_LANGUAGE = "ga:language";
  public static final String DIMENSION_SCREEN_COLORS = "ga:screenColors";
  public static final String DIMENSION_SCREEN_RESOLUTION = "ga:screenResolution";

  // Geo/Network
  public static final String DIMENSION_CONTINENT = "ga:continent";
  public static final String DIMENSION_SUB_CONTINENT = "ga:subContinent";
  public static final String DIMENSION_COUNTRY = "ga:country";
  public static final String DIMENSION_REGION = "ga:region";
  public static final String DIMENSION_CITY = "ga:city";
  public static final String DIMENSION_LATITUDE = "ga:latitude";
  public static final String DIMENSION_LONGITUDE = "ga:longitude";
  public static final String DIMENSION_NETWORK_DOMAIN = "ga:networkDomain";
  public static final String DIMENSION_NETWORK_LOCATION = "ga:networkLocation";

  // Page Tracking
  public static final String DIMENSION_HOSTNAME = "ga:hostname";
  public static final String DIMENSION_PAGE_PATH = "ga:pagePath";
  public static final String DIMENSION_PAGE_TITLE = "ga:pageTitle";
  public static final String DIMENSION_LANDING_PAGE_PATH = "ga:landingPagePath";
  public static final String DIMENSION_SECOND_PAGE_PATH = "ga:secondPagePath";
  public static final String DIMENSION_EXIT_PAGE_PATH = "ga:exitPagePath";
  public static final String DIMENSION_PREVIOUS_PAGE_PATH = "ga:previousPagePath";
  public static final String DIMENSION_NEXT_PAGE_PATH = "ga:nextPagePath";
  public static final String DIMENSION_PAGE_DEPTH = "ga:pageDepth";

  // Internal Search
  public static final String DIMENSION_SEARCH_USED = "ga:searchUsed";
  public static final String DIMENSION_SEARCH_KEYWORD = "ga:searchKeyword";
  public static final String DIMENSION_SEARCH_KEYWORD_REFINEMENT = "ga:searchKeywordRefinement";
  public static final String DIMENSION_CATEGORY = "ga:searchCategory";
  public static final String DIMENSION_START_PAGE = "ga:searchStartPage";
  public static final String DIMENSION_DESTINATION_PAGE = "ga:searchDestinationPage";

  // Event Tracking
  public static final String DIMENSION_EVENT_CATEGORY = "ga:eventCategory";
  public static final String DIMENSION_EVENT_ACTION = "ga:eventAction";
  public static final String DIMENSION_EVENT_LABEL = "ga:eventLabel";

  // Ecommerce
  public static final String DIMENSION_TRANSACTION_ID = "ga:transactionId";
  public static final String DIMENSION_AFFILIATION = "ga:affiliation";
  public static final String DIMENSION_VISITS_TO_TRANSACTION = "ga:visitsToTransaction";
  public static final String DIMENSION_DAYS_TO_TRANSACTION = "ga:daysToTransaction";
  public static final String DIMENSION_PRODUCT_CATEGORY = "ga:productCategory";
  public static final String DIMENSION_PRODUCT_NAME = "ga:productName";
  public static final String DIMENSION_PRODUCT_SKU = "ga:productSku";

  // Custom Variables
  public static final String DIMENSION_CUSTOM_VAR_NAME_N = "ga:customVarName(%d)";
  public static final String DIMENSION_CUSTOM_VAR_VALUE_N = "ga:customVarValue(%d)";

  // Time
  public static final String DIMENSION_DATE = "ga:date";
  public static final String DIMENSION_YEAR = "ga:year";
  public static final String DIMENSION_MONTH = "ga:month";
  public static final String DIMENSION_WEEK = "ga:week";
  public static final String DIMENSION_DAY = "ga:day";
  public static final String DIMENSION_HOUR = "ga:hour";
  public static final String DIMENSION_NTH_MONTH = "ga:nthMonth";
  public static final String DIMENSION_NTH_WEEK = "ga:nthWeek";
  public static final String DIMENSION_NTH_DAY = "ga:nthDay";
  public static final String DIMENSION_DAY_OF_WEEK = "ga:dayOfWeek";


  // Visitor
  public static final String METRIC_VISITORS = "ga:visitors";
  public static final String METRIC_NEW_VISITS = "ga:newVisits";
  public static final String METRIC_PERCENT_NEW_VISITS = "ga:percentNewVisits";

  // Session
  public static final String METRIC_VISITS = "ga:visits";
  public static final String METRIC_TIME_ON_SITE = "ga:timeOnSite";
  public static final String METRIC_AVG_TIME_ON_SITE = "ga:avgTimeOnSite";

  // Campaign
  public static final String METRIC_ORGANIG_SEARCHES = "ga:organincSearches";

  // AdWords
  public static final String METRIC_IMPRESSIONS = "ga:impressions";
  public static final String METRIC_AD_CLICKS = "ga:adClicks";
  public static final String METRIC_AD_COST = "ga:adCost";
  public static final String METRIC_CPM = "ga:CPM";
  public static final String METRIC_CPC = "ga:CPC";
  public static final String METRIC_CTR = "ga:CTR";
  public static final String METRIC_COST_PER_TRANSACTION = "ga:costPerTransaction";
  public static final String METRIC_COST_PER_GOAL_CONVERSION = "ga:costPerGoalConversion";
  public static final String METRIC_COST_PER_CONVERSION = "ga:costPerConversion";
  public static final String METRIC_RPC = "ga:RPC";
  public static final String METRIC_ROI = "ga:ROI";
  public static final String METRIC_MARGIN = "ga:margin";

  // Goals
  public static final String METRIC_GOAL_N_STARTS = "ga:goal(%d)Starts";
  public static final String METRIC_GOAL_STARTS_ALL = "ga:goalStartsAll";
  public static final String METRIC_GOAL_N_COMPLETIONS = "ga:goal(%d)Completions";
  public static final String METRIC_GOAL_COMPLETIONS_ALL = "ga:goalCompletionsAll";
  public static final String METRIC_GOAL_N_VALUE = "ga:goal(%d)Value";
  public static final String METRIC_GOAL_VALUE_ALL = "ga:goalValueAll";
  public static final String METRIC_GOAL_VALUE_PER_VISIT = "ga:goalValuePerVisit";
  public static final String METRIC_GOAL_N_CONVERSION_RATE = "ga:goal(%d)ConversionRate";
  public static final String METRIC_GOAL_CONVERSION_RATE_ALL = "ga:goalConversionRateAll";
  public static final String METRIC_GOAL_N_ABANDONS = "ga:goal(%d)Abandons";
  public static final String METRIC_GOAL_ABANDONS_ALL = "ga:goalAbandonsAll";
  public static final String METRIC_GOAL_N_ABANDON_RATE = "ga:goal(%d)AbandonRate";
  public static final String METRIC_GOAL_ABANDON_RATE_ALL = "ga:goalAbandonRateAll";

  // Page Tracking
  public static final String METRIC_ENTRANCES = "ga:entrances";
  public static final String METRIC_ENTRANCE_RATE = "ga:entranceRate";
  public static final String METRIC_BOUNCES = "ga:bounces";
  public static final String METRIC_ENTRANCE_BOUNCE_RATE = "ga:entranceBounceRate";
  public static final String METRIC_VISIT_BOUNCE_RATE = "ga:visitBounceRate";
  public static final String METRIC_PAGEVIEWS = "ga:pageviews";
  public static final String METRIC_PAGEVIEWS_PER_VISIT = "ga:pageviewsPerVisit";
  public static final String METRIC_UNIQUE_PAGEVIEWS = "ga:uniquePageviews";
  public static final String METRIC_TIME_ON_PAGE = "ga:timeOnPage";
  public static final String METRIC_AVG_TIME_ON_PAGE = "ga:avgTimeOnPage";
  public static final String METRIC_EXITS = "ga:exits";
  public static final String METRIC_EXIT_RATE = "ga:exitRate";
  public static final String METRIC_PAGE_LOAD_SAMPLE = "ga:pageLoadSample";
  public static final String METRIC_PAGE_LOAD_TIME = "ga:pageLoadTime";
  public static final String METRIC_AVG_PAGE_LOAD_TIME = "ga:avgPageLoadTime";

  // Internal Search
  public static final String METRIC_SEARCH_RESULT_VIEWS = "ga:searchResultViews";
  public static final String METRIC_SEARCH_UNIQUES = "ga:searchUniques";
  public static final String METRIC_AVG_SEARCH_RESULT_VIEWS = "ga:avgSearchResultViews";
  public static final String METRIC_SEARCH_VISITS = "ga:searchVisits";
  public static final String METRIC_PERCENT_VISITS_WITH_SEARCH = "ga:percentVisitsWithSearch";
  public static final String METRIC_SEARCH_DEPTH = "ga:searchDepth";
  public static final String METRIC_AVG_SEARCH_DEPTH = "ga:avgSearchDepth";
  public static final String METRIC_SEARCH_REFINEMENTS = "ga:searchRefinements";
  public static final String METRIC_SEARCH_DURATION = "ga:searchDuration";
  public static final String METRIC_AVG_SEARCH_DURATION = "ga:avgSearchDuration";
  public static final String METRIC_SEARCH_EXITS = "ga:searchExits";
  public static final String METRIC_SEARCH_EXIT_RATE = "ga:searchExitRate";
  public static final String METRIC_SEARCH_GOAL_N_CONVERSION_RATE = "ga:searchGoal(%d)ConversionRate";
  public static final String METRIC_SEARCH_GOAL_CONVERSION_RATE_ALL = "ga:searchGoalConversionRateAll";
  public static final String METRIC_GOAL_VALUE_ALL_PER_SEARCH = "ga:goalValueAllPerSearch";

  // Event Tracking
  public static final String METRIC_TOTAL_EVENTS = "ga:totalEvents";
  public static final String METRIC_UNIQUE_EVENTS = "ga:uniqueEvents";
  public static final String METRIC_EVENT_VALUE = "ga:eventValue";
  public static final String METRIC_AVG_EVENT_VALUE = "ga:avgEventValue";
  public static final String METRIC_VISITS_WITH_EVENT = "ga:visitsWithEvent";
  public static final String METRIC_EVENTS_PER_VISIT_WITH_EVENT = "ga:eventsPerVisitWithEvent";

  // Ecommerce
  public static final String METRIC_TRANSACTIONS = "ga:transactions";
  public static final String METRIC_TRANSACTIONS_PER_VISIT = "ga:transactionsPerVisit";
  public static final String METRIC_TRANSACTION_REVENUE = "ga:transactionRevenue";
  public static final String METRIC_REVENUE_PER_TRANSACTION = "ga:revenuePerTransaction";
  public static final String METRIC_TRANSACTION_REVENUE_PER_VISIT = "ga:transactionRevenuePerVisit";
  public static final String METRIC_TRANSACTION_SHIPPING = "ga:transactionShipping";
  public static final String METRIC_TRANSACTION_TAX = "ga:transactionTax";
  public static final String METRIC_TOTAL_VALUE = "ga:totalValue";
  public static final String METRIC_ITEM_QUANTITY = "ga:itemQuantity";
  public static final String METRIC_UNIQUE_PURCHASES = "ga:uniquePurchases";
  public static final String METRIC_REVENUE_PER_ITEM = "ga:revenuePerItem";
  public static final String METRIC_ITEM_REVENUE = "ga:itemRevenue";
  public static final String METRIC_ITEMS_PER_PURCHASE = "ga:itemsPerPurchase";

//  private static String arrayToString(String[] strings) {
//    StringBuilder sb = new StringBuilder();
//    for (int i = 0, len = strings.length; i < len; i++) {
//      sb.append(strings[i]);
//      if (i + 1 < len)
//        sb.append(",");
//    }
//    return sb.toString();
//  }


//  private AnalyticsService service;
//  private AnalyticsConfigTO config;
//  private boolean isLoggedIn;

//  public GoogleAnalyticsServiceImpl(AnalyticsConfigTO config) {
//    this.config = config;
//    this.service = new com.google.gdata.client.analytics.AnalyticsService("ga-service-v1.0");
//    login(config.getUsername(), config.getPassword());
//  }

  @Override
  public AnalyticsQuery createQuery() {
    return new GoogleAnalyticsQueryImpl();
  }

  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static Calendar calendar = Calendar.getInstance();

  private static String parseDate(String date) { //valid formats: today,today-20,2011-01-01
    date = date.trim();
    if (date.equalsIgnoreCase("today"))
      return dateFormat.format(new Date());
    else if (date.startsWith("today-")) {
      calendar.setTime(new Date());
      calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(date.substring(date.indexOf("-")).trim()));
      return dateFormat.format(calendar.getTime());
    }
    return date;
  }

  @Override
  public AnalyticsReportTO query(AnalyticsQuery query) {
    try {
      GoogleAnalyticsQueryImpl gaQuery = (GoogleAnalyticsQueryImpl) query;
      AnalyticsService service = new AnalyticsService("csga-service-v1.0");
      service.setUserCredentials(gaQuery.getUsername(), gaQuery.getPassword());

      DataQuery dq = new DataQuery(new URL(DATA_URL));
      dq.setIds(gaQuery.getViewId());
      dq.setStartDate(parseDate(gaQuery.getStartDate()));
      dq.setEndDate(parseDate(gaQuery.getEndDate()));
      dq.setDimensions(gaQuery.getDimensionsAsString());
      dq.setMetrics(gaQuery.getMetricsAsString());
      dq.setFilters(gaQuery.getFilters());
      dq.setSort(gaQuery.getSort());

      DataFeed df = service.getFeed(dq, DataFeed.class);

      AnalyticsReportTO.Entry gare;
      AnalyticsReportTO gar = new AnalyticsReportTO();
      for (DataEntry de : df.getEntries()) {
        gare = new AnalyticsReportTO.Entry();
        for (String dimension : gaQuery.getDimensions())
          gare.put(dimension, de.stringValueOf(dimension));
        for (String metric : gaQuery.getMetrics())
          gare.put(metric, de.stringValueOf(metric));
        gar.addEntry(gare);
      }
      return gar;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ServiceException e) {
      e.printStackTrace();
    }
    return null;
  }

//  public boolean isLoggedIn() {
//    return isLoggedIn;
//  }
//
//  public boolean login(String username, String password) {
//    try {
//      service.setUserCredentials(username, password);
//      return isLoggedIn = true;
//    } catch (AuthenticationException ae) {
//      ae.printStackTrace();
//    }
//    return isLoggedIn = false;
//  }
//
//  public AccountFeed getAvailableAccounts() {
//    try {
//      return service.getFeed(new URL(ACCOUNTS_URL), AccountFeed.class);
//    } catch (ServiceException e) {
//      e.printStackTrace();
//    } catch (MalformedURLException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return null;
//  }
//
//  public List<AccountEntry> getAvailableProfiles() {
//    AccountFeed af = getAvailableAccounts();
//    if (af != null)
//      return af.getEntries();
//    return null;
//  }
//
//  public DataQuery createQuery(String tableId, String startDate, String endDate, String dimensions, String metrics) {
//    try {
//      DataQuery query = new DataQuery(new URL(DATA_URL));
//      query.setIds(tableId);
//      query.setStartDate(startDate);
//      query.setEndDate(endDate);
//      query.setDimensions(dimensions);
//      query.setMetrics(metrics);
//      return query;
//    } catch (MalformedURLException e) {
//      e.printStackTrace();
//    }
//    return null;
//  }
//
//  public DataQuery createSortedQuery(String tableId, String startDate, String endDate, String dimensions, String metrics, String sort) {
//    DataQuery dq = createQuery(tableId, startDate, endDate, dimensions, metrics);
//    if (dq != null) {
//      dq.setSort(sort);
//      return dq;
//    }
//    return null;
//  }
//
//  public DataQuery createFilteredQuery(String tableId, String startDate, String endDate, String dimensions, String metrics, String filters) {
//    DataQuery dq = createQuery(tableId, startDate, endDate, dimensions, metrics);
//    if (dq != null) {
//      dq.setFilters(filters);
//      return dq;
//    }
//    return null;
//  }
//
//  public DataQuery createFilteredSortedQuery(String tableId, String startDate, String endDate, String dimensions, String metrics, String filters, String sort) {
//    DataQuery dq = createQuery(tableId, startDate, endDate, dimensions, metrics);
//    if (dq != null) {
//      dq.setFilters(filters);
//      dq.setSort(sort);
//      return dq;
//    }
//    return null;
//  }
//
//  public DataFeed getFeed(DataQuery dataQuery) {
//    try {
//      return service.getFeed(dataQuery, DataFeed.class);
//    } catch (IOException e) {
//      e.printStackTrace();
//    } catch (ServiceException e) {
//      e.printStackTrace();
//    }
//    return null;
//  }
//
//  public AnalyticsReportTO query(String tableId, String startDate, String endDate, String[] dimensions, String[] metrics) {
//    DataQuery query = createQuery(tableId, startDate, endDate, arrayToString(dimensions), arrayToString(metrics));
//    if (query != null) {
//      DataFeed feed = getFeed(query);
//      if (feed != null) {
//        AnalyticsReportTO.Entry entry;
//        AnalyticsReportTO report = new AnalyticsReportTO();
//        for (DataEntry de : feed.getEntries()) {
//          entry = new AnalyticsReportTO.Entry();
//          for (String dimension : dimensions)
//            entry.put(dimension, de.stringValueOf(dimension));
//          for (String metric : metrics)
//            entry.put(metric, de.stringValueOf(metric));
//          report.addEntry(entry);
//        }
//        return report;
//      }
//    }
//    return null;
//  }
//
//  public AnalyticsReportTO query(String siteId, String reportId) {
//    AnalyticsConfigTO.Site.Report report = config.getReport(siteId, reportId);
//    if (report != null) {
//      try {
//        DataQuery query = new DataQuery(new URL(DATA_URL));
//        query.setIds(report.getTableId());
//        query.setStartDate(report.getStartDate());
//        query.setEndDate(report.getEndDate());
//        query.setDimensions(report.getQuery().getDimensionsAsString());
//        query.setMetrics(report.getQuery().getMetricsAsString());
//        query.setFilters(report.getQuery().getFiltersAsString());
//        query.setSort(report.getQuery().getSortsAsString());
//
//        DataFeed feed = service.getFeed(query, DataFeed.class);
//
//        AnalyticsReportTO.Entry gare;
//        AnalyticsReportTO gar = new AnalyticsReportTO();
//        for (DataEntry de : feed.getEntries()) {
//          gare = new AnalyticsReportTO.Entry();
//          for (String dimension : report.getQuery().getDimensions())
//            gare.put(dimension, de.stringValueOf(dimension));
//          for (String metric : report.getQuery().getMetrics())
//            gare.put(metric, de.stringValueOf(metric));
//          gar.addEntry(gare);
//        }
//        return gar;
//      } catch (MalformedURLException e) {
//        e.printStackTrace();
//      } catch (ServiceException e) {
//        e.printStackTrace();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//    return null;
//  }
//
//  private String getSingleMetric(String tableId, String startDate, String endDate, String metric) {
//    try {
//      DataQuery query = new DataQuery(new URL(DATA_URL));
//      query.setIds(tableId);
//      query.setStartDate(startDate);
//      query.setEndDate(endDate);
//      query.setMetrics(metric);
//      DataFeed feed = service.getFeed(query, DataFeed.class);
//      List<DataEntry> entries = feed.getEntries();
//      if (entries != null && !entries.isEmpty())
//        return entries.get(0).stringValueOf(metric);
//    } catch (ServiceException e) {
//      e.printStackTrace();
//    } catch (MalformedURLException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return null;
//  }
//
//  public long getVisitors(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_VISITORS);
//    if (value != null)
//      return Long.parseLong(value);
//    return 0;
//  }
//
//  public long getNewVisits(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_NEW_VISITS);
//    if (value != null)
//      return Long.parseLong(value);
//    return 0;
//  }
//
//  public double getPercentNewVisits(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_PERCENT_NEW_VISITS);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public long getVisits(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_VISITS);
//    if (value != null)
//      return Long.parseLong(value);
//    return 0;
//  }
//
//  public double getTimeOnSite(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_TIME_ON_SITE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getAvgTimeOnSite(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_AVG_TIME_ON_SITE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//
//  public double getEntrances(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_ENTRANCES);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getEntranceRate(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_ENTRANCE_RATE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getBounces(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_BOUNCES);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getBounceRate(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_ENTRANCE_BOUNCE_RATE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getVisitBounceRate(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_VISIT_BOUNCE_RATE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getPageviews(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_PAGEVIEWS);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getPageviewsPerVisit(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_PAGEVIEWS_PER_VISIT);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getUniquePageviews(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_UNIQUE_PAGEVIEWS);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getTimeOnPage(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_TIME_ON_PAGE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getAvgTimeOnPage(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_AVG_TIME_ON_PAGE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getExits(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_EXITS);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getExitRate(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_EXIT_RATE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getPageLoadSample(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_PAGE_LOAD_SAMPLE);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getPageLoadTime(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_PAGE_LOAD_TIME);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
//
//  public double getAvgPageLoadTime(String tableId, String startDate, String endDate) {
//    String value = getSingleMetric(tableId, startDate, endDate, METRIC_AVG_PAGE_LOAD_TIME);
//    if (value != null)
//      return Double.parseDouble(value);
//    return 0;
//  }
}
