/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v1.service.search;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.search.SearchService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.PREVIEW_SEARCH_CREATE_URL;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.PREVIEW_SEARCH_DELETE_URL;

/**
 * Created by Sumer Jabri on 2/15/17.
 */
public class SearchServiceImpl implements SearchService {

	private final static Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

	protected StudioConfiguration studioConfiguration;
	protected CloseableHttpClient httpClient;

	public SearchServiceImpl() {
		RequestConfig requestConfig = RequestConfig.custom().setExpectContinueEnabled(true).build();
		httpClient = HttpClientBuilder.create()
			.setConnectionManager(new PoolingHttpClientConnectionManager())
			.setDefaultRequestConfig(requestConfig)
			.build();
	}

	@Override
    @ValidateParams
	public void createIndex(@ValidateStringParam(name = "siteId") final String siteId) throws ServiceException {
		logger.info("Creating search index for site:" + siteId);
		String requestUrl = studioConfiguration.getProperty(PREVIEW_SEARCH_CREATE_URL);

		HttpPost postRequest = new HttpPost(requestUrl);
		String rqBody = "{ \"id\" : \"" + siteId + "\" }";  // TODO: SJ: Replace this with something better
		HttpEntity requestEntity = null;

		requestEntity = new StringEntity(rqBody, ContentType.APPLICATION_JSON);
		postRequest.setEntity(requestEntity);

		// TODO: SJ: Review exception handling
		try {
			CloseableHttpResponse response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				throw new ServiceException("Error while creating search index for site " + siteId + ". Request URL: "
					+ requestUrl + ". Request Body: " + rqBody + ". Response: "
					+ IOUtils.toString(response.getEntity().getContent()));
			}
		} catch (IOException e) {
			logger.error("Error while creating search index for site " + siteId, e);
			throw new ServiceException("Error while creating search index for site " + siteId, e);
		} finally {
			postRequest.releaseConnection();
		}
	}

	@Override
    @ValidateParams
	public void deleteIndex(@ValidateStringParam(name = "siteId") final String siteId) throws ServiceException {
		logger.debug("Deleting search index for site:" + siteId);

		String requestUrl = studioConfiguration.getProperty(PREVIEW_SEARCH_DELETE_URL);
		requestUrl = requestUrl.replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, siteId);

		logger.debug("Deleting search index for site:" + siteId + "URL: " + requestUrl);

		HttpPost postRequest = new HttpPost(requestUrl);
		String rqBody = "{ \"delete_mode\": \"ALL_DATA_AND_CONFIG\" }";  // TODO: SJ: Replace this with something better
		HttpEntity requestEntity = null;

		logger.debug("Deleting search index for site:" + siteId + " using URL: " + requestUrl + " with body: " +
			rqBody);

		requestEntity = new StringEntity(rqBody, ContentType.APPLICATION_JSON);
		postRequest.setEntity(requestEntity);

		// TODO: SJ: Review exception handling
		try {
			CloseableHttpResponse response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				throw new ServiceException("Error while deleting search index for site " + siteId + ". Request URL: "
					+ requestUrl + ". Request Body: " + rqBody + ". Response: "
					+ IOUtils.toString(response.getEntity().getContent()));
			}

			logger.info("Deleted search index for site:" + siteId + ". HTTP Status Code: " +
				response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			logger.error("Error while deleting search index for site " + siteId, e);
			throw new ServiceException("Error while deleting search index for site " + siteId, e);
		} finally {
			postRequest.releaseConnection();
		}
	}

	public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
	public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

}
