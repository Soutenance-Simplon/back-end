@REM -----------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM -----------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET DP0=%~dp0

@SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBasedir

@SET EXEC_DIR=%CD%
@SET WDIR=%EXEC_DIR%
:findBaseDir
@IF EXIST "%WDIR%"\.mvn goto baseDirFound
@cd ..
@IF "%WDIR%"=="%CD%" goto baseDirNotFound
@SET WDIR=%CD%
@goto findBaseDir

:baseDirFound
@SET MAVEN_PROJECTBASEDIR=%WDIR%
@cd "%EXEC_DIR%"
@goto endDetectBasedir

:baseDirNotFound
@SET MAVEN_PROJECTBASEDIR=%EXEC_DIR%
@cd "%EXEC_DIR%"

:endDetectBasedir

@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=
@SET MVNW_VERBOSE=false

@IF NOT "%MVNW_USERNAME%"=="" (
@SET WGET_ARGS=--http-user=%MVNW_USERNAME% --http-password=%MVNW_PASSWORD%
@SET CURL_ARGS=-u %MVNW_USERNAME%:%MVNW_PASSWORD%
)

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    @IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@IF EXIST %WRAPPER_JAR% (
    @IF "%MVNW_VERBOSE%"=="true" @echo Found %WRAPPER_JAR%
) ELSE (
    @IF NOT "%MVNW_VERBOSE%"=="true" @echo Downloading from: %DOWNLOAD_URL%
    @powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"^
        "}"
    @IF "%MVNW_VERBOSE%"=="true" @echo Finished downloading %WRAPPER_JAR%
)

@SET MAVEN_JAVA_EXE=%JAVA_HOME%\bin\java.exe
@set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@set CLASSPATH=%WRAPPER_JAR%
@"%JAVA_HOME%\bin\java.exe" ^
  %JVM_CONFIG_MAVEN_PROPS% ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath %CLASSPATH% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
@IF ERRORLEVEL 1 GOTO error
@GOTO end

:error
@EXIT /B 1

:end
@IF "%MVNW_VERBOSE%"=="true" (
  @ECHO Finished running Maven
)
