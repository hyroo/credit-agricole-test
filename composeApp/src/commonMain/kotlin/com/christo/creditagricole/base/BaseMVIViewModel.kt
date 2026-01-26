import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

interface UiState
interface UiEvent
interface UiAction

interface StateMediator<S : UiState, A : UiAction> {
    fun uiState(): StateFlow<S>
    fun onAction(action: A)
}

fun interface Reducer<S : UiState, E : UiEvent> {
    fun reduce(current: S, event: E): S
}

abstract class BaseViewModel<S : UiState, E : UiEvent, A : UiAction>(
    initialState: S,
    private val reducer: Reducer<S, E>,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : StateMediator<S, A>, Closeable {

    private val job = SupervisorJob()
    protected val scope = CoroutineScope(dispatcher + job)

    private val _state = MutableStateFlow(initialState)
    override fun uiState(): StateFlow<S> = _state.asStateFlow()

    private val _event = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event: SharedFlow<E> = _event.asSharedFlow()

    abstract override fun onAction(action: A)

    protected fun sendEvent(event: E) {
        _state.value = reducer.reduce(_state.value, event)
        _event.tryEmit(event)
    }

    protected fun launchCatching(
        onFailure: (Throwable) -> Unit = ::onError,
        block: suspend () -> Unit
    ) {
        scope.launch {
            try {
                block()
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                onFailure(t)
            }
        }
    }

    protected open fun onError(t: Throwable) {
        // Logger / mapper AppError
    }

    override fun close() {
        job.cancel()
    }
}

interface Closeable {
    fun close()
}
