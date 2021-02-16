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

(function ({ content, search, classes }, { operators }) {
  const pathRegExp = /^\/(.*?)\.xml$/;
  function getICEAttributes(config, wrapperUtility = '[Error @ getICEAttributes]') {
    let {
      model,
      parentModelId = null,
      label,
      isAuthoring = true,
      fieldId = null
    } = config;

    if (!isAuthoring) {
      return {};
    }

    if (label === null || label === undefined) {
      label = (model?.craftercms.label || '');
    }

    let error = false;
    const isEmbedded = model?.craftercms.path == null;
    const path = model?.craftercms.path ?? parentModelId;
    const modelId = model?.craftercms.id;

    if (isEmbedded && parentModelId == null) {
      error = true;

      (!modelId) &&
      console?.error?.(
        wrapperUtility +
        'The "parentModelId" argument is required for embedded components. ' +
        'Note the value of "parentModelId" should be the *path* of it\'s top parent component. ' +
        'The error occurred with the model attached to this error.',
        model
      );
    }

    if (parentModelId != null && !pathRegExp.test(parentModelId)) {
      error = true;
      (!modelId) &&
      console?.error?.(
        wrapperUtility +
        'The "parentModelId" argument should be the "path" of it\'s top parent component. ' +
        `Provided value was "${parentModelId}" which doesn't comply with the expected format ` +
        '(i.e. \'/a/**/b.xml\'). The error occurred with the model attached to this error. ' +
        'Did you send the id (objectId) instead of the path?',
        model
      );
    }

    if (error) {
      return {};
    }

    return {
      'data-craftercms-model-path': path,
      'data-craftercms-model-id': modelId,
      'data-craftercms-field-id': fieldId
    };

  }
  function getICE(model, fieldId = null) {
    return (getICEAttributes({
      model,
      fieldId,
      isAuthoring: true // TODO: pending
    }))
  }

  const { crafterConf } = classes;

  crafterConf.configure({
    baseUrl: '',
    site: 'headless-blog'
  });


  const { map } = operators;
  const { createQuery } = search;
  const searchContent = search.search;

  const { parseDescriptor, preParseSearchResults } = content;

  var browser = new Vue({
    el: '#browser',
    data: {
      types: [
        {
          label: 'Authors',
          labelField: 'name',
          listUrl: '/api/1/author/list.json'
        },
        {
          label: 'Posts',
          labelField: 'title',
          listUrl: '/api/1/post/list.json'
        }
      ],
      selectedType: null,
      items: [],
      selectedItem: null
    },
    methods: {
      setType: function (type) {
        this.selectedType = type;
        this.selectedItem = null;
        var self = this;

        searchContent(
          createQuery('elasticsearch', {
            query: {
              'bool': {
                'filter': [
                  {
                    'match': {
                      'content': '/component/post'
                    }
                  }
                ]
              }
            },
            sort: {
              by: 'createdDate_dt',
              order: 'desc'
            }
          })
        ).pipe(
          map(({ hits, ...rest }) => ({
            ...rest,
            hits: hits.map(({ _source }) => parseDescriptor(
              preParseSearchResults(_source)
            ))
          }))
        ).subscribe((results) => {
          console.log('results', results);
        });

        this.$http.get(type.listUrl).then(function (response) {
          self.items = response.body;
        });
      },
      setItem: function (item) {
        this.selectedItem = item;
        // TODO: equivalent in NEXT ICE?
        this.$nextTick(function () {
          if (window.studioICERepaint) {
            studioICERepaint();
          }
        });
      },
      getICE
    }
  });
})(craftercms, rxjs);