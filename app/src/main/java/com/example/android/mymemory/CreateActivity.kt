package com.example.android.mymemory

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mymemory.models.BoardSize
import com.example.android.mymemory.utils.EXTRA_BOARD_SIZE
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {



    companion object{
        private const val PICK_PHOTO_CODE = 655
        private const val READ_EXTERNAL_PHOTOS_CODE = 200
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MIN_GAME_NAME_LENGTH = 3
        private const val MAX_GAME_NAME_LENGTH = 14

    }


    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var adapter: ImagePickerAdapter

    private lateinit var boardSize: BoardSize
    private  var numImagesRequired = -1
    private val chosenImageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

     val boardSize =   intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)


        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))
        etGameName.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
               btnSave.isEnabled = shouldEnableSaveButton()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSave.setOnClickListener{
                saveDataToFirebase()
        }

      adapter = ImagePickerAdapter(this,chosenImageUris, boardSize,object : ImagePickerAdapter.ImageClickListner{
            override fun onPlaceHolderClicked() {
                if(isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION )){
                    launchIntentForPhotos()
                } else{
                    requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTOS_CODE)
                }

            }
        })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())



    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_PHOTOS_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchIntentForPhotos()
            }
                else{
                    Toast.makeText(this, "In order to create a custom game, you need to provide access to your photos", Toast.LENGTH_LONG).show()
                }

            }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null){
            return
        }
        val selectedUri = data.data
        val clipData = data.clipData
        if(clipData != null){
            for(i: Int in 0 until  clipData.itemCount){
                val clipItem = clipData.getItemAt(i)
                if(chosenImageUris.size < numImagesRequired){
                    chosenImageUris.add(clipItem.uri)
                }

            }
        }
        else if(selectedUri != null){
            chosenImageUris.add(selectedUri)
        }

        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics ( ${chosenImageUris.size} / $numImagesRequired )"
        btnSave.isEnabled = shouldEnableSaveButton()

    }

    private fun shouldEnableSaveButton(): Boolean {
        if(chosenImageUris.size != numImagesRequired){
            return false

        }
        if(etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NAME_LENGTH)
        {
            return false
        }
        return true

    }


    private fun launchIntentForPhotos() {
       val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTO_CODE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == android.R.id.home){
            finish()
            return true
        }


            return super.onOptionsItemSelected(item)
    }


    private fun saveDataToFirebase() {
        for((index: Int, photoUri: Uri) in chosenImageUris.withIndex()){
            val imageByteArray = getImageByteArray(photoUri)
        }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val OriginalBitmap= if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        }
        else{
            MediaStore.Images.Media.getBitmap(contentResolver,photoUri)

        }
        val scaleBitmap = BitmapScaler.scaleToFitHeight(OriginalBitmap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaleBitmap.compress(Bitmap.CompressFormat.JPEG,60,byteOutputStream)
        return byteOutputStream.toByteArray()

    }


}


