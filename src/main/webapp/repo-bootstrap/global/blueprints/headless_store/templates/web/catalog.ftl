<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>${model.title}</title>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
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
						<li class="dropdown">
							<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Company<span v-if="selectedCompany">: {{ selectedCompany.name }}</span><span class="caret"></span></a>
							<ul class="dropdown-menu">
								<li v-for="company in companies.items" v-bind:class="{ active: selectedCompany == company }">
									<a href="#" v-on:click="updateCompany(selectedCompany != company? company : null)">
										{{ company.name }}
									</a>
								</li>
							</ul>
						</li>
						<li class="dropdown">
							<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Category<span class="caret"></span></a>
							<ul class="dropdown-menu">
								<li v-for="category in categories.items" v-if="selectedCategories.indexOf(category) == -1"><a href="#" v-on:click="updateCategories(category)">{{ category.label }}</a></li>
							</ul>
						</li>
						<li class="dropdown">
							<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Tag<span class="caret"></span></a>
							<ul class="dropdown-menu">
								<li v-for="tag in tags.items" v-if="selectedTags.indexOf(tag) == -1"><a href="#" v-on:click="updateTags(tag)">{{ tag.label }}</a></li>
							</ul>
						</li>
						<li class="dropdown">
							<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">Show: {{ pagination.rows }}<span class="caret"></span></a>
							<ul class="dropdown-menu">
								<li><a href="#" v-on:click="updateRows(5)">5</a></li>
								<li><a href="#" v-on:click="updateRows(10)">10</a></li>
								<li><a href="#" v-on:click="updateRows(20)">20</a></li>
							</ul>
						</li>
					</ul>
				</nav>
			</div>
			<div class="row" v-if="selectedTags.length > 0 || selectedCategories.length > 0">
				<div class="col-sm-12">
					<div class="panel panel-default">
						<div class="panel-body">
							<strong>Filtering By:&nbsp;</strong>
							<span class="label label-primary" style="margin-right:5px" v-for="category in selectedCategories">
								{{ category.label }}
								&nbsp;
								<span class="glyphicon glyphicon-remove" v-on:click="removeCategory(category)"></span>
							</span>
							<span class="label label-primary" style="margin-right:5px" v-for="tag in selectedTags">
								{{ tag.label }}
								&nbsp;
								<span class="glyphicon glyphicon-remove" v-on:click="removeTag(tag)"></span>
							</span>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-3" v-for="product in products.items">
					<div class="panel panel-default">
						<div class="panel-body">
							<img v-bind:src="product.image" class="img-responsive img-thumbnail center-block"/>
							<h4>{{ product.name }}<small> by {{ product.company }}<small></h4>
							<span class="badge pull-right">&#36;{{ product.price }}</span>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12" v-if="pagination.total > 1">
					<nav aria-label="Page navigation" class="text-center">
						<ul class="pagination">
							<li v-bind:class="{ disabled: !pagination.hasPrev }">
								<a href="#" aria-label="Previous" v-on:click="pagination.hasPrev? changePage(pagination.current - 1) : null">
									<span aria-hidden="true">&laquo;</span>
								</a>
							</li>
							<li v-for="n in pagination.total" v-bind:class="{ active: n == pagination.current }"><a href="#" v-on:click="changePage(n)">{{ n }}</a></li>
							<li v-bind:class="{ disabled: !pagination.hasNext }">
								<a href="#" aria-label="Next" v-on:click="pagination.hasNext? changePage(pagination.current + 1) : null">
									<span aria-hidden="true">&raquo;</span>
								</a>
							</li>
						</ul>
					</nav>
				</div>
			</div>
		</div>
		<script src="https://unpkg.com/vue"></script>
		<script src="https://unpkg.com/vue-resource"></script>
		<script>
			var catalog = new Vue({
				el: '#catalog',
				data: {
					companies: [],
					selectedCompany: null,
					categories: [],
					selectedCategories: [],
					tags: [],
					selectedTags: [],
					products: [],
					pagination: {
						rows: 5,
						total: 0,
						current: 1,
						hasPrev: false,
						hasNext: false
					}
				},
				methods: {
					updateCompany: function(company) {
						this.selectedCompany = company;
						this.loadProducts();
					},
					updateCategories: function(category) {
						this.selectedCategories.push(category);
						this.loadProducts();
					},
					removeCategory: function(category) {
						this.selectedCategories.splice(this.selectedCategories.indexOf(category), 1);
						this.loadProducts();
					},
					updateTags: function(tag) {
						this.selectedTags.push(tag);
						this.loadProducts();
					},
					removeTag: function(tag) {
						this.selectedTags.splice(this.selectedTags.indexOf(tag), 1);
						this.loadProducts();
					},
					updateRows: function(rows) {
						this.pagination.rows = rows;
						this.pagination.current = 1;
						this.pagination.hasPrev = false;
						this.loadProducts();
					},
					changePage: function(page) {
						this.pagination.current = page;
						this.pagination.hasPrev = page != 1;
						this.pagination.hasNext = page < this.pagination.total;
						this.loadProducts();
					},
					loadFilters: function() {
						var self = this;
						this.$http.get('/api/1/services/api/1/company/list.json').then(function(response) {
							self.companies = response.body;
						});
						this.$http.get('/api/1/services/api/1/category/list.json').then(function(response) {
							self.categories = response.body;
						});
						this.$http.get('/api/1/services/api/1/tag/list.json').then(function(response) {
							self.tags = response.body;
						});
					},
					loadProducts: function() {
						var self = this;
						var params = { 
							rows: this.pagination.rows,
							start: (this.pagination.current-1) * this.pagination.rows
						};
						if(this.selectedCompany) {
							params.company = this.selectedCompany.id;
						}
						if(this.selectedCategories.length > 0) {
							params.categories = this.selectedCategories.map(function(cat){ return cat.value }).join(",")
						}
						if(this.selectedTags.length > 0) {
							params.tags = this.selectedTags.map(function(tag){ return tag.value }).join(",")
						}
						this.$http.get('/api/1/services/api/1/product/list.json', { params: params }).then(function(response) {
							self.products = response.body;
							self.pagination.total = Math.ceil(parseInt(response.body.total) / self.pagination.rows);
							self.pagination.hasNext = self.pagination.current < self.pagination.total;
						});
					}
				},
				mounted: function() {
					this.loadFilters();
					this.loadProducts();
				}
			});
		</script>
		<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<@studio.toolSupport/>
	</body>
</html>