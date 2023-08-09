package com.ankur.akartadmin.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ankur.akartadmin.R
import com.ankur.akartadmin.databinding.ImageItemBinding
import com.bumptech.glide.Glide

class AddProductAdapter(private val context: Context ,private val list: ArrayList<Uri>):
    RecyclerView.Adapter<AddProductAdapter.ViewHolder>() {


    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView)
    {

        val binding=ImageItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.image_item,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.itemImg.setImageURI(list[position])

    }
}


