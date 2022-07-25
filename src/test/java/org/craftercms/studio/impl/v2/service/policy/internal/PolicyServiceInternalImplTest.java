/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.service.policy.internal;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.impl.v1.log.l4j.L4jLogProvider;
import org.craftercms.studio.impl.v2.service.policy.validators.ContentTypePolicyValidator;
import org.craftercms.studio.impl.v2.service.policy.validators.FileSizePolicyValidator;
import org.craftercms.studio.impl.v2.service.policy.validators.MimeTypePolicyValidator;
import org.craftercms.studio.impl.v2.service.policy.validators.PathPolicyValidator;
import org.craftercms.studio.impl.v2.service.policy.validators.SystemPolicyValidator;
import org.craftercms.studio.model.policy.Action;
import org.craftercms.studio.model.policy.Type;
import org.craftercms.studio.model.policy.ValidationResult;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.model.policy.Action.METADATA_CONTENT_TYPE;
import static org.craftercms.studio.model.policy.Action.METADATA_FILE_SIZE;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author joseross
 */
public class PolicyServiceInternalImplTest {

    public static final String SITE_ID = "mySite";

    public static final Resource CONFIG = new ClassPathResource("crafter/studio/config/policy.xml");

    public static final String CONFIG_PATH = "/studio/config/policy-config.xml";

    public static final String DOC1_FILENAME = "doc1.pdf";
    public static final String DOC2_FILENAME = "doc2.pdf";
    public static final String DOC3_FILENAME = "doc3.pdf";
    public static final String DOCS_FOLDER_PATH = "/static-assets/docs";
    public static final String SUB_FOLDER_NAME = "folder";

    public static final String PIC_FILENAME = "pic.png";
    public static final String PICS_FOLDER_PATH = "/static-assets/pics";

    public static final String FOOTER_CONTENT_TYPE = "/component/footer";
    public static final String HEADER_CONTENT_TYPE = "/component/header";

    public static final String UNRESTRICTED_FOLDER = "/static-assets/secret";
    public static final String SIZE_RESTRICTED_FOLDER = "/static-assets/small";
    public static final String TYPE_RESTRICTED_FOLDER = "/static-assets/images";
    public static final String SIMPLE_PATH_RESTRICTED_FOLDER = "/static-assets/no-numbers";
    public static final String CASE_PATH_RESTRICTED_FOLDER = "/static-assets/only-lowercase";
    public static final String REMOVE_PATH_RESTRICTED_FOLDER = "/static-assets/remove-numbers";
    public static final String CONTENT_TYPE_RESTRICTED_FOLDER = "/site/components/headers";

    public static final String PIC_SVG_FILENAME = "pic.svg";
    public static final String RESTRICTED_MIME_TYPE = "image/svg+xml";

    public static final String CONTENT_TYPE_FOOTER_ONLY_FOLDER = "/site/components/footers";

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2;

    @Mock
    private ConfigurationService configurationService;

    private PolicyServiceInternalImpl policyService;

    @BeforeClass
    public static void setUpLogger() {
        var provider = new L4jLogProvider();
        provider.init();
    }

    @BeforeMethod
    public void setUp() throws IOException,
            org.apache.commons.configuration2.ex.ConfigurationException, ConfigurationException {
        initMocks(this);

        var systemValidator = new SystemPolicyValidator(255, 1024);
        var policyValidators = List.of(
                new FileSizePolicyValidator(),
                new MimeTypePolicyValidator(),
                new PathPolicyValidator(),
                new ContentTypePolicyValidator());

        policyService = new PolicyServiceInternalImpl(contentRepository, contentRepositoryV2, configurationService,
                systemValidator, policyValidators, CONFIG_PATH);

        var config = new XMLConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        try (var is = CONFIG.getInputStream()) {
            var fh = new FileHandler(config);
            fh.load(is);
        }

        when(configurationService.getXmlConfiguration(SITE_ID, CONFIG_PATH)).thenAnswer(i -> config);

        setUpRepository();
    }

    /**
     * Setup a mock repository with the following structure:
     *
     * config
     *   studio
     *      policy-config.xml (content from test resources)
     *
     * static-assets
     *   docs
     *      folder
     *          doc1.pdf (20 kb)
     *          doc2.pdf (5 kb)
     *          doc3.pdf (30 kb)
     *   pics
     *      folder
     *          pic.png (1 kb)
     *
     */
    protected void setUpRepository() {
        when(contentRepository.getContentChildren(SITE_ID, concat(PICS_FOLDER_PATH, SUB_FOLDER_NAME))).thenAnswer(i -> {
            var item = new RepositoryItem();
            item.path = concat(PICS_FOLDER_PATH, SUB_FOLDER_NAME);
            item.name = PIC_FILENAME;

            return new RepositoryItem[] { item };
        });

        when(contentRepository.getContentChildren(SITE_ID, PICS_FOLDER_PATH)).thenAnswer(i -> {
            var item = new RepositoryItem();
            item.isFolder = true;
            item.path = PICS_FOLDER_PATH;
            item.name = SUB_FOLDER_NAME;

            return new RepositoryItem[] { item };
        });

        when(contentRepository.getContentChildren(SITE_ID, concat(DOCS_FOLDER_PATH, SUB_FOLDER_NAME))).thenAnswer(i -> {
            var item1 = new RepositoryItem();
            item1.path = concat(DOCS_FOLDER_PATH, SUB_FOLDER_NAME);
            item1.name = DOC1_FILENAME;

            var item2 = new RepositoryItem();
            item2.path = concat(DOCS_FOLDER_PATH, SUB_FOLDER_NAME);
            item2.name = DOC2_FILENAME;

            var item3 = new RepositoryItem();
            item3.path = concat(DOCS_FOLDER_PATH, SUB_FOLDER_NAME);
            item3.name = DOC3_FILENAME;

            return new RepositoryItem[] { item1, item2, item3 };
        });

        when(contentRepository.getContentChildren(SITE_ID, DOCS_FOLDER_PATH)).thenAnswer(i -> {
            var item = new RepositoryItem();
            item.isFolder = true;
            item.path = DOCS_FOLDER_PATH;
            item.name = SUB_FOLDER_NAME;

            return new RepositoryItem[] { item };
        });

        when(contentRepositoryV2.getContentSize(SITE_ID, concat(DOCS_FOLDER_PATH, SUB_FOLDER_NAME, DOC1_FILENAME)))
                .thenReturn(20000L);
        when(contentRepositoryV2.getContentSize(SITE_ID, concat(DOCS_FOLDER_PATH, SUB_FOLDER_NAME, DOC2_FILENAME)))
                .thenReturn(5000L);
        when(contentRepositoryV2.getContentSize(SITE_ID, concat(DOCS_FOLDER_PATH, SUB_FOLDER_NAME, DOC3_FILENAME)))
                .thenReturn(30000L);

        when(contentRepositoryV2.getContentSize(SITE_ID, concat(PICS_FOLDER_PATH, SUB_FOLDER_NAME, PIC_FILENAME)))
                .thenReturn(1000L);
    }

    protected String concat(String... args) {
        return String.join("/", args);
    }

    @Test
    public void systemPolicyTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(UNRESTRICTED_FOLDER + "/" + RandomStringUtils.randomAlphabetic(255) + ".pdf");
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));

        var results= policyService.validate(SITE_ID, List.of(action));

        checkSingleResult(results, false);

        action.setTarget(RandomStringUtils.randomAlphabetic(1024) + "/" + DOC1_FILENAME);

        results= policyService.validate(SITE_ID, List.of(action));

        checkSingleResult(results, false);
    }

    @Test
    public void noConfigTest() throws ConfigurationException, IOException, ContentNotFoundException {
        policyService.configPath = "/config/studio/policy.xml";

        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(SIZE_RESTRICTED_FOLDER + "/" + DOC1_FILENAME);
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));

        var results = policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void noMatchesTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(UNRESTRICTED_FOLDER + "/" + DOC1_FILENAME);
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));

        var results= policyService.validate(SITE_ID, List.of(action));

        checkSingleResult(results, true);
    }

    @Test
    public void bulkUploadTest() throws ConfigurationException {
        var longName = StringUtils.repeat("longName", 100);

        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget("/static-assets/" + longName + "/" + DOC1_FILENAME);

        var results = policyService.validate(SITE_ID, List.of(action));

        checkSingleResult(results, false);
    }

    @Test
    public void maxSizeTest() throws IOException, ContentNotFoundException, ConfigurationException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(SIZE_RESTRICTED_FOLDER + "/" + DOC1_FILENAME);
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));

        var results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, false);

        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, 1000));

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void mimeTypeTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(TYPE_RESTRICTED_FOLDER + "/" + DOC1_FILENAME);
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));

        var results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, false);

        action.setTarget(TYPE_RESTRICTED_FOLDER + "/" + PIC_FILENAME);

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);

        action.setTarget(TYPE_RESTRICTED_FOLDER + "/" + PIC_SVG_FILENAME);

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, false);
    }

    @Test
    public void simplePathTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(SIMPLE_PATH_RESTRICTED_FOLDER + "/" + DOC1_FILENAME);
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));

        var results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, false);

        action.setTarget(SIMPLE_PATH_RESTRICTED_FOLDER + "/" + DOC1_FILENAME.replaceAll("\\d", ""));

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void lowerCasePathTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(CASE_PATH_RESTRICTED_FOLDER + "/" + DOC1_FILENAME.toUpperCase());
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));

        var results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true, CASE_PATH_RESTRICTED_FOLDER + "/" + DOC1_FILENAME);

        action.setTarget(CASE_PATH_RESTRICTED_FOLDER + "/" + DOC1_FILENAME);

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void removeNumbersPathTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(REMOVE_PATH_RESTRICTED_FOLDER + "/" + DOC1_FILENAME);
        action.setContentMetadata(Map.of(METADATA_FILE_SIZE, Long.MAX_VALUE));
        var expectedFilename = DOC1_FILENAME.replaceAll("\\d", "");

        var results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true, REMOVE_PATH_RESTRICTED_FOLDER + "/" + expectedFilename);

        action.setTarget(CASE_PATH_RESTRICTED_FOLDER + "/" + expectedFilename);

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void createFolderPathTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(REMOVE_PATH_RESTRICTED_FOLDER);

        var results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResultNotModified(results);
    }

    @Test
    public void contentTypeTest() throws ConfigurationException, IOException, ContentNotFoundException {
        // Check that validation fails if the content-type is not included in the metadata
        var action = new Action();
        action.setType(Type.CREATE);
        action.setTarget(CONTENT_TYPE_RESTRICTED_FOLDER + "/component.xml");
        var results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, false);

        // Check that validation fails if the content-type is not allowed
        action.setContentMetadata(Map.of(METADATA_CONTENT_TYPE, FOOTER_CONTENT_TYPE));
        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, false);

        // Check that validation is ok with an allowed content-type
        action.setContentMetadata(Map.of(METADATA_CONTENT_TYPE, HEADER_CONTENT_TYPE));
        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);

        // Check denied content-type
        action.setTarget(CONTENT_TYPE_FOOTER_ONLY_FOLDER + "/component.xml");
        action.setContentMetadata(Map.of(METADATA_CONTENT_TYPE, HEADER_CONTENT_TYPE));
        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, false);
    }

    @Test
    public void recursiveSizeTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.COPY);
        action.setSource(DOCS_FOLDER_PATH);
        action.setTarget(SIZE_RESTRICTED_FOLDER);
        action.setRecursive(true);

        var results= policyService.validate(SITE_ID, List.of(action));
        checkMultipleResults(results, false);

        action.setSource(PICS_FOLDER_PATH);

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void recursiveMimeTypeTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.COPY);
        action.setSource(DOCS_FOLDER_PATH);
        action.setTarget(TYPE_RESTRICTED_FOLDER + "/docs");
        action.setRecursive(true);

        var results= policyService.validate(SITE_ID, List.of(action));
        checkMultipleResults(results, false);

        action.setSource(PICS_FOLDER_PATH);

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void recursiveSimplePathTest() throws ConfigurationException, IOException, ContentNotFoundException {
        var action = new Action();
        action.setType(Type.COPY);
        action.setSource(DOCS_FOLDER_PATH);
        action.setTarget("/static-assets/no-numbers");
        action.setRecursive(true);

        var results= policyService.validate(SITE_ID, List.of(action));
        checkMultipleResults(results, false);

        action.setSource("/static-assets/pics");

        results= policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true);
    }

    @Test
    public void multipleTransformationTest() throws ConfigurationException {
        Action action = new Action();
        action.setType(Type.CREATE);
        action.setTarget("/static-assets/test/paren(thesis)in_name.png");
        List<ValidationResult> results = policyService.validate(SITE_ID, List.of(action));
        checkSingleResult(results, true, "/static-assets/test/paren-thesis-in-name.png");
    }

    protected void checkSingleResult(List<ValidationResult> results, boolean allowed) {
        checkSingleResult(results, allowed, null);
    }

    protected void checkSingleResult(List<ValidationResult> results, boolean allowed, String modified) {
        assertNotNull(results);
        assertEquals(results.size(), 1);
        assertNotNull(results.get(0));
        assertEquals(results.get(0).isAllowed(), allowed);

        if (modified != null) {
            assertEquals(results.get(0).getModifiedValue(), modified);
        }
    }

    protected void checkSingleResultNotModified(List<ValidationResult> results) {
        assertNotNull(results);
        assertEquals(results.size(), 1);
        assertNotNull(results.get(0));
        assertTrue(results.get(0).isAllowed());
        assertNull(results.get(0).getModifiedValue());
    }

    protected void checkMultipleResults(List<ValidationResult> results, boolean allAllowed) {
        assertNotNull(results);
        assertTrue(results.size() > 1);
        if (allAllowed) {
            assertTrue(results.stream().allMatch(ValidationResult::isAllowed));
        } else {
            assertTrue(results.stream().anyMatch(result -> !result.isAllowed()));
        }
    }

}
