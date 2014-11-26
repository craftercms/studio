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
package org.craftercms.cstudio.alfresco.activityfeed;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.service.api.ActivityService;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.to.TableIndexCheckTO;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CStudioActivityFeedDaoServiceImpl implements CStudioActivityFeedDaoService
{
    private static final String STATEMENT_UPDATE_URL = "customactivityfeed.updateurl";

	private static final String STATEMENT_DELETE_OLD_ACTIVITY = "customactivityfeed.deleteOldActivity";

	private static final Logger LOGGER = LoggerFactory.getLogger(CStudioActivityFeedDaoServiceImpl.class);

    private static final String STATEMENT_DELETE_ACTIVITIES_FOR_SITE = "customactivityfeed.deleteActivitiesForSite";
    private static final String STATEMENT_SELECT = "customactivityfeed.select";
    private static final String STATEMENT_SELECT_HIDE_LIVE = "customactivityfeed.selectHideLive";
    private static final String STATEMENT_SELECT_BY_CONTENT_TYPE = "customactivityfeed.selectByContentType";
    private static final String STATEMENT_SELECT_BY_CONTENT_TYPE_HIDE_LIVE = "customactivityfeed.selectByContentTypeHideLive";

    /** table check and creation **/
    private static final String STATEMENT_CREATE_TABLE = "customactivityfeed.createTable";
    private static final String STATEMENT_CHECK_TABLE_EXISTS = "customactivityfeed.checkTableExists";

    /** table indexes **/
    private static final String STATEMENT_ADD_USER_IDX = "customactivityfeed.addUserIndex";
    private static final String STATEMENT_CHECK_USER_IDX = "customactivityfeed.checkUserIndex";
    private static final String STATEMENT_ADD_SITE_IDX = "customactivityfeed.addSiteIndex";
    private static final String STATEMENT_CHECK_SITE_IDX = "customactivityfeed.checkSiteIndex";
    private static final String STATEMENT_ADD_CONTENT_IDX = "customactivityfeed.addContentIndex";
    private static final String STATEMENT_CHECK_CONTENT_IDX = "customactivityfeed.checkContentIndex";

	protected String activityFeedTableName;
	protected SqlMapClient sqlMapper;
    
    public void setSqlMapClient(SqlMapClient sqlMapper)
    {
        this.sqlMapper = sqlMapper;
    }
    
    public SqlMapClient getSqlMapClient()
    {
        return this.sqlMapper;
    }

    public String getActivityFeedTableName() {
		return activityFeedTableName;
	}

	public void setActivityFeedTableName(String activityFeedTableName) {
		this.activityFeedTableName = activityFeedTableName;
	}

    protected String initializeScriptPath;
    public String getInitializeScriptPath() {
        return initializeScriptPath;
    }
    public void setInitializeScriptPath(String initializeScriptPath) {
        this.initializeScriptPath = initializeScriptPath;
    }

    @Override
    public void initIndexes() {
        DataSource dataSource = sqlMapper.getDataSource();
        Connection connection = null;
        int oldval = -1;
        try {
            connection = dataSource.getConnection();
            oldval = connection.getTransactionIsolation();
            if (oldval != Connection.TRANSACTION_READ_COMMITTED) {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }

            List<HashMap> checkTable = sqlMapper.queryForList(STATEMENT_CHECK_TABLE_EXISTS);
            if (checkTable == null || checkTable.size() < 1) {
                ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);
                scriptRunner.runScript(Resources.getResourceAsReader(initializeScriptPath));
            }
            connection.commit();
            List<TableIndexCheckTO> indexCheckResult = sqlMapper.queryForList(STATEMENT_CHECK_USER_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                sqlMapper.insert(STATEMENT_ADD_USER_IDX);
            }
            indexCheckResult = sqlMapper.queryForList(STATEMENT_CHECK_SITE_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                sqlMapper.insert(STATEMENT_ADD_SITE_IDX);
            }
            indexCheckResult = sqlMapper.queryForList(STATEMENT_CHECK_CONTENT_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                sqlMapper.insert(STATEMENT_ADD_CONTENT_IDX);
            }
            connection.commit();
            if (oldval != -1) {
                connection.setTransactionIsolation(oldval);
            }
        } catch (SQLException e) {
            LOGGER.error("Error while initializing CStudio Activity DB indexes.", e);
        } catch (IOException e) {
            LOGGER.error("Error while initializing CStudio Activity DB indexes.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    public long postFeedEntry(CStudioActivityFeedDAO activityFeed) throws SQLException
    {
		int count=getCountUserContentFeedEntries(activityFeed.getUserId(),activityFeed.getSiteNetwork(),activityFeed.getContentId());
		if(count==0)
		{
			activityFeed.setCreationDate(new Date());
			return insertFeedEntry(activityFeed);
		}
		else
		{
			updateFeedEntry(activityFeed);
			return -1;
		}
    }
	
	public long insertFeedEntry(CStudioActivityFeedDAO activityFeed) throws SQLException
    {
		SqlMapClient sqlClient=getSqlMapClient();
        Long id = (Long)sqlClient.insert("customactivityfeed.insert", activityFeed);
        return (id != null ? id : -1);
    }

	@Override
    public void updateUrl(String oldUrl,String newUrl,String site) throws SQLException
    {
    	SqlMapClient sqlClient=getSqlMapClient();
    	sqlClient.startTransaction();
    	HashMap<String,String> params = new HashMap<String,String>();
    	try{
    		params.put("site",site);
      	    params.put("newUrl",newUrl);
    		sqlClient.delete(STATEMENT_DELETE_OLD_ACTIVITY,params);
    		params.clear();
    		params.put("site",site);
     	    params.put("oldUrl",oldUrl);
     	    params.put("newUrl",newUrl);
    	    sqlClient.update(STATEMENT_UPDATE_URL,params);
    		sqlClient.commitTransaction();
    	}catch(SQLException ex){
    		LOGGER.error("Unable to update url ",ex);
    	}finally{
    		sqlClient.endTransaction();
    	}
  
    }
    
    public void updateSummary(String site,String url,String newSummary) throws SQLException
    {
    	
    	SqlMapClient sqlClient=getSqlMapClient();
    	CStudioActivityFeedDAO activityFeed= new CStudioActivityFeedDAO();
    	activityFeed.setModifiedDate(new Date());
    	activityFeed.setSiteNetwork(site);
    	activityFeed.setContentId(url);
    	activityFeed.setSummary(newSummary);    	
    	sqlClient.update("customactivityfeed.updatesummary",activityFeed);

    }


    public void updateFeedEntry(CStudioActivityFeedDAO activityFeed) throws SQLException
    {
    	SqlMapClient sqlClient=getSqlMapClient();
    	sqlClient.update("customactivityfeed.updatefeedentry",activityFeed);

    }

    public int deleteFeedEntries(Date keepDate) throws SQLException
    {
    	SqlMapClient sqlClient=getSqlMapClient();
        return sqlClient.delete("customactivityfeed.delete", keepDate);
    }

    public List<CStudioActivityFeedDAO> selectUserFeedEntries(String feedUserId, String format, String siteId,int startPos, int feedSize,String contentType, boolean hideLiveItems) throws SQLException
    {
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("userId",feedUserId);
        params.put("summaryFormat",format);
        params.put("siteNetwork",siteId);
        params.put("startPos", startPos);
        params.put("feedSize", feedSize);
        SqlMapClient sqlClient=getSqlMapClient();
        String queryName = STATEMENT_SELECT;
        if (hideLiveItems) {
            queryName = STATEMENT_SELECT_HIDE_LIVE;
            List<String> statesValues = new ArrayList<String>();
            for (ObjectStateService.State state : ObjectStateService.State.LIVE_STATES) {
                statesValues.add(state.name());
            }
            params.put("states", statesValues);
        }

        if(StringUtils.isNotEmpty(contentType) && !contentType.toLowerCase().equals("all")){
            params.put("contentType",contentType);
            if (hideLiveItems) {
                queryName = STATEMENT_SELECT_BY_CONTENT_TYPE_HIDE_LIVE;
            } else {
                queryName = STATEMENT_SELECT_BY_CONTENT_TYPE;
            }
        }

        // where feed user is me and post user is not me
        return (List<CStudioActivityFeedDAO>)sqlClient.queryForList(queryName, params);
    }
    
    @SuppressWarnings("unchecked")
    public int getCountUserContentFeedEntries(String feedUserId, String siteId,String contentId) throws SQLException
    {
    	HashMap<String,String> params = new HashMap<String,String>();
        params.put("userId",feedUserId);
        params.put("contentId",contentId);
        params.put("siteNetwork",siteId);

        SqlMapClient sqlClient=getSqlMapClient();
        // where feed user is me and post user is not me
        return (Integer)sqlClient.queryForObject("customactivityfeed.getCountUserContentFeedEntries", params);
    }

    /*
     * (non-Javadoc)
     * @see org.craftercms.cstudio.alfresco.activityfeed.CStudioActivityFeedDaoService#getLastActor(java.lang.String, java.lang.String, java.lang.String)
     */
	public String getLastActor(String site, String contentId, String activityType) throws SQLException {
    	HashMap<String,String> params = new HashMap<String,String>();
        params.put("contentId", contentId);
        params.put("siteNetwork", site);
        String queryType = "customactivityfeed.getLastActor";
        if (!StringUtils.isEmpty(activityType)) {
        	params.put("activityType", activityType);
        	queryType = "customactivityfeed.getLastActorByActivityType";
        }

        SqlMapClient sqlClient = getSqlMapClient();
        // where feed user is me and post user is not me
        CStudioActivityFeedDAO feed = (CStudioActivityFeedDAO) sqlClient.queryForObject(queryType, params);
        if (feed != null) {
        	return feed.getUserId(); 
        } else {
        	if (LOGGER.isDebugEnabled()) {
        		LOGGER.debug("[ACTIVITY] no feed found by [site: " + site 
        				+ ", contentId: " + contentId + ", activityType: " + activityType +"]");
        	}
        	return null;
        }
	}

    @Override
    public void deleteActivitiesForSite(String site) {
        try {
            LOGGER.info("Deleting activities for site " + site);
            this.sqlMapper.delete(STATEMENT_DELETE_ACTIVITIES_FOR_SITE, site);
        } catch (SQLException e) {
            LOGGER.error("Error while deleting activities for site " + site, e);
        }
    }

    @Override
    public CStudioActivityFeedDAO getDeletedActivity(String site, String contentId) throws SQLException {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("contentId", contentId);
        params.put("siteNetwork", site);
        String activityType = ContentUtils.generateActivityValue(ActivityService.ActivityType.DELETED);
        params.put("activityType", activityType);
        String queryType = "customactivityfeed.getDeletedActivity";

        SqlMapClient sqlClient = getSqlMapClient();
        List<CStudioActivityFeedDAO> feed = (List<CStudioActivityFeedDAO>) sqlClient.queryForList(queryType, params);
        if (feed != null && feed.size() > 0) {
            return feed.get(0);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ACTIVITY] no feed found by [site: " + site
                        + ", contentId: " + contentId + ", activityType: " + activityType +"]");
            }
            return null;
        }
    }
}
