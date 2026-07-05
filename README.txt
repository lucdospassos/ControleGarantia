Controle de Lacres e Garantias - Android Offline
Versão: 1.1.0

Aplicativo Android offline para registrar uso de lacres de garantia.

Recursos principais:
- Cadastro de número do lacre.
- Aceita letras e números no lacre.
- Converte o lacre automaticamente para maiúsculo.
- Remove espaços em branco do lacre automaticamente.
- Nome do cliente.
- Telefone / WhatsApp.
- Data da compra/cadastro no padrão brasileiro DD/MM/AAAA.
- Garantia por quantidade de dias.
- Garantia por data específica.
- Cálculo automático da data final da garantia.
- Campo de observação.
- Busca por lacre, cliente ou telefone.
- Filtros: todos, em garantia, vencendo em até 30 dias, vencidos.
- Edição e exclusão de registros.
- Não permite repetir o mesmo lacre dentro do celular.
- Banco SQLite local no próprio aparelho.
- Exportação/importação de backup JSON.
- Botão "Comprar Lacre de garantia" apontando para o WhatsApp 81988496130.

Arquivos importantes:
- app/: projeto Android.
- web/controle-lacres-offline.html: versão HTML simples de teste.
- .github/workflows/build-apk.yml: workflow do GitHub Actions.
- GERAR_APK_GITHUB_ACTIONS.txt: instruções para gerar APK debug.
- CONFIGURAR_RELEASE_ASSINADO_GITHUB_ACTIONS.txt: instruções para gerar APK release assinado.
- CHANGELOG_v1.1.0.txt: resumo das mudanças desta versão.

Observações:
- O banco SQLite fica dentro da área privada do app no Android.
- Os dados permanecem ao fechar, desligar ou reiniciar o celular.
- Os dados podem ser apagados se o usuário desinstalar o app, limpar dados do app ou restaurar o celular.
- Use o botão Exportar JSON para criar backups.
- Antes de atualizar o app em aparelhos com dados reais, exporte um backup JSON.
