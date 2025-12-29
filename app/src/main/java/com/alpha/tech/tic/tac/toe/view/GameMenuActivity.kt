package com.alpha.tech.tic.tac.toe.view

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityGameMenuBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameMenuActivity : BaseActivity() {
    private lateinit var binding: ActivityGameMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clickView()
    }

    private fun clickView() {
        binding.apply {
            btnPlayOnline.setOnClickListener {
                openGameOnline()
            }

            btnPlayOffline.setOnClickListener {
                openGameOffline()
            }
        }
    }

    private fun openGameOffline() {
        showLoading()
        lifecycleScope.launch {
            delay(3000)
            val intent = Intent(this@GameMenuActivity, GameOfflineActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openGameOnline() {
        showLoading()
        lifecycleScope.launch {
            delay(3000)
            val intent = Intent(this@GameMenuActivity, GameOnlineActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}