package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val T1 = findViewById<EditText>(R.id.t1)  // Usuario
        val T2 = findViewById<EditText>(R.id.t2)  // Contraseña
        val b1 = findViewById<Button>(R.id.b1)    // Botón login

        b1.setOnClickListener {
            val usuario = T1.text.toString().trim()
            val clave = T2.text.toString().trim()

            if (usuario.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Ingrese un usuario o contraseña", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(usuario, clave)
            }
        }
    }

    private fun loginUser(usuario: String, clave: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .whereEqualTo("Nombre", usuario)
            .whereEqualTo("Contraseña", clave)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()

                    //esto se usa para pasar el usuario para la otra pagina
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.putExtra("usuario", usuario)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
