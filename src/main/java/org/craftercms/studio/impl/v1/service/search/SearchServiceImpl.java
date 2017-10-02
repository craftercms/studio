package org.craftercms.studio.impl.v1.service.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
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

	@Override
    @SuppressWarnings("deprecation")
	public void createIndex(final String siteId) throws ServiceException {
		logger.info("Creating search index for site:" + siteId);
		String requestUrl = studioConfiguration.getProperty(PREVIEW_SEARCH_CREATE_URL);

		HttpPost postMethod = new HttpPost(requestUrl);
		postMethod.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
		String rqBody = "{ \"id\" : \"" + siteId + "\" }";  // TODO: SJ: Replace this with something better
		EntityBuilder requestEntity = EntityBuilder.create();
        requestEntity.setText(rqBody)
                .setContentType(ContentType.APPLICATION_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.displayName());
		postMethod.setEntity(requestEntity.build());

		// TODO: SJ: Review exception handling
		CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
		try {
			response = client.execute(postMethod);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				throw new ServiceException("Error while creating search index for site " + siteId + ". Request URL: "
					+ requestUrl + ". Request Body: " + rqBody + ". Response: "
					+ EntityUtils.toString(response.getEntity()));
			}
		} catch (IOException e) {
			logger.error("Error while creating search index for site " + siteId, e);
			throw new ServiceException("Error while creating search index for site " + siteId, e);
		} finally {
			postMethod.releaseConnection();
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.info("Error while closing http response", e );
            }
            try {
                client.close();
            } catch (IOException e) {
                logger.info("Error while closing http client", e );
            }
		}
	}

	@Override
    @SuppressWarnings("deprecation")
	public void deleteIndex(final String siteId) throws ServiceException {
		logger.debug("Deleting search index for site:" + siteId);

		String requestUrl = studioConfiguration.getProperty(PREVIEW_SEARCH_DELETE_URL);
		requestUrl = requestUrl.replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, siteId);

		logger.debug("Deleting search index for site:" + siteId + "URL: " + requestUrl);

		HttpPost postMethod = new HttpPost(requestUrl);
		postMethod.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
		String rqBody = "{ \"delete_mode\": \"ALL_DATA_AND_CONFIG\" }";  // TODO: SJ: Replace this with something better
		EntityBuilder requestEntity = EntityBuilder.create();

		logger.debug("Deleting search index for site:" + siteId + " using URL: " + requestUrl + " with body: " +
			rqBody);

        requestEntity.setText(rqBody)
                .setContentType(ContentType.APPLICATION_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.displayName());
        postMethod.setEntity(requestEntity.build());

		// TODO: SJ: Review exception handling
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			response = client.execute(postMethod);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				throw new ServiceException("Error while deleting search index for site " + siteId + ". Request URL: "
					+ requestUrl + ". Request Body: " + rqBody + ". Response: "
					+ EntityUtils.toString(response.getEntity()));
			}

			logger.info("Deleted search index for site:" + siteId + ". HTTP Status Code: " + response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			logger.error("Error while deleting search index for site " + siteId, e);
			throw new ServiceException("Error while deleting search index for site " + siteId, e);
		} finally {
			postMethod.releaseConnection();
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.info("Error while closing http response", e );
            }
            try {
                client.close();
            } catch (IOException e) {
                logger.info("Error while closing http client", e );
            }
		}
	}

	public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
	public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

	protected StudioConfiguration studioConfiguration;
}
