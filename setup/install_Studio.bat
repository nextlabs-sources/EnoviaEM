@echo off
rem Installation script for Enovia EM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Installation script
rem
rem $Id: install_Studio.bat kent $
rem ---------------------------------------------------------------------------
set HOME_DIR=%~dp0
echo Current directories is %HOME_DIR%

if "%STUDIO_CUSTOM_APP_HOME%" == "" goto noHome

if exist "%STUDIO_CUSTOM_APP_HOME%" goto okPath
	echo Error:%STUDIO_CUSTOM_APP_HOME% is not exists in the system, installation abort
goto end

:okPath
mkdir %STUDIO_CUSTOM_APP_HOME%\nextlabs
mkdir %STUDIO_CUSTOM_APP_HOME%\nextlabs\conf
mkdir %STUDIO_CUSTOM_APP_HOME%\nextlabs\logs

xcopy %HOME_DIR%\..\nextlabs-enovia-em.jar %STUDIO_CUSTOM_APP_HOME%\
xcopy %HOME_DIR%\..\xlib\*.* %STUDIO_CUSTOM_APP_HOME%\
xcopy %HOME_DIR%\..\conf\*.* %STUDIO_CUSTOM_APP_HOME%\nextlabs\conf\
echo "Installation done"
goto end

:noHome
echo Error: STUDIO_CUSTOM_APP_HOME is not set, please set the STUDIO_CUSTOM_APP_HOME
echo Error: This environment variable is needed to run this program
goto end

:end
pause