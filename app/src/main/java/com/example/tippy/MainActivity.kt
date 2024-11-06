package com.example.tippy

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15

class MainActivity : AppCompatActivity() {
    private lateinit var etBaseAmount: EditText
    private lateinit var seekBar: SeekBar
    private lateinit var tvTipPercentLabel: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTipDescription: TextView
    private lateinit var tvBillPerPerson: TextView
    private lateinit var tvNoofpeople: EditText // Declare tvNoofpeople here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvnooffreinds)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etBaseAmount = findViewById(R.id.etBaseAmount)
        seekBar = findViewById(R.id.seekBar)
        tvTipPercentLabel = findViewById(R.id.tvTipPercentLabel)
        tvTipAmount = findViewById(R.id.tvTipAmouunt) // Fixed typo here
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipDescription = findViewById(R.id.tvTipDescription)
        tvBillPerPerson = findViewById(R.id.tvBillPerPerson)
        tvNoofpeople = findViewById(R.id.tvNoofpeople)

        // Set initial tip description
        updateTipDescription(INITIAL_TIP_PERCENT)

        // Set initial tip percent
        seekBar.progress = INITIAL_TIP_PERCENT
        tvTipPercentLabel.text = "$INITIAL_TIP_PERCENT%"
        tvTipDescription.text = "Good"

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "on progress changed $progress")
                tvTipPercentLabel.text = "$progress%"
                computeTip() // Recompute tip when progress changes
                updateTipDescription(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Code to handle start of tracking
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Code to handle stop of tracking
            }
        })

        // TextWatcher for base amount
        etBaseAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not implemented but could be used if needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not implemented but could be used if needed
            }

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "after text changed $s")
                computeTip() // Recompute tip when base amount changes
            }
        })

        // TextWatcher for number of people
        tvNoofpeople.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not implemented
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not implemented
            }

            override fun afterTextChanged(s: Editable?) {
                perpersonbill() // Recompute bill per person when the number of people changes
            }
        })
    }

    // Helper function to safely get the base amount as Double
    private fun getBaseAmount(): Double {
        val baseAmountString = etBaseAmount.text.toString()
        return try {
            baseAmountString.toDouble()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid input", e)
            0.0
        }
    }

    // Function to compute the tip and total
    private fun computeTip() {
        val baseAmount = getBaseAmount()
        if (baseAmount == 0.0) {
            tvTipAmount.text = "0.0"
            tvTotalAmount.text = "0.0"
            return
        }

        val tipPercent = seekBar.progress
        val tipAmount = baseAmount * (tipPercent / 100.0)
        val totalAmount = baseAmount + tipAmount

        tvTipAmount.text = String.format("%.2f", tipAmount)
        tvTotalAmount.text = String.format("%.2f", totalAmount)
    }

    // Function to update the bill per person
    private fun perpersonbill() {
        val baseAmount = getBaseAmount()
        if (baseAmount == 0.0) {
            tvTipAmount.text = "0.0"
            tvTotalAmount.text = "0.0"
            tvBillPerPerson.text = "0.0" // Default value for Bill per person
            return
        }

        val tipPercent = seekBar.progress
        val tipAmount = baseAmount * (tipPercent / 100.0)
        val totalAmount = baseAmount + tipAmount

        val peopleString = tvNoofpeople.text.toString()
        val numberOfPeople = try {
            peopleString.toInt()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid number of people", e)
            tvBillPerPerson.text = "0.0"
            return
        }

        val billPerPerson = if (numberOfPeople > 0) {
            totalAmount / numberOfPeople
        } else {
            0.0
        }

        tvTipAmount.text = String.format("%.2f", tipAmount)
        tvTotalAmount.text = String.format("%.2f", totalAmount)
        tvBillPerPerson.text = String.format("%.2f", billPerPerson)
    }

    // Function to update tip description and color
    private fun updateTipDescription(progress: Int) {
        // Update the description based on the progress
        val tipDescription = when (progress) {
            in 0..9 -> "Poor"
            in 10..14 -> "Acceptable"
            in 15..19 -> "Good"
            in 20..24 -> "Great"
            else -> "Amazing"
        }
        tvTipDescription.text = tipDescription

        // Smooth color transition from red to green based on the progress
        val color = ArgbEvaluator().evaluate(
            progress.toFloat() / seekBar.max.toFloat(),
            ContextCompat.getColor(this, R.color.worst_tip), // Red
            ContextCompat.getColor(this, R.color.best_tip)  // Green
        ) as Int

        // Update the text color with the interpolated color
        tvTipDescription.setTextColor(color)
    }
}
