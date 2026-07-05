@echo off
setlocal

echo ============================================================
echo Gerador de chave release - Controle de Lacres e Garantias

echo Este script cria uma chave .jks e um arquivo Base64 para usar

echo nos Secrets do GitHub Actions.
echo ============================================================
echo.

set KEYSTORE_FILE=controlelacres-release-key.jks
set KEY_ALIAS=controlelacres

set /p STORE_PASSWORD=Digite uma senha forte para RELEASE_STORE_PASSWORD: 
set /p KEY_PASSWORD=Digite uma senha forte para RELEASE_KEY_PASSWORD: 

echo.
echo Criando chave de assinatura...
keytool -genkeypair -v -keystore "%KEYSTORE_FILE%" -storetype JKS -keyalg RSA -keysize 2048 -validity 10000 -alias "%KEY_ALIAS%" -storepass "%STORE_PASSWORD%" -keypass "%KEY_PASSWORD%" -dname "CN=Passos Brindes, OU=Controle Lacres, O=Passos Brindes, L=Recife, ST=PE, C=BR"

if errorlevel 1 (
  echo.
  echo ERRO: Nao foi possivel executar o keytool.
  echo Instale o Java JDK 17 ou superior e tente novamente.
  pause
  exit /b 1
)

echo.
echo Convertendo a chave para Base64...
powershell -NoProfile -Command "[Convert]::ToBase64String([IO.File]::ReadAllBytes('%KEYSTORE_FILE%')) | Set-Content -NoNewline 'android-keystore-base64.txt'"

echo.
echo ============================================================
echo Arquivos criados:
echo   %KEYSTORE_FILE%
echo   android-keystore-base64.txt
echo.
echo Secrets para cadastrar no GitHub:
echo   ANDROID_KEYSTORE_BASE64 = conteudo do arquivo android-keystore-base64.txt
echo   RELEASE_STORE_PASSWORD  = a primeira senha digitada
echo   RELEASE_KEY_ALIAS       = %KEY_ALIAS%
echo   RELEASE_KEY_PASSWORD    = a segunda senha digitada
echo.
echo IMPORTANTE: Guarde o arquivo %KEYSTORE_FILE% em local seguro.
echo Nunca envie esse arquivo para o GitHub.
echo ============================================================
pause
