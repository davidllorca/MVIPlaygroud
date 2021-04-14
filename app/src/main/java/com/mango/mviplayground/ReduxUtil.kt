package com.mango.mviplayground

import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.reduxkotlin.Dispatcher
import org.reduxkotlin.Store

typealias SafeDispatcher = suspend ((Any) -> Any)

//typealias ScopedDispatch<Scope> = (Scope.(Any) -> Any)
interface ScopedDispatch<Scope : Any> {
    fun dispatch(action: Any): Any
    fun dispatch(block: Scope.() -> Any): Any
}

fun Dispatcher.toSafeDispatcher(coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main): SafeDispatcher = { action ->
    val outer = this
    withContext(coroutineDispatcher) {
        outer(action)
    }
}

fun <State> createLoggingMiddleware(): (Store<State>) -> (Dispatcher) -> (Any) -> Any = { store ->
    { next: Dispatcher ->
        { action: Any ->
            if (action is Function<*>) {
                throw IllegalStateException("Shouldn't reach this")
            } else {
                Log.d("Borrame", "Received command: $action")
                val res = next(action)
                Log.d("Borrame", "Result is: ${store.state}")
                res
            }
        }
    }
}

val DispatchLocalComposeProvider =
    compositionLocalOf<Dispatcher>(referentialEqualityPolicy()) { error("No dispatch defined") }
val DispatchScopeComposeProvider = compositionLocalOf<Any?> { null }

@Composable
fun <Scope : Any> ProvideDispatch(
    dispatch: Dispatcher,
    scope: Scope,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    DispatchLocalComposeProvider provides dispatch,
    DispatchScopeComposeProvider provides scope,
    content = content
)

@Composable
fun <Scope : Any> getScopedDispatch(): ScopedDispatch<Scope> {
    val scope = requireNotNull(DispatchScopeComposeProvider.current) as Scope
    val dispatcher = DispatchLocalComposeProvider.current
    return remember {
        object : ScopedDispatch<Scope> {
            override fun dispatch(action: Any): Any {
                return dispatcher(action)
            }

            override fun dispatch(block: Scope.() -> Any): Any {
                return dispatcher(scope.block())
            }
        }
    }
}
