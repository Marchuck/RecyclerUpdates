package pl.marczak.recyclerupdates

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import kotlin.random.Random

class ActuatorViewModel : ViewModel() {

    private val random = Random(0)

    companion object {
        val INTERVAL_MS = 2_000L

        val VISIBLE_CELLS_LIMIT = 15
        val CELLS_COUNT = 50
    }

    private val visibleRange =
        MutableStateFlow(VisibleRange(first = 0, last = VISIBLE_CELLS_LIMIT - 1))

    private var updatingScope = CoroutineScope(Dispatchers.IO)

    suspend fun ActivatorEntry.updateWithNewValue(): ActivatorEntry {
        val randomTime = random.nextLong(50, 1_100)
        val randomValue = random.nextInt(0, 100)
        delay(randomTime)
        val newValue = "$randomValue %"
        return copy(
            value = newValue,
            activationState = activationState.activateUp()
        )
    }

    val viewState = MutableStateFlow(ActuatorViewState(
        items = (0..CELLS_COUNT).map { index ->
            ActivatorEntry(
                position = index,
                name = "item_$index",
                value = index.toString(),
                activationState = if (index < VISIBLE_CELLS_LIMIT) {
                    ActivationState.ACTIVATING
                } else {
                    ActivationState.NOT_ACTIVE
                }
            )
        }
    ))

    fun onCommand(command: ActuatorCommand) = when (command) {
        ActuatorCommand.Pause -> {
            pauseUpdatingTask()
        }
        ActuatorCommand.Resume -> {
            startUpdatingTask()
        }
        is ActuatorCommand.WindowChanged -> {
            visibleRange.value = VisibleRange(command.first, command.last)
        }
    }

    private fun startUpdatingTask() {
        Timber.w("startUpdatingTask")
        updatingScope.cancel()
        updatingScope = CoroutineScope(Dispatchers.IO)
        updatingScope.launch {
            while (isActive) {
                val (from, to) = visibleRange.value
                val currentState = viewState.value
                val currentItems = currentState.items
                val startTime = System.currentTimeMillis()
                val updatedItems = updateInRange(updatingScope, currentItems, from, to)

                viewState.value = currentState.copy(items = updatedItems)
                val elapsed = System.currentTimeMillis() - startTime
                delay((INTERVAL_MS - elapsed).coerceAtLeast(0L))
            }
        }
    }

    private suspend fun updateInRange(
        scope: CoroutineScope,
        currentItems: List<ActivatorEntry>,
        from: Int,
        to: Int
    ): List<ActivatorEntry> {
        return currentItems.map {
            scope.async {
                if (it.position in from..to) {
                    it.updateWithNewValue()
                } else {
                    val state = it.activationState
                    it.copy(
                        activationState = state.activateDown()
                    )
                }
            }
        }.awaitAll()
    }

    private fun pauseUpdatingTask() {
        Timber.w("pauseUpdatingTask")
        updatingScope.cancel()
    }
}

data class ActuatorViewState(
    val items: List<ActivatorEntry>
)

sealed class ActuatorCommand {

    data class WindowChanged(val first: Int, val last: Int) : ActuatorCommand()

    object Resume : ActuatorCommand()

    object Pause : ActuatorCommand()
}

data class VisibleRange(val first: Int, val last: Int)