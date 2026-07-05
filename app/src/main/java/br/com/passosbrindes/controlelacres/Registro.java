package br.com.passosbrindes.controlelacres;

public class Registro {
    public String id;
    public String numeroLacre;
    public String cliente;
    public String telefone;
    public String dataCadastro;
    public String tipoGarantia; // dias ou data
    public Integer diasGarantia;
    public String dataVencimento;
    public String observacao;
    public String criadoEm;
    public String atualizadoEm;

    public String statusLabel() {
        int diff = DateUtils.daysBetweenToday(dataVencimento);
        if (diff < 0) return "Vencida";
        if (diff <= 30) return "Vence em " + diff + "d";
        return "Em garantia";
    }

    public String resumo() {
        String fone = telefone == null || telefone.trim().isEmpty() ? "" : "\nFone: " + telefone;
        return "Lacre: " + numeroLacre +
                "\nCliente: " + cliente + fone +
                "\nCadastro/compra: " + DateUtils.toBrDate(dataCadastro) +
                "\nGarantia até: " + DateUtils.toBrDate(dataVencimento) +
                "\nStatus: " + statusLabel();
    }
}
