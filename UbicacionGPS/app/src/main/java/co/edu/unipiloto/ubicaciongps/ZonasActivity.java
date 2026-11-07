package co.edu.unipiloto.ubicaciongps;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Muestra las zonas ordenadas por cantidad de estudiantes. */
public class ZonasActivity extends AppCompatActivity {

    ReportesDB db;
    TextView tvTotal;
    ListView lvZonas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zonas);

        db = new ReportesDB(this);
        tvTotal = findViewById(R.id.tv_total);
        lvZonas = findViewById(R.id.lv_zonas);
        Button btBorrar = findViewById(R.id.bt_borrar);

        btBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.borrarTodo();
                cargar();
            }
        });

        cargar();
    }

    private void cargar() {
        tvTotal.setText("Estudiantes reportados: " + db.totalEstudiantes());
        List<String> zonas = db.zonasConMasEstudiantes();
        lvZonas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, zonas));
    }
}
