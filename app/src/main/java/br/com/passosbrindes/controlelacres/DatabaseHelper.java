package br.com.passosbrindes.controlelacres;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "controle_lacres.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE registros (" +
                "id TEXT PRIMARY KEY," +
                "numero_lacre TEXT NOT NULL UNIQUE," +
                "cliente TEXT NOT NULL," +
                "telefone TEXT," +
                "data_cadastro TEXT NOT NULL," +
                "tipo_garantia TEXT NOT NULL," +
                "dias_garantia INTEGER," +
                "data_vencimento TEXT NOT NULL," +
                "observacao TEXT," +
                "criado_em TEXT NOT NULL," +
                "atualizado_em TEXT NOT NULL" +
                ")");
        db.execSQL("CREATE INDEX idx_registros_cliente ON registros(cliente)");
        db.execSQL("CREATE INDEX idx_registros_vencimento ON registros(data_vencimento)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // reservado para versões futuras
    }

    public boolean existsLacre(String lacre, String exceptId) {
        lacre = DateUtils.normalizarLacre(lacre);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM registros WHERE numero_lacre = ?", new String[]{lacre});
        try {
            if (!c.moveToFirst()) return false;
            String foundId = c.getString(0);
            return exceptId == null || !exceptId.equals(foundId);
        } finally {
            c.close();
        }
    }

    public void save(Registro r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = toValues(r);
        db.insertWithOnConflict("registros", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void delete(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("registros", "id = ?", new String[]{id});
    }

    public Registro findById(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM registros WHERE id = ?", new String[]{id});
        try {
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            c.close();
        }
    }

    public List<Registro> search(String term, String filter) {
        ArrayList<Registro> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM registros";
        ArrayList<String> args = new ArrayList<>();
        ArrayList<String> where = new ArrayList<>();
        if (term != null && !term.trim().isEmpty()) {
            where.add("(numero_lacre LIKE ? OR cliente LIKE ? OR telefone LIKE ?)");
            String like = "%" + term.trim() + "%";
            args.add(like); args.add(like); args.add(like);
        }
        if ("vencidos".equals(filter)) {
            where.add("data_vencimento < ?");
            args.add(DateUtils.todayIso());
        } else if ("proximos".equals(filter)) {
            where.add("data_vencimento >= ? AND data_vencimento <= ?");
            args.add(DateUtils.todayIso());
            args.add(DateUtils.addDays(DateUtils.todayIso(), 30));
        } else if ("vigentes".equals(filter)) {
            where.add("data_vencimento >= ?");
            args.add(DateUtils.todayIso());
        }
        if (!where.isEmpty()) sql += " WHERE " + join(where, " AND ");
        sql += " ORDER BY data_vencimento ASC, numero_lacre ASC";
        Cursor c = db.rawQuery(sql, args.toArray(new String[0]));
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public JSONArray exportJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (Registro r : search("", "todos")) arr.put(toJson(r));
        return arr;
    }

    public int importJson(JSONArray arr) throws JSONException {
        int added = 0;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Registro r = fromJson(o);
                if (r.id == null || r.id.trim().isEmpty()) continue;
                if (r.numeroLacre == null || r.numeroLacre.trim().isEmpty()) continue;
                if (existsLacre(r.numeroLacre, r.id)) continue;
                ContentValues v = toValues(r);
                long result = db.insertWithOnConflict("registros", null, v, SQLiteDatabase.CONFLICT_IGNORE);
                if (result != -1) added++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return added;
    }

    private static ContentValues toValues(Registro r) {
        ContentValues v = new ContentValues();
        v.put("id", r.id);
        v.put("numero_lacre", DateUtils.normalizarLacre(r.numeroLacre));
        v.put("cliente", r.cliente);
        v.put("telefone", r.telefone);
        v.put("data_cadastro", r.dataCadastro);
        v.put("tipo_garantia", r.tipoGarantia);
        if (r.diasGarantia == null) v.putNull("dias_garantia"); else v.put("dias_garantia", r.diasGarantia);
        v.put("data_vencimento", r.dataVencimento);
        v.put("observacao", r.observacao);
        v.put("criado_em", r.criadoEm);
        v.put("atualizado_em", r.atualizadoEm);
        return v;
    }

    private static Registro fromCursor(Cursor c) {
        Registro r = new Registro();
        r.id = c.getString(c.getColumnIndexOrThrow("id"));
        r.numeroLacre = c.getString(c.getColumnIndexOrThrow("numero_lacre"));
        r.cliente = c.getString(c.getColumnIndexOrThrow("cliente"));
        r.telefone = c.getString(c.getColumnIndexOrThrow("telefone"));
        r.dataCadastro = c.getString(c.getColumnIndexOrThrow("data_cadastro"));
        r.tipoGarantia = c.getString(c.getColumnIndexOrThrow("tipo_garantia"));
        int idxDias = c.getColumnIndexOrThrow("dias_garantia");
        r.diasGarantia = c.isNull(idxDias) ? null : c.getInt(idxDias);
        r.dataVencimento = c.getString(c.getColumnIndexOrThrow("data_vencimento"));
        r.observacao = c.getString(c.getColumnIndexOrThrow("observacao"));
        r.criadoEm = c.getString(c.getColumnIndexOrThrow("criado_em"));
        r.atualizadoEm = c.getString(c.getColumnIndexOrThrow("atualizado_em"));
        return r;
    }

    private static JSONObject toJson(Registro r) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", r.id);
        o.put("numero_lacre", r.numeroLacre);
        o.put("cliente", r.cliente);
        o.put("telefone", r.telefone == null ? "" : r.telefone);
        o.put("data_cadastro", r.dataCadastro);
        o.put("tipo_garantia", r.tipoGarantia);
        if (r.diasGarantia == null) o.put("dias_garantia", JSONObject.NULL); else o.put("dias_garantia", r.diasGarantia);
        o.put("data_vencimento", r.dataVencimento);
        o.put("observacao", r.observacao == null ? "" : r.observacao);
        o.put("criado_em", r.criadoEm);
        o.put("atualizado_em", r.atualizadoEm);
        return o;
    }

    private static Registro fromJson(JSONObject o) {
        Registro r = new Registro();
        r.id = o.optString("id", "");
        r.numeroLacre = DateUtils.normalizarLacre(o.optString("numero_lacre", o.optString("codigo", "")));
        r.cliente = o.optString("cliente", o.optString("nome", ""));
        r.telefone = o.optString("telefone", o.optString("fone", ""));
        r.dataCadastro = DateUtils.toIsoDate(o.optString("data_cadastro", DateUtils.todayIso()));
        r.tipoGarantia = o.optString("tipo_garantia", "data");
        if (o.has("dias_garantia") && !o.isNull("dias_garantia")) r.diasGarantia = o.optInt("dias_garantia");
        r.dataVencimento = DateUtils.toIsoDate(o.optString("data_vencimento", o.optString("data", DateUtils.todayIso())));
        r.observacao = o.optString("observacao", "");
        r.criadoEm = o.optString("criado_em", DateUtils.nowIsoDateTime());
        r.atualizadoEm = o.optString("atualizado_em", DateUtils.nowIsoDateTime());
        return r;
    }

    private static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}
