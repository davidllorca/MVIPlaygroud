package com.mango.mviplayground

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.reduxkotlin.Store
import timber.log.Timber

abstract class StateViewModel<State, R>(
    private val storeFactory: CoroutineStoreFactory<State>,
) : ViewModel()
    where R : Renderer<State> {
    private val store: Store<State> by lazy { storeFactory.createStore(viewModelScope) }
    val renderer: R by lazy { createRenderer() }

    protected abstract fun createRenderer(): R
    private val _sideEffects: MutableSharedFlow<SideEffect> = MutableSharedFlow()
    val sideEffects: SharedFlow<SideEffect>
        get() = _sideEffects.asSharedFlow()

    val subscription = store.subscribe {
        Timber.d("Received state update: ${store.state}")
        renderer.renderState(store.state)
    }

    val currentState: State
        get() = store.state

    init {
        // Avoid immediate just for this case, as we may depend on base class be fully initialized too
        viewModelScope.launch(Dispatchers.Main) {
            renderer.renderState(store.state)
            onFinishInitialize()
        }
    }

    /**
     * Use to launch initialization routines (data loading, etc...)
     */
    protected abstract fun onFinishInitialize()

    protected suspend fun dispatchSideEffect(effect: SideEffect) {
        try {
            withTimeout(500) {
                _sideEffects.emit(effect)
            }
        } catch (to: TimeoutCancellationException) {
            Timber.e("Nobody is listening to side effects!!")
        }
    }

    protected fun dispatchMessage(msg: Any) {
        store.dispatch(msg)
    }

    override fun onCleared() {
        super.onCleared()
        // this clears the subscription
        subscription()
    }
}

/**
 * Pure UI connection, no other logics in implementors allowed, no analytics no shit!
 */
interface Renderer<in State> {
    fun renderState(state: State)
}

interface BindingRenderer<in State, in Binding> : Renderer<State> {
    fun connect(owner: LifecycleOwner, binding: Binding)
}

fun interface CoroutineStoreFactory<State> {
    fun createStore(scope: CoroutineScope): Store<State>
}
