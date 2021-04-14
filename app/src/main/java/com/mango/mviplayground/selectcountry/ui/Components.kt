package com.mango.mviplayground.selectcountry.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.mango.mviplayground.getScopedDispatch
import com.mango.mviplayground.selectcountry.data.Country

fun rememberErrorMsg(state: SelectCountryState): String? {
    return when (state) {
        is SelectCountryState.Error -> state.msg
        is SelectCountryState.HappyPath -> null
    }
}

fun rememberQueryText(state: SelectCountryState): String?  {
    return when (state) {
        is SelectCountryState.Error -> null
        is SelectCountryState.HappyPath -> state.payload.queryText
    }
}

fun rememberCountryList(state: SelectCountryState): List<Country>  {
    return when (state) {
        is SelectCountryState.Error -> emptyList<Country>()
        is SelectCountryState.HappyPath -> state.payload.displayCountryList
    }
}

@Composable
fun CountryScreen(state: SelectCountryState) {
//    val viewModel = viewModel<SelectCountryViewModel>()
//    val error by viewModel.errorMsg.collectAsState()
//    val textQuery by viewModel.queryText.collectAsState()
//    val countries

    val error = rememberErrorMsg(state)
    val textQuery = rememberQueryText(state)
    val countries = rememberCountryList(state)

    val scopedDispatch = getScopedDispatch<SelectCountryScope>()

    MaterialTheme {
        CountryScreenScaffold(
            error != null,
            textQuery,
            onTextChanged = { textUpdated ->
                scopedDispatch.dispatch {
                    filterCountriesUseCase(textUpdated)
                }
            }
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                items(countries) { c ->
                    Column(
                        Modifier
                            .fillParentMaxWidth()
                    ) {
                        Text(c.name)
                        c.languages.firstOrNull()?.let { lang ->
                            Text(lang.displayName)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun CountryScreenScaffold(
    showError: Boolean,
    text: String?,
    onTextChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {

    var queryTextValue by remember { mutableStateOf(TextFieldValue(text ?: "")) }


//    val queryTextValue = remember(text) { TextFieldValue(text ?: "") }
    val onValueChange: (TextFieldValue) -> Unit = remember {
        {
            queryTextValue = it
            onTextChanged(it.text)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar {
                TextField(
                    queryTextValue,
                    onValueChange,
                    enabled = !showError
                )
            }
        },
        content = content
    )
}
