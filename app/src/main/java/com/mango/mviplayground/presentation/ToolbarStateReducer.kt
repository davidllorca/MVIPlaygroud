package com.mango.mviplayground.presentation

fun toolbarStateReducer(state: ToolbarState, action: Any): ToolbarState {
    val toolbarAction = action as? ToolbarAction ?: return state
    return when (toolbarAction) {
        is ToolbarAction.SetQuery -> ToolbarState.Search(toolbarAction.query)
        is ToolbarAction.SetTitle -> ToolbarState.Title(toolbarAction.title)
    }
}

sealed class ToolbarAction {
    class SetTitle(val title: String) : ToolbarAction()
    class SetQuery(val query: String?) : ToolbarAction()
}
