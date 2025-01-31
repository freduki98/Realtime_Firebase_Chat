package com.example.tarea_chat_firebase_020225

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tarea_chat_firebase_020225.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private var responseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            val datos = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                val cuenta = datos.getResult(ApiException::class.java)
                if(cuenta != null){
                    val credenciales = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                    // Credenciales para autenticar al usuario
                    FirebaseAuth.getInstance().signInWithCredential(credenciales)
                        .addOnCompleteListener{
                            if(it.isSuccessful){
                                irActivityPrincipal()
                            }
                        }
                        .addOnFailureListener{
                            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                }
                Toast.makeText(this, "Bienvenido ${cuenta.email}", Toast.LENGTH_SHORT).show()
            } catch (e: ApiException){
                Log.e("Error de Api -->", e.message.toString())
            }
        }

        if(it.resultCode == RESULT_CANCELED){
            Toast.makeText(this, "El usuario canceló", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var binding : ActivityMainBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.setPersistenceEnabled(true)

        auth = FirebaseAuth.getInstance()
        setListeners()

    }

    private fun setListeners() {
        binding.btnLogin.setOnClickListener {
            loginGoogle()
        }

    }

    private fun loginGoogle() {
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, googleConf)
        googleClient.signOut() // Fundamental para que no haga login automatico si he cerrado sesion
        responseLauncher.launch(googleClient.signInIntent)
    }



    private fun irActivityPrincipal(){
        startActivity(Intent(this, ChatActivity::class.java))

    }

    override fun onStart(){

        // Si ya tiene la sesion iniciada
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            irActivityPrincipal()
        }
    }
}