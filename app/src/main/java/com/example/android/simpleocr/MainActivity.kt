package com.example.android.simpleocr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    //----------------------------
    // PROPERTIES FOR OCR
    //----------------------------

    lateinit var image: Bitmap
    lateinit var tess: TessBaseAPI
    lateinit var dataPath: String

    //----------------------------
    // PROPERTIES FOR CAMERA
    //----------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // On button clicked

        btn_takePicture.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btn_runOCR.setOnClickListener {
            processImage()
        }

        // Init Tesseract API
        val lang = "eng"
        dataPath = "$filesDir/tesseract/"
        tess = TessBaseAPI()

        // Make sure training data has been copied
        checkFile(File("$dataPath/tessdata/"))

        tess.init(dataPath, lang)
    }

    //----------------------------
    // METHODS FOR OCR
    //----------------------------

    // Copy the file to the device
    private fun copyFiles() {
        try {
            // Location of the file
            val filePath = "$dataPath/tessdata/eng.traineddata"

            // Get access to AssetManager
            val assetManager = assets

            // Open byte streams for reading/writing
            val streamIn = assetManager.open("eng.traineddata")
            val streamOut = FileOutputStream(filePath)

            // Copy file to location in filepath
            val buffer = ByteArray(1024)
            var read:Int? = null
            while ({read = streamIn.read(buffer); read}() != -1) {
                streamOut.write(buffer, 0, read!!)
            }

            println("Successful copy")

            streamOut.flush()
            streamOut.close()
            streamIn.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Cover two scenariors in which we should copy the file over
    private fun checkFile(dir: File) {
        // Dir not exist, but can successfully create
        if (!dir.exists() && dir.mkdirs()) {
            println("Dir not exist, but can successfully create")
            copyFiles()
        }

        // Dir already exists, but no data file
        if (dir.exists()) {
            println("Dir already exists, but no data file")
            val dataFilePath = "$dataPath/tessdata/eng.traineddata"
            val dataFile = File(dataFilePath)
            if (!dataFile.exists()) {
                copyFiles()
            }
        }
    }

    // Display text after OCR
    fun processImage() {
        println(image)
        tess.setImage(image.copy(Bitmap.Config.ARGB_8888, true))
        tv_text.text = tess.utF8Text
    }

    //----------------------------
    // METHODS FOR CAMERA
    //----------------------------

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null)
            startActivityForResult(takePictureIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val extras = data!!.extras
            image = extras.get("data") as Bitmap
            iv_camera.setImageBitmap(image)
        }
    }

}
