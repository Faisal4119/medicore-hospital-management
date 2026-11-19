@echo off
rem compile the whole project into the "out" folder
rem change PATH_TO_FX if your javafx sdk is somewhere else
set PATH_TO_FX=C:\Users\User\javafx-sdk-25.0.1\lib

if not exist out mkdir out
javac --module-path "%PATH_TO_FX%" --add-modules javafx.controls -d out src\exceptions\*.java src\interfaces\*.java src\model\*.java src\storage\*.java src\gui\*.java src\main\*.java

if %errorlevel% equ 0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)
pause
