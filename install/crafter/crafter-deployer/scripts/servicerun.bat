@echo off

call "/Users/rdanner/crafter-installs/2-5-0-test-1\scripts\setenv.bat"

:deployer
if ""%1"" == ""STOP"" goto deployer_stop
if ""%1"" == ""RESTART"" goto deployer_restart
if ""%1"" == ""START"" goto deployer_start

:deployer_start

Echo Starting Deploy agent...
cd /D "/Users/rdanner/crafter-installs/2-5-0-test-1\crafter-deployer"
call "/Users/rdanner/crafter-installs/2-5-0-test-1\crafter-deployer\start-deploy-agent.bat"
goto end

:deployer_stop
echo Deployer agent shutting down...
cd /D "/Users/rdanner/crafter-installs/2-5-0-test-1\crafter-deployer"
call "/Users/rdanner/crafter-installs/2-5-0-test-1\crafter-deployer\stop-deploy-agent.bat"
if ""%1"" == ""stop"" goto end
ping localhost -n 8 >NUL
if ""%1"" == ""restart"" goto deployer_start
goto end

:end
exit

:deployer_restart
goto deployer_stop
goto deployer_start
goto end
