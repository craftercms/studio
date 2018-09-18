package org.craftercms.studio.api.v2.upgrade;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

// Provides access to subsystems
public interface UpgradeContext {


    String getCurrentVersion();

    String getTargetVersion();

    Connection getConnection() throws SQLException;

    ContentRepository getContentRepository();

    List<String> getSites();

    String getProperty(String key);

    Resource getServletResource(String path);

    void writeToRepo(String site, String path, InputStream content, String message);
}
