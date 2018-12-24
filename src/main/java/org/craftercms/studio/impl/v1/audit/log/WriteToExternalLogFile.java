package org.craftercms.studio.impl.v1.audit.log;

import org.apache.log4j.xml.DOMConfigurator;
import org.craftercms.studio.api.v1.dal.AuditFeed;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

/**
 * @author mhashmath
 *
 */
public class WriteToExternalLogFile {

	private static final Logger logger = LoggerFactory.getLogger(WriteToExternalLogFile.class);
	private static final org.apache.log4j.Logger auditLogger = org.apache.log4j.Logger.getLogger("auditLogs");

	public long WriteToFile(AuditFeed activityFeed, String logPath, String logFileName, String log4jPath) {

		try {
			StringBuffer data = new StringBuffer();

			if (!activityFeed.getSiteNetwork().equals("studio_root")) {
				System.setProperty("auditLogPath", logPath + "/" + activityFeed.getSiteNetwork());
				System.setProperty("auditFileName", logFileName);

				DOMConfigurator.configure(log4jPath);

				data.append(activityFeed.getType() + ", ");
				data.append(activityFeed.getSummary() + ", ");
				data.append(activityFeed.getSummaryFormat() + ", ");
				data.append(activityFeed.getUserId() + ", ");
				data.append(activityFeed.getCreationDate() + ", ");
				data.append(activityFeed.getModifiedDate() + ", ");
				data.append(activityFeed.getSiteNetwork() + ", ");
				data.append(activityFeed.getContentId() + ", ");
				data.append(activityFeed.getContentType() + ", ");
				data.append(activityFeed.getSource());

				auditLogger.info(data.toString());

			}

		} catch (Exception e) {
			logger.error("Error while writing audit log to file " + e);
			return -1;
		}

		return 1;
	}
}
