package com.ankur.akartadmin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ankur.akartadmin.Models.CategoryModel
import com.ankur.akartadmin.R
import com.ankur.akartadmin.databinding.ItemCategoryBinding
import com.bumptech.glide.Glide

class CategoryAdapter(private val context: Context,private var list:ArrayList<CategoryModel>): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {


    inner class CategoryViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val binding =ItemCategoryBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category,parent,false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        var currentItem =list[position]
        holder.binding.textView2.text=currentItem.category
        Glide.with(context).load(currentItem.img).into(holder.binding.imageView2)
    }
}