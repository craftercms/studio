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
(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@craftercms/classes'), require('@craftercms/utils')) :
    typeof define === 'function' && define.amd ? define('@craftercms/content', ['exports', '@craftercms/classes', '@craftercms/utils'], factory) :
    (global = global || self, factory((global.craftercms = global.craftercms || {}, global.craftercms.content = {}), global.craftercms.classes, global.craftercms.utils));
}(this, (function (exports, classes, utils) { 'use strict';

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

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License version 3
     * as published by the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this program. If not, see http://www.gnu.org/licenses/.
     */
    function getItem(path, config) {
        config = classes.crafterConf.mix(config);
        var requestURL = utils.composeUrl(config, config.endpoints.GET_ITEM_URL);
        return classes.SDKService.httpGet(requestURL, { url: path, crafterSite: config.site });
    }
    function getDescriptor(path, config) {
        config = classes.crafterConf.mix(config);
        var requestURL = utils.composeUrl(config, config.endpoints.GET_DESCRIPTOR);
        return classes.SDKService.httpGet(requestURL, {
            url: path,
            crafterSite: config.site,
            flatten: Boolean(config.flatten)
        });
    }
    function getChildren(path, config) {
        config = classes.crafterConf.mix(config);
        var requestURL = utils.composeUrl(config, config.endpoints.GET_CHILDREN);
        return classes.SDKService.httpGet(requestURL, { url: path, crafterSite: config.site });
    }
    function getTree(path, depth, config) {
        if (depth === void 0) { depth = 1; }
        if (typeof depth === 'object') {
            config = depth;
            depth = 1;
        }
        config = classes.crafterConf.mix(config);
        var requestURL = utils.composeUrl(config, config.endpoints.GET_TREE);
        return classes.SDKService.httpGet(requestURL, { url: path, depth: depth, crafterSite: config.site });
    }
    var ContentStoreService = {
        getItem: getItem,
        getDescriptor: getDescriptor,
        getChildren: getChildren,
        getTree: getTree
    };

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License version 3
     * as published by the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this program. If not, see http://www.gnu.org/licenses/.
     */
    function getNavTree(path, depth, currentPageUrl, config) {
        if (depth === void 0) { depth = 1; }
        if (currentPageUrl === void 0) { currentPageUrl = ''; }
        config = classes.crafterConf.mix(config);
        var requestURL = utils.composeUrl(config, config.endpoints.GET_NAV_TREE);
        return classes.SDKService.httpGet(requestURL, {
            crafterSite: config.site,
            currentPageUrl: currentPageUrl,
            url: path,
            depth: depth
        });
    }
    function getNavBreadcrumb(path, root, config) {
        if (root === void 0) { root = ''; }
        config = classes.crafterConf.mix(config);
        var requestURL = utils.composeUrl(config, config.endpoints.GET_BREADCRUMB);
        return classes.SDKService.httpGet(requestURL, {
            crafterSite: config.site,
            url: path,
            root: root
        });
    }
    /**
     * Navigation Service API
     */
    var NavigationService = {
        getNavTree: getNavTree,
        getNavBreadcrumb: getNavBreadcrumb
    };

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License version 3
     * as published by the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this program. If not, see http://www.gnu.org/licenses/.
     */
    function urlTransform(transformerName, url, config) {
        config = classes.crafterConf.mix(config);
        var requestURL = utils.composeUrl(config, config.endpoints.TRANSFORM_URL);
        return classes.SDKService.httpGet(requestURL, {
            crafterSite: config.site,
            transformerName: transformerName,
            url: url
        });
    }
    /**
     * URL Transformation Service API
     */
    var UrlTransformationService = {
        transform: urlTransform,
        urlTransform: urlTransform
    };

    /*! *****************************************************************************
    Copyright (c) Microsoft Corporation. All rights reserved.
    Licensed under the Apache License, Version 2.0 (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.apache.org/licenses/LICENSE-2.0

    THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED
    WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
    MERCHANTABLITY OR NON-INFRINGEMENT.

    See the Apache Version 2.0 License for specific language governing permissions
    and limitations under the License.
    ***************************************************************************** */

    var __assign = function() {
        __assign = Object.assign || function __assign(t) {
            for (var s, i = 1, n = arguments.length; i < n; i++) {
                s = arguments[i];
                for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
            }
            return t;
        };
        return __assign.apply(this, arguments);
    };

    /*
     * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU Lesser General Public License version 3
     * as published by the Free Software Foundation.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this program. If not, see http://www.gnu.org/licenses/.
     */
    var systemPropMap = {
        guid: 'id',
        cmsId: 'id',
        objectId: 'id',
        localId: 'path',
        'file-name': 'fileName',
        'file__name': 'fileName',
        placeInNav: 'placeInNav',
        'internal-name': 'label',
        internal__name: 'label',
        'content-type': 'contentTypeId',
        content__type: 'contentTypeId',
        createdDate_dt: 'dateCreated',
        lastModifiedDate_dt: 'dateModified',
        disabled: 'disabled'
    };
    var ignoreProps = [
        'orderDefault_f',
        'merge-strategy',
        'display-template',
        'objectGroupId',
        'folder-name',
        'createdDate',
        'lastModifiedDate',
        'no-template-required'
    ];
    var systemProps = Object.keys(systemPropMap).concat(Object.values(systemPropMap));
    function parseDescriptor(data) {
        if (data == null) {
            return null;
        }
        else if (Array.isArray(data)) {
            return data.map(function (item) { return parseDescriptor(item); });
        }
        else if (data.children) {
            return parseDescriptor(extractChildren(data.children));
        }
        else if (data.descriptorDom === null && data.descriptorUrl) {
            // This path catches calls to getChildren (/api/1/site/content_store/children.json?url=&crafterSite=)
            // The getChildren call contains certain items that can't be parsed into content items.
            throw new Error('[parseDescriptor] Invalid descriptor supplied. Did you call ' +
                'parseDescriptor with a `getChildren` API response? The `getChildren` API ' +
                'response may contain certain items that are not parsable into ContentInstances. ' +
                'Try a different API (getItem, getDescriptor or getTree) or filter out the metadata ' +
                'items which descriptorDom property has a `page` or `component` property with the content item.');
        }
        var parsed = {
            craftercms: {
                id: null,
                path: null,
                label: null,
                contentTypeId: null,
                dateCreated: null,
                dateModified: null,
                sourceMap: {}
            }
        };
        return parseProps(extractContent(data), parsed);
    }
    function parseProps(props, parsed) {
        if (parsed === void 0) { parsed = {}; }
        Object.entries(props).forEach(function (_a) {
            var _b, _c;
            var prop = _a[0], value = _a[1];
            if (ignoreProps.includes(prop)) {
                return; // continue, skip prop.
            }
            if (value === null || value === void 0 ? void 0 : value['crafter-source-content-type-id']) {
                // @ts-ignore
                parsed.craftercms.sourceMap[prop] = value['crafter-source-content-type-id'];
                if (typeof value.text === 'string') {
                    value = value.text;
                }
                else if (Object.keys(value).length === 2) {
                    // Only has `crafter-source` & `crafter-source-content-type-id`. Empty value for the actual prop.
                    value = null;
                }
            }
            if (systemProps.includes(prop)) {
                // @ts-ignore
                parsed.craftercms[(_b = systemPropMap[prop]) !== null && _b !== void 0 ? _b : prop] = value;
                // Is there a risk prop name that matches what's considered a system prop?
                // In that case, here, parsed.craftercms might not be in the target object
                // and throw. We could do the below to de-risk but feels this needs assessment.
                // if (parsed.craftercms) {
                //   parsed.craftercms[systemPropMap[prop] ?? prop] = value;
                // } else {
                //   parsed[prop] = value;
                // }
            }
            else if (prop.endsWith('_o')) {
                parsed[prop] = (_c = value === null || value === void 0 ? void 0 : value.item) !== null && _c !== void 0 ? _c : [];
                if (!Array.isArray(parsed[prop])) {
                    parsed[prop] = [parsed[prop]];
                }
                parsed[prop] = parsed[prop].map(function (item) {
                    var key = item.key, value = item.value, component = item.component, include = item.include;
                    if ((item.component) || (item.key && item.value)) {
                        // Components
                        var newComponent = __assign(__assign({ label: value }, component), { path: (key === null || key === void 0 ? void 0 : key.startsWith('/')) ? key : ((include === null || include === void 0 ? void 0 : include.startsWith('/')) ? include : null) });
                        return parseDescriptor(newComponent);
                    }
                    else {
                        // Repeat group items
                        return parseProps(item);
                    }
                });
            }
            else {
                parsed[prop] = value !== null && value !== void 0 ? value : null;
            }
        });
        return parsed;
    }
    /**
     * Inspects the data for getItem or getDescriptor responses and returns the inner content object
     * */
    function extractContent(data) {
        var output = data;
        if (data.descriptorDom) {
            return __assign(__assign({}, (data.descriptorDom.page || data.descriptorDom.component)), { path: data.url });
        }
        else if (data.page) {
            return data.page;
        }
        else if (data.component) {
            return data.component;
        }
        return output;
    }
    /**
     * Flattens a getChildren response into a flat list of content items
     * */
    function extractChildren(children) {
        return children.flatMap(function (child) {
            return child.children ? extractChildren(child.children) : child;
        });
    }
    var propsToRemove = [
        'rootId',
        'crafterSite',
        'crafterPublishedDate',
        'crafterPublishedDate_dt',
        'inheritsFrom_smv'
    ];
    function preParseSearchResults(source) {
        Object.entries(source).forEach(function (_a) {
            var prop = _a[0], value = _a[1];
            if (propsToRemove.includes(prop)) {
                delete source[prop];
            }
            else if (prop.endsWith('_o')) {
                var collection = value;
                if (!Array.isArray(collection.item)) {
                    source[prop] = { item: [collection.item] };
                }
                source[prop].item.forEach(function (item, i) {
                    source[prop].item[i] = preParseSearchResults(item);
                    if (item.component) {
                        source[prop].item[i].component = preParseSearchResults(item.component);
                    }
                });
            }
        });
        return source;
    }

    exports.ContentStoreService = ContentStoreService;
    exports.NavigationService = NavigationService;
    exports.UrlTransformationService = UrlTransformationService;
    exports.getChildren = getChildren;
    exports.getDescriptor = getDescriptor;
    exports.getItem = getItem;
    exports.getNavBreadcrumb = getNavBreadcrumb;
    exports.getNavTree = getNavTree;
    exports.getTree = getTree;
    exports.parseDescriptor = parseDescriptor;
    exports.parseProps = parseProps;
    exports.preParseSearchResults = preParseSearchResults;
    exports.urlTransform = urlTransform;

    Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=content.umd.js.map
