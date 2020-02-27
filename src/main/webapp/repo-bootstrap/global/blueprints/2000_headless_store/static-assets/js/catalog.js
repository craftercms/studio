/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

// Crafter Engine GraphQL API
const GRAPHQL_URL = '/api/1/site/graphql';

// Define all GraphQL queries
const GRAPHQL_QUERIES = '\
  query getFilters {\
    companies: component_company(sortBy: "name_s", sortOrder: ASC) {\
      items {\
        objectId\
        name_s\
        logo_s\
        website_s\
        description_html_raw\
      }\
    }\
    categories: taxonomy {\
      items {\
        localId(filter:{regex:".*categories.*"})\
        items {\
          item {\
            key\
            value\
          }\
        }\
      }\
    }\
    tags: taxonomy {\
      items {\
        localId(filter:{regex:".*tags.*"})\
        items {\
          item {\
            key\
            value\
          }\
        }\
      }\
    }\
  }\
  query getProducts($offset: Int, $limit: Int, $company: String, $category: String, $tag: String) {\
    products: component_product(offset: $offset, limit: $limit) {\
      total\
      items {\
        localId\
        name_s\
        description_html_raw\
        price_d\
        image_s\
        categories_o {\
          item {\
            key(filter:{ matches: $category })\
          }\
        }\
        tags_o {\
          item {\
            key(filter:{ matches: $tag })\
          }\
        }\
        company_o {\
          item {\
            component {\
              objectId(filter:{ regex: $company })\
              name_s\
            }\
          }\
        }\
      }\
    }\
  }'; 

// Create the Vue application
var catalog = new Vue({
  el: '#catalog',
  data: {
    selection: {
      company: null,
      category: null,
      tag: null
    },
    numberOfProducts: 4,
    currentPage: 1
  },
  computed: {
    pagination: function() {
      // function to update the pagination
      var total = Math.ceil(this.products.total / this.numberOfProducts);
      return {
        total: total,
        hasPrev: this.currentPage > 1,
        hasNext: this.currentPage < total
      }
    }
  },
  asyncComputed: {
    filters: {
      default: {},
      get: function() {
        // function to load the filters using the GraphQL query
        return this.$http.post(GRAPHQL_URL, { query: GRAPHQL_QUERIES, operationName: 'getFilters' }).then(response => {
          return {
            companies: response.body.data.companies.items,
            categories: response.body.data.categories.items[0].items.item,
            tags: response.body.data.tags.items[0].items.item
          };
        });
      }
    },
    products: {
      default: {},
      get: function() {
        // function to load the products using the GraphQL query with the selected filters
        var variables = {
          offset: (this.currentPage - 1) * this.numberOfProducts,
          limit: this.numberOfProducts
        };
        if(this.selection.company) {
          variables.company = this.selection.company.objectId;
        }
        if(this.selection.category) {
          variables.category = this.selection.category.key;
        }
        if(this.selection.tag) {
          variables.tag = this.selection.tag.key;
        }
        return this.$http.post(GRAPHQL_URL, { 
          query: GRAPHQL_QUERIES,
          operationName: 'getProducts',
          variables: variables
        }).then(response => {
          return response.body.data.products;
        });
      }
    }
  },
  mounted: function() {
    // when the app is ready init all popups
    $(function () {
      $('[data-toggle="popover"]').popover()
    })
  },
  updated: function() {
    // when the app is updated, update all popups and Crafter Studio ICE controls
    this.$nextTick(function() {
      if(window.studioICERepaint) {
        studioICERepaint();
      }
      jQuery('[data-toggle="popover"]').popover();
    });
  }
});
