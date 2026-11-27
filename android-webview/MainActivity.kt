package com.example.bpnote

import android.annotation.SuppressLint
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 * 血圧記録ノート WebView版
 * app/src/main/assets/ に index.html と icons/ フォルダを入れて使います。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    // 「復元する」（JSONファイル選択）用
    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        fileCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else null)
        fileCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true      // 記録の保存（localStorage）に必須
            allowFileAccess = true
        }
        web.addJavascriptInterface(Bridge(), "AndroidBridge")  // CSV・バックアップ保存用

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?, callback: ValueCallback<Array<Uri>>?, params: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                pickFile.launch("*/*")
                return true
            }
        }
        web.webViewClient = WebViewClient()
        web.loadUrl("file:///android_asset/index.html")

        // 戻るボタン：ポップアップを閉じる → アプリ終了
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (web.canGoBack()) web.goBack() else finish()
            }
        })
    }

    /** HTML側の download() から呼ばれ、「ダウンロード」フォルダに保存する */
    inner class Bridge {
        @JavascriptInterface
        fun saveFile(name: String, text: String, mime: String) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, name)
                        put(MediaStore.Downloads.MIME_TYPE, mime.substringBefore(";"))
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: throw Exception("保存先を作成できません")
                    contentResolver.openOutputStream(uri)!!.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                } else {
                    val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    File(dir, name).writeText(text, Charsets.UTF_8)
                }
                toast("ダウンロードに保存しました：$name")
            } catch (e: Exception) {
                toast("保存できませんでした：${e.message}")
            }
        }
    }

    private fun toast(msg: String) = runOnUiThread {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
