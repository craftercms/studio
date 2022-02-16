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
import org.apache.tika.io.FilenameUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.exception.validation.ValidationException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.policy.internal.PolicyServiceInternal;
import org.craftercms.studio.impl.v2.service.policy.PolicyValidator;
import org.craftercms.studio.model.policy.Action;
import org.craftercms.studio.model.policy.Type;
import org.craftercms.studio.model.policy.ValidationResult;

import java.beans.ConstructorProperties;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.craftercms.studio.model.policy.Action.METADATA_FILE_SIZE;

/**
 * Default implementation of {@link PolicyServiceInternal}
 *
 * @author joseross
 * @since 4.0.0
 */
public class PolicyServiceInternalImpl implements PolicyServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(PolicyServiceInternalImpl.class);

    public static final String CONFIG_KEY_STATEMENT = "statement";
    public static final String CONFIG_KEY_PATTERN = "target-path-pattern";
    public static final String CONFIG_KEY_PERMITTED = "permitted";

    protected ContentRepository contentRepository;

    protected org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2;

    protected ConfigurationService configurationService;

    protected PolicyValidator systemValidator;

    protected List<PolicyValidator> policyValidators;

    protected String configPath;

    @ConstructorProperties({"contentRepository", "contentRepositoryV2", "configurationService", "systemValidator",
            "policyValidators", "configPath"})
    public PolicyServiceInternalImpl(ContentRepository contentRepository,
                                     org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2,
                                     ConfigurationService configurationService,
                                     PolicyValidator systemValidator,
                                     List<PolicyValidator> policyValidators,
                                     String configPath) {
        this.contentRepository = contentRepository;
        this.contentRepositoryV2 = contentRepositoryV2;
        this.configurationService = configurationService;
        this.systemValidator = systemValidator;
        this.policyValidators = policyValidators;
        this.configPath = configPath;
    }

    @Override
    public List<ValidationResult> validate(String siteId, List<Action> actions)
            throws ConfigurationException {

        var config = configurationService.getXmlConfiguration(siteId, configPath);
        actions.forEach(this::validateAction);

        var results = new LinkedList<ValidationResult>();
        actions.forEach(action -> {
            if (action.isRecursive()) {
                evaluateRecursiveAction(config, siteId, action, results, true);
            } else {
                evaluateAction(config, action, results, true);
            }
        });
        return results;
    }

    protected void validateAction(Action action) {
        if (action.isRecursive() && isEmpty(action.getSource())) {
            throw new IllegalArgumentException("All recursive actions need to include a source");
        }
        if (action.getType() == Type.CREATE) {
            if (action.isRecursive()) {
                throw new IllegalArgumentException("CREATE actions can't be recursive");
            }
        }
    }

    protected void evaluateAction(HierarchicalConfiguration<?> config, Action action, List<ValidationResult> results,
                                  boolean includeAllowed) {
        try {
            systemValidator.validate(null, action);

            if (config != null) {
                var statementConfig = config.configurationsAt(CONFIG_KEY_STATEMENT).stream()
                        .filter(statement -> action.getTarget().matches(statement.getString(CONFIG_KEY_PATTERN)))
                        .findFirst();

                if (statementConfig.isPresent()) {
                    var statement = statementConfig.get();

                    for (var validator : policyValidators) {
                        logger.debug("Evaluating {0} using validator {1}", action, validator.getClass().getSimpleName());
                        validator.validate(statement.configurationAt(CONFIG_KEY_PERMITTED), action);
                    }
                } else {
                    logger.debug("No statement matches found, skipping {0}", action);
                }
            } else {
                logger.debug("No policy configuration found, skipping {0}", action);
            }
            if (includeAllowed) {
                logger.debug("Allowed {0}", action);
                results.add(ValidationResult.allowed(action));
            }
        } catch (ValidationException e) {
            logger.error("Validation failed for " + action, e);
            if (e.getModifiedValue() != null) {
                logger.debug("Allowed with modifications {0}", action);
                results.add(ValidationResult.allowedWithModifications(action, e.getModifiedValue()));
            } else {
                logger.debug("Not allowed {0}", action);
                results.add(ValidationResult.notAllowed(action));
            }
        }
    }

    protected void evaluateRecursiveAction(HierarchicalConfiguration<?> config, String siteId, Action action,
                                           List<ValidationResult> results, boolean includeAllowed) {
        // First check if the original action is ok
        evaluateAction(config, action, results, includeAllowed);
        // If it's ok then start checking the children
        var children = contentRepository.getContentChildren(siteId, action.getSource());
        var sourceName = FilenameUtils.getName(action.getSource());
        for (var child : children) {
            var childPath = Paths.get(child.path, child.name);
            // Calculate the new path
            var childTarget = Paths.get(action.getTarget(), sourceName).
                    resolve(Paths.get(action.getSource()).relativize(childPath)).toString();

            var childAction = new Action();
            childAction.setType(action.getType());
            childAction.setSource(childPath.toString());
            childAction.setTarget(childTarget);
            if (child.isFolder) {
                evaluateRecursiveAction(config, siteId, childAction, results, false);
            } else {
                childAction.setContentMetadata(
                        Map.of(METADATA_FILE_SIZE, contentRepositoryV2.getContentSize(siteId, childPath.toString())));
                evaluateAction(config, childAction, results, false);
            }
        }
    }

}
