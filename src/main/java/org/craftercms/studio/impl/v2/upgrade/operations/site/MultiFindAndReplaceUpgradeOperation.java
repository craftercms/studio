/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.upgrade.operations.site;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that replaces text in the content repository
 * according to multiple 'pattern/replacement' rules.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>rules</strong>: (required) the list of rules to find and replace text. Each rule should contain the properties:
 *     <ul>
 *          <li><strong>pattern</strong>: (required) the pattern to search in the files, can be a regular expression</li>
 *          <li><strong>replacement</strong>: (required) the expression to replace in the files, can use matched groups
 *     from the regular expression in the pattern</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * @author jmendeza
 */
public class MultiFindAndReplaceUpgradeOperation extends AbstractContentUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(MultiFindAndReplaceUpgradeOperation.class);

    public static final String CONFIG_KEY_RULES = "rules";
    public static final String CONFIG_KEY_PATTERN = "pattern";
    public static final String CONFIG_KEY_REPLACEMENT = "replacement";

    /**
     * The list of find-replace rules
     */

    protected List<Rule> rules;

    public MultiFindAndReplaceUpgradeOperation(StudioConfiguration studioConfiguration) {
        super(studioConfiguration);
    }

    @Override
    protected void doInit(final HierarchicalConfiguration config) {
        super.doInit(config);
        rules = new ArrayList<>();
        List<HierarchicalConfiguration> ruleConfigs = config.configurationsAt(CONFIG_KEY_RULES);
        ruleConfigs.forEach(rule -> rules.add(new Rule(rule.getString(CONFIG_KEY_PATTERN), rule.getString(CONFIG_KEY_REPLACEMENT))));
    }

    @Override
    protected boolean shouldBeUpdated(StudioUpgradeContext context, Path file) {
        return true;
    }

    @Override
    protected void updateFile(StudioUpgradeContext context, Path path) throws UpgradeException {
        String content = readFile(path);
        String updated = null;
        if (isNotEmpty(content)) {
            updated = content;
            for (Rule rule : rules) {
                updated = RegExUtils.replaceAll(updated, rule.pattern, rule.replacement);
            }
        }

        if (isNotEmpty(updated) && !StringUtils.equals(content, updated)) {
            logger.info("Update the file '{}'", path);
            writeFile(path, updated);
        }
    }

    private class Rule {
        String pattern;
        String replacement;

        public Rule(final String pattern, final String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
    }

}
