package com.alpha.tech.tic.tac.toe.view

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityOpenAppBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OpenAppActivity : BaseActivity() {
    private lateinit var binding: ActivityOpenAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openMain()
    }

    private fun openMain() {
        lifecycleScope.launch {
            delay(3000)
            val intent = Intent(this@OpenAppActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}