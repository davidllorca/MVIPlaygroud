package com.mango.mviplayground.presentation

sealed class ToolbarState {
    class Title(val title: String) : ToolbarState()
    class Search(val query: String?) : ToolbarState()
}

