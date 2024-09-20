@echo off

REM ********************************************************* Auros Query Tool ****************************************************************
REM *                                                 Copyright Frank Nauroth, Siemens 2019                                                   *
REM *  Usage(see also help section):                                                                                                          *
REM *  RunReport.bat [CHECKLIST|ISSUELIST] [ATTRIBUTE1=VALUE1 ATTRIBUTE2=VALUE2 ... ] [suffix=<free text>] [OUTPUT DIRECTORY]                                      *
REM *                                                                                                                                         *
REM *  Result will be stored in Excel Format                                                                                                  *
REM *                                                                                                                                         *
REM *******************************************************************************************************************************************

if "%1"=="" goto Help
if "%1"=="-h"    goto Help
if "%1"=="-help" goto Help
if "%1"=="?"     goto Help   


echo Starting Auros Report v1.6...
C:\Progra~1\Java\jre8\bin\java -Xmx512M -jar %~dp0auros.jar %*

goto Exit




:Exit
echo.
echo Auros Report finished. See output for potential errors.
echo Press any key to terminate this Window
pause
goto Finish

:Help

echo:

echo Usage: RunReport.bat [CHECKLIST^|ISSUELIST] [ATTRIBUTE1=VALUE1 ATTRIBUTE2=VALUE2 ... ] [OUTPUT DIRECTORY]

echo:
echo Samples:
echo --------
echo Example 1: RunReport.bat ISSUELIST "Project=BX726_2020.25" "IssueType=DPA5" "UnUp=Un" "suffix=BX726_2020.25_DPA5" "C:\\tmp\\"
echo Example 4: RunReport.bat ISSUELIST "Status=closed" "Project=B500_2017" "IssueType=DPA5" "UnUp=UN" "suffix=B500_2017_DPA5" "C:\\Users\\fnaurot1\\Desktop\\Auros Output Folder\\"
echo Example 2: RunReport.bat CHECKLIST "cops=FA_DPA5" "status=Evaluation Ready" "Project=B500_2017" "UnUp=UN,UP" "suffix=B500_2017_DPA5" "C:\\tmp\\"
echo Example 3: RunReport.bat CHECKLIST "cops=FA_DPA5" "status=Evaluation Ready" "Project=B515_2016" "CBG=FSAO" "suffix=B515_2016_DPA5_FSAO"  "C:\\Users\\fnaurot1\\Desktop\\Auros Output Folder\\"
echo:
pause

:Finish               