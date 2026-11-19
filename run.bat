@echo off
rem run the program (compile.bat must be run first)
rem change PATH_TO_FX if your javafx sdk is somewhere else
set PATH_TO_FX=C:\Users\User\javafx-sdk-25.0.1\lib

java --module-path "%PATH_TO_FX%" --add-modules javafx.controls -cp out main.Main
pause
