package pl.marczak.recyclerupdates

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pl.marczak.recyclerupdates.databinding.ItemActivatorEntryBinding
import pl.marczak.recyclerupdates.databinding.ItemActuatorEntryBinding

class ActuatorAdapter : ListAdapter<ActivatorEntry, ActuatorViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActuatorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemActuatorEntryBinding.inflate(inflater, parent, false)
        return ActuatorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActuatorViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class ActuatorViewHolder(
    private val binding: ItemActivatorEntryBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var animator: ValueAnimator? = null

    private fun animatorFactory(
        state: ActivationState
    ) = ValueAnimator.ofArgb(state.startColor, state.endColor).apply {
        duration = ActuatorViewModel.INTERVAL_MS
        addUpdateListener {
            itemView.setBackgroundColor(it.animatedValue as Int)
        }
    }

    fun bind(entry: ActivatorEntry) {
        binding.label.text = entry.name
        binding.value.text = entry.value
        animator?.cancel()
        animator = animatorFactory(entry.activationState)
        animator?.start()
    }
}

private class Diff : DiffUtil.ItemCallback<ActivatorEntry>() {
    override fun areItemsTheSame(oldItem: ActivatorEntry, newItem: ActivatorEntry): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: ActivatorEntry, newItem: ActivatorEntry): Boolean {
        return oldItem.value == newItem.value
    }
}
