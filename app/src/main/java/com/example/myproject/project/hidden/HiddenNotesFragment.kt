package com.example.myproject.project.hidden

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentHiddenNotesBinding
import com.example.myproject.project.adapter.NoteAdapter
import com.example.myproject.project.type.Type
import com.example.myproject.project.list.NormalNotesFragment
import com.example.myproject.project.model.DataModel
import com.example.myproject.project.data.Note
import com.example.myproject.project.util.Constants

class HiddenNotesFragment : Fragment(), NoteAdapter.ItemClickListener {
    private var binding: FragmentHiddenNotesBinding? = null
    private val dataModel: DataModel by viewModels()
    private val adapter = NoteAdapter(this)
    private var isListView = false

    private val type = Type.IS_HIDDEN.name

    interface OpenFragment {

        fun openDetailFragment(note: Note, isNew: Boolean)

        fun openPasswordFragment(isDataChange: Boolean)
    }

    companion object {
        fun newInstance() = HiddenNotesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHiddenNotesBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rcHiddenList?.adapter = adapter
        initDataModelContract()
        recyclerViewStateCreated()
        initToolbar()
        initSearchView()
        initButton()
        initBottomNavigationView()
        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun initDataModelContract() {
        dataModel.noteItemList.observe(viewLifecycleOwner, {
            adapter.updateAdapter(it)
            binding?.tvHiddenListEmpty?.visibleIf(it.isEmpty())
        })
    }

    private fun View.visibleIf(show: Boolean) {
        visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun recyclerViewStateCreated() {
        val share =
            activity?.getSharedPreferences(
                Constants.SHARED_PREF_NAME_NOTES_FRAGMENT,
                Context.MODE_PRIVATE
            )
        isListView = share!!.getBoolean(Constants.SHARED_PREF_KEY_NOTES_FRAGMENT, false)
        choiceStateRecyclerView(isListView)
    }

    private fun choiceStateRecyclerView(isListView: Boolean) {
        if (isListView) {
            binding?.rcHiddenList?.layoutManager = GridLayoutManager(context, 1)
            binding?.tbHiddenNotes?.menu?.findItem(R.id.list)?.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid)
        } else {
            binding?.rcHiddenList?.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            binding?.tbHiddenNotes?.menu?.findItem(R.id.list)?.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_list)
        }
    }

    private fun initToolbar() {
        binding?.imSecret?.setOnClickListener {
            (activity as OpenFragment).openPasswordFragment(true)
        }
        binding!!.tbHiddenNotes.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.list -> {
                    isListView = !isListView
                    choiceStateRecyclerView(isListView)
                    saveTableVariant(isListView)
                }
                R.id.chooseAll -> {
                    val check = dataModel.getCheckedId().size < dataModel.noteItemList.value!!.size
                    dataModel.allChecked(check)
                    it.changeColor(check)
                    val newCount = dataModel.getCheckedId().size
                    binding?.tvHiddenNotesTitle?.text =
                        resources.getString(R.string.selected) + " $newCount"
                    bottomMenuEnable(newCount > 0)
                    val isAnchor = dataModel.defineAnchor()
                    if (isAnchor) {
                        changeIcon(true)
                    } else {
                        if (newCount != 0) {
                            changeIcon(false)
                        }
                    }
                }
            }
            true
        }
    }

    private fun MenuItem.changeColor(color: Boolean) {
        if (color) icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ContextCompat.getColor(
                requireContext(), R.color.blue
            ), BlendModeCompat.SRC_ATOP
        )
        else icon.clearColorFilter()
    }

    private fun saveTableVariant(isListView: Boolean) {
        val savedVariant =
            activity?.getSharedPreferences(
                Constants.SHARED_PREF_NAME_NOTES_FRAGMENT,
                Context.MODE_PRIVATE
            )
                ?.edit()
        savedVariant?.putBoolean(Constants.SHARED_PREF_KEY_NOTES_FRAGMENT, isListView)
        savedVariant?.apply()
    }

    private fun initSearchView() {
        binding?.svHiddenNotes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                dataModel.getAdapterItemList(newText!!, type)
                return true
            }
        })
    }


    private fun initBottomNavigationView() {
        val checkedId: HashSet<Int> = dataModel.getCheckedId()
        binding?.btMenuHiddenNotes?.setOnItemSelectedListener {
            val checkedId: HashSet<Int> = dataModel.getCheckedId()
            when (it.itemId) {
                R.id.declassify -> {
                    checkedId.forEach { id ->
                        dataModel.movieToNormal(id)
                        adapter.notifyItemRemoved(id)
                    }
                    dataModel.getAdapterItemList("", type)
                    goToNormalView()
                }
                R.id.pinToTopOfList -> {
                    val isAnchor = dataModel.defineAnchor()
                    checkedId.forEach { id ->
                        dataModel.moveInList(isAnchor, id)
                        adapter.notifyItemRemoved(id)
                    }
                    it.setIcon(R.drawable.ic_pin)
                    it.setTitle(R.string.anchor)
                    dataModel.getAdapterItemList("", type)
                    goToNormalView()
                }
                R.id.delete -> {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                        this.resources.getQuantityString(
                            R.plurals.plurals_note_count,
                            checkedId.size,
                            checkedId.size
                        )
                    val message = "${resources.getString(R.string.delete)} $noteString?"
                    alertDialog.setMessage(message)
                    alertDialog.setNegativeButton(
                        R.string.undo
                    ) { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialog.setPositiveButton(
                        R.string.ok
                    ) { dialog, _ ->
                        checkedId.forEach { id ->
                            dataModel.movieToTrash(id)
                            adapter.notifyItemRemoved(id)
                        }
                        dataModel.getAdapterItemList("", type)
                        dialog.dismiss()
                        goToNormalView()
                    }
                    val alert = alertDialog.create()
                    alert.show()
                }
            }
            true
        }
    }

    private fun goToNormalView() {
        binding?.btMenuHiddenNotes?.visibility = View.GONE
        binding?.fbAdd?.visibility = View.VISIBLE
        binding?.tvHiddenNotesTitle?.text = resources.getString(R.string.title_toolbar_hidden_notes)
        binding?.tbHiddenNotes?.menu?.clear()
        binding?.tbHiddenNotes?.inflateMenu(R.menu.list_or_grid_toolbar_menu)
        adapter.isShowCheckBox(false)
        dataModel.allChecked(false)
        binding?.tbHiddenNotes?.menu?.findItem(R.id.chooseAll)?.changeColor(false)
    }


    private fun initButton() {
        binding!!.fbAdd.setOnClickListener {
            val newNote = Note(Type.IS_HIDDEN.name)
            (activity as NormalNotesFragment.OpenFragment).openDetailFragment(newNote, true)
        }
    }


    override fun onClickItem(note: Note?) {
        if (binding?.btMenuHiddenNotes?.visibility == View.GONE) {
            (activity as NormalNotesFragment.OpenFragment).openDetailFragment(note!!, false)
        } else {
            dataModel.updateCheckedList(note!!.id)
            val count = dataModel.getCheckedId().size
            binding?.tvHiddenNotesTitle?.text = resources.getString(R.string.selected) + " $count"
            if (count != 0) {
                bottomMenuEnable(true)
                val isAnchor = dataModel.defineAnchor()
                changeIcon(isAnchor)
            } else bottomMenuEnable(false)
        }
    }

    override fun onLongClickItem() {
        binding?.btMenuHiddenNotes?.visibility = View.VISIBLE
        binding!!.fbAdd.visibility = View.GONE
        binding?.tvHiddenNotesTitle?.text = resources.getString(R.string.select_objects)
        binding?.tbHiddenNotes?.menu?.clear()
        binding?.tbHiddenNotes?.inflateMenu(R.menu.choose_all_toolbar_menu)
        adapter.isShowCheckBox(true)
        if (dataModel.getCheckedId().isEmpty()) {
            bottomMenuEnable(false)
        }
    }


    private fun bottomMenuEnable(isEnabled: Boolean) {
        if (isEnabled) {
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.pinToTopOfList)?.isEnabled = true
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.delete)?.isEnabled = true
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.declassify)?.isEnabled = true
        } else {
            changeIcon(true)
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.pinToTopOfList)?.isEnabled = false
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.delete)?.isEnabled = false
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.declassify)?.isEnabled = false
            binding!!.btMenuHiddenNotes.menu.findItem(R.id.plug).isChecked = true
            binding!!.btMenuHiddenNotes.menu.setGroupCheckable(0, false, true)
        }
    }

    private fun changeIcon(isAnchor: Boolean) {
        val menuItem = binding?.btMenuHiddenNotes?.menu?.findItem(R.id.pinToTopOfList)
        if (isAnchor) {
            menuItem?.setIcon(R.drawable.ic_pin)
            menuItem?.setTitle(R.string.anchor)
        } else {
            menuItem?.setIcon(R.drawable.ic_unfasten)
            menuItem?.setTitle(R.string.unfasten)
        }
    }


    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding?.fbAdd?.visibility == View.GONE) {
                goToNormalView()
            } else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataModel.getAdapterItemList("", type)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback.remove()
        binding?.rcHiddenList?.adapter = null
    }


    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
