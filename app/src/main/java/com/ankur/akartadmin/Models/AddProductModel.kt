package com.ankur.akartadmin.Models

import android.media.Image

data class AddProductModel(

    val productName:String? ="",
    val productDescription:String? ="",
    val productCoverImg:String? ="",
    val productCategory:String? ="",
    val productId:String? ="",
    val productMRP:String? ="",
    val productSP:String? ="",
    val productImage:ArrayList<String> = ArrayList()


)
