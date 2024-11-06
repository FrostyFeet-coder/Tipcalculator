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
    private lateinit var tvNoofpeople: EditText // tvNoofpeople yahan declare kiya

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // System bars ke liye padding set karo
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvnooffreinds)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etBaseAmount = findViewById(R.id.etBaseAmount)
        seekBar = findViewById(R.id.seekBar)
        tvTipPercentLabel = findViewById(R.id.tvTipPercentLabel)
        tvTipAmount = findViewById(R.id.tvTipAmouunt) // Typo fix kiya
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipDescription = findViewById(R.id.tvTipDescription)
        tvBillPerPerson = findViewById(R.id.tvBillPerPerson)
        tvNoofpeople = findViewById(R.id.tvNoofpeople)

        // Initial tip description set karo
        updateTipDescription(INITIAL_TIP_PERCENT)

        // Initial tip percent set karo
        seekBar.progress = INITIAL_TIP_PERCENT
        tvTipPercentLabel.text = "$INITIAL_TIP_PERCENT%"
        tvTipDescription.text = "Good"

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "on progress changed $progress")
                tvTipPercentLabel.text = "$progress%"
                computeTip() // Jab progress change ho, tip dubara calculate karo
                updateTipDescription(progress)
                perpersonbill()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Tracking start hone pe kuch nahi karna
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Tracking stop hone pe kuch nahi karna
            }
        })

        // Base amount ke liye TextWatcher
        etBaseAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "after text changed $s")
                computeTip() // Jab base amount change ho, tip dubara calculate karo
            }
        })

        // Number of people ke liye TextWatcher
        tvNoofpeople.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                perpersonbill() // Jab number of people change ho, per person bill dubara calculate karo
            }
        })
    }

    // Helper function jo safely base amount ko Double mein convert kare
    private fun getBaseAmount(): Double {
        val baseAmountString = etBaseAmount.text.toString()
        return try {
            baseAmountString.toDouble()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid input", e)
            0.0
        }
    }

    // Tip aur total calculate karne ka function
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

    // Bill per person ko update karne ka function
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

    // Tip description aur color update karne ka function
    private fun updateTipDescription(progress: Int) {
        // Progress ke hisaab se description update karo
        val tipDescription = when (progress) {
            in 0..9 -> "Poor"
            in 10..14 -> "Acceptable"
            in 15..19 -> "Good"
            in 20..24 -> "Great"
            else -> "Amazing"
        }
        tvTipDescription.text = tipDescription

        // Red se green tak smooth color transition karo progress ke hisaab se
        val color = ArgbEvaluator().evaluate(
            progress.toFloat() / seekBar.max.toFloat(),
            ContextCompat.getColor(this, R.color.worst_tip), // Red
            ContextCompat.getColor(this, R.color.best_tip)  // Green
        ) as Int

        // Interpolated color ke saath text color update karo
        tvTipDescription.setTextColor(color)
    }
}
