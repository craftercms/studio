package org.craftercms.studio.api.v2.upgrade;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.springframework.jdbc.core.JdbcTemplate;

// Provides access to subsystems
public interface UpgradeContext {


    String getCurrentVersion();

    String getTargetVersion();

    Connection getConnection() throws SQLException;

    ContentRepository getContentRepository();

    List<String> getSites();

}
