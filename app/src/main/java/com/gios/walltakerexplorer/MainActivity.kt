@file:Suppress("DEPRECATION")
@file:SuppressLint("StaticFieldLeak")

package com.gios.walltakerexplorer

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.dcastalia.localappupdate.DownloadApk
import com.github.kittinunf.fuel.httpGet
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import org.json.JSONArray
import kotlin.random.Random


lateinit var webUrl: String
lateinit var uri: Uri
lateinit var DI: String

lateinit var webView: WebView
var url = "https://walltaker.joi.how/"

lateinit var photo: ImageView
lateinit var swipeRefreshLayout: SwipeRefreshLayout

class MainActivity : AppCompatActivity() {
    private lateinit var menu: Menu

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storagePerm()
        if (isDarkThemeOn()) {
            this.supportActionBar!!.title =
                Html.fromHtml("<font color='#FFB300'>Walltaker Explorer</font>")
        } else {
            this.supportActionBar!!.title =
                Html.fromHtml("<font color='#0D47A1'>Walltaker Explorer</font>")
        }

        photo = findViewById(R.id.Photo)
        webView = findViewById(R.id.Explorer)
        swipeRefreshLayout = findViewById(R.id.swipe)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        // speeding page loading
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.enableSmoothTransition()
        webView.loadUrl(url)
        api()

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            Handler().postDelayed({
                swipeRefreshLayout.isRefreshing = false
                webView.reload()
            }, 500)
        }
    }

    private fun storagePerm() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _: Boolean ->
            }
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }


    private fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    private fun rand() {
        val link =
            "https://walltaker.joi.how/api/links.json?online"
        link.httpGet().header("User-Agent" to "Walltaker Explorer")
            .responseString { _, response, result ->
                if (response.statusCode == 200) {
                    val jsonArray = JSONArray(result.get())
                    //get a random link
                    val ran =
                        jsonArray
                            .getJSONObject(Random.nextInt(jsonArray.length()))
                            .getInt("id")
                    webView.post {
                        webView.loadUrl("https://walltaker.joi.how/links/$ran")
                    }
                }
            }
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
            if (webUrl.endsWith(".webm")) {
                Toast.makeText(this, "This doesn't work with videos", Toast.LENGTH_SHORT).show()
            } else if (webUrl.contains("static1.e621.net")) {
                Glide.with(this).load(webUrl).into(photo)
                photo.visibility = View.VISIBLE
                webView.visibility = View.INVISIBLE
            } else {
                Toast.makeText(this, "This isn’t a image", Toast.LENGTH_SHORT).show()
            }
            true
        }

        R.id.action_home -> {
            webView.loadUrl("https://walltaker.joi.how/")
            true
        }

        R.id.action_update -> {
            api()
            webView.reload()
            true
        }

        R.id.action_random -> {
            rand()
            true
        }

        R.id.action_search -> {
            webUrl = webView.url!!
            if (webUrl.contains("static1.e621.net")) {
                webView.loadUrl(
                    "https://e621.net/posts?tags=md5%3A${
                        webUrl.split("/").last().split(".").first()
                    }"
                )
            } else {
                Toast.makeText(this, "This isn’t a image", Toast.LENGTH_SHORT).show()
            }
            true
        }

        R.id.action_upApp -> {
            val url =
                "https://github.com/gios2/Walltaker-Explorer/raw/main/app/release/app-release.apk"
            val downloadApk = DownloadApk(this@MainActivity)
            downloadApk.startDownloadingApk(url)
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun api() {
        "https://status.e621.ws/json".httpGet().header("User-Agent" to "Walltaker-Explorer")
            .responseString { _, response, result ->
                val item = menu.findItem(R.id.action_home)
                if (response.statusCode == 200) {
                    val gson = GsonBuilder().create()
                    val data = gson.fromJson(result.get(), Current::class.java)
                    val current = data.current
                    runOnUiThread {
                        if (current["state"] == "up") {
                            item.setIcon(R.drawable.green)
                        } else {
                            item.setIcon(R.drawable.red)
                        }
                    }
                }
            }
    }

    @Deprecated("Deprecated in Java", replaceWith = ReplaceWith(""))
    override fun onBackPressed() {
        if (photo.isVisible) {
            photo.visibility = View.INVISIBLE
            webView.visibility = View.VISIBLE
            this.cacheDir.deleteRecursively()
            Glide.get(this).clearMemory()
        }
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

class Current(
    var current: LinkedTreeMap<String, Any>
)
