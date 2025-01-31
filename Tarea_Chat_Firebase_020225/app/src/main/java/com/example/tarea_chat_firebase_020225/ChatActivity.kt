package com.example.tarea_chat_firebase_020225

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tarea_chat_firebase_020225.adapter.ChatAdapter
import com.example.tarea_chat_firebase_020225.databinding.ActivityChatBinding
import com.example.tarea_chat_firebase_020225.model.ChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    var email = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    private var listaChat = mutableListOf<ChatModel>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        email = auth.currentUser?.email.toString()

        dbReference = FirebaseDatabase.getInstance().getReference("chat")

        setListeners()
        setMenuLateral()
        setRecyclerView()
    }

    private fun setMenuLateral() {
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_logout -> {
                    auth.signOut()
                    finish()
                    true
                }

                R.id.item_salir -> {
                    finishAffinity()
                    true
                }

                R.id.item_borrar -> {
                    borrarDatosUsuario()
                    true
                }

                else -> {
                    false

                }
            }
        }
    }

    private fun borrarDatosUsuario() {
        dbReference.get().addOnSuccessListener {
            for (nodo in it.children) {
                val correo = nodo.child("email").value.toString()
                if (correo == email) {
                    nodo.ref.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Mensajes borrados", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(listaChat, email, { mensaje -> onClickDelete(mensaje) })
        binding.rvChat.adapter = adapter
    }

    private fun setListeners() {
        binding.imageView.setOnClickListener {
            enviar()
        }

        // Ponemos un listener a la base de datos para recuperar los mensajes
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaChat.clear()
                for (nodo in snapshot.children) {
                    val chat = nodo.getValue(ChatModel::class.java)
                    listaChat.add(chat!!)
                }
                listaChat.sortBy { it.fecha }
                adapter.updateAdapter(listaChat)

                // Scroll automatico al ultimo mensaje
                binding.rvChat.scrollToPosition(listaChat.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })

        // Cuando pulsas enter en el teclado
        binding.etMensaje.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN
            ) {
                enviar()
                ocultarTeclado()
                true
            } else {
                false
            }


        }


    }

    private fun onClickDelete(chat : ChatModel){
        var fechaMensaje = chat.fecha

        // Busco el mensaje por fecha
        dbReference.orderByChild("fecha").equalTo(fechaMensaje.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    // Borro el mensaje
                    dbReference.child(snapshot.children.first().key.toString()).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this@ChatActivity, "Mensaje borrado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@ChatActivity, it.message, Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun enviar() {

        var texto = binding.etMensaje.text.toString().trim()

        if (texto.isEmpty()) {
            Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show()
            return
        }

        var fecha = System.currentTimeMillis()
        var chat = ChatModel(email, texto, fecha)
        var key = fecha.toString()

        dbReference.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dbReference.child(key).setValue(chat).addOnSuccessListener {
                    binding.etMensaje.setText("")
                }.addOnFailureListener {
                    Toast.makeText(this@ChatActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun ocultarTeclado() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

    }


}