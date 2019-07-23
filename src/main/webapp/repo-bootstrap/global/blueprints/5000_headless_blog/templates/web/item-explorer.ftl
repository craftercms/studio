<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>${model.title_s}</title>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<style>
			[v-cloak] { display:none; }
		</style>
		<meta name="viewport" content="width=device-width, initial-scale=1">
	</head>
	<body>
		<div id="browser" class="container" v-cloak>
			<div class="row">
				<div class="page-header">
					<h1>Headless Blog <small><span class="glyphicon glyphicon-question-sign" data-toggle="modal" data-target="#help-modal"/></small></h1>
				</div>
				<div class="col-md-2">
					<div class="panel panel-default">
						<div class="panel-heading"><h2 class="panel-title">Items</h2></div>
						<div class="panel-body">
							<div class="list-group">
								<a href="#" class="list-group-item" v-for="type in types" v-on:click="setType(type)">{{ type.label }}<span v-if="type == selectedType" class="badge"><span class="glyphicon glyphicon-chevron-right"/></span></a>
							</div>
						</div>
					</div>
				</div>
				<div class="col-md-4">
					<div class="panel panel-default" v-if="selectedType">
						<div class="panel-heading"><h2 class="panel-title">{{ selectedType.label }} ({{ items.total }})</h2></div>
						<div class="panel-body">
							<div class="list-group">
								<a href="#" class="list-group-item" v-for="item in items.items" v-on:click="setItem(item)">{{ item[selectedType.labelField] }}<span v-if="item == selectedItem" class="badge"><span class="glyphicon glyphicon-chevron-right"/></span></a>
							</div>
						</div>
					</div>
				</div>
				<div class="col-md-6">
					<div class="panel panel-default" v-if="selectedItem">
						<div class="panel-heading"><h2 class="panel-title">Details</h2></div>
						<div class="panel-body" v-bind:data-studio-component-path="selectedItem.itemUrl" v-bind:data-studio-component="selectedItem.itemUrl" data-studio-ice="" v-bind:data-studio-ice-path="selectedItem.itemUrl">
							<table class="table">
								<thead>
									<tr>
										<th>Field</th>
										<th>Value</th>
									</tr>
								</thead>
								<tbody>
									<tr v-for="(value, field) in selectedItem">
										<td>{{ field }}</td>
										<td v-if="field == 'photo' || field == 'featuredImage'"><img class="img-responsive img-rounded" v-bind:src="value"/></td>
										<td v-else-if="field == 'date'">{{ new Date(value).toDateString() }}</td>
										<td v-else-if="field == 'categories' || field == 'tags'">
											<span class="label label-primary" style="margin-right:5px" v-for="val in value">{{ val.label }}</span>
										</td>
										<td v-else-if="field == 'body' || field == 'biography'">
											<div v-html="value"></div>
										</td>
										<td v-else-if="Array.isArray(value)">{{ value.join(', ') }}</td>
										<td v-else>{{ value }}</td>
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
						<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span></button>
						<h4 class="modal-title">Help</h4>
					</div>
					<div class="modal-body">
						${model.body_html}
					</div>
				</div>
			</div>
		</div>
		<script src="https://unpkg.com/vue"></script>
		<script src="https://unpkg.com/vue-resource"></script>
		<script>
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
					setType: function(type) {
						this.selectedType = type;
						this.selectedItem = null;
						var self = this;
						this.$http.get(type.listUrl).then(function(response) {
							self.items = response.body
						});
					},
					setItem: function(item) {
						this.selectedItem = item;
                        this.$nextTick(function(){
                            if(window.studioICERepaint) {
								studioICERepaint();
							}
                        });
					}
				}
			});
		</script>
		<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<@studio.toolSupport/>
	</body>
</html>
