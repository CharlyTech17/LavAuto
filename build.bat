@echo off
echo Compilation du projet...
mkdir target\classes 2>nul

:: Nettoyage des anciens fichiers
if exist dist rmdir /s /q dist
if exist target rmdir /s /q target
mkdir target\classes

:: Compilation
echo Compilation des fichiers Java...
javac -d target/classes -cp "lib/*" ^
src/main/java/com/lavauto/Main.java ^
src/main/java/com/lavauto/model/*.java ^
src/main/java/com/lavauto/dao/*.java ^
src/main/java/com/lavauto/ui/*.java ^
src/main/java/com/lavauto/db/*.java

if errorlevel 1 (
    echo Erreur lors de la compilation
    pause
    exit /b 1
)

:: Création du JAR
echo Création du JAR...
cd target/classes
jar cfm ../LavAuto.jar ../../src/main/resources/META-INF/MANIFEST.MF com
cd ../..

:: Création du package de distribution
echo.
echo Création du package de distribution...
mkdir dist 2>nul
copy target\LavAuto.jar dist\
copy LavAuto.bat dist\
mkdir dist\lib 2>nul
copy lib\*.jar dist\lib\

:: Création du fichier README
echo Comment utiliser LavAuto : > dist\README.txt
echo 1. Assurez-vous que Java est installé (https://www.java.com/fr/download/) >> dist\README.txt
echo 2. Double-cliquez sur LavAuto.bat pour lancer l'application >> dist\README.txt
echo 3. Si l'application ne se lance pas, vérifiez que Java est bien installé >> dist\README.txt
echo 4. En cas d'erreur avec la base de données, vérifiez la configuration dans le fichier config.properties >> dist\README.txt

:: Copie du fichier de configuration
if exist config.properties (
    copy config.properties dist\
) else (
    echo # Configuration de la base de données > dist\config.properties
    echo db.url=jdbc:mysql://localhost:3306/lavauto >> dist\config.properties
    echo db.user=root >> dist\config.properties
    echo db.password= >> dist\config.properties
)

echo.
echo Package de distribution créé dans le dossier 'dist'
echo Construction terminée !
pause 