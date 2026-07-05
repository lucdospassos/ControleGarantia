Controle de Lacres e Garantias - v1.5.0 estável

Aplicativo Android offline para controle de lacres de garantia.

Funcionalidades principais:
- Cadastro de número do lacre.
- Número do lacre aceita letras e números, converte para maiúsculo e remove espaços.
- Cadastro de cliente, telefone/WhatsApp, data da compra/cadastro e observação.
- Garantia por quantidade de dias ou por data específica.
- Cálculo automático do vencimento.
- Busca por lacre, cliente ou telefone.
- Filtros: todos, em garantia, vencendo em até 30 dias e vencidos.
- Paginação da lista de registros: padrão 5 por página, com opções 5, 10, 20, 50 e todos.
- Bloqueio de lacre repetido dentro do celular.
- Dados salvos no banco SQLite local do Android.
- Exportação e importação em JSON.
- Botão Comprar lacres: (81) 98849-6130 via WhatsApp.
- Ícone próprio do aplicativo.
- Workflow GitHub Actions para APK debug e release assinado.

Arquivos importantes:
- .github/workflows/build-apk.yml
- CONFIGURAR_RELEASE_ASSINADO_GITHUB_ACTIONS.txt
- scripts/gerar_chave_release_windows.bat
- docs/icone-controle-lacres-v1.5.png

Para gerar o APK debug:
Actions > Gerar APK Android > Run workflow
Baixe o artifact ControleLacresGarantia-v1.5-debug-apk.

Para gerar o APK release assinado:
Leia CONFIGURAR_RELEASE_ASSINADO_GITHUB_ACTIONS.txt.
