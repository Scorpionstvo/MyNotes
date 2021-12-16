package com.example.myproject.project.hidden

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentHiddenNotesBinding
import com.example.myproject.project.data.AdapterItemModel
import com.example.myproject.project.adapter.NoteAdapter
import com.example.myproject.project.type.Type
import com.example.myproject.project.list.NormalNotesFragment
import com.example.myproject.project.model.DataModel
import com.example.myproject.project.data.Note
import com.example.myproject.project.util.Constants
import kotlin.collections.ArrayList

class HiddenNotesFragment : Fragment(), NoteAdapter.ItemClickListener {
    private var binding: FragmentHiddenNotesBinding? = null
    private val dataModel: DataModel by viewModels()
    private val adapter = NoteAdapter(this)
    private var hiddenList = ArrayList<AdapterItemModel>()
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
            hiddenList = it
            hiddenList.sort()
            adapter.updateAdapter(hiddenList)
            binding?.tvHiddenListEmpty?.visibleIf(hiddenList.isEmpty())
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
                resources.getDrawable(R.drawable.ic_grid)
        } else {
            binding?.rcHiddenList?.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            binding?.tbHiddenNotes?.menu?.findItem(R.id.list)?.icon =
                resources.getDrawable(R.drawable.ic_list)
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
                    val check = dataModel.getCheckedId().size < hiddenList.size
                    dataModel.allChecked(check)
                    dataModel.getAdapterItemList("", type)
                    val newCount = dataModel.getCheckedId().size
                    binding?.tvHiddenNotesTitle?.text =
                        resources.getString(R.string.selected) + " $newCount"
                    bottomMenuEnable(newCount > 0)

                    var isAnchor = false
                    for (adapterItemModel in hiddenList) {
                        if (dataModel.getCheckedId().contains(adapterItemModel.note.id))
                            if (!adapterItemModel.note.isTop) isAnchor = true
                    }

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
        binding?.btMenuHiddenNotes?.setOnItemSelectedListener {
            val checkedId: HashSet<Int> = dataModel.getCheckedId()
            when (it.itemId) {
                R.id.declassify -> {
                    for (itemModel in hiddenList) {
                        if (checkedId.contains(itemModel.note.id)) {
                            declassify(itemModel.note)
                            adapter.notifyItemRemoved(itemModel.note.id)
                        }
                    }
                    dataModel.getAdapterItemList("", type)
                    goToNormalView()
                }
                R.id.pinToTopOfList -> {
                    if (it.title.equals(resources.getString(R.string.anchor)))
                        for (itemModel in hiddenList) {
                            if (checkedId.contains(itemModel.note.id)) {
                                moveTop(itemModel.note)
                                adapter.notifyItemRemoved(itemModel.note.id)
                            }
                        }
                    else
                        for (item in hiddenList) {
                            if (checkedId.contains(item.note.id)) {
                                checkedId.remove(item.note.id)
                                removeTop(item.note)
                                adapter.notifyItemRemoved(item.note.id)
                            }
                        }
                    dataModel.getAdapterItemList("", type)
                    goToNormalView()
                    it.setIcon(R.drawable.ic_pin)
                    it.setTitle(R.string.anchor)
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
                        for (itemModel in hiddenList) {
                            if (checkedId.contains(itemModel.note.id)) {
                                moveToTrash(itemModel.note)
                                adapter.notifyItemRemoved(itemModel.note.id)
                            }
                        }
                        dialog.dismiss()
                        dataModel.getAdapterItemList("", type)
                        goToNormalView()
                    }
                    val alert = alertDialog.create()
                    alert.show()
                }
            }
            true
        }
    }

    private fun moveTop(note: Note) {
        note.isTop = true
        dataModel.updateNote(note)
    }

    private fun removeTop(note: Note) {
        note.isTop = false
        dataModel.updateNote(note)
    }

    private fun declassify(note: Note) {
        note.typeName = Type.IS_NORMAL.name
        dataModel.updateNote(note)
    }

    private fun moveToTrash(note: Note) {
        note.typeName = Type.IS_TRASHED.name
        note.removalTime = System.currentTimeMillis()
        dataModel.updateNote(note)
    }

    private fun goToNormalView() {
        binding?.btMenuHiddenNotes?.visibility = View.GONE
        binding?.fbAdd?.visibility = View.VISIBLE
        binding?.tvHiddenNotesTitle?.text = resources.getString(R.string.title_toolbar_hidden_notes)
        binding?.tbHiddenNotes?.menu?.clear()
        binding?.tbHiddenNotes?.inflateMenu(R.menu.list_or_grid_toolbar_menu)
        adapter.isShowCheckBox(false)
        dataModel.allChecked(false)

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
                var isAnchor = false
                val checkedId = dataModel.getCheckedId()
                for (adapterItemModel in hiddenList) {
                    if (checkedId.contains(adapterItemModel.note.id))
                        if (!adapterItemModel.note.isTop) isAnchor = true
                }
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
        dataModel.closeBd()
        super.onDestroy()
    }
}
