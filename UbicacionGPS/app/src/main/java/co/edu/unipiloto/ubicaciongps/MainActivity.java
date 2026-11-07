package co.edu.unipiloto.ubicaciongps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 44;
    private static final int ACCION_MOSTRAR = 0;
    private static final int ACCION_REPORTAR = 1;

    // Initialize variable
    Button btLocation, btReportar, btZonas;
    EditText etNombre;
    TextView textView1, textView2, textView3, textView4, textView5, location_view;
    FusedLocationProviderClient fusedLocationProviderClient;
    ReportesDB db;

    // Accion pendiente mientras el usuario responde el dialogo de permisos
    private int accionPendiente = ACCION_MOSTRAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign variable
        btLocation = findViewById(R.id.bt_location);
        btReportar = findViewById(R.id.bt_reportar);
        btZonas = findViewById(R.id.bt_zonas);
        etNombre = findViewById(R.id.et_nombre);
        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);
        textView4 = findViewById(R.id.text_view4);
        textView5 = findViewById(R.id.text_view5);
        location_view = findViewById(R.id.location_view);

        db = new ReportesDB(this);

        // Initialize fusedLocation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solicitarUbicacion(ACCION_MOSTRAR);
            }
        });

        btReportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etNombre.getText().toString().trim().isEmpty()) {
                    etNombre.setError("Escribe tu nombre");
                    return;
                }
                solicitarUbicacion(ACCION_REPORTAR);
            }
        });

        btZonas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ZonasActivity.class));
            }
        });
    }

    /** Revisa el permiso; si ya esta concedido obtiene la ubicacion, si no lo pide. */
    private void solicitarUbicacion(int accion) {
        accionPendiente = accion;
        location_view.setText("Check permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            location_view.setText("when permission granted");
            getLocation();
        } else {
            location_view.setText("when permission denied");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                location_view.setText("Permiso de ubicacion denegado");
                Toast.makeText(this, "Se necesita el permiso de ubicacion", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            location_view.setText("No se pudo obtener la ubicacion. "
                                    + "Activa el GPS y abre Google Maps una vez.");
                            return;
                        }
                        procesarUbicacion(location);
                    }
                });
    }

    private void procesarUbicacion(Location location) {
        // Initialize Geocoder
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            // Initialize address list
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses == null || addresses.isEmpty()) {
                location_view.setText("No se encontro direccion para esta ubicacion");
                return;
            }
            Address a = addresses.get(0);

            textView1.setText("Latitud: " + a.getLatitude());
            textView2.setText("Longitud: " + a.getLongitude());
            textView3.setText(a.getCountryName());
            textView4.setText(a.getLocality());
            textView5.setText(a.getAddressLine(0));
            location_view.setText("Ubicacion obtenida");

            if (accionPendiente == ACCION_REPORTAR) {
                reportar(a);
            }
        } catch (IOException e) {
            e.printStackTrace();
            location_view.setText("Error del Geocoder (revisa tu conexion a internet)");
        }
    }

    /** Guarda el reporte usando barrio (subLocality) o, si no hay, la ciudad como zona. */
    private void reportar(Address a) {
        String nombre = etNombre.getText().toString().trim();
        String zona = a.getSubLocality();
        if (zona == null || zona.isEmpty()) zona = a.getLocality();
        if (zona == null || zona.isEmpty()) zona = "Zona desconocida";

        db.guardarReporte(nombre, a.getLatitude(), a.getLongitude(), zona, a.getAddressLine(0));
        Toast.makeText(this, "Reporte guardado en: " + zona, Toast.LENGTH_LONG).show();
        accionPendiente = ACCION_MOSTRAR;
    }
}
