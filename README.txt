CONTROLE DE LACRES E GARANTIAS - APP ANDROID OFFLINE
======================================================

Este pacote contém uma primeira versão do aplicativo Android offline para controle de lacres e garantias.

O que foi criado:

1) Projeto Android Studio nativo em Java
   Pasta: app/
   Pacote: br.com.passosbrindes.controlelacres
   Nome do app: Controle de Lacres

2) Banco local SQLite
   O aplicativo grava os registros no banco interno do celular.
   O número do lacre é único dentro do aparelho, então o app não permite cadastrar o mesmo lacre duas vezes.

3) Backup e restauração em JSON
   O app usa SQLite como armazenamento principal.
   O JSON é usado para exportar e importar backup.

4) Versão HTML de teste
   Arquivo: web/controle-lacres-offline.html
   Essa versão serve apenas para testar a lógica e a tela no navegador.
   O app Android final usa SQLite; o HTML usa localStorage.

CAMPOS DO CADASTRO
==================

- Número do lacre
- Nome do cliente
- Telefone / WhatsApp
- Data da compra / cadastro
- Tipo de garantia:
  - Por quantidade de dias
  - Por data específica
- Quantidade de dias da garantia
- Data final da garantia
- Observação

RECURSOS JÁ INCLUÍDOS
=====================

- Cadastro de lacres
- Edição de registro
- Exclusão de registro
- Busca por lacre, cliente ou telefone
- Filtros:
  - Todos
  - Em garantia
  - Vencendo em até 30 dias
  - Vencidos
- Cálculo automático do vencimento quando a garantia é por dias
- Data atual preenchida automaticamente no cadastro
- Botão de WhatsApp quando houver telefone
- Exportação de backup JSON
- Importação de backup JSON
- Bloqueio de lacre repetido dentro do celular
- Funcionamento offline

COMO GERAR O APK NO ANDROID STUDIO
==================================

1. Extraia este ZIP no seu computador.
2. Abra o Android Studio.
3. Clique em: File > Open.
4. Selecione a pasta ControleLacresGarantia.
5. Aguarde o Android Studio baixar/sincronizar o Gradle e o Android SDK, se necessário.
6. Clique em: Build > Build Bundle(s) / APK(s) > Build APK(s).
7. O APK será gerado normalmente em:
   app/build/outputs/apk/debug/app-debug.apk

COMO INSTALAR NO CELULAR
========================

1. Copie o app-debug.apk para o celular.
2. Abra o arquivo no Android.
3. Se o Android pedir, permita instalar apps de fontes desconhecidas.
4. Instale o aplicativo.

IMPORTANTE
==========

Este ambiente do ChatGPT não possui Android SDK/Gradle instalado, por isso o APK final não foi compilado aqui.
O projeto está pronto para ser aberto e compilado no Android Studio.

RECOMENDAÇÃO PARA PRÓXIMA ETAPA
===============================

Antes de distribuir para clientes, recomendo testar em 1 ou 2 celulares Android reais e depois ajustar:

- Nome final do app
- Ícone final
- Cores e logomarca da sua empresa
- Tela de boas-vindas com instruções
- Possível campo de número da OS ou nome do produto/serviço
- Exportação automática de backup em intervalos
- Proteção com senha, se desejar

GERAR APK SEM ANDROID STUDIO
----------------------------

Também foi incluído um workflow do GitHub Actions para gerar o APK pela nuvem.
Arquivo:
.github/workflows/build-apk.yml

Veja as instruções completas em:
GERAR_APK_GITHUB_ACTIONS.txt
