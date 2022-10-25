@echo off
rem Installation script for Enovia EM
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Installation script
rem
rem $Id: installServer.bat kent $
rem ---------------------------------------------------------------------------set HOME_DIR=%~dp0
echo Current directories is %HOME_DIR%
if "%ENOVIA_SERVER_PATH%" == "" goto noESPath
if exist "%ENOVIA_SERVER_PATH%\mql.exe" goto okMQL
	echo Error:%ENOVIA_SERVER_PATH%\winnt\mql.exe is not exists in the system, installation abort
goto end

:noESPath
echo Error: ENOVIA_SERVER_PATH is not set, please set the ENOVIA_SERVER_PATH, installation abort
goto end
:okMQL
%ENOVIA_SERVER_PATH%\mql.exe install_JPO.tcl
echo "Installation done"
goto end
:end
pause