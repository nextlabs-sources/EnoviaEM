@echo off
rem Uninstallation script for Enovia EM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Installation script
rem
rem $Id: uninstall_JPO.bat kent $
rem ---------------------------------------------------------------------------
set HOME_DIR=%~dp0
echo Current directories is %HOME_DIR%

if "%ENOVIA_SERVER_PATH%" == "" goto noESPath

if exist "%ENOVIA_SERVER_PATH%\mql.exe" goto okMQL
	echo Error:%ENOVIA_SERVER_PATH%\winnt\mql.exe is not exists in the system, uninstallation abort
goto end

:noESPath
echo Error: ENOVIA_SERVER_PATH is not set, please set the ENOVIA_SERVER_PATH, uninstallation abort
goto end

:okMQL
%ENOVIA_SERVER_PATH%\mql.exe uninstall_JPO.tcl
echo "Uninstallation done"

goto end

:end

pause