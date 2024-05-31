package com.example.calculator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class ScientificCalculator : SimpleCalculator() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val layoutId = intent.getIntExtra("layout", R.layout.activity_scientific_calculator)
        setContentView(layoutId)
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

    override fun equalsAction(view: View) {
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
                "log" -> result = log(firstNumber, secondNumber)
                "x^y" -> result = firstNumber.pow(secondNumber)
                else -> return
            }

            displayResult()

            isClearLine = true
            isNumberNegative = if(result < 0) true else false
            isSecondNumberSelected = false
            isFirstNumberSelected = if(view is Button) !view.text.equals("=") else false
        }
    }

    override fun enterNumberAction(view: View) {
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
            if(!resultTextView.text.isEmpty()
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

    override fun deleteLastDigit(view: View) {
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

    override fun clearAllOperations(view: View) {
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

    override fun changeNumberSign(view: View) {
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

    override fun selectOperation(view: View) {
        if (view is Button && isAnyNumber) {

            equalsAction(view)

            currentOperation = view.text.toString()

            calculateScientificActions(currentOperation)

            val numberString = resultTextView.text.toString()

            firstNumber = if(numberString.endsWith("."))
                extractOperandFromString(numberString + "0")
            else
                extractOperandFromString(numberString)

            isClearLine = true
            isFirstNumberSelected = true
        }
    }

    private fun calculateScientificActions(text : String) {
        when(text) {
            "sin" -> result = sin(extractOperandFromString(resultTextView.text.toString()))
            "cos" -> result = cos(extractOperandFromString(resultTextView.text.toString()))
            "tan" -> result = tan(extractOperandFromString(resultTextView.text.toString()))
            "ln" -> {
                val extractedOperand = extractOperandFromString(resultTextView.text.toString())
                if (extractedOperand < 0) {
                    Toast.makeText(this, "Logarytm jest niezdefiniowany dla liczb ujemnych", Toast.LENGTH_SHORT).show()
                    return
                }
                result = ln(extractedOperand)
            }
            "sqrt" -> {
                val extractedOperand = extractOperandFromString(resultTextView.text.toString())
                if (extractedOperand < 0) {
                    Toast.makeText(this, "Pierwiastek jest niezdefiniowany dla liczb ujemnych", Toast.LENGTH_SHORT).show()
                    return
                }
                result = sqrt(extractedOperand)
            }
            "%" -> result = extractOperandFromString(resultTextView.text.toString()) / 100
            "x^2" -> result = extractOperandFromString(resultTextView.text.toString()).pow(2)
            else -> return
        }
        isNumberNegative = if(result < 0) true else false
        displayResult()
    }


}
