package org.craftercms.studio.impl.v1.service.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.entity.ContentType;
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

	@Override
	public void createIndex(final String siteId) throws ServiceException {
		String requestUrl = studioConfiguration.getProperty(PREVIEW_SEARCH_CREATE_URL);

		PostMethod postMethod = new PostMethod(requestUrl);
		postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
		String rqBody = "{ \"id\" : \"" + siteId + "\" }";  // TODO: SJ: Replace this with something better
		RequestEntity requestEntity = null;

		try {
			requestEntity = new StringRequestEntity(rqBody, ContentType.APPLICATION_JSON.toString(), StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException e) {
			logger.info("Unsupported encoding for request body. Using deprecated method instead.");
		}
		if (requestEntity != null) {
			postMethod.setRequestEntity(requestEntity);
		} else {
			postMethod.setRequestBody(rqBody);
		}

		// TODO: SJ: Review exception handling
		HttpClient client = new HttpClient();
		try {
			int status = client.executeMethod(postMethod);
			if (status != HttpStatus.SC_CREATED) {
				throw new ServiceException("Error while creating search index for site " + siteId + ". Response: "
					+ postMethod.getResponseBodyAsString());
			}
		} catch (IOException e) {
			logger.error("Error while creating search index for site " + siteId, e);
			throw new ServiceException("Error while creating search index for site " + siteId, e);
		} finally {
			postMethod.releaseConnection();
		}
	}

	@Override
	public void deleteIndex(final String siteId) throws ServiceException {
		String requestUrl = studioConfiguration.getProperty(PREVIEW_SEARCH_DELETE_URL);

		PostMethod postMethod = new PostMethod(requestUrl);
		postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
		String rqBody = "{ \"id\" : \"" + siteId + "\" }";  // TODO: SJ: Replace this with something better
		RequestEntity requestEntity = null;

		try {
			requestEntity = new StringRequestEntity(rqBody, ContentType.APPLICATION_JSON.toString(), StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException e) {
			logger.info("Unsupported encoding for request body. Using deprecated method instead.");
		}
		if (requestEntity != null) {
			postMethod.setRequestEntity(requestEntity);
		} else {
			postMethod.setRequestBody(rqBody);
		}

		// TODO: SJ: Review exception handling
		HttpClient client = new HttpClient();
		try {
			int status = client.executeMethod(postMethod);
			if (status != HttpStatus.SC_NO_CONTENT) {
				throw new ServiceException("Error while deleting search index for site " + siteId + ". Response: "
					+ postMethod.getResponseBodyAsString());
			}
		} catch (IOException e) {
			logger.error("Error while deleting search index for site " + siteId, e);
			throw new ServiceException("Error while deleting search index for site " + siteId, e);
		} finally {
			postMethod.releaseConnection();
		}
	}

	public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
	public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

	protected StudioConfiguration studioConfiguration;
}
