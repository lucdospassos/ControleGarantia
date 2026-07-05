package br.com.passosbrindes.controlelacres;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int REQ_EXPORT = 2001;
    private static final int REQ_IMPORT = 2002;

    private DatabaseHelper db;
    private String editandoId = null;

    private EditText txtLacre;
    private EditText txtCliente;
    private EditText txtTelefone;
    private EditText txtDataCadastro;
    private RadioButton rbDias;
    private RadioButton rbData;
    private EditText txtDias;
    private EditText txtDataVencimento;
    private EditText txtObservacao;
    private TextView lblVencimentoCalculado;
    private Button btnSalvar;
    private EditText txtBusca;
    private Spinner spFiltro;
    private LinearLayout listaContainer;
    private TextView lblContagem;
    private Spinner spRegistrosPorPagina;
    private TextView lblPaginaInfo;
    private Button btnPaginaAnterior;
    private Button btnPaginaProxima;
    private int paginaAtual = 1;
    private int registrosPorPagina = 5;


    private final String[] filtroLabels = {"Todos", "Em garantia", "Vencendo em até 30 dias", "Vencidos"};
    private final String[] filtroKeys = {"todos", "vigentes", "proximos", "vencidos"};
    private final String[] paginaLabels = {"5 por página", "10 por página", "20 por página", "50 por página", "Todos"};
    private final int[] paginaValores = {5, 10, 20, 50, 0};
    private String filtroAtual = "todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this);
        criarTela();
        preencherDataHoje();
        atualizarModoGarantia();
        renderizarLista();
    }

    private void criarTela() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14), dp(14), dp(24));
        root.setBackgroundColor(Color.rgb(244, 246, 248));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView titulo = new TextView(this);
        titulo.setText("Controle de Lacres e Garantias");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(22);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setPadding(dp(16), dp(18), dp(16), dp(4));

        TextView subtitulo = new TextView(this);
        subtitulo.setText("Funciona offline. Os dados ficam gravados no banco local do celular e podem ser exportados em JSON. Versão estável 1.5.");
        subtitulo.setTextColor(Color.rgb(207, 227, 220));
        subtitulo.setTextSize(13);
        subtitulo.setPadding(dp(16), 0, dp(16), dp(18));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(Color.rgb(22, 79, 65));
        header.addView(titulo);
        header.addView(subtitulo);
        root.addView(header, matchWrap());

        Button btnComprarLacre = button("Comprar lacres: (81) 98849-6130", true);
        btnComprarLacre.setOnClickListener(v -> abrirCompraLacre());
        root.addView(btnComprarLacre, matchWrapTop(dp(10)));

        LinearLayout cardCadastro = card();
        txtLacre = editText("Ex: A01567", InputType.TYPE_CLASS_TEXT);
        instalarNormalizacaoLacre();
        txtCliente = editText("Ex: Maria Souza", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        txtTelefone = editText("Ex: (81) 99999-9999", InputType.TYPE_CLASS_PHONE);
        txtDataCadastro = editText("DD/MM/AAAA", InputType.TYPE_NULL);
        txtDataCadastro.setFocusable(false);
        txtDataCadastro.setOnClickListener(v -> escolherData(txtDataCadastro));

        cardCadastro.addView(label("Número do lacre *"));
        cardCadastro.addView(txtLacre);
        cardCadastro.addView(label("Nome do cliente *"));
        cardCadastro.addView(txtCliente);
        cardCadastro.addView(label("Telefone / WhatsApp"));
        cardCadastro.addView(txtTelefone);
        cardCadastro.addView(label("Data da compra / cadastro *"));
        cardCadastro.addView(txtDataCadastro);

        RadioGroup grupo = new RadioGroup(this);
        grupo.setOrientation(RadioGroup.VERTICAL);
        rbDias = new RadioButton(this);
        rbDias.setText("Garantia por quantidade de dias");
        rbDias.setTextSize(15);
        rbData = new RadioButton(this);
        rbData.setText("Garantia até uma data específica");
        rbData.setTextSize(15);
        grupo.addView(rbDias);
        grupo.addView(rbData);
        rbDias.setChecked(true);
        grupo.setOnCheckedChangeListener((g, id) -> atualizarModoGarantia());

        cardCadastro.addView(label("Tipo de garantia"));
        cardCadastro.addView(grupo);

        txtDias = editText("Ex: 90", InputType.TYPE_CLASS_NUMBER);
        txtDataVencimento = editText("DD/MM/AAAA", InputType.TYPE_NULL);
        txtDataVencimento.setFocusable(false);
        txtDataVencimento.setOnClickListener(v -> escolherData(txtDataVencimento));
        lblVencimentoCalculado = new TextView(this);
        lblVencimentoCalculado.setTextColor(Color.rgb(91, 100, 114));
        lblVencimentoCalculado.setTextSize(13);
        lblVencimentoCalculado.setPadding(0, dp(6), 0, dp(2));

        cardCadastro.addView(label("Quantidade de dias"));
        cardCadastro.addView(txtDias);
        cardCadastro.addView(label("Data final da garantia"));
        cardCadastro.addView(txtDataVencimento);
        cardCadastro.addView(lblVencimentoCalculado);

        txtObservacao = editText("Ex: Troca de tela, manutenção, número da OS etc.", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        txtObservacao.setMinLines(3);
        txtObservacao.setGravity(Gravity.TOP | Gravity.START);
        cardCadastro.addView(label("Observação"));
        cardCadastro.addView(txtObservacao);

        btnSalvar = button("Salvar registro", true);
        btnSalvar.setOnClickListener(v -> salvarRegistro());
        cardCadastro.addView(btnSalvar, matchWrapTop(dp(14)));

        Button btnLimpar = button("Limpar campos", false);
        btnLimpar.setOnClickListener(v -> limparCampos());
        cardCadastro.addView(btnLimpar, matchWrapTop(dp(8)));

        root.addView(cardCadastro, matchWrapTop(dp(14)));

        LinearLayout cardBusca = card();
        txtBusca = editText("Buscar por lacre, cliente ou telefone", InputType.TYPE_CLASS_TEXT);
        txtBusca.addTextChangedListener(simpleWatcher(() -> { paginaAtual = 1; renderizarLista(); }));
        spFiltro = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtroLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltro.setAdapter(adapter);
        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtroAtual = filtroKeys[position];
                paginaAtual = 1;
                renderizarLista();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        cardBusca.addView(label("Buscar"));
        cardBusca.addView(txtBusca);
        cardBusca.addView(label("Filtro"));
        cardBusca.addView(spFiltro);
        root.addView(cardBusca, matchWrapTop(dp(14)));

        LinearLayout botoesBackup = new LinearLayout(this);
        botoesBackup.setOrientation(LinearLayout.HORIZONTAL);
        botoesBackup.setGravity(Gravity.CENTER);
        botoesBackup.setBaselineAligned(false);
        Button btnExportar = button("Exportar JSON", false);
        Button btnImportar = button("Importar JSON", false);
        btnExportar.setOnClickListener(v -> exportarJson());
        btnImportar.setOnClickListener(v -> importarJson());
        botoesBackup.addView(btnExportar, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        LinearLayout.LayoutParams gapParams = new LinearLayout.LayoutParams(dp(8), 1);
        TextView gap = new TextView(this);
        botoesBackup.addView(gap, gapParams);
        botoesBackup.addView(btnImportar, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(botoesBackup, matchWrapTop(dp(12)));

        LinearLayout headerRegistros = new LinearLayout(this);
        headerRegistros.setOrientation(LinearLayout.HORIZONTAL);
        headerRegistros.setGravity(Gravity.CENTER_VERTICAL);
        headerRegistros.setPadding(dp(4), dp(18), dp(4), dp(8));

        lblContagem = new TextView(this);
        lblContagem.setTextColor(Color.rgb(91, 100, 114));
        lblContagem.setTypeface(Typeface.DEFAULT_BOLD);
        lblContagem.setTextSize(13);
        headerRegistros.addView(lblContagem, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        spRegistrosPorPagina = new Spinner(this);
        ArrayAdapter<String> adapterPaginas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paginaLabels);
        adapterPaginas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegistrosPorPagina.setAdapter(adapterPaginas);
        spRegistrosPorPagina.setSelection(0);
        spRegistrosPorPagina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                registrosPorPagina = paginaValores[position];
                paginaAtual = 1;
                renderizarLista();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        headerRegistros.addView(spRegistrosPorPagina, new LinearLayout.LayoutParams(dp(165), ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(headerRegistros, matchWrap());

        listaContainer = new LinearLayout(this);
        listaContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listaContainer, matchWrap());

        LinearLayout controlesPagina = new LinearLayout(this);
        controlesPagina.setOrientation(LinearLayout.HORIZONTAL);
        controlesPagina.setGravity(Gravity.CENTER_VERTICAL);
        controlesPagina.setPadding(0, dp(8), 0, 0);

        btnPaginaAnterior = button("Anterior", false);
        btnPaginaAnterior.setOnClickListener(v -> {
            if (paginaAtual > 1) {
                paginaAtual--;
                renderizarLista();
            }
        });

        lblPaginaInfo = new TextView(this);
        lblPaginaInfo.setTextColor(Color.rgb(91, 100, 114));
        lblPaginaInfo.setTextSize(13);
        lblPaginaInfo.setGravity(Gravity.CENTER);

        btnPaginaProxima = button("Próxima", false);
        btnPaginaProxima.setOnClickListener(v -> {
            paginaAtual++;
            renderizarLista();
        });

        controlesPagina.addView(btnPaginaAnterior, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        controlesPagina.addView(lblPaginaInfo, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        controlesPagina.addView(btnPaginaProxima, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(controlesPagina, matchWrap());

        txtDias.addTextChangedListener(simpleWatcher(() -> atualizarVencimentoCalculado()));
        txtDataCadastro.addTextChangedListener(simpleWatcher(() -> atualizarVencimentoCalculado()));
        txtDataVencimento.addTextChangedListener(simpleWatcher(() -> atualizarVencimentoCalculado()));

        setContentView(scroll);
    }

    private void preencherDataHoje() {
        txtDataCadastro.setText(DateUtils.toBrDate(DateUtils.todayIso()));
        txtDias.setText("90");
        atualizarVencimentoCalculado();
    }

    private void atualizarModoGarantia() {
        boolean porDias = rbDias.isChecked();
        txtDias.setVisibility(porDias ? View.VISIBLE : View.GONE);
        txtDataVencimento.setVisibility(porDias ? View.GONE : View.VISIBLE);
        atualizarVencimentoCalculado();
    }

    private void atualizarVencimentoCalculado() {
        String venc = calcularVencimento();
        if (venc == null || venc.trim().isEmpty()) {
            lblVencimentoCalculado.setText("");
        } else {
            lblVencimentoCalculado.setText("Vencimento calculado: " + DateUtils.toBrDate(venc) + " · " + statusPorData(venc));
        }
    }

    private String calcularVencimento() {
        if (rbDias.isChecked()) {
            String base = DateUtils.toIsoDate(txtDataCadastro.getText().toString().trim());
            String diasTxt = txtDias.getText().toString().trim();
            if (base.isEmpty() || diasTxt.isEmpty()) return null;
            try {
                int dias = Integer.parseInt(diasTxt);
                return DateUtils.addDays(base, dias);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return DateUtils.toIsoDate(txtDataVencimento.getText().toString().trim());
    }

    private String statusPorData(String iso) {
        int diff = DateUtils.daysBetweenToday(iso);
        if (diff < 0) return "vencida";
        if (diff <= 30) return "vence em " + diff + " dia(s)";
        return "em garantia";
    }

    private void salvarRegistro() {
        String lacre = DateUtils.normalizarLacre(txtLacre.getText().toString());
        txtLacre.setText(lacre);
        String cliente = txtCliente.getText().toString().trim();
        String telefone = txtTelefone.getText().toString().trim();
        String dataCadastro = DateUtils.toIsoDate(txtDataCadastro.getText().toString().trim());
        String observacao = txtObservacao.getText().toString().trim();
        String vencimento = calcularVencimento();

        if (lacre.isEmpty() || cliente.isEmpty() || dataCadastro.isEmpty() || vencimento == null || vencimento.isEmpty()) {
            aviso("Preencha número do lacre, cliente, data de cadastro/compra e garantia.");
            return;
        }
        if (db.existsLacre(lacre, editandoId)) {
            new AlertDialog.Builder(this)
                    .setTitle("Lacre já cadastrado")
                    .setMessage("Este número de lacre já existe neste celular. O app não permite repetir lacres.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        Registro r;
        boolean novo = editandoId == null;
        if (novo) {
            r = new Registro();
            r.id = UUID.randomUUID().toString();
            r.criadoEm = DateUtils.nowIsoDateTime();
        } else {
            r = db.findById(editandoId);
            if (r == null) {
                aviso("Registro não encontrado para edição.");
                limparCampos();
                return;
            }
        }
        r.numeroLacre = lacre;
        r.cliente = cliente;
        r.telefone = telefone;
        r.dataCadastro = dataCadastro;
        r.tipoGarantia = rbDias.isChecked() ? "dias" : "data";
        if (rbDias.isChecked()) {
            try { r.diasGarantia = Integer.parseInt(txtDias.getText().toString().trim()); }
            catch (Exception e) { r.diasGarantia = null; }
        } else {
            r.diasGarantia = null;
        }
        r.dataVencimento = vencimento;
        r.observacao = observacao;
        r.atualizadoEm = DateUtils.nowIsoDateTime();
        db.save(r);
        aviso(novo ? "Registro salvo." : "Registro atualizado.");
        limparCampos();
        renderizarLista();
    }

    private void limparCampos() {
        editandoId = null;
        txtLacre.setText("");
        txtCliente.setText("");
        txtTelefone.setText("");
        txtObservacao.setText("");
        txtDataCadastro.setText(DateUtils.toBrDate(DateUtils.todayIso()));
        rbDias.setChecked(true);
        txtDias.setText("90");
        txtDataVencimento.setText("");
        btnSalvar.setText("Salvar registro");
        atualizarModoGarantia();
    }

    private void editarRegistro(Registro r) {
        editandoId = r.id;
        txtLacre.setText(r.numeroLacre);
        txtCliente.setText(r.cliente);
        txtTelefone.setText(r.telefone == null ? "" : r.telefone);
        txtDataCadastro.setText(DateUtils.toBrDate(r.dataCadastro));
        txtObservacao.setText(r.observacao == null ? "" : r.observacao);
        if ("dias".equals(r.tipoGarantia)) {
            rbDias.setChecked(true);
            txtDias.setText(r.diasGarantia == null ? "" : String.valueOf(r.diasGarantia));
            txtDataVencimento.setText("");
        } else {
            rbData.setChecked(true);
            txtDataVencimento.setText(DateUtils.toBrDate(r.dataVencimento));
        }
        btnSalvar.setText("Atualizar registro");
        atualizarModoGarantia();
        aviso("Editando lacre " + r.numeroLacre);
    }

    private void excluirRegistro(Registro r) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir registro")
                .setMessage("Deseja excluir o lacre " + r.numeroLacre + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d, w) -> {
                    db.delete(r.id);
                    if (r.id.equals(editandoId)) limparCampos();
                    renderizarLista();
                })
                .show();
    }

    private void renderizarLista() {
        if (listaContainer == null) return;
        String termo = txtBusca == null ? "" : txtBusca.getText().toString();
        List<Registro> registros = db.search(termo, filtroAtual);
        int total = registros.size();
        lblContagem.setText("Registros (" + total + ")");
        listaContainer.removeAllViews();

        if (total == 0) {
            atualizarControlesPaginacao(0, 0, 0, 1);
            TextView vazio = new TextView(this);
            vazio.setText("Nenhum registro encontrado.\nCadastre acima para começar.");
            vazio.setTextColor(Color.rgb(91, 100, 114));
            vazio.setTextSize(15);
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(dp(12), dp(30), dp(12), dp(30));
            listaContainer.addView(vazio, matchWrap());
            return;
        }

        int totalPaginas;
        int inicio;
        int fim;
        if (registrosPorPagina <= 0) {
            totalPaginas = 1;
            paginaAtual = 1;
            inicio = 0;
            fim = total;
        } else {
            totalPaginas = Math.max(1, (int) Math.ceil(total / (double) registrosPorPagina));
            if (paginaAtual < 1) paginaAtual = 1;
            if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;
            inicio = (paginaAtual - 1) * registrosPorPagina;
            fim = Math.min(inicio + registrosPorPagina, total);
        }

        for (int i = inicio; i < fim; i++) {
            listaContainer.addView(cardRegistro(registros.get(i)), matchWrapTop(dp(10)));
        }

        atualizarControlesPaginacao(total, inicio + 1, fim, totalPaginas);
    }

    private void atualizarControlesPaginacao(int total, int inicioExibido, int fimExibido, int totalPaginas) {
        if (lblPaginaInfo == null || btnPaginaAnterior == null || btnPaginaProxima == null) return;
        if (total == 0) {
            lblPaginaInfo.setText("");
            btnPaginaAnterior.setEnabled(false);
            btnPaginaProxima.setEnabled(false);
            return;
        }
        if (registrosPorPagina <= 0) {
            lblPaginaInfo.setText("Todos: " + total);
            btnPaginaAnterior.setEnabled(false);
            btnPaginaProxima.setEnabled(false);
            return;
        }
        lblPaginaInfo.setText("Pág. " + paginaAtual + "/" + totalPaginas + "\n" + inicioExibido + "-" + fimExibido);
        btnPaginaAnterior.setEnabled(paginaAtual > 1);
        btnPaginaProxima.setEnabled(paginaAtual < totalPaginas);
    }

    private View cardRegistro(Registro r) {
        LinearLayout card = card();
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        TextView codigo = new TextView(this);
        codigo.setText("Lacre " + r.numeroLacre);
        codigo.setTextColor(Color.rgb(22, 79, 65));
        codigo.setTypeface(Typeface.DEFAULT_BOLD);
        codigo.setTextSize(13);
        card.addView(codigo);

        TextView nome = new TextView(this);
        nome.setText(r.cliente);
        nome.setTextColor(Color.rgb(28, 36, 48));
        nome.setTypeface(Typeface.DEFAULT_BOLD);
        nome.setTextSize(17);
        nome.setPadding(0, dp(2), 0, 0);
        card.addView(nome);

        TextView info = new TextView(this);
        StringBuilder sb = new StringBuilder();
        if (r.telefone != null && !r.telefone.trim().isEmpty()) sb.append(r.telefone).append("\n");
        sb.append("Compra/cadastro: ").append(DateUtils.toBrDate(r.dataCadastro)).append("\n");
        sb.append("Garantia até: ").append(DateUtils.toBrDate(r.dataVencimento));
        if (r.observacao != null && !r.observacao.trim().isEmpty()) sb.append("\nObs: ").append(r.observacao);
        info.setText(sb.toString());
        info.setTextColor(Color.rgb(91, 100, 114));
        info.setTextSize(14);
        info.setPadding(0, dp(4), 0, dp(6));
        card.addView(info);

        TextView status = new TextView(this);
        status.setText(r.statusLabel());
        status.setTextColor(corStatusTexto(r));
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setTextSize(13);
        status.setPadding(0, 0, 0, dp(8));
        card.addView(status);

        LinearLayout acoes = new LinearLayout(this);
        acoes.setOrientation(LinearLayout.HORIZONTAL);
        Button editar = button("Editar", false);
        Button excluir = button("Excluir", false);
        Button whats = button("WhatsApp", false);
        editar.setOnClickListener(v -> editarRegistro(r));
        excluir.setOnClickListener(v -> excluirRegistro(r));
        whats.setOnClickListener(v -> abrirWhatsApp(r.telefone));
        acoes.addView(editar, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        acoes.addView(spacer(dp(8)), new LinearLayout.LayoutParams(dp(8), 1));
        acoes.addView(excluir, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (r.telefone != null && !r.telefone.trim().isEmpty()) {
            acoes.addView(spacer(dp(8)), new LinearLayout.LayoutParams(dp(8), 1));
            acoes.addView(whats, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
        card.addView(acoes, matchWrap());
        return card;
    }

    private int corStatusTexto(Registro r) {
        int diff = DateUtils.daysBetweenToday(r.dataVencimento);
        if (diff < 0) return Color.rgb(164, 50, 42);
        if (diff <= 30) return Color.rgb(179, 84, 30);
        return Color.rgb(22, 79, 65);
    }

    private void instalarNormalizacaoLacre() {
        txtLacre.addTextChangedListener(new TextWatcher() {
            private boolean atualizando = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (atualizando) return;
                String original = s.toString();
                String normalizado = DateUtils.normalizarLacre(original);
                if (!original.equals(normalizado)) {
                    atualizando = true;
                    txtLacre.setText(normalizado);
                    txtLacre.setSelection(txtLacre.getText().length());
                    atualizando = false;
                }
            }
        });
    }

    private void abrirCompraLacre() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/5581988496130?text=Ol%C3%A1%2C%20gostaria%20de%20comprar%20lacre%20de%20garantia."));
        try { startActivity(intent); }
        catch (Exception e) { aviso("Não foi possível abrir o WhatsApp de compra."); }
    }

    private void abrirWhatsApp(String telefone) {
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.isEmpty()) {
            aviso("Este registro não possui telefone.");
            return;
        }
        if (digitos.length() <= 11) digitos = "55" + digitos;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + digitos));
        try { startActivity(intent); }
        catch (Exception e) { aviso("Não foi possível abrir o WhatsApp."); }
    }

    private void exportarJson() {
        String nome = "backup-lacres-" + DateUtils.todayIso() + ".json";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, nome);
        startActivityForResult(intent, REQ_EXPORT);
    }

    private void importarJson() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQ_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQ_EXPORT) {
            try {
                JSONArray arr = db.exportJson();
                OutputStream out = getContentResolver().openOutputStream(uri);
                if (out == null) throw new Exception("Sem acesso ao arquivo");
                out.write(arr.toString(2).getBytes(StandardCharsets.UTF_8));
                out.flush();
                out.close();
                aviso("Backup JSON exportado.");
            } catch (Exception e) {
                aviso("Falha ao exportar JSON: " + e.getMessage());
            }
        } else if (requestCode == REQ_IMPORT) {
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                if (in == null) throw new Exception("Sem acesso ao arquivo");
                String json = readAll(in);
                JSONArray arr = new JSONArray(json);
                int added = db.importJson(arr);
                renderizarLista();
                aviso("Importação concluída: " + added + " registro(s) novo(s).");
            } catch (Exception e) {
                aviso("Falha ao importar JSON. Verifique se o arquivo é válido.");
            }
        }
    }

    private String readAll(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        in.close();
        return out.toString("UTF-8");
    }

    private void escolherData(EditText target) {
        Calendar c = Calendar.getInstance();
        String val = target.getText().toString().trim();
        String isoAtual = DateUtils.toIsoDate(val);
        if (isoAtual.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                String[] p = isoAtual.split("-");
                c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {}
        }
        DatePickerDialog dlg = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String iso = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            target.setText(DateUtils.toBrDate(iso));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1), Color.rgb(225, 230, 235));
        l.setBackground(bg);
        return l;
    }

    private TextView label(String text) {
        TextView v = new TextView(this);
        v.setText(text.toUpperCase());
        v.setTextColor(Color.rgb(91, 100, 114));
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextSize(12);
        v.setPadding(0, dp(10), 0, dp(4));
        return v;
    }

    private EditText editText(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(inputType);
        e.setTextSize(16);
        e.setSingleLine((inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        e.setPadding(dp(12), dp(9), dp(12), dp(9));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(251, 252, 253));
        bg.setCornerRadius(dp(9));
        bg.setStroke(dp(1), Color.rgb(225, 230, 235));
        e.setBackground(bg);
        return e;
    }

    private Button button(String text, boolean primary) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(primary ? Color.WHITE : Color.rgb(28, 36, 48));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(9));
        bg.setColor(primary ? Color.rgb(31, 111, 92) : Color.WHITE);
        if (!primary) bg.setStroke(dp(1), Color.rgb(225, 230, 235));
        b.setBackground(bg);
        return b;
    }

    private View spacer(int width) {
        TextView v = new TextView(this);
        v.setWidth(width);
        return v;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapTop(int top) {
        LinearLayout.LayoutParams p = matchWrap();
        p.topMargin = top;
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextWatcher simpleWatcher(final Runnable r) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { r.run(); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void aviso(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
