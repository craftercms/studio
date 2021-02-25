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

  function getCookie(name) {
    var v = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
    return v ? v[2] : null;
  }
  function isAuthoring() {
    const html = document.documentElement;
    const attr = html.getAttribute('data-craftercms-preview');

    return (
      attr === '${modePreview?c}' || // Otherwise disable/enable if you want to see pencils in dev server.
      attr === 'true'
    );
  }
  function getICE(model, fieldId = null) {
    return (craftercms?.guest?.getICEAttributes({
      model,
      fieldId,
      isAuthoring: authoring
    }))
  }

  const authoring = isAuthoring();
  const { crafterConf } = classes;

  crafterConf.configure({
    baseUrl: '',
    site: getCookie('crafterSite')
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
          id: 'authors',
          label: 'Authors',
          labelField: 'name_s',
          contentType: '/component/author'
        },
        {
          id: 'posts',
          label: 'Posts',
          labelField: 'title_s',
          contentType: '/component/post'
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
            'query': {
              'bool': {
                'filter': [
                  {
                    'match': {
                      'content-type': type.contentType
                    }
                  }
                ]
              }
            },
            'sort': {
              'createdDate_dt': { 'order': 'desc' }
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
          self.items = results;
        });
      },
      setItem: function (item) {
        this.selectedItem = item;
      },
      getICE
    },
    beforeUpdate: function() {
      document.querySelectorAll('[data-craftercms-model-id]').forEach((el) => {
        const record = craftercms.guest.ElementRegistry.fromElement(el);

        // This is supposed to be before updating DOM, but query is returning both old and new elements
        if (record) {
          craftercms?.guest?.ElementRegistry.deregister(record.id);
        }
      });
    },
    watch: {
      selectedItem: function() {
        this.$nextTick(function() {
          if (this.selectedItem) {
            document.querySelectorAll('[data-craftercms-model-id]').forEach((element) => {
              let //
                path = element.getAttribute('data-craftercms-model-path'),
                modelId = element.getAttribute('data-craftercms-model-id'),
                fieldId = element.getAttribute('data-craftercms-field-id'),
                index = element.getAttribute('data-craftercms-index'),
                label = element.getAttribute('data-craftercms-label');

              if ((index !== null) && (index !== undefined) && !index.includes('.')) {
                // Unsure if somewhere, the system relies on the index being an integer/number.
                // Affected inventory:
                // - Guest.moveComponent() - string type handled
                index = parseInt(index, 10);
              }

              craftercms?.guest?.ElementRegistry.register({ element, modelId, fieldId, index, label, path });
            });
          }
        });
      }
    }
  });
})(craftercms, rxjs);