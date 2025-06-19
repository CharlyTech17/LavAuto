@echo off
:: Vérifier si Java est installé
java -version >nul 2>&1
if errorlevel 1 (
    echo Java n'est pas installé ou n'est pas dans le PATH.
    echo Veuillez installer Java depuis https://www.java.com/fr/download/
    pause
    exit /b 1
)

:: Définir le classpath avec toutes les dépendances
set CLASSPATH=LavAuto.jar;lib\*

:: Lancer l'application
start javaw -cp %CLASSPATH% com.lavauto.Main

:: En cas d'erreur, afficher les détails
if errorlevel 1 (
    echo Une erreur s'est produite lors du lancement de l'application.
    echo Vérifiez que tous les fichiers sont présents :
    echo - LavAuto.jar
    echo - Dossier lib avec les dépendances
    java -cp %CLASSPATH% com.lavauto.Main
    pause
) 