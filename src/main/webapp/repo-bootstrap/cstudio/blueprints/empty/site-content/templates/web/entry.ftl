<#import "/templates/system/common/cstudio-support.ftl" as studio />

<html>
  <head>
  </head>
  
  <body>
	<h1>Empty Site</h1>
    <p>Welcome to your Crafter Site. </p>
    <p>This site blueprint is the equivalent of a Hello World.  It's a blank slate on which you can build
    your own digital experience.  For sites with built in features, try other blueprints or visit the Crafter CMS App Store
    to install features and other types of plugins in to this site.</p>
    <p>
        <ul>
            <li>To create new content types use the Admin Console under Site Content in the navigation</li>
            <li>To Update markup (including this text) edit this template under preview tools.</li>
            <li>To modify the text below this list click edit in the toolbar.</li>
            <li>To get features and other plugins use the Admin Console to access the Crafter CMS App Store</li>

        </ul>
     </p>

	${model.body}
	

		<@studio.toolSupport/>	
	</body>
</html>