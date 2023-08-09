package com.ankur.akartadmin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ankur.akartadmin.Models.AllOrderModel
import com.ankur.akartadmin.R
import com.ankur.akartadmin.databinding.AllOrderItemBinding

class AllOrderAdapter(private val context: Context ,private val list: ArrayList<AllOrderModel>)
    :RecyclerView.Adapter<AllOrderAdapter.ViewHolder>(){

        inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            val binding=AllOrderItemBinding.bind(itemView)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.all_order_item,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem =list[position]
        holder.binding.productName.text=currentItem.name.toString()
        holder.binding.productPrice.text=currentItem.price.toString()
    }
}