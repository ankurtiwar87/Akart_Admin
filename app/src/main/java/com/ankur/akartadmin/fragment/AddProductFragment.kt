package com.ankur.akartadmin.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.ankur.akartadmin.Models.AddProductModel
import com.ankur.akartadmin.Models.CategoryModel
import com.ankur.akartadmin.R
import com.ankur.akartadmin.adapters.AddProductAdapter
import com.ankur.akartadmin.adapters.CategoryAdapter
import com.ankur.akartadmin.databinding.FragmentAddProductBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private lateinit var list: ArrayList<Uri>
    private lateinit var listImage:ArrayList<String>
    private lateinit var adapter:AddProductAdapter
    private var coverImage:Uri? = null
    private lateinit var dialog: Dialog
    private var coverImageUrl:String ?= ""
    private lateinit var categoryList : ArrayList<String>


    private var launchGalleyActivity =registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode== Activity.RESULT_OK)
        {
            coverImage=it.data!!.data
            binding.productCoverImg.setImageURI(coverImage)
            binding.productCoverImg.visibility=View.VISIBLE
        }
    }

    private var launchProductActivity =registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode== Activity.RESULT_OK)
        {
            val imageUrl =it.data!!.data
            list.add(imageUrl!!)
            adapter.notifyDataSetChanged()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentAddProductBinding.inflate(layoutInflater)
//       This is for spinner Category
        getCategoryList()

        list= ArrayList()
        listImage= ArrayList()

        dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.progress_dialog)
        dialog.setCancelable(false)


        binding.selectCoverImg.setOnClickListener {

            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type="image/*"
            launchGalleyActivity.launch(intent)
        }


        binding.ProductImg.setOnClickListener {

            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type="image/*"
            launchProductActivity.launch(intent)
        }

        adapter= AddProductAdapter(requireContext(),list)
        binding.productImageRecyclerView.adapter=adapter


        binding.SubmitProduct.setOnClickListener {

            validateData()
        }
        return binding.root
    }

    private fun validateData() {
        if (binding.productName.text.toString().isEmpty())
        {
            binding.productName.requestFocus()
            binding.productName.error="Empty"

        }
        else if (binding.productDesc.text.toString().isEmpty())
        {
            binding.productDesc.requestFocus()
            binding.productDesc.error="Empty"

        }
        else if (binding.productMRP.text.toString().isEmpty())
        {
            binding.productDesc.requestFocus()
            binding.productMRP.error="Empty"

        }
        else if (binding.productSP.text.toString().isEmpty())
        {
            binding.productSP.requestFocus()
            binding.productSP.error="Empty"

        }
        else if (coverImage==null)
        {
            Toast.makeText(requireContext(), "Please Enter a Cover Image", Toast.LENGTH_SHORT).show()
        }
        else if (list.size<1)
        {
            Toast.makeText(requireContext(), "Please Enter a product Images", Toast.LENGTH_SHORT).show()
        }
        else{

            uploadImage()
        }
    }

    private fun uploadImage() {
        dialog.show()

        val fileName = UUID.randomUUID().toString()+".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("Products/$fileName")
        refStorage.putFile(coverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { Image  ->
//                    Data to be upload on firebase
                    coverImageUrl=Image.toString()

                    uploadProductImage()
                }
            }.addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong with Storage", Toast.LENGTH_SHORT).show()
            }
    }

    private var i =0
//    Through Recursion We will upload Image
    private fun uploadProductImage() {
        dialog.show()

        val fileName = UUID.randomUUID().toString()+".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("Products/$fileName")
        refStorage.putFile(list[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { Image  ->
//                    Data to be upload on firebase
                    listImage.add(Image!!.toString())

                    if (list.size==listImage.size)
                    {
                        storeData()
                    }
                    else
                    {
                        i+=1
                        uploadProductImage()
                    }

                }
            }.addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong with Storage", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData() {
        val db =Firebase.firestore.collection("products")
        val key = db.document().id

        val data = AddProductModel(
            binding.productName.text.toString(),
            binding.productDesc.text.toString(),
            coverImageUrl.toString(),
            categoryList[binding.ProductSpinner.selectedItemPosition],
            key,
            binding.productMRP.text.toString(),
            binding.productSP.text.toString(),
            listImage
        )

        lifecycleScope.launch(Dispatchers.IO) {

            db.document(key).set(data).await()

            withContext(Dispatchers.Main)
            {
                dialog.dismiss()
                binding.productName.text=null
                binding.productDesc.text=null
                binding.productMRP.text=null
                binding.productSP.text=null
                binding.productImageRecyclerView.visibility=View.GONE
                binding.productCoverImg.visibility=View.GONE

                Toast.makeText(requireContext(), "Product Added", Toast.LENGTH_SHORT).show()
            }


        }
    }


    private fun getCategoryList() {
        categoryList= ArrayList()
        lifecycleScope.launch(Dispatchers.IO) {
            categoryList.clear()
            Firebase.firestore.collection("Category").get().await()
                .also {
                    for (doc in it.documents)
                    {
                        val data = doc.toObject(CategoryModel::class.java)
                        categoryList.add(data!!.category!!)
                    }
                    categoryList.add(0,"Select Category")

                }

            withContext(Dispatchers.Main)
            {
                val arrayAdapter = ArrayAdapter(requireContext(),R.layout.drop_down_item,categoryList)
                binding.ProductSpinner.adapter=arrayAdapter
            }

        }

    }


}