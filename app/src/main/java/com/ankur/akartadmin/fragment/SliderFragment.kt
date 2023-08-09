package com.ankur.akartadmin.fragment

import android.app.Activity
import android.app.Dialog
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.ankur.akartadmin.R
import com.ankur.akartadmin.databinding.FragmentSliderBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class SliderFragment : Fragment() {

    private lateinit var binding: FragmentSliderBinding
    private lateinit var dialog: Dialog


//    This code it ti display the selected image from gallery
    private var imgUrl:Uri? = null
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
        binding= FragmentSliderBinding.inflate(layoutInflater)
        dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.progress_dialog)
        dialog.setCancelable(false)

        binding.apply {

            imageView.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type="image/*"
                launchGalleyActivity.launch(intent)
            }


            button.setOnClickListener {
                if (imgUrl!=null)
                {
                    uploadImage(imgUrl!!)
                }
                else
                {
                    Toast.makeText(requireContext(), "Please select an Image", Toast.LENGTH_SHORT).show()
                }
            }
        }


        return binding.root
    }

    private fun uploadImage(uri: Uri) {

        dialog.show()

        val fileName =UUID.randomUUID().toString()+".jpg"

        val refStorage =FirebaseStorage.getInstance().reference.child("slider/$fileName")
        refStorage.putFile(uri)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {image->
                    storeData(image.toString())
                }
            }.addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong with Storage", Toast.LENGTH_SHORT).show()
            }


    }

    private fun storeData(Image: String) {

        val db=Firebase.firestore.collection("slider").document("Items")

        val data = hashMapOf<String,Any>(
            "img" to Image
        )
        lifecycleScope.launch(Dispatchers.IO) {

            db.set(data).await()


        }
        dialog.dismiss()



    }


}