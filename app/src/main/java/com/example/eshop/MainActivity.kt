package com.example.eshop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.os.persistableBundleOf
import com.example.eshop.data.Product
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val REQUEST_CODE_IMAGE_PICK = 0

class MainActivity : AppCompatActivity() {
    var currFile: Uri?=null
    val imageRef = Firebase.storage.reference

    private val productCollectionRef = Firebase.firestore.collection("products")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image1.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE_IMAGE_PICK)
            }
        }

        button.setOnClickListener {
           uploadImageToStorage("myFile")
           val product_name=tv_product_name.text.toString()
           val product_category=tv_product_category.text.toString()
           val product_price=tv_product_price.text.toString()
           val product_gst=tv_product_gst_amount.text.toString()
           val product_discount=tv_product_discount.text.toString()
           val product_delivery_charge=tv_Product_delivery_charge.text.toString()
           val product = Product(product_category,product_name,product_price,product_gst,product_discount,product_delivery_charge)
           saveProduct(product)
        }
    }

    private fun saveProduct(product: Product)= CoroutineScope(Dispatchers.IO).launch {
        try {
            productCollectionRef.add(product).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,"Successfully saved data.",Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            currFile?.let {
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Successfully uploaded image",
                        Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && requestCode== REQUEST_CODE_IMAGE_PICK){
            data?.data?.let {
                currFile = it
                image1.setImageURI(it)
            }
        }
    }
}