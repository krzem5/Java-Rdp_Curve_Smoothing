echo off
echo NUL>_.class&&del /s /f /q *.class
cls
javac com/krzem/rdp_curve_smoothing/Main.java&&java com/krzem/rdp_curve_smoothing/Main
start /min cmd /c "echo NUL>_.class&&del /s /f /q *.class"