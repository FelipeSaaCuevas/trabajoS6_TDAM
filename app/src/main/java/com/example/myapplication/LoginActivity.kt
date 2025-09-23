package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    // se usa el leteinit par colocar las variables sinntener que declaralas
     lateinit var auth: FirebaseAuth
     lateinit var fusedLocationClient: FusedLocationProviderClient

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                obtenerUbicacionYGuardar()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                irAMenu()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val T1 = findViewById<EditText>(R.id.t1)
        val T2 = findViewById<EditText>(R.id.t2)
        val b1 = findViewById<Button>(R.id.b1)

        b1.setOnClickListener {
            val email = T1.text.toString().trim()
            val clave = T2.text.toString().trim()

            if (email.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Ingrese un correo y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, clave)
            }
        }
    }

   fun loginUser(email: String, clave: String) {
        auth.signInWithEmailAndPassword(email, clave)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    verificarPermisoUbicacion()
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun verificarPermisoUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            obtenerUbicacionYGuardar()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionYGuardar() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                val usuarioId = auth.currentUser?.uid ?: return@addOnSuccessListener

                val db = FirebaseDatabase.getInstance().reference
                db.child("usuarios").child(usuarioId).child("coordenadas")
                    .push()
                    .setValue(mapOf("lat" to lat, "lon" to lon))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Coordenada guardada en Realtime DB", Toast.LENGTH_SHORT).show()
                        irAMenu()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                        irAMenu()
                    }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                irAMenu()
            }
        }
    }

    fun irAMenu() {
        val email = auth.currentUser?.email ?: ""  // obtenemos el correo del usuario autenticado
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("usuario", email)  // lo enviamos a MenuActivity
        startActivity(intent)
        finish()
    }
}
