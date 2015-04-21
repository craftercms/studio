<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Login</title>
</head>

<body>
	<h2>Login</h2>

	<#if RequestParameters.login_error??>
	<p>
		<#if Session.authenticationSystemException??>
		${model.authenticationSystemErrorMsg}
		<#elseif Session.userAuthenticationException??>
		${model.userAuthenticationErrorMsg}
		</#if>
	</p>
	</#if>
    
	<form name="login" action="/crafter-security-login" method="post">
		<label for="username">${model.usernameFieldLabel}</label><input type="text" name="username"><br/>
		<label for="password">${model.passwordFieldLabel}</label><input type="password" name="password"><br/>
		<input type="submit" value="${model.submitButtonLabel}">
	</form>

	<@cstudioOverlaySupport/>
</body>
</html>