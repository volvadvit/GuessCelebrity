package com.volvadvit.guessthecelebrity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private val task = DownloadTask()
    private val celebNames: MutableList<String> = mutableListOf()
    private val celebPhotos: MutableList<String> = mutableListOf()
    private var flag: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getDataFromHTML()
        newQuestion()
    }

    private fun newQuestion() {
        val celebRandom = (0 until celebNames.size).random()
        setImage(celebRandom)
        setButtonText(celebRandom)
    }

    private fun setImage(index: Int) {
        val imageTask = ImageDownloader()
        val imageBitmap = getImageBitmap(imageTask, index)
        imageView.setImageBitmap(imageBitmap)
    }

    private fun getImageBitmap(imageTask: ImageDownloader, index: Int): Bitmap? {
        val imageBitmap: Bitmap
        try {
            imageBitmap = imageTask.execute(celebPhotos[index]).get()
            return imageBitmap
        } catch (e: Exception) {
            Log.d("!@#", "ImageBitmapExecute   ::    ${e.message!!}")
            return null
        }
    }

    private fun getDataFromHTML() {
        try {
            val result = task.execute("https://www.imdb.com/list/ls052283250/").get()

            if (result != null) parseHTML(result)

        } catch (e: Exception) {
            Log.d("!@#", "DownloadTaskExecute   ::    ${e.message!!}")
        }
    }

    private fun parseHTML(result: String) {
        // Here must be split string, but code work correctly without it
        var pattern: Pattern = Pattern.compile("<img alt=\"(.*?)\"")
        var matcher: Matcher = pattern.matcher(result)

        while (matcher.find()) {
            celebNames.add(matcher.group(1)!!)
        }

        pattern = Pattern.compile("src=\"(.*?).jpg\"")
        matcher = pattern.matcher(result)

        while (matcher.find()) {
            celebPhotos.add(matcher.group(1)!!)
        }
        celebPhotos.remove(celebPhotos[celebPhotos.size-1])
        celebPhotos.remove(celebPhotos[celebPhotos.size-1])
    }

    private fun setButtonText(index: Int) {
        flag = (0 until 4).random()
        println("FLAG::" + flag.toString())
        if (flag == 0) {
            btn0.text = celebNames[index]
            btn1.text = celebNames[(0..99).random()]
            btn2.text = celebNames[(0..99).random()]
            btn3.text = celebNames[(0..99).random()]
        } else if (flag == 1) {
            btn1.text = celebNames[index]
            btn0.text = celebNames[(0..99).random()]
            btn2.text = celebNames[(0..99).random()]
            btn3.text = celebNames[(0..99).random()]
        } else if (flag == 2) {
            btn2.text = celebNames[index]
            btn0.text = celebNames[(0..99).random()]
            btn1.text = celebNames[(0..99).random()]
            btn3.text = celebNames[(0..99).random()]
        } else {
            btn3.text = celebNames[index]
            btn0.text = celebNames[(0..99).random()]
            btn1.text = celebNames[(0..99).random()]
            btn2.text = celebNames[(0..99).random()]
        }

    }

    class DownloadTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {

            val url: URL
            var conn: HttpURLConnection? = null
            val stringBuilder = StringBuilder()
            var result = ""

            try {
                url = URL(params[0])
                conn = url.openConnection() as HttpURLConnection
                val reader = BufferedInputStream(conn.inputStream)
                var data = reader.read()
                while (data != -1) {
                    val current = data.toChar()
                    stringBuilder.append(current)
                    data = reader.read()
                }
                result = stringBuilder.toString()
                return result
            } catch (e: Exception) {
                Log.d("!@#", "DownloadWebTask   ::    ${e.message!!}")
                return "Fail"
            } finally {
                conn?.disconnect()
            }
        }
    }

    class ImageDownloader : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg params: String?): Bitmap? {
            Log.d("!@#", params[0]!!)
            val image: Bitmap
            val url: URL
            var conn: HttpURLConnection? = null
            try {
                url = URL(params[0])
                conn = url.openConnection() as HttpURLConnection
                image = BitmapFactory.decodeStream(conn.inputStream)
                return image
            } catch (e: Exception) {
                Log.d("!@#", "ImageDownloader   ::    ${e.message!!}")
                return null
            } finally {
                conn?.disconnect()
            }
        }
    }

    fun checkAnswer(view: View) {
        val answer = view.tag.toString()
        if (answer == flag.toString()) {
            toast("Great!")
            newQuestion()
        } else {
            toast("Wrong!")
        }
    }

    private fun toast(s: String) {
        Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT).show()
    }
}