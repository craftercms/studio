package org.craftercms.studio.impl.v2.upgrade;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.upgrade.UpgradeContext;

public class DefaultUpgradeContext implements UpgradeContext {

    protected String currentVersion;
    protected String targetVersion;

    protected List<String> sites;

    protected DataSource dataSource;
    protected ContentRepository contentRepository;

    @Override
    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(final String currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(final String targetVersion) {
        this.targetVersion = targetVersion;
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    @Override
    public List<String> getSites() {
        return sites;
    }

    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setSites(final List<String> sites) {
        this.sites = sites;
    }

}
