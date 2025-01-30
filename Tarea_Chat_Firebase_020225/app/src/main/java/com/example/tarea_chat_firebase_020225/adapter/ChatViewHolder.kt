package com.example.tarea_chat_firebase_020225.adapter

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.tarea_chat_firebase_020225.R
import com.example.tarea_chat_firebase_020225.databinding.ChatLayoutBinding
import com.example.tarea_chat_firebase_020225.model.ChatModel
import java.text.SimpleDateFormat
import java.util.Date

class ChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private val binding = ChatLayoutBinding.bind(v)

    fun render (chat: ChatModel, userLogeado: String, onClickDelete: (ChatModel) -> Unit){
        binding.tvEmail.text = chat.email
        binding.tvMensaje.text = chat.mensaje
        binding.tvFecha.text = fechaFormateada(chat.fecha)

        val params = binding.cardViewChat.layoutParams as FrameLayout.LayoutParams

        if(chat.email == userLogeado){
            binding.clChat.setBackgroundColor(binding.tvFecha.context.getColor(R.color.color_logeado))
            params.gravity = Gravity.END
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.clChat.setBackgroundColor(binding.tvFecha.context.getColor(R.color.color_no_logeado))
            params.gravity = Gravity.START
            binding.btnDelete.visibility = View.INVISIBLE
        }

        binding.btnDelete.setOnClickListener{
            onClickDelete(chat)
        }



    }

    private fun fechaFormateada (fecha: Long): String {
        val date : Date = Date(fecha)
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a")
        return format.format(date)
    }


}