<!--
  ~ Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License version 3 as published by
  ~ the Free Software Foundation.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>${model.title_t}</title>
  <link rel="stylesheet" href="/static-assets/css/vendor/bootstrap-3.3.7.min.css">
  <style>
    [v-cloak] { display:none; }
  </style>
  <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body>
  <div id="catalog" class="container" v-cloak>
    <div class="row">
      <div class="page-header">
        <h1>Store Catalog</h1>
      </div>
    </div>
    <div class="row">
      <nav class="navbar navbar-default">
        <ul class="nav navbar-nav">
          <li class="dropdown" v-if="filters && filters.companies">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Company<span class="caret"></span></a>
            <ul class="dropdown-menu">
              <li v-for="company in filters.companies" v-bind:class="{ active: selection.company == company }">
                <a href="#" v-on:click="selection.company = company; currentPage = 1">{{ company.name_s }}</a>
              </li>
            </ul>
          </li>
          <li class="dropdown" v-if="filters && filters.categories">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Category<span class="caret"></span></a>
            <ul class="dropdown-menu">
              <li v-for="category in filters.categories">
                  <a href="#" v-on:click="selection.category = category; currentPage = 1">{{ category.value }}</a>
              </li>
            </ul>
          </li>
          <li class="dropdown" v-if="filters && filters.tags">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Tag<span class="caret"></span></a>
            <ul class="dropdown-menu">
              <li v-for="tag in filters.tags">
                  <a href="#" v-on:click="selection.tag = tag; currentPage = 1">{{ tag.value }}</a>
              </li>
            </ul>
          </li>
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Show: {{ numberOfProducts }}<span class="caret"></span></a>
            <ul class="dropdown-menu">
              <li><a href="#" v-on:click="numberOfProducts = 4; currentPage = 1">4</a></li>
              <li><a href="#" v-on:click="numberOfProducts = 8; currentPage = 1">8</a></li>
            </ul>
          </li>
        </ul>
      </nav>
    </div>
    <div class="row" v-if="selection.company || selection.category || selection.tag">
        <div class="col-sm-12">
            <div class="panel panel-default">
                <div class="panel-body">
                    <strong>Filtering By:&nbsp;</strong>
                    <span class="label label-primary" style="margin-right:5px" v-if="selection.company">
                        {{ selection.company.name_s }}
                        &nbsp;
                        <span class="glyphicon glyphicon-remove" v-on:click="selection.company = null"></span>
                    </span>
                    <span class="label label-primary" style="margin-right:5px" v-if="selection.category">
                        {{ selection.category.value }}
                        &nbsp;
                        <span class="glyphicon glyphicon-remove" v-on:click="selection.category = null"></span>
                    </span>
                    <span class="label label-primary" style="margin-right:5px" v-if="selection.tag">
                        {{ selection.tag.value }}
                        &nbsp;
                        <span class="glyphicon glyphicon-remove" v-on:click="selection.tag = null"></span>
                    </span>
                </div>
            </div>
        </div>
    </div>
    <div class="row" v-if="selection.company">
      <div class="col-sm-12">
        <div class="panel panel-default">
          <div class="panel-body">
            <div class="col-sm-2">
              <img class="img-responsive img-circle center-block" v-bind:src="selection.company.logo_s"/>
            </div>
            <div class="col-sm-10 text-center">
              <h2><a v-bind:href="selection.company.website_s">{{ selection.company.name_s }}</a></h2>
              <div v-html="selection.company.description_html_raw"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row" v-if="products && products.items">
        <div v-for="(product, i) in products.items">
            <div class="col-md-3">
                <div class="panel panel-default" v-bind:data-studio-component-path="product.localId"
                     v-bind:data-studio-component="product.localId" data-studio-ice=""
                     v-bind:data-studio-ice-path="product.localId">
                    <div class="panel-body">
                        <img v-bind:src="product.image_s" class="img-responsive img-thumbnail center-block"
                             data-toggle="popover" data-trigger="hover" v-bind:data-content="product.description_html_raw"
                             data-html="true"/>
                        <h4>{{ product.name_s }}<small> by {{ product.company_o.item[0].component.name_s }}<small></h4>
                        <span class="badge pull-right">&#36;{{ product.price_d }}</span>
                    </div>
                </div>
            </div>
            <div class="clearfix" v-if="(i + 1) % 4 == 0"></div>
        </div>
    </div>
    <div class="row" v-if="products.total == 0">
        <div class="col-md-12 well text-center">
            <h2>No products found</h2>
        </div>
    </div>
    <div class="row">
      <div class="col-md-12" v-if="pagination.total > 1">
        <nav aria-label="Page navigation" class="text-center">
          <ul class="pagination">
            <li v-bind:class="{ disabled: !pagination.hasPrev }">
              <a href="#" aria-label="Previous" v-on:click="currentPage--">
                <span aria-hidden="true">&laquo;</span>
              </a>
            </li>
            <li v-for="n in pagination.total" v-bind:class="{ active: n == currentPage }">
                <a href="#" v-on:click="currentPage = n">{{ n }}</a>
            </li>
            <li v-bind:class="{ disabled: !pagination.hasNext }">
              <a href="#" aria-label="Next" v-on:click="currentPage++">
                <span aria-hidden="true">&raquo;</span>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
  <script src="/static-assets/js/vendor/jquery-3.2.1.min.js"></script>
  <script src="/static-assets/js/vendor/bootstrap-3.3.7.min.js"></script>
  <script src="/static-assets/js/vendor/vue-2.6.10.min.js"></script>
  <script src="/static-assets/js/vendor/vue-resource-1.5.1.min.js"></script>
  <script src="/static-assets/js/vendor/vue-async-computed-3.6.1.js"></script>
  <script src="/static-assets/js/catalog.js"></script>
  <@studio.toolSupport/>
</body>
</html>
