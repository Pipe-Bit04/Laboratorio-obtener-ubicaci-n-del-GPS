package co.edu.unipiloto.ubicaciongps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda los reportes de ubicacion de los estudiantes en SQLite (local)
 * y calcula las zonas con mayor cantidad de estudiantes.
 */
public class ReportesDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "reportes.db";
    private static final int DB_VERSION = 1;
    private static final String TABLA = "reportes";

    public ReportesDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nombre TEXT NOT NULL,"
                + "latitud REAL,"
                + "longitud REAL,"
                + "zona TEXT,"
                + "direccion TEXT,"
                + "fecha INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    /** Un estudiante solo cuenta una vez: si ya reporto, se actualiza su ubicacion. */
    public void guardarReporte(String nombre, double lat, double lng, String zona, String direccion) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("latitud", lat);
        v.put("longitud", lng);
        v.put("zona", zona);
        v.put("direccion", direccion);
        v.put("fecha", System.currentTimeMillis());

        int filas = db.update(TABLA, v, "nombre = ? COLLATE NOCASE", new String[]{nombre});
        if (filas == 0) {
            db.insert(TABLA, null, v);
        }
    }

    public int totalEstudiantes() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLA, null);
        int total = 0;
        if (c.moveToFirst()) total = c.getInt(0);
        c.close();
        return total;
    }

    /** Devuelve lineas "Zona - N estudiantes" ordenadas de mayor a menor. */
    public List<String> zonasConMasEstudiantes() {
        List<String> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT zona, COUNT(*) AS total FROM " + TABLA
                        + " GROUP BY zona ORDER BY total DESC, zona ASC", null);
        while (c.moveToNext()) {
            String zona = c.getString(0);
            int total = c.getInt(1);
            lista.add(zona + "  -  " + total + (total == 1 ? " estudiante" : " estudiantes"));
        }
        c.close();
        return lista;
    }

    public void borrarTodo() {
        getWritableDatabase().delete(TABLA, null, null);
    }
}
