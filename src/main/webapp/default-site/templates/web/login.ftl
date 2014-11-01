



<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>Crafter Software &raquo; Login</title>
   <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
   <script type="text/javascript" src="/studio/static-assets/js/studio.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/guest/login.js"></script>
   <script type="text/javascript">//<![CDATA[
new Alfresco.component.Login("page_x002e_components_x002e_slingshot-login_x0023_default").setOptions({"lastUsername": null, "error": false, "errorDisplay": "container"}).setMessages({});
//]]></script>
   <style type="text/css" media="screen">
      @import url("/studio/static-assets/css/yui-fonts-grids.css");
      @import url("/studio/static-assets/yui/columnbrowser/assets/columnbrowser.css");
      @import url("/studio/static-assets/yui/columnbrowser/assets/skins/default/columnbrowser.css");
      @import url("/studio/static-assets/themes/lightTheme/yui/assets/skin.css");
      @import url("/studio/static-assets/css/base.css");
      @import url("/studio/static-assets/css/yui-layout.css");
      @import url("/studio/static-assets/themes/lightTheme/presentation.css");
   </style>

   <style type="text/css" media="screen">
      @import url("/studio/static-assets/js/lib/dojo-1.9.0/dijit/themes/claro/claro.css");
      @import url("/studio/static-assets/js/alfresco/css/global.css");
   </style>

   <style type="text/css" media="screen">
      @import url("/studio/static-assets/components/guest/login.css");
   </style>
   <link rel="stylesheet" type="text/css" href="/studio/themes/cstudioTheme/login-extension.css"></link>

   <!-- MSIE CSS fix overrides -->
   <!--[if lt IE 7]><link rel="stylesheet" type="text/css" href='/studio/static-assets/css/ie6.css'/><![endif]-->
   <!--[if IE 7]><link rel="stylesheet" type="text/css" href='/studio/static-assets/css/ie7.css'/><![endif]-->


</head>

<body id="Share" class="yui-skin-lightTheme alfresco-share alfresco-guest claro">
   <div class="sticky-wrapper">
      <div id="doc3">
<div id="page_x002e_components_x002e_slingshot-login">
    <div id="page_x002e_components_x002e_slingshot-login_x0023_default">

      <div id="page_x002e_components_x002e_slingshot-login_x0023_default-body" class="theme-overlay login">
      
         <div class="theme-company-logo"></div>
      
         <script type="text/javascript">//<![CDATA[
            document.cookie = "_alfTest=_alfTest";
            var cookieEnabled = (document.cookie.indexOf("_alfTest") !== -1);
            if (!cookieEnabled)
            {
               document.write('<div class="error">Cookies must be enabled in your browser.</div>');
            }
         //]]></script>
      
         <form id="page_x002e_components_x002e_slingshot-login_x0023_default-form" accept-charset="UTF-8" method="post" action="/studio/page/dologin" class="form-fields login UNKNOWN">
            <input type="hidden" id="page_x002e_components_x002e_slingshot-login_x0023_default-success" name="success" value="/studio/page/"/>
            <input type="hidden" name="failure" value="/studio/page/type/login?error=true"/>
            <div class="form-field">
               <label for="page_x002e_components_x002e_slingshot-login_x0023_default-username">User Name</label><br/>
               <input type="text" id="page_x002e_components_x002e_slingshot-login_x0023_default-username" name="username" maxlength="255" value="" />
            </div>
            <div class="form-field">
               <label for="page_x002e_components_x002e_slingshot-login_x0023_default-password">Password</label><br/>
               <input type="password" id="page_x002e_components_x002e_slingshot-login_x0023_default-password" name="password" maxlength="255" />
            </div>
            <div class="form-field">
               <input type="submit" id="page_x002e_components_x002e_slingshot-login_x0023_default-submit" class="login-button" value="Login"/>
            </div>
         </form>
      
         <div class="copy">&copy; 2007-2014 Crafter Software Corp. All rights reserved.</div>
  

      </div>
    </div>

</div>      </div>
      <div class="sticky-push"></div>
   </div>

   <div class="sticky-footer">
   </div>
   <div id="alfresco-yuiloader"></div>

</body>
</html>
