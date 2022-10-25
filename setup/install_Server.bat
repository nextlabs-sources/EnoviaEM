@echo off
rem Installation script for Enovia EM
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Installation script
rem
rem $Id: installServer.bat kent $
rem ---------------------------------------------------------------------------
set HOME_DIR=%~dp0
echo Current directories is %HOME_DIR%
if "%DSPACE_CUSTOM_APP_HOME%" == "" goto noHome
if exist "%DSPACE_CUSTOM_APP_HOME%\" goto okPath
	echo Error:%DSPACE_CUSTOM_APP_HOME% is not exists in the system, installation abort
goto end
:okPath
mkdir %DSPACE_CUSTOM_APP_HOME%\nextlabs
mkdir %DSPACE_CUSTOM_APP_HOME%\nextlabs\conf
mkdir %DSPACE_CUSTOM_APP_HOME%\nextlabs\logs

xcopy %HOME_DIR%\..\nextlabs-enovia-em.jar %DSPACE_CUSTOM_APP_HOME%\
xcopy %HOME_DIR%\..\xlib\*.* %DSPACE_CUSTOM_APP_HOME%\
xcopy %HOME_DIR%\..\conf\*.* %DSPACE_CUSTOM_APP_HOME%\nextlabs\conf\
echo "Installation done"
goto end
:noHome
echo Error:DSPACE_CUSTOM_APP_HOME is not set, please set the DSPACE_CUSTOM_APP_HOME
echo Error:This environment variable is needed to run this program
goto end
:end
pause