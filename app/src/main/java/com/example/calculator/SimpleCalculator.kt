package com.example.calculator

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

open class SimpleCalculator : AppCompatActivity() {
    protected var isClearLine = false
    protected var isNumberNegative = false
    protected var isAnyNumber = false
    protected var currentOperation = ""
    protected var isFirstNumberSelected = false
    protected var isSecondNumberSelected = false
    protected var firstNumber : Double = 0.0
    protected var secondNumber : Double = 0.0
    protected var result : Double = 0.0
    protected var cleanerCounter = 0
    protected lateinit var resultTextView : TextView
    protected lateinit var clearButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_simple_calculator)
        findViewById<Button>(R.id.clearButton).text = "AC"

        resultTextView = findViewById<TextView>(R.id.outputView)
        clearButton = findViewById<Button>(R.id.clearButton)

        if(savedInstanceState != null) {
            findViewById<TextView>(R.id.outputView).text = savedInstanceState.getString("outputViewText")
            this.isClearLine = savedInstanceState.getBoolean("clearLine")
            this.isNumberNegative = savedInstanceState.getBoolean("isNumberMinus")
            this.isAnyNumber = savedInstanceState.getBoolean("isAnyNumber")
            this.currentOperation = savedInstanceState.getString("takenOperation").toString()
            this.isFirstNumberSelected = savedInstanceState.getBoolean("isFirstNumberSelected")
            this.isSecondNumberSelected = savedInstanceState.getBoolean("isSecondNumberSelected")
            this.firstNumber = savedInstanceState.getDouble("firstNumber")
            this.secondNumber = savedInstanceState.getDouble("secondNumber")
            this.result = savedInstanceState.getDouble("output")
            this.cleanerCounter = savedInstanceState.getInt("cleanerCounter")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val outputViewText = findViewById<TextView>(R.id.outputView).text.toString()
        outState.putString("outputViewText", outputViewText)
        outState.putBoolean("clearLine", isClearLine)
        outState.putBoolean("isNumberMinus", isNumberNegative)
        outState.putBoolean("isAnyNumber", isAnyNumber)
        outState.putString("takenOperation", currentOperation)
        outState.putBoolean("isFirstNumberSelected", isFirstNumberSelected)
        outState.putBoolean("isSecondNumberSelected", isSecondNumberSelected)
        outState.putDouble("firstNumber", firstNumber)
        outState.putDouble("secondNumber", secondNumber)
        outState.putDouble("output", result)
        outState.putInt("cleanerCounter", cleanerCounter)
    }

    open fun equalsAction(view: View) {
        if (resultTextView.text.isEmpty()) {
            Toast.makeText(this, "Niedozwolona operacja", Toast.LENGTH_SHORT).show()
            return
        }

        if(isFirstNumberSelected && isAnyNumber && !isClearLine) {
            val numberString = resultTextView.text.toString()

            secondNumber = if(numberString.endsWith("."))
                extractOperandFromString(numberString + "0")
            else
                extractOperandFromString(numberString)

            isSecondNumberSelected = true
        }

        if(isFirstNumberSelected && isSecondNumberSelected) {
            when(currentOperation) {
                "+" -> result = firstNumber + secondNumber
                "-" -> result = firstNumber - secondNumber
                "*" -> result = firstNumber * secondNumber
                "/" -> {
                    if(secondNumber.equals(0.0)) {
                        resultTextView.text = "Error"
                        isFirstNumberSelected = false
                        isSecondNumberSelected = false
                        isClearLine = true
                        isNumberNegative = false
                        return
                    }
                    result = firstNumber / secondNumber
                }
                else -> return
            }

            displayResult()

            isClearLine = true
            isNumberNegative = if(result < 0) true else false
            isSecondNumberSelected = false
            isFirstNumberSelected = if(view is Button) !view.text.equals("=") else false
        }
    }

    protected fun displayResult() {
        resultTextView.text = if (result % 1 == 0.0) String.format("%.0f", result) else DecimalFormat("0.###").format(result)
    }


    open fun enterNumberAction(view: View) {
        if (view is Button)
        {
            if(isClearLine) {
                resultTextView.setText("")
                isAnyNumber = false
                isClearLine = false
                cleanerCounter = 0
                clearButton.setText("AC")
            }

            if (view.text.equals(".") && resultTextView.text.isEmpty()) {
                resultTextView.append("0")
            }

            if(view.text.equals(".") &&
                resultTextView.text.count { it == '.' } == 1)
                return
            if(resultTextView.text.isNotEmpty()
                && extractOperandFromString(resultTextView.text.toString()) == 0.toDouble()
                && !view.text.equals(".")
                && resultTextView.text.count { it == '.' } == 0)
            {
                resultTextView.setText("")
            }

            resultTextView.append(view.text)
            isAnyNumber = true
            clearButton.setText("C")
        }
    }

    open fun deleteLastDigit(view: View) {
        val textToEdit = resultTextView.text
        val textLength = textToEdit.length
        if (textToEdit.isNotBlank()) {
            if(isNumberNegative && textLength == 2) {
                cleanerCounter = 0
                clearAllOperations(view)
                return
            }

            resultTextView.text = textToEdit.subSequence(0, textLength - 1)

        }

        if (textLength == 0)
            clearAllOperations(view)
    }

    open fun clearAllOperations(view: View) {
        if (cleanerCounter == 0) {
            cleanerCounter++
        } else {
            isFirstNumberSelected = false
            isSecondNumberSelected = false
            cleanerCounter = 0
            currentOperation = ""
        }
        clearButton.setText("AC")
        resultTextView.text = ""
        isNumberNegative = false
        isAnyNumber = false
        isClearLine = false
    }
    open fun changeNumberSign(view: View) {
        val textToEdit = resultTextView.text
        val textLength = textToEdit.length
        if(isAnyNumber) {
            if (resultTextView.text.startsWith("-")) {
                resultTextView.text = textToEdit.subSequence(1, textLength)
                isNumberNegative = false
            }
            else {
                resultTextView.text = "-$textToEdit"
                isNumberNegative = true

            }
        }
    }

    open fun selectOperation(view: View) {
        if (view is Button && isAnyNumber) {

            equalsAction(view)

            currentOperation = view.text.toString()

            val numberString = resultTextView.text.toString()

            firstNumber = if(numberString.endsWith("."))
                extractOperandFromString(numberString + "0")
            else
                extractOperandFromString(numberString)

            isClearLine = true
            isFirstNumberSelected = true
        }
    }

    protected fun extractOperandFromString(numberString : String) : Double {
        isNumberNegative = false
        return if (numberString.startsWith("-")) {
            numberString.substring(1).toDouble() * (-1)
        } else {
            numberString.toDouble()
        }
    }
}