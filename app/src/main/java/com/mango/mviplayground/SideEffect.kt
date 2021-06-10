package com.mango.mviplayground

sealed class SideEffect {
    class ToastEffect(val msg: String) : SideEffect()
}
