package org.muslim.app.core.ui.text

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import org.muslim.app.core.common.text.Digits

/**
 * Central digit-normalizing input handler (PROJECT_PROMPT.md §8 "تقييد الأرقام
 * الغربية"): every text input in the app must route its [onValueChange] through
 * [normalizeDigitsInput] so Arabic-Indic (٠١٢٣…) and Persian (۰۱۲۳…) digits
 * typed by the device keyboard are converted to western (0-9) digits before the
 * value is stored or parsed. This keeps search, settings and comment fields
 * consistent in every locale.
 */
object DigitNormalizedInput {

    /** Converts [raw] to western digits then forwards it to [onChanged]. */
    fun onValueChange(raw: String, onChanged: (String) -> Unit) {
        onChanged(Digits.toWesternDigits(raw))
    }
}

/**
 * Convenience wrapper: same API as Material3 [OutlinedTextField], but every
 * user keystroke is passed through [DigitNormalizedInput.onValueChange] first.
 * Use it for free-text and numeric fields that must never lose digits.
 */
@Composable
fun DigitNormalizedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    supportingText: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { DigitNormalizedInput.onValueChange(it, onValueChange) },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        supportingText = supportingText,
    )
}

/** Filled variant of [DigitNormalizedOutlinedTextField]. */
@Composable
fun DigitNormalizedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    supportingText: @Composable (() -> Unit)? = null,
) {
    TextField(
        value = value,
        onValueChange = { DigitNormalizedInput.onValueChange(it, onValueChange) },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        supportingText = supportingText,
    )
}
