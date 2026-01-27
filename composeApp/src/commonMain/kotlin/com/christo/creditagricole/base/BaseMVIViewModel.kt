import com.christo.creditagricole.base.MviEffect
import com.christo.creditagricole.base.MviIntent
import com.christo.creditagricole.base.MviReducer
import com.christo.creditagricole.base.MviState
import com.christo.creditagricole.core.DispatcherProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

abstract class BaseMVIViewModel<I : MviIntent, S : MviState, R, E : MviEffect>(
    initialState: S,
    private val reducer: MviReducer<S, R>,
    private val dispatcherProvider: DispatcherProvider,
    coroutineScope: CoroutineScope? = null,
    intentsCapacity: Int = 64,
) {
    private val scope: CoroutineScope = coroutineScope ?: CoroutineScope(SupervisorJob() + dispatcherProvider.main)
    private val intents = Channel<I>(capacity = intentsCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<E>(extraBufferCapacity = intentsCapacity)
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    init {
        val handler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
        scope.launch(dispatcherProvider.main + handler) {
            for (intent in intents) {
                val result = withContext(dispatcherProvider.io) {
                    executeIntent(intent)
                }
                val previousState = _state.value
                val newState = reducer.reduce(previousState, result)
                if (newState != previousState) {
                    _state.value = newState
                    onStateChanged(previousState, newState)
                }
                onEffect(result)?.let { effect ->
                    _effects.emit(effect)
                }
            }
        }
    }

    protected abstract suspend fun executeIntent(intent: I): R

    protected open suspend fun onEffect(result: R): E? = null

    protected open suspend fun onStateChanged(previous: S, current: S) {}

    fun dispatch(intent: I) {
        if (!intents.trySend(intent).isSuccess) {
            scope.launch { intents.send(intent) }
        }
    }

    fun clear() {
        scope.cancel()
    }
}