<#import "/templates/system/common/cstudio-support.ftl" as studio />

<html>
  <head>
  </head>
  
  <body>
    <h1>Hello!</h1>
    <p>Welcome to your Crafter Site. </p>
    <p>This site blueprint is the equivalent of a Hello World.  It's a blank slate on which you can build
    your own digital experience.<p>
        <ul>
            <li>To create new content types use the Admin Console under Site Content in the navigation.</li>
            <li>To Update markup (including this text) edit this template by going to Preview Tools (wrench on top right)  and then In-Context Editing and then Edit Template</li>
            <li>To modify the text below this list click edit in the toolbar above.</li>
        </ul>
     </p>
    <div id='exampleAuthoredContent' <@studio.iceAttr iceGroup="body"/>>
    ${model.body}
    </div>
    

        <@studio.toolSupport/>    
    </body>
</html>
