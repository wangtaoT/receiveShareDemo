package com.wt.receivesharedemo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etText = findViewById(R.id.etText)
        receiveActionSend(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        receiveActionSend(intent)
    }

    private fun receiveActionSend(intent: Intent?) {
        if (intent == null) {
            return
        }
        val action = intent.action
        val type = intent.type

        //判断action事件
        if (type == null || (Intent.ACTION_VIEW != action && Intent.ACTION_SEND != action)) {
            return
        }

        //取出文件uri
        var uri = intent.data
        if (uri == null) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        //获取文件真实地址
        val filePath = Utils.getFileFromUri(this, uri)

        filePath?.let {
            etText.setText(filePath)
        }
    }
}