package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {
    //esto se usa para declara las variables y usarlas luego
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var latitude: Double? = null
    var longitude: Double? = null
    private lateinit var textView: TextView
    private lateinit var usuario: String

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                obtenerUbicacion()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        textView = findViewById(R.id.t3)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Recuperar el nombre de usuario enviado desde LoginActivity
        usuario = intent.getStringExtra("usuario") ?: ""
        verificarPermisoUbicacion()
    }

    private fun verificarPermisoUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            obtenerUbicacion()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                textView.text = "Lat: $latitude, Lon: $longitude"

                // Guardar coordenadas en el Firestore
                guardarCoordenadasEnFirestore()
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarCoordenadasEnFirestore() {
        val db = FirebaseFirestore.getInstance()

        if (latitude != null && longitude != null && usuario.isNotEmpty()) {
            val coord = mapOf(
                "lat" to latitude,
                "lon" to longitude
            )

            // Buscar el documento del usuario en la colección "usuarios"
            db.collection("usuarios")
                .whereEqualTo("Nombre", usuario)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            db.collection("usuarios")
                                .document(document.id)
                                .update("cordenadas", FieldValue.arrayUnion(coord))
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Coordenada guardada", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}

