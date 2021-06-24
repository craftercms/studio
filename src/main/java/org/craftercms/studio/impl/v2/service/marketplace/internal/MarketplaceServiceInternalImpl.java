/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.marketplace.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.monitoring.VersionInfo;
import org.craftercms.commons.plugin.PluginDescriptorReader;
import org.craftercms.commons.plugin.model.Installation;
import org.craftercms.commons.plugin.model.Plugin;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.plugin.model.Version;
import org.craftercms.commons.rest.RestTemplate;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.exception.marketplace.IncompatiblePluginException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceNotInitializedException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceRegistryException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceUnreachableException;
import org.craftercms.studio.api.v2.exception.marketplace.PluginAlreadyInstalledException;
import org.craftercms.studio.api.v2.exception.marketplace.PluginInstallationException;
import org.craftercms.studio.api.v2.exception.marketplace.PluginNotFoundException;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.marketplace.Constants;
import org.craftercms.studio.api.v2.service.marketplace.MarketplacePlugin;
import org.craftercms.studio.api.v2.service.marketplace.Paths;
import org.craftercms.studio.api.v2.service.marketplace.PluginTreeCopier;
import org.craftercms.studio.api.v2.service.marketplace.internal.MarketplaceServiceInternal;
import org.craftercms.studio.api.v2.service.marketplace.registry.FileRecord;
import org.craftercms.studio.api.v2.service.marketplace.registry.PluginRecord;
import org.craftercms.studio.api.v2.service.marketplace.registry.PluginRegistry;
import org.craftercms.studio.api.v2.service.site.internal.SitesServiceInternal;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.marketplace.CreateSiteRequest;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.transform.TransformerException;
import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.apache.commons.lang.text.StrSubstitutor.replace;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.service.marketplace.Constants.SOURCE_GIT;
import static org.craftercms.studio.impl.v2.utils.XsltUtils.executeTemplate;

/**
 * Default implementation of {@link MarketplaceServiceInternal} that proxies all request to the configured Marketplace
 *
 * @author joseross
 * @since 3.1.2
 */
public class MarketplaceServiceInternalImpl implements MarketplaceServiceInternal, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MarketplaceServiceInternalImpl.class);

    public static final String INSTALLABLE_TYPES_CONFIG_KEY = "studio.marketplace.plugin.installable";

    public static final String FOLDER_MAPPING_CONFIG_KEY = "studio.marketplace.plugin.mapping";

    public static final String WIDGET_MAPPING_CONFIG_KEY = "studio.marketplace.plugin.wire.mapping";

    public static final String TEMPLATE_MAPPING_CONFIG_KEY = "studio.marketplace.plugin.template.mapping";

    public static final String MODULE_CONFIG_KEY = "module";

    public static final String PATH_CONFIG_KEY = "path";

    public static final String TEMPLATE_CONFIG_KEY = "template";

    public static final String SYSTEM_PATH_KEY = "systemPath";

    public static final String PLUGIN_PATTERN_KEY = "pluginPattern";

    public static final String PARAM_PARENT_ID = "parentId";

    public static final String PARAM_NEW_XML = "newXml";

    public static final String PARAM_PARENT_XPATH = "parentXpath";

    public static final String PARAM_PLUGIN_ID = "pluginId";

    public static final String PARAM_PLUGIN_PATH = "pluginPath";

    protected final InstanceService instanceService;

    protected final SiteService siteService;

    protected final SitesServiceInternal sitesServiceInternal;

    protected final ContentService contentService;

    protected final ConfigurationService configurationService;

    protected final StudioConfiguration studioConfiguration;

    protected final GitRepositoryHelper gitRepositoryHelper;

    protected final PluginDescriptorReader pluginDescriptorReader;

    protected final String pluginDescriptorFilename;

    protected final RestTemplate restTemplate = new RestTemplate();

    protected ObjectMapper mapper;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected final Lock readLock = lock.readLock();

    protected final Lock writeLock = lock.writeLock();

    /**
     * The custom HTTP headers to sent with all requests
     */
    protected HttpHeaders httpHeaders;

    /**
     * The current Crafter CMS version, sent with all requests
     */
    protected String version;

    /**
     * The current Crafter CMS edition, sent with all requests
     */
    protected String edition;

    /**
     * The Marketplace URL to use
     */
    protected String url;

    /**
     * The path of the plugin registry in the site
     */
    protected String pluginRegistryPath;

    /**
     * Indicates if the search should include plugins pending of approval
     */
    protected boolean showPending = false;

    /**
     * List of plugin types that can be installed
     */
    protected List<String> installableTypes = Collections.emptyList();

    /**
     * Folder mappings to use during plugin installation
     */
    protected final Map<String, String> folderMapping = new HashMap<>();

    /**
     * Name of the folder to copy all plugin files
     */
    protected String pluginsFolder;

    /**
     * Mapping used to wire plugin files into the site configuration
     */
    protected HierarchicalConfiguration<?> widgetMapping;

    /**
     * Mapping used to wire plugin files into the site templates
     */
    protected Map<String, String> templateMapping;

    /**
     * Code injected in the templates for each plugin
     */
    protected String templateCode;

    /**
     * Comment added in the templates for each plugin
     */
    protected String templateComment;

    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    @ConstructorProperties({ "instanceService", "siteService", "sitesServiceInternal", "contentService",
            "configurationService", "studioConfiguration", "pluginDescriptorReader", "gitRepositoryHelper",
            "pluginDescriptorFilename", "templateCode", "templateComment", "retryingRepositoryOperationFacade" })
    public MarketplaceServiceInternalImpl(InstanceService instanceService, SiteService siteService,
                                          SitesServiceInternal sitesServiceInternal, ContentService contentService,
                                          ConfigurationService configurationService,
                                          StudioConfiguration studioConfiguration,
                                          PluginDescriptorReader pluginDescriptorReader,
                                          GitRepositoryHelper gitRepositoryHelper, String pluginDescriptorFilename,
                                          String templateCode, String templateComment,
                                          RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.instanceService = instanceService;
        this.siteService = siteService;
        this.sitesServiceInternal = sitesServiceInternal;
        this.contentService = contentService;
        this.configurationService = configurationService;
        this.studioConfiguration = studioConfiguration;
        this.pluginDescriptorReader = pluginDescriptorReader;
        this.gitRepositoryHelper = gitRepositoryHelper;
        this.pluginDescriptorFilename = pluginDescriptorFilename;
        this.templateCode = templateCode;
        this.templateComment = templateComment;
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setPluginRegistryPath(String pluginRegistryPath) {
        this.pluginRegistryPath = pluginRegistryPath;
    }

    public void setShowPending(final boolean showPending) {
        this.showPending = showPending;
    }

    public void setPluginsFolder(final String pluginsFolder) {
        this.pluginsFolder = pluginsFolder;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        VersionInfo versionInfo = VersionInfo.getVersion(MarketplaceServiceInternalImpl.class);
        if (versionInfo == null) {
            logger.warn("Marketplace service could not be initialized");
            return;
        }
        String versionStr = versionInfo.getPackageVersion();

        // init version
        version = Version.getVersion(versionStr);
        edition = Version.getEdition(versionStr);

        // init headers
        httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER_STUDIO_ID, instanceService.getInstanceId());

        httpHeaders.set(HEADER_STUDIO_BUILD, versionInfo.getPackageBuild());
        httpHeaders.set(HEADER_STUDIO_VERSION, versionInfo.getPackageVersion());
        httpHeaders.set(HEADER_JAVA_VERSION, versionInfo.getJavaVersion());

        httpHeaders.set(HEADER_OS_NAME, versionInfo.getOsName());
        httpHeaders.set(HEADER_OS_VERSION, versionInfo.getOsVersion());
        httpHeaders.set(HEADER_OS_ARCH, versionInfo.getOsArch());

        installableTypes = studioConfiguration.getList(INSTALLABLE_TYPES_CONFIG_KEY, String.class);

        studioConfiguration.getSubConfigs(FOLDER_MAPPING_CONFIG_KEY).forEach(mapping ->
            mapping.getKeys().forEachRemaining(folder ->
                folderMapping.put(folder.replace("\\/", "/"), mapping.getString(folder)))
        );

        mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndRegisterModules();

        widgetMapping = studioConfiguration.getSubConfig(WIDGET_MAPPING_CONFIG_KEY);

        templateMapping = new HashMap<>();
        studioConfiguration.getSubConfigs(TEMPLATE_MAPPING_CONFIG_KEY).forEach(config ->
                templateMapping.put(config.getString(SYSTEM_PATH_KEY), config.getString(PLUGIN_PATTERN_KEY)));
    }

    @Override
    public Map<String, Object> searchPlugins(final String type, final String keywords, final boolean showIncompatible,
                                             final long offset, final long limit)
        throws MarketplaceException {

        validate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .path(Paths.PLUGIN_SEARCH)
            .queryParam(Constants.PARAM_VERSION, version)
            .queryParam(Constants.PARAM_EDITION, edition)
            .queryParam(Constants.PARAM_SHOW_PENDING, showPending)
            .queryParam(Constants.PARAM_SHOW_INCOMPATIBLE, showIncompatible)
            .queryParam(Constants.PARAM_OFFSET, offset)
            .queryParam(Constants.PARAM_LIMIT, limit);

        if (isNotEmpty(type)) {
            builder.queryParam(Constants.PARAM_TYPE, type);
        }

        if (isNotEmpty(keywords)) {
            builder.queryParam(Constants.PARAM_KEYWORDS, keywords);
        }

        HttpEntity<Void> request = new HttpEntity<>(null, httpHeaders);

        try {
            ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(builder.build().toString(), HttpMethod.GET, request,
                    new ParameterizedTypeReference<>() {});
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new MarketplaceUnreachableException(url, e);
        }
    }

    protected void validate() throws MarketplaceException {
        if (isEmpty(version)) {
            throw new MarketplaceNotInitializedException();
        }
    }

    protected MarketplacePlugin getDescriptor(String id, Version version)
        throws MarketplaceException {
        validate();

        logger.debug("Getting descriptor for plugin {0} v{1}", id, version);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .path(Paths.GET_PLUGIN)
            .pathSegment(id, version.toString())
            .queryParam(Constants.PARAM_SHOW_PENDING, showPending);

        HttpEntity<Void> request = new HttpEntity<>(null, httpHeaders);

        try {
            ResponseEntity<MarketplacePlugin> response =
                restTemplate.exchange(builder.build().toString(), HttpMethod.GET, request, MarketplacePlugin.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new MarketplaceUnreachableException(url, e);
        } catch (IllegalArgumentException e) {
            throw new PluginNotFoundException(format("Plugin not found in the Marketplace: %s v%s", id, version));
        }
    }

    @Override
    public void createSite(CreateSiteRequest request) throws RemoteRepositoryNotFoundException,
        InvalidRemoteRepositoryException, InvalidRemoteUrlException,
        ServiceLayerException, InvalidRemoteRepositoryCredentialsException {

        logger.debug("Creating site {0} from marketplace blueprint {1} v{2}",
                request.getSiteId(), request.getBlueprintId(), request.getBlueprintVersion());

        if (isEmpty(request.getSandboxBranch())) {
            logger.debug("Using default sandbox branch for site {0}", request.getSiteId());
            request.setSandboxBranch(studioConfiguration.getProperty(StudioConfiguration.REPO_SANDBOX_BRANCH));
        }

        if (isEmpty(request.getRemoteName())) {
            logger.debug("Using default remote name for site {0}", request.getSiteId());
            request.setRemoteName(studioConfiguration.getProperty(StudioConfiguration.REPO_DEFAULT_REMOTE_NAME));
        }

        MarketplacePlugin plugin = getDescriptor(request.getBlueprintId(), request.getBlueprintVersion());

        sitesServiceInternal.validateBlueprintParameters(PluginDescriptor.of(plugin), request.getSiteParams());

        siteService.createSiteWithRemoteOption(request.getSiteId(), request.getName(), request.getSandboxBranch(),
            request.getDescription(), request.getBlueprintId(), request.getRemoteName(),
            plugin.getUrl(), plugin.getRef(), false, RemoteRepository.AuthenticationType.NONE, null,
            null, null, null, StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_CLONE, request.getSiteParams(),
            true);

        logger.debug("Site creation complete for {0}", request.getSiteId());
    }

    @Override
    public List<PluginRecord> getInstalledPlugins(final String siteId) throws MarketplaceException {
        if (!contentService.contentExists(siteId, pluginRegistryPath)) {
            return Collections.emptyList();
        }
        return getPluginRegistry(siteId).getPlugins();
    }

    protected PluginRegistry getPluginRegistry(String siteId) throws MarketplaceRegistryException {
        logger.debug("Reading plugin registry for site {0}", siteId);
        if (!contentService.contentExists(siteId, pluginRegistryPath)) {
            logger.debug("Creating new plugin registry for site {0}", siteId);
            return new PluginRegistry();
        }
        readLock.lock();
        try (InputStream is = contentService.getContent(siteId, pluginRegistryPath)) {
            return mapper.readValue(is, PluginRegistry.class);
        } catch (ContentNotFoundException | IOException e) {
            throw new MarketplaceRegistryException("Error collecting installed plugins registry for site " + siteId, e);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void installPlugin(String siteId, String pluginId, Version pluginVersion) throws MarketplaceException {
        writeLock.lock();
        try {
            if (pluginAlreadyInstalled(siteId, pluginId)) {
                throw new PluginAlreadyInstalledException(format("Plugin %s is already installed in site %s",
                        pluginId, siteId));
            }

            logger.info("Starting installation of plugin {0} v{1} for site {2}", pluginId, pluginVersion, siteId);

            MarketplacePlugin plugin = getDescriptor(pluginId, pluginVersion);

            if (installableTypes.stream().noneMatch(plugin.getType()::equalsIgnoreCase)) {
                throw new IncompatiblePluginException(
                        format("Plugin '%s' of type '%s' can't be installed", plugin.getId(), plugin.getType()));
            }

            List<FileRecord> files;

            switch (plugin.getSource()) {
                case SOURCE_GIT:
                    files = installPluginFromGit(siteId, plugin);
                    break;
                default:
                    throw new IncompatiblePluginException(
                            format("Plugin '%s' from source '%s' can't be installed",
                                    plugin.getId(), plugin.getSource()));
            }

            logger.info("Installation of plugin {0} v{1} completed for site {2}",
                    plugin.getId(), plugin.getVersion(), siteId);

            updatePluginRegistry(siteId, plugin, files);

            try {
                // Wire plugin to the site configuration
                performConfigurationWiring(plugin, siteId);

                // Wire plugin to the freemarker hooks
                performTemplateWiring(plugin, siteId, files);
            } catch (Exception e) {
                throw new PluginInstallationException("Error wiring plugin " + pluginId + " in site " + siteId, e);
            }
        } finally {
            writeLock.unlock();
        }
    }

    protected boolean pluginAlreadyInstalled(String siteId, String pluginId) throws MarketplaceException {
        logger.debug("Checking if plugin {0} is already installed in site {1}", pluginId, siteId);
        return getInstalledPlugins(siteId).stream()
                .map(PluginRecord::getId)
                .anyMatch(pluginId::equalsIgnoreCase);
    }

    protected List<FileRecord> installPluginFromGit(String siteId, MarketplacePlugin plugin)
            throws PluginInstallationException {
        logger.info("Installing plugin {0} v{1} from remote repository for site {2}",
                plugin.getId(), plugin.getVersion(), siteId);
        Path temp = null;
        try {
            logger.debug("Cloning remote repository {0} with tag {1}", plugin.getUrl(), plugin.getRef());
            temp = Files.createTempDirectory("plugin-" + plugin.getId());
            try (Git git = Git.cloneRepository()
                .setDirectory(temp.toFile())
                .setURI(plugin.getUrl())
                .setBranch(plugin.getRef())
                .call()) {
                return copyPluginFiles(temp, siteId, plugin);
            }

        } catch (Exception e) {
            throw new PluginInstallationException("Error installing plugin " + plugin.getId(), e);
        } finally {
            if (temp != null) {
                try {
                    FileUtils.deleteDirectory(temp.toFile());
                } catch (IOException e) {
                    logger.warn("Could not delete temporary directory " + temp);
                }
            }
        }
    }

    protected List<FileRecord> copyPluginFiles(Path pluginDir, String siteId, MarketplacePlugin plugin)
        throws IOException, GitAPIException {
        logger.info("Copying files from plugin {0} v{1} for site {2}", plugin.getId(), plugin.getVersion(), siteId);
        List<FileRecord> files = new LinkedList<>();
        Path siteDir = gitRepositoryHelper.buildRepoPath(GitRepositories.SANDBOX, siteId);
        try (Git git = Git.open(siteDir.toFile())) {
            String pluginIdPath = getPluginPath(plugin.getId());

            for(Map.Entry<String, String> mapping : folderMapping.entrySet()) {
                var rootFolder = contains(mapping.getValue(), pluginsFolder)?
                        mapping.getValue() : appendIfMissing(mapping.getValue(), "/" + pluginsFolder);
                Path source = pluginDir.resolve(mapping.getKey());
                if (Files.exists(source)) {
                    Path target = siteDir.resolve(rootFolder).resolve(pluginIdPath);
                    Files.createDirectories(target);

                    Files.walkFileTree(source,
                            new PluginTreeCopier(source, target, studioConfiguration, siteId, files, true));
                }
            }

            AddCommand add = git.add();
            files.stream()
                    .map(FileRecord::getPath)
                    .forEach(add::addFilepattern);
            retryingRepositoryOperationFacade.call(add);
            CommitCommand commitCommand = git.commit()
                    .setMessage(format("Install plugin %s %s", plugin.getId(), plugin.getVersion()));
            retryingRepositoryOperationFacade.call(commitCommand);

        }
        return files;
    }

    protected void updatePluginRegistry(String siteId, Plugin plugin, List<FileRecord> files)
            throws MarketplaceRegistryException {
        PluginRecord record = new PluginRecord();
        record.setId(plugin.getId());
        record.setVersion(plugin.getVersion());
        record.setType(plugin.getType());
        record.setPluginUrl(plugin.getWebsite().getUrl());
        record.setInstallationDate(Instant.now());
        record.setFiles(files);

        PluginRegistry registry = getPluginRegistry(siteId);
        registry.getPlugins().add(record);

        logger.debug("Adding plugin {0} v{1} to registry for site {2}", plugin.getId(), plugin.getVersion(), siteId);

        try {
            String content = mapper.writeValueAsString(registry);
            contentService.writeContent(siteId, pluginRegistryPath, toInputStream(content, UTF_8));
            logger.debug("Plugin registry successfully updated for site {0}", siteId);
        } catch (JsonProcessingException | ServiceLayerException e) {
            throw new MarketplaceRegistryException("Error updating the plugin registry for site: " + siteId, e);
        }
    }

    @Override
    public void copyPlugin(String siteId, String path) throws MarketplaceException {
        Path pluginFolder = Path.of(path);
        if (!Files.exists(pluginFolder)) {
            throw new PluginInstallationException("The provided path does not exist: " + path);
        }
        if (!Files.isDirectory(pluginFolder)) {
            throw new PluginInstallationException("The provided path is not a folder: " + path);
        }

        try {
            logger.info("Copying plugin from {0} to site {1}", path, siteId);
            Path descriptorFile = pluginFolder.resolve(pluginDescriptorFilename);
            try (InputStream is = Files.newInputStream(descriptorFile)) {
                PluginDescriptor descriptor = pluginDescriptorReader.read(is);
                Plugin plugin = descriptor.getPlugin();
                var files = new LinkedList<FileRecord>();

                logger.info("Copying files from plugin {0} v{1} for site {2}",
                        plugin.getId(), plugin.getVersion(), siteId);
                Path siteDir = gitRepositoryHelper.buildRepoPath(GitRepositories.SANDBOX, siteId);
                try (Git git = Git.open(siteDir.toFile())) {
                    String pluginIdPath = getPluginPath(plugin.getId());

                    for (Map.Entry<String, String> mapping : folderMapping.entrySet()) {
                        String rootFolder = contains(mapping.getValue(), pluginsFolder) ?
                                mapping.getValue() : appendIfMissing(mapping.getValue(), "/" + pluginsFolder);
                        Path source = pluginFolder.resolve(mapping.getKey());
                        if (Files.exists(source)) {
                            Path target = siteDir.resolve(rootFolder).resolve(pluginIdPath);
                            Files.createDirectories(target);

                            Files.walkFileTree(source,
                                    new PluginTreeCopier(source, target, studioConfiguration, siteId, files, false));
                        }
                    }

                    AddCommand add = git.add();
                    files.stream()
                            .map(FileRecord::getPath)
                            .forEach(add::addFilepattern);
                    retryingRepositoryOperationFacade.call(add);
                    CommitCommand commitCommand = git.commit()
                            .setMessage(format("Copy plugin %s %s", plugin.getId(), plugin.getVersion()));
                    retryingRepositoryOperationFacade.call(commitCommand);

                    // Wire plugin to the site configuration
                    performConfigurationWiring(plugin, siteId);

                    // Wire plugin to the freemarker hooks
                    performTemplateWiring(plugin, siteId, files);

                    logger.info("Copy of plugin {0} v{1} completed for site {2}",
                            plugin.getId(), plugin.getVersion(), siteId);
                }
            }
        } catch (Exception e) {
            throw new PluginInstallationException("Error copying plugin from " + path, e);
        }
    }

    protected void performConfigurationWiring(Plugin plugin, String siteId) throws
            TransformerException, IOException, ServiceLayerException, UserNotFoundException {
        if (CollectionUtils.isNotEmpty(plugin.getInstallation())) {
            logger.info("Starting wiring for plugin {0} in site {1}", plugin.getId(), siteId);

            for(Installation i : plugin.getInstallation()) {
                try {
                    HierarchicalConfiguration<?> mapping = widgetMapping.configurationAt(i.getType());

                    logger.info("Wiring widget of type {0}", i.getType());
                    Element element = buildXml(i.getElement());

                    String newXml = element.asXML();
                    logger.debug("New configuration: {0}", newXml);


                    String module = mapping.getString(MODULE_CONFIG_KEY);
                    String configPath = mapping.getString(PATH_CONFIG_KEY);
                    String templatePath = mapping.getString(TEMPLATE_CONFIG_KEY);
                    var params = new HashMap<String, Object>();
                    if (i.getParent() != null) {
                        params.put(PARAM_PARENT_ID, i.getParent().getId());
                    }
                    params.put(PARAM_PLUGIN_ID, plugin.getId());
                    params.put(PARAM_NEW_XML, newXml);

                    String config = configurationService.getConfigurationAsString(siteId, module, configPath, null);
                    ClassPathResource templateRes = new ClassPathResource(templatePath);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    String template = IOUtils.toString(templateRes.getInputStream(), UTF_8);

                    if (i.getParent() != null) {
                        template = replace(template, Map.of(PARAM_PARENT_XPATH, i.getParent().getXpath()));
                    }

                    executeTemplate(toInputStream(template, UTF_8), params, null,
                                    toInputStream(config, UTF_8), output);

                    configurationService.writeConfiguration(siteId, module, configPath, null,
                            new ByteArrayInputStream(output.toByteArray()));
                } catch (ConfigurationRuntimeException e) {
                    logger.warn("Unsupported installation type {0} for plugin {1}", i.getType(), plugin.getId());
                }
            }
            logger.info("Completed wiring for plugin {0} in site {1}", plugin.getId(), siteId);
        } else {
            logger.info("No wiring required for plugin {0}", plugin.getId());
        }
    }

    protected void performTemplateWiring(Plugin plugin, String siteId, List<FileRecord> files)
            throws ServiceLayerException {
        List<String> paths = files.stream().map(FileRecord::getPath).collect(toList());
        String pluginPath = getPluginPath(plugin.getId());
        String pluginIdComment = replace(templateComment, Map.of(PARAM_PLUGIN_ID, plugin.getId()));

        // Check if the plugin contains templates to be wired
        for(Map.Entry<String, String> mapping : templateMapping.entrySet()) {
            String actualPath = replace(mapping.getValue(), Map.of(PARAM_PLUGIN_PATH, pluginPath));
            addIncludeIfNeeded(siteId, paths, mapping.getKey(), pluginIdComment, actualPath);
        }
    }

    protected String getPluginPath(String pluginId) {
        return pluginId.replaceAll("\\.", "/");
    }

    protected void addIncludeIfNeeded(String siteId, List<String> paths, String includePath, String includeComment,
                                      String pluginPath) throws ServiceLayerException {
        if (paths.contains(pluginPath)) {
            logger.debug("Detected template {}", pluginPath);
            String fileContent = EMPTY;
            if (contentService.contentExists(siteId, includePath)) {
                logger.debug("Site {} already has template {}, it will be updated", siteId, includePath);
                fileContent = contentService.getContentAsString(siteId, includePath);
            } else {
                logger.debug("Site {} does not have template {}, it will be created", siteId, includePath);
            }
            if (isEmpty(fileContent) || !StringUtils.contains(fileContent, includeComment)) {
                logger.debug("Wiring plugin template {} in {} for site {}", pluginPath, includePath, siteId);
                String newLine =
                        format("\n%s%s\n", replace(templateCode, Map.of(PATH_CONFIG_KEY, pluginPath)), includeComment);
                fileContent += newLine;
                contentService.writeContent(siteId, includePath, toInputStream(fileContent, UTF_8));
            } else {
                logger.debug("Plugin template {} already wired in {} for site {}", pluginPath, includePath, siteId);
            }
        }
    }

    protected Element buildXml(Installation.Element element) {
        Element xmlElement = DocumentHelper.createElement(element.getName());
        if (CollectionUtils.isNotEmpty(element.getAttributes())) {
            element.getAttributes().forEach(
                    attribute -> xmlElement.addAttribute(attribute.getName(), attribute.getValue()));
        }
        if (isNotEmpty(element.getValue())) {
            xmlElement.setText(element.getValue());
        }
        if (CollectionUtils.isNotEmpty(element.getChildren())) {
            element.getChildren().forEach(child -> {
                Element xmlChild = buildXml(child);
                xmlElement.add(xmlChild);
            });
        }
        return xmlElement;
    }

}
