<#import "/templates/system/common/crafter.ftl" as crafter />

<!DOCTYPE html>
<html lang="en" data-craftercms-preview="${modePreview?c}">
<head>
  <meta charset="utf-8">
  <title>${model.title_s}</title>
  <link rel="stylesheet" href="/static-assets/css/vendor/bootstrap-3.3.7.min.css">
  <style>
    [v-cloak] {
      display: none;
    }
  </style>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <@crafter.head/>
</head>
<body>
<@crafter.body_top/>
<div id="browser" class="container" v-cloak>
  <div class="row">
    <div class="page-header">
      <h1>
        Headless Blog
        <small>
          <span
            class="glyphicon glyphicon-question-sign" data-toggle="modal"
            data-target="#help-modal"></span>
        </small>
      </h1>
    </div>
    <div class="col-md-2">
      <div class="panel panel-default">
        <div class="panel-heading">
          <h2 class="panel-title">Items</h2>
        </div>
        <div class="panel-body">
          <div class="list-group">
            <a href="#" class="list-group-item" v-for="type in types" v-on:click="setType(type)">
              {{ type.label }}
              <span v-if="type == selectedType" class="badge">
                <span class="glyphicon glyphicon-chevron-right"></span>
              </span>
            </a>
          </div>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="panel panel-default" v-if="selectedType">
        <div class="panel-heading">
          <h2 class="panel-title">{{ selectedType.label }} ({{ items.total && items.total.value }})</h2>
        </div>
        <div class="panel-body">
          <div class="list-group">
            <a href="#" class="list-group-item" v-for="item in items.hits" v-on:click="setItem(item)">
              {{ item[selectedType.labelField] }}
              <span v-if="item == selectedItem" class="badge">
                <span class="glyphicon glyphicon-chevron-right"></span>
              </span>
            </a>
          </div>
        </div>
      </div>
    </div>
    <div class="col-md-6">
      <div class="panel panel-default" v-if="selectedItem">
        <div class="panel-heading">
          <h2 class="panel-title">Details</h2>
        </div>
        <div class="panel-body" v-bind="getICE(selectedItem)">
          <table class="table" :key="selectedItem.craftercms.id + '' + selectedItemNumUpdates">
            <thead>
            <tr>
              <th>Field</th>
              <th>Value</th>
            </tr>
            </thead>
            <tbody>
              <tr>
                <td>id</td>
                <td>{{ selectedItem.craftercms.id }}</td>
              </tr>
              <template v-for="(value, field) in selectedItem">
                <tr v-if="field != 'craftercms' && field != 'biography_html_raw' && field != 'body_html_raw'">
                  <td>{{ field }}</td>
                  <td v-if="field == 'photo_s' || field == 'featuredImage_s'">
                    <img class="img-responsive img-rounded" v-bind:src="value" v-bind="getICE(selectedItem, field)"/>
                  </td>
                  <td v-else-if="field == 'body_html' || field == 'biography_html'">
                    <div v-html="value" v-bind="getICE(selectedItem, field)"></div>
                  </td>
                  <td v-else-if="field == 'authors_o'">{{ value.map(author => author.name_s).join(', ') }}</td>
                  <td v-else-if="Array.isArray(value)">
                    <span class="label label-primary" style="margin-right:5px" v-for="val in value">{{ val.value_smv }}</span>
                  </td>

                  <td v-else><span v-bind="getICE(selectedItem, field)">{{ value }}</span></td>
                </tr>
              </template>
              <tr v-if="selectedType.id == 'posts'">
                <td>dateCreated</td>
                <td>{{ new Date(selectedItem.craftercms.dateCreated).toDateString() }}</td>
              </tr>
              <tr>
                <td>itemUrl</td>
                <td>{{ selectedItem.craftercms.path }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>
<div class="modal fade" id="help-modal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span
                  aria-hidden="true"
          >&times;</span></button>
        <h4 class="modal-title">Help</h4>
      </div>
      <div class="modal-body">
          ${model.body_html}
      </div>
    </div>
  </div>
</div>
<script src="/static-assets/js/vendor/vue-2.6.12.min.js"></script>
<script src="/static-assets/js/vendor/vue-resource-1.5.2.min.js"></script>
<script src="/static-assets/js/vendor/jquery-3.2.1.min.js"></script>
<script src="/static-assets/js/vendor/bootstrap-3.3.7.min.js"></script>

<script src="/static-assets/js/vendor/rxjs-6.6.0.umd.min.js"></script>
<script src="/static-assets/js/craftercms-sdk/utils/utils.umd.js"></script>
<script src="/static-assets/js/craftercms-sdk/classes/classes.umd.js"></script>
<script src="/static-assets/js/craftercms-sdk/content/content.umd.js"></script>
<script src="/static-assets/js/craftercms-sdk/search/search.umd.js"></script>
<script src="/static-assets/js/main.js"></script>

<@crafter.body_bottom/>
</body>
</html>
