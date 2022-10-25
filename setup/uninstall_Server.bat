@echo off
rem Uninstallation script for Enovia EM
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Installation script
rem
rem $Id: installServer.bat kent $
rem ---------------------------------------------------------------------------
set HOME_DIR=%~dp0echo Current directories is %HOME_DIR%
if "%DSPACE_CUSTOM_APP_HOME%" == "" goto noHome
if exist "%DSPACE_CUSTOM_APP_HOME%" goto okPath
	echo Error:%DSPACE_CUSTOM_APP_HOME% is not exists in the system, uninstallation abort
goto end
:okPath
rmdir /Q /S %DSPACE_CUSTOM_APP_HOME%\nextlabs
del %DSPACE_CUSTOM_APP_HOME%\commons-lang3-3.3.2.jar
del %DSPACE_CUSTOM_APP_HOME%\commons-configuration-1.8.jar
del %DSPACE_CUSTOM_APP_HOME%\commons-logging-1.1.1.jar
del %DSPACE_CUSTOM_APP_HOME%\ehcache-2.10.3.jar
del %DSPACE_CUSTOM_APP_HOME%\log4j-1.2.17.jar
del %DSPACE_CUSTOM_APP_HOME%\slf4j-api-1.7.21.jar
del %DSPACE_CUSTOM_APP_HOME%\slf4j-ext-1.7.21.jardel %DSPACE_CUSTOM_APP_HOME%\nextlabs-openaz-pep.jardel %DSPACE_CUSTOM_APP_HOME%\openaz-pep-0.0.1-SNAPSHOT.jardel %DSPACE_CUSTOM_APP_HOME%\openaz-xacml-0.0.1-SNAPSHOT.jardel %DSPACE_CUSTOM_APP_HOME%\jackson-databind-2.6.3.jardel %DSPACE_CUSTOM_APP_HOME%\jackson-core-2.6.3.jardel %DSPACE_CUSTOM_APP_HOME%\jackson-annotations-2.6.0.jardel %DSPACE_CUSTOM_APP_HOME%\httpcore-4.3.jardel %DSPACE_CUSTOM_APP_HOME%\httpclient-4.3.1.jardel %DSPACE_CUSTOM_APP_HOME%\guava-19.0.jardel %DSPACE_CUSTOM_APP_HOME%\crypt.jardel %DSPACE_CUSTOM_APP_HOME%\commons-lang-2.6.jar
del %DSPACE_CUSTOM_APP_HOME%\nextlabs-enovia-em.jar
echo "Uninstallation done!"
goto end
:noHome
echo Error: DSPACE_CUSTOM_APP_HOME is not set, please set the DSPACE_CUSTOM_APP_HOME
echo Error: This environment variable is needed to run this program
goto end
:end
pause