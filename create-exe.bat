@echo off
echo Création de l'exécutable...

:: Vérification de l'existence de Launch4j
set LAUNCH4J_PATH="C:\Program Files (x86)\Launch4j\launch4j.exe"
if not exist %LAUNCH4J_PATH% (
    echo Launch4j n'est pas installé dans le chemin par défaut.
    echo Veuillez installer Launch4j depuis https://launch4j.sourceforge.net/
    echo et l'installer dans %LAUNCH4J_PATH%
    pause
    exit /b 1
)

:: Appel du script de build pour créer le JAR
call build.bat

:: Utilisation de Launch4j pour créer l'exe
%LAUNCH4J_PATH% launch4j-config.xml

echo.
if exist LavAuto.exe (
    echo L'exécutable LavAuto.exe a été créé avec succès !
    
    :: Création du dossier de distribution
    mkdir dist 2>nul
    copy LavAuto.exe dist\
    mkdir dist\lib 2>nul
    copy lib\*.jar dist\lib\
    
    echo.
    echo Les fichiers ont été copiés dans le dossier 'dist'
    echo Vous pouvez distribuer le contenu du dossier 'dist'
) else (
    echo Erreur lors de la création de l'exécutable.
)
pause 