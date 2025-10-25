@echo off
if not exist "bin" mkdir bin
rmdir /s /q bin
mkdir bin
xcopy /s /e /i res bin\res
javac -d bin src\main\*.java src\piece\*.java
java -cp bin main.Main