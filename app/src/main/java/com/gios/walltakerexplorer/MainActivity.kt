@file:Suppress("DEPRECATION")

package com.gios.walltakerexplorer

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso


lateinit var webUrl: String
lateinit var uri: Uri
lateinit var DI: String

@SuppressLint("StaticFieldLeak")
lateinit var webView: WebView
var url = "https://walltaker.joi.how/"

@SuppressLint("StaticFieldLeak")
lateinit var photo: ImageView

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isDarkThemeOn()) {
            this.supportActionBar!!.title =
                Html.fromHtml("<font color='#FFB300'>Walltaker Explorer</font>")
        } else {
            this.supportActionBar!!.title =
                Html.fromHtml("<font color='#0D47A1'>Walltaker Explorer</font>")
        }

        photo = findViewById(R.id.Photo)
        webView = findViewById(R.id.Explorer)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
    }

    private fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    private fun image() {
        if (webUrl.contains("static1.e621.net")) {
            downloadFile(webUrl)
            Toast.makeText(this, "download $uri", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "this isn’t a image or video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile(url: String) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        uri = Uri.parse(url)
        DI = uri.lastPathSegment.toString()
        val request = DownloadManager.Request(uri)
        request.setTitle("Download $uri")
        request.setDescription("Downloading")
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, DI)
        downloadManager.enqueue(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        url = webView.url!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_Download -> {
            webUrl = webView.url!!
            image()
            true
        }
        R.id.action_Center -> {
            webUrl = webView.url!!
            if (webUrl.contains("static1.e621.net")) {
                Picasso.get().load(webUrl).into(photo)
                photo.visibility = View.VISIBLE
                webView.visibility = View.INVISIBLE
            } else {
                Toast.makeText(this, "this isn’t a image or video", Toast.LENGTH_SHORT).show()
            }
            true
        }
        R.id.action_Back -> {
            if (photo.isVisible) {
                photo.visibility = View.INVISIBLE
                webView.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "cannot perform this", Toast.LENGTH_SHORT).show()
            }
            true
        }
        else -> {

            super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}