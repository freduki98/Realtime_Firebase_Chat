package com.example.tarea_chat_firebase_020225.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tarea_chat_firebase_020225.R
import com.example.tarea_chat_firebase_020225.model.ChatModel

class ChatAdapter(
    var lista: MutableList<ChatModel>, private var userLogeado: String,
    private var onClickDelete: (ChatModel) -> Unit
)
    : RecyclerView.Adapter<ChatViewHolder>() {

    fun updateAdapter(lista: MutableList<ChatModel>){
        this.lista = lista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false)
        return ChatViewHolder(v)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.render(lista[position], userLogeado, onClickDelete)
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}