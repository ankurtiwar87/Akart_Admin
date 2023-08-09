package com.ankur.akartadmin.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ankur.akartadmin.Models.CategoryModel
import com.ankur.akartadmin.R
import com.ankur.akartadmin.adapters.CategoryAdapter
import com.ankur.akartadmin.databinding.FragmentCategoryBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.model.Document
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var dialog: Dialog


    //    This code it ti display the selected image from gallery
    private var imgUrl: Uri? = null
    private var launchGalleyActivity =registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode== Activity.RESULT_OK)
        {
            imgUrl=it.data!!.data
            binding.imageView.setImageURI(imgUrl)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentCategoryBinding.inflate(layoutInflater)

        dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.progress_dialog)
        dialog.setCancelable(false)
        binding.CategoryRecyclerView.layoutManager=LinearLayoutManager(requireContext())
        getDataFromFireStore()


        binding.apply {

//            This only show Image in UI for upload we use fireStorage
            imageView.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type="image/*"
                launchGalleyActivity.launch(intent)

            }
            button5.setOnClickListener {

                validateData(binding.CategoryName.text.toString())

            }
        }
        return binding.root
    }

    private fun getDataFromFireStore() {

        var list = ArrayList<CategoryModel>()

        lifecycleScope.launch(Dispatchers.IO) {
           Firebase.firestore.collection("Category").get().await()
               .also {
                   for (doc in it.documents)
                   {
                       val data = doc.toObject(CategoryModel::class.java)
                       list.add(data!!)
                   }
               }

            withContext(Dispatchers.Main)
            {
                binding.CategoryRecyclerView.adapter=CategoryAdapter(requireContext(),list)
            }

        }
    }

    private fun validateData(CategoryName: String) {

        if (CategoryName.isEmpty())
        {
            Toast.makeText(requireContext(), "Enter the Category", Toast.LENGTH_SHORT).show()
        }
        else if (imgUrl==null)
        {
            Toast.makeText(requireContext(), "Enter the Image", Toast.LENGTH_SHORT).show()

        }
        else
        {
            uploadImage(CategoryName)
        }

    }

    private fun uploadImage(CategoryName: String) {
        dialog.show()

        val fileName = UUID.randomUUID().toString()+".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("Category/$fileName")
        refStorage.putFile(imgUrl!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { Image  ->
//                    Data to be upload on firebase
                    storeData(CategoryName,Image.toString())
                }
            }.addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong with Storage", Toast.LENGTH_SHORT).show()
            }

    }


    private fun storeData(CategoryName: String, Image: String) {

        val db= Firebase.firestore.collection("Category")

        val data = hashMapOf<String,Any>(
            "category" to CategoryName,
            "img" to Image
        )

        lifecycleScope.launch(Dispatchers.IO) {

            db.add(data).await()
            getDataFromFireStore()

            withContext(Dispatchers.Main)
            {
                binding.CategoryName.text=null
                binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.preview))
                dialog.dismiss()
                Toast.makeText(requireContext(), "Category Added", Toast.LENGTH_SHORT).show()
            }

        }





    }


}