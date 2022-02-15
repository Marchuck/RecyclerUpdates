package pl.marczak.recyclerupdates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.marczak.recyclerupdates.databinding.FragmentActivatorBinding

class ActivatorFragment : Fragment() {

    private lateinit var binding: FragmentActivatorBinding
    private var onScrollListener: VisibleRangeListener? = null
    private val viewModel by lazy { ActuatorViewModel() }
    private val adapter = ActuatorAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActivatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        onScrollListener = VisibleRangeListener(binding.recyclerView)
        onScrollListener?.onVisibleRangeChangedListener = { first, last ->
            viewModel.onCommand(ActuatorCommand.WindowChanged(first, last))
        }
        binding.recyclerView.addOnScrollListener(requireNotNull(onScrollListener))
        viewModel.viewState.asLiveData().observe(viewLifecycleOwner, ::renderState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onCommand(ActuatorCommand.Resume)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onCommand(ActuatorCommand.Pause)
    }

    private fun renderState(state: ActuatorViewState) {
        adapter.submitList(state.items)
    }

    private class VisibleRangeListener(recyclerView: RecyclerView) :
        RecyclerView.OnScrollListener() {

        private val layoutManager = requireNotNull(
            recyclerView.layoutManager as LinearLayoutManager
        )

        var onVisibleRangeChangedListener: (Int, Int) -> Unit = { _, _ -> }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            recalculateVisibleCells()
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recalculateVisibleCells()
        }

        private fun recalculateVisibleCells() {
            val first = layoutManager.findFirstCompletelyVisibleItemPosition()
            if (first != RecyclerView.NO_POSITION) {
                val last = (first + ActuatorViewModel.VISIBLE_CELLS_LIMIT)
                    .coerceAtMost(ActuatorViewModel.CELLS_COUNT)
                onVisibleRangeChangedListener.invoke(first, last)
            }
        }
    }
}
