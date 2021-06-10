package com.mango.mviplayground.selectcountry.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mango.mviplayground.getScopedDispatch
import com.mango.mviplayground.selectcountry.domain.v1.SelectCountryScope
import com.mango.mviplayground.selectcountry.domain.v1.filterCountriesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

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

fun rememberCountryList(state: SelectCountryState): List<CountryView>  {
    return when (state) {
        is SelectCountryState.Error -> emptyList()
        is SelectCountryState.HappyPath -> state.payload.displayCountryList
    }
}

@OptIn(ExperimentalTime::class)
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

    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        CountryScreenScaffold(
            error != null,
            textQuery,
            onTextChanged = { textUpdated ->
                coroutineScope.launch {
                    delay(2000.milliseconds)
                    scopedDispatch.dispatch {
                        filterCountriesUseCase(textUpdated)
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                items(countries) { c ->
                    CountryItem(
                        c,
                        Modifier.fillParentMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryItem(country: CountryView, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clickable {  }
            .padding(16.dp)
    ) {
        Text(country.name)
        Spacer(modifier = Modifier.height(2.dp))
        Text(country.language, fontSize = 10.sp)
    }
    Divider()
}

val items = List(2) { idx ->
    val idxCode = idx.toString().padStart(4, '0')
    CountryView(CountryKey(idxCode, idxCode), "Country $idxCode", "Language of country $idxCode")
}

class CountriesProvider : CollectionPreviewParameterProvider<CountryView>(items)

@Preview(
    backgroundColor = 0xffffffff,
    name = "Country Cell",
    showBackground = true
)
@Composable
fun PreviewCountryItem(
    @PreviewParameter(CountriesProvider::class) country: CountryView
) {
    CountryItem(country = country, modifier = Modifier.fillMaxWidth())
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
