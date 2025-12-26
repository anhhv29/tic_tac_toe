package com.alpha.tech.tic.tac.toe.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.alpha.tech.tic.tac.toe.base.BaseActivity
import com.alpha.tech.tic.tac.toe.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private var player1: String = "Player 1"
    private var player2: String = "Player 2"
    private var clonePlayer2: String = ""
    private var player1ax = true
    private var selectedSinglePlayer = false

    private var easy = true
    private var medium = false
    private var hard = false
    private var impossible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDifficultySpinner()
        setupCheckBoxes()
        setupEditTexts()
        eventStartGame()
    }

    private fun eventStartGame() {
        binding.btnStart.setOnClickListener {
            startGame()
        }
    }

    private fun setupDifficultySpinner() {
        val difficulties = listOf("Easy", "Medium", "Hard", "Impossible")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDifficulty.adapter = adapter
        binding.spDifficulty.isEnabled = false

        binding.spDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (parent?.getItemAtPosition(position).toString()) {
                    "Easy" -> {
                        easy = true; medium = false; hard = false; impossible = false
                    }

                    "Medium" -> {
                        easy = false; medium = true; hard = false; impossible = false
                    }

                    "Hard" -> {
                        easy = false; medium = false; hard = true; impossible = false
                    }

                    "Impossible" -> {
                        easy = false; medium = false; hard = false; impossible = true
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                medium = true
                easy = false
                hard = false
                impossible = false
            }
        }
    }

    private fun setupEditTexts() {
        binding.edtPlayerOne.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                player1 = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtPlayerTwo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                player2 = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCheckBoxes() {
        val listener = View.OnClickListener { view ->
            val checked = (view as? CheckBox)?.isChecked ?: false
            when (view.id) {
                binding.cbPlayer1x.id -> handlePlayer1X(checked)
                binding.cbPlayer1o.id -> handlePlayer1O(checked)
                binding.cbPlayer2x.id -> handlePlayer2X(checked)
                binding.cbPlayer2o.id -> handlePlayer2O(checked)
                binding.cbSinglePlayer.id -> handleSinglePlayer(checked)
                binding.cbTwoPlayer.id -> handleTwoPlayer(checked)
            }
        }

        binding.cbPlayer1x.setOnClickListener(listener)
        binding.cbPlayer1o.setOnClickListener(listener)
        binding.cbPlayer2x.setOnClickListener(listener)
        binding.cbPlayer2o.setOnClickListener(listener)
        binding.cbSinglePlayer.setOnClickListener(listener)
        binding.cbTwoPlayer.setOnClickListener(listener)

        binding.cbPlayer1x.isChecked = true
        binding.cbPlayer2o.isChecked = true
        binding.cbTwoPlayer.isChecked = true
    }

    private fun handlePlayer1X(checked: Boolean) {
        if (checked) {
            binding.cbPlayer1o.isChecked = false
            binding.cbPlayer2x.isChecked = false
            binding.cbPlayer2o.isChecked = true
            player1ax = true
        } else {
            binding.cbPlayer1o.isChecked = true
            binding.cbPlayer2x.isChecked = true
            binding.cbPlayer2o.isChecked = false
            player1ax = false
        }
    }

    private fun handlePlayer1O(checked: Boolean) {
        if (checked) {
            binding.cbPlayer1x.isChecked = false
            binding.cbPlayer2o.isChecked = false
            binding.cbPlayer2x.isChecked = true
            player1ax = false
        } else {
            binding.cbPlayer1x.isChecked = true
            binding.cbPlayer2o.isChecked = true
            binding.cbPlayer2x.isChecked = false
            player1ax = true
        }
    }

    private fun handlePlayer2X(checked: Boolean) {
        if (checked) {
            binding.cbPlayer2o.isChecked = false
            binding.cbPlayer1x.isChecked = false
            binding.cbPlayer1o.isChecked = true
            player1ax = false
        } else {
            binding.cbPlayer2o.isChecked = true
            binding.cbPlayer1x.isChecked = true
            binding.cbPlayer1o.isChecked = false
            player1ax = true
        }
    }

    private fun handlePlayer2O(checked: Boolean) {
        if (checked) {
            binding.cbPlayer2x.isChecked = false
            binding.cbPlayer1o.isChecked = false
            binding.cbPlayer1x.isChecked = true
            player1ax = true
        } else {
            binding.cbPlayer2x.isChecked = true
            binding.cbPlayer1o.isChecked = true
            binding.cbPlayer1x.isChecked = false
            player1ax = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSinglePlayer(checked: Boolean) {
        if (checked) {
            binding.cbTwoPlayer.isChecked = false
            selectedSinglePlayer = true
            clonePlayer2 = player2
            binding.edtPlayerTwo.setText("CPU")
            binding.edtPlayerOne.imeOptions = EditorInfo.IME_ACTION_DONE
            binding.edtPlayerOne.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE)
            binding.spDifficulty.isEnabled = true
        } else {
            binding.cbTwoPlayer.isChecked = true
            selectedSinglePlayer = false
            binding.edtPlayerTwo.setText(clonePlayer2)
            binding.edtPlayerOne.imeOptions = EditorInfo.IME_ACTION_NEXT
            binding.edtPlayerOne.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_NEXT)
            binding.spDifficulty.isEnabled = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleTwoPlayer(checked: Boolean) {
        if (checked) {
            binding.cbSinglePlayer.isChecked = false
            selectedSinglePlayer = false
            binding.edtPlayerTwo.setText(clonePlayer2)
            binding.edtPlayerOne.imeOptions = EditorInfo.IME_ACTION_NEXT
            binding.edtPlayerOne.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_NEXT)
            binding.spDifficulty.isEnabled = false
        } else {
            binding.cbSinglePlayer.isChecked = true
            selectedSinglePlayer = true
            binding.edtPlayerTwo.setText("CPU")
            binding.edtPlayerOne.imeOptions = EditorInfo.IME_ACTION_DONE
            binding.edtPlayerOne.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE)
            binding.spDifficulty.isEnabled = true
        }
    }

    fun startGame() {
        if (!selectedSinglePlayer && player2.isEmpty()) player2 = "player 2"
        if (player1.isEmpty()) player1 = "player 1"

        val players = arrayOf(player1, player2)
        val intent = Intent(this, AfterStartActivity::class.java).apply {
            putExtra("easy", easy)
            putExtra("medium", medium)
            putExtra("hard", hard)
            putExtra("impossible", impossible)
            putExtra("players_names", players)
            putExtra("player1ax", player1ax)
            putExtra("selected_single_player", selectedSinglePlayer)
        }
        startActivity(intent)
    }
}