@echo off
cls
if exist build rmdir /s /q build
mkdir build
cd src
javac -d ../build com/krzem/rdp_curve_smoothing/Main.java&&jar cvmf ../manifest.mf ../build/rdp_curve_smoothing.jar -C ../build *&&goto run
cd ..
goto end
:run
cd ..
pushd "build"
for /D %%D in ("*") do (
	rd /S /Q "%%~D"
)
for %%F in ("*") do (
	if /I not "%%~nxF"=="rdp_curve_smoothing.jar" del "%%~F"
)
popd
cls
java -jar build/rdp_curve_smoothing.jar
:end
