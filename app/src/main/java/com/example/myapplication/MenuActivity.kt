package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MenuActivity : AppCompatActivity() {

    lateinit var textUbicacion: TextView
    lateinit var database: DatabaseReference
    lateinit var email: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val textUsuario = findViewById<TextView>(R.id.textUsuario)

        textUbicacion = findViewById(R.id.textUbicacion)

        //recupera el email del loginActivity
        email = intent.getStringExtra("usuario") ?: "Sin usuario"
        textUsuario.text = "Bienvenido: $email"


        database = FirebaseDatabase.getInstance().reference

        CargarLaUltimaUbicacion()
    }

    private fun CargarLaUltimaUbicacion() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userRef = database.child("usuarios").child(userId).child("ubicaciones")


        userRef.limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (ubicacionSnap in snapshot.children) {
                        val lat = ubicacionSnap.child("lat").getValue(Double::class.java)
                        val lon = ubicacionSnap.child("lon").getValue(Double::class.java)

                        if (lat != null && lon != null) {
                            textUbicacion.text = "Última ubicación:\nLat: $lat\nLon: $lon"
                        }
                    }
                } else {
                    textUbicacion.text = "No hay ubicación guardada"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MenuActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
