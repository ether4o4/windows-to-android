package com.neversoft.launcher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim
import kotlin.math.abs

private const val ERROR_DIV_ZERO = "Cannot divide by zero"

private fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return ERROR_DIV_ZERO
    // Integral values render without a trailing ".0".
    if (value == value.toLong().toDouble() && abs(value) < 1e15) {
        return value.toLong().toString()
    }
    var s = value.toString()
    // Trim trailing zeros from a plain decimal (avoid touching exponent form).
    if (s.contains('.') && !s.contains('E') && !s.contains('e')) {
        s = s.trimEnd('0').trimEnd('.')
    }
    return s
}

private fun opSymbol(op: String): String = when (op) {
    "+" -> "+"
    "-" -> "−"
    "*" -> "×"
    "/" -> "÷"
    else -> ""
}

private fun compute(a: Double, b: Double, op: String): Double = when (op) {
    "+" -> a + b
    "-" -> a - b
    "*" -> a * b
    "/" -> if (b == 0.0) Double.NaN else a / b
    else -> b
}

@Composable
fun CalculatorApp() {
    // State machine: accumulator holds the running result, pendingOp the queued
    // operator, current the text being entered, justEvaluated tracks "=" so the
    // next digit starts fresh.
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var accumulator by remember { mutableStateOf(0.0) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var startNewEntry by remember { mutableStateOf(true) }
    var justEvaluated by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    fun resetAll() {
        display = "0"
        expression = ""
        accumulator = 0.0
        pendingOp = null
        startNewEntry = true
        justEvaluated = false
        hasError = false
    }

    fun currentValue(): Double = display.toDoubleOrNull() ?: 0.0

    fun inputDigit(d: String) {
        if (hasError) resetAll()
        if (startNewEntry || justEvaluated) {
            // A fresh number after "=" starts a brand-new calculation.
            if (justEvaluated && pendingOp == null) {
                expression = ""
                accumulator = 0.0
            }
            display = if (d == "0") "0" else d
            startNewEntry = false
            justEvaluated = false
        } else {
            display = if (display == "0") {
                if (d == "0") "0" else d
            } else {
                if (display.replace("-", "").replace(".", "").length >= 15) display
                else display + d
            }
        }
    }

    fun inputDot() {
        if (hasError) resetAll()
        if (startNewEntry || justEvaluated) {
            if (justEvaluated && pendingOp == null) {
                expression = ""
                accumulator = 0.0
            }
            display = "0."
            startNewEntry = false
            justEvaluated = false
        } else if (!display.contains('.')) {
            display = "$display."
        }
    }

    fun toggleSign() {
        if (hasError) return
        if (display == "0" || display == "0.") return
        display = if (display.startsWith("-")) display.removePrefix("-") else "-$display"
    }

    fun applyPercent() {
        if (hasError) return
        val value = currentValue()
        // Windows behaviour: percent of the accumulator when an op is pending,
        // otherwise percent of nothing (-> 0).
        val result = if (pendingOp != null) accumulator * value / 100.0 else value / 100.0
        display = formatNumber(result)
        startNewEntry = false
        justEvaluated = false
    }

    fun setOperator(op: String) {
        if (hasError) return
        val value = currentValue()
        if (pendingOp != null && !startNewEntry) {
            val result = compute(accumulator, value, pendingOp!!)
            if (result.isNaN() || result.isInfinite()) {
                display = ERROR_DIV_ZERO
                expression = ""
                hasError = true
                pendingOp = null
                return
            }
            accumulator = result
            display = formatNumber(result)
        } else {
            accumulator = value
        }
        pendingOp = op
        startNewEntry = true
        justEvaluated = false
        expression = "${formatNumber(accumulator)} ${opSymbol(op)}"
    }

    fun evaluate() {
        if (hasError) return
        val op = pendingOp ?: return
        val value = currentValue()
        val shownExpr = "${formatNumber(accumulator)} ${opSymbol(op)} ${formatNumber(value)} ="
        val result = compute(accumulator, value, op)
        if (result.isNaN() || result.isInfinite()) {
            display = ERROR_DIV_ZERO
            expression = ""
            hasError = true
            pendingOp = null
            return
        }
        expression = shownExpr
        accumulator = result
        display = formatNumber(result)
        pendingOp = null
        startNewEntry = true
        justEvaluated = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid)
            .padding(12.dp)
    ) {
        // Display area.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                color = NsColor.TextTertiary,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = display,
                color = NsColor.Text,
                fontSize = if (hasError) 30.sp else 48.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        // Button grid.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcRow {
                CalcButton("C", Modifier.weight(1f), CalcKind.Function) { resetAll() }
                CalcButton("+/−", Modifier.weight(1f), CalcKind.Function) { toggleSign() }
                CalcButton("%", Modifier.weight(1f), CalcKind.Function) { applyPercent() }
                CalcButton("÷", Modifier.weight(1f), CalcKind.Operator) { setOperator("/") }
            }
            CalcRow {
                CalcButton("7", Modifier.weight(1f), CalcKind.Number) { inputDigit("7") }
                CalcButton("8", Modifier.weight(1f), CalcKind.Number) { inputDigit("8") }
                CalcButton("9", Modifier.weight(1f), CalcKind.Number) { inputDigit("9") }
                CalcButton("×", Modifier.weight(1f), CalcKind.Operator) { setOperator("*") }
            }
            CalcRow {
                CalcButton("4", Modifier.weight(1f), CalcKind.Number) { inputDigit("4") }
                CalcButton("5", Modifier.weight(1f), CalcKind.Number) { inputDigit("5") }
                CalcButton("6", Modifier.weight(1f), CalcKind.Number) { inputDigit("6") }
                CalcButton("−", Modifier.weight(1f), CalcKind.Operator) { setOperator("-") }
            }
            CalcRow {
                CalcButton("1", Modifier.weight(1f), CalcKind.Number) { inputDigit("1") }
                CalcButton("2", Modifier.weight(1f), CalcKind.Number) { inputDigit("2") }
                CalcButton("3", Modifier.weight(1f), CalcKind.Number) { inputDigit("3") }
                CalcButton("+", Modifier.weight(1f), CalcKind.Operator) { setOperator("+") }
            }
            CalcRow {
                CalcButton("0", Modifier.weight(1f), CalcKind.Number) { inputDigit("0") }
                CalcButton(".", Modifier.weight(1f), CalcKind.Number) { inputDot() }
                CalcButton("=", Modifier.weight(2f), CalcKind.Equals) { evaluate() }
            }
        }
    }
}

private enum class CalcKind { Number, Operator, Function, Equals }

@Composable
private fun ColumnScope.CalcRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun RowScope.CalcButton(
    label: String,
    modifier: Modifier,
    kind: CalcKind,
    onClick: () -> Unit
) {
    val accent = LauncherState.accent
    val bg: Color = when (kind) {
        CalcKind.Number -> NsColor.ControlActive
        CalcKind.Function -> NsColor.ControlSelected
        CalcKind.Operator -> accent.copy(alpha = 0.22f)
        CalcKind.Equals -> accent
    }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(NsDim.RadiusControl))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = NsColor.Text,
            fontSize = 22.sp
        )
    }
}
