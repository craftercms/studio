(function($) {

   $(function() {
     var queryParam = $.urlParam('q');
     if (queryParam) {
       queryParam = decodeURI(queryParam).trim();
       $('#query').val(queryParam);
     }

     var source = $("#search-results-template").html();
     var template = Handlebars.compile(source);

     var doSearch = function(userTerm, categories) {
       var params = {};

       if (userTerm) {
         params.userTerm = userTerm;
       }
       if (categories) {
         params.categories = categories;
       }

       $.get("/api/search.json", params).done(function(data) {
         if (data == null) {
           data = [];
         }

         var context = { results: data };
         var html = template(context);

         $('#search-results').html(html);
       });
     }

     $('#categories input').click(function() {
       var categories = [];

       $('#categories input:checked').each(function() {
         categories.push($(this).val());
       });

       doSearch(queryParam, categories);
     });

     doSearch(queryParam);
   });

})(jQuery);
