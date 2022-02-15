package pl.marczak.recyclerupdates

import android.graphics.Color
import androidx.annotation.ColorInt

data class ActivatorEntry(
    val position: Int,
    val name: String,
    val value: String,
    val activationState: ActivationState
)

enum class ActivationState(
    @ColorInt val startColor: Int,
    @ColorInt val endColor: Int,
) {
    NOT_ACTIVE(Color.WHITE, Color.WHITE),
    ACTIVATING(Color.WHITE, Color.GREEN),
    ACTIVATING_DEACTIVATED(Color.YELLOW, Color.GREEN),
    ACTIVE(Color.GREEN, Color.GREEN),
    DEACTIVATING(Color.GREEN, Color.YELLOW),
    INACTIVE(Color.YELLOW, Color.WHITE);

    fun activateUp(): ActivationState = when (this) {
        NOT_ACTIVE -> ACTIVATING
        ACTIVATING -> ACTIVE
        ACTIVE -> ACTIVE
        DEACTIVATING -> ACTIVATING_DEACTIVATED
        ACTIVATING_DEACTIVATED -> ACTIVATING
        INACTIVE -> ACTIVATING
    }

    fun activateDown(): ActivationState = when (this) {
        NOT_ACTIVE -> NOT_ACTIVE
        ACTIVE -> DEACTIVATING
        ACTIVATING -> DEACTIVATING
        ACTIVATING_DEACTIVATED -> DEACTIVATING
        DEACTIVATING -> INACTIVE
        INACTIVE -> NOT_ACTIVE
    }

}