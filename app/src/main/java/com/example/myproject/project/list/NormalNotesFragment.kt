package com.example.myproject.project.list

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentNotesBinding
import com.example.myproject.project.data.AdapterItemModel
import com.example.myproject.project.adapter.NoteAdapter
import com.example.myproject.project.type.Type
import com.example.myproject.project.model.DataModel
import com.example.myproject.project.data.Note
import com.example.myproject.project.util.Constants
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class NormalNotesFragment : Fragment(), NoteAdapter.ItemClickListener {
    private var binding: FragmentNotesBinding? = null
    private val dataModel: DataModel by viewModels()
    private val adapter = NoteAdapter(this)
    private var list = ArrayList<AdapterItemModel>()
    private var isListView = false

    private val type = Type.IS_NORMAL.name

    interface OpenFragment {

        fun openDetailFragment(note: Note, isNew: Boolean)

        fun openTrashCanFragment()

        fun openPasswordFragment(isDataChange: Boolean)

    }

    companion object {
        fun newInstance() = NormalNotesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rcList?.adapter = adapter
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
            list = it
            list.sort()
            adapter.updateAdapter(list)
            binding?.tvListEmpty?.visibleIf(list.isEmpty())
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
            binding?.rcList?.layoutManager = GridLayoutManager(context, 1)
            binding?.tbNotes?.menu?.findItem(R.id.list)?.icon =
                resources.getDrawable(R.drawable.ic_grid)
        } else {
            binding?.rcList?.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            binding?.tbNotes?.menu?.findItem(R.id.list)?.icon =
                resources.getDrawable(R.drawable.ic_list)
        }
    }

    private fun initToolbar() {
        binding?.imFolders?.setOnClickListener {
            showPopupMenu(binding?.imFolders!!)
        }
        binding?.tbNotes?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.list -> {
                    isListView = !isListView
                    choiceStateRecyclerView(isListView)
                    saveTableVariant(isListView)
                }
                R.id.chooseAll -> {
                    val check = dataModel.getCheckedId().size < list.size
                    dataModel.allChecked(check)
                    val newCount = dataModel.getCheckedId().size
                    binding?.tvTitle?.text =
                        resources.getString(R.string.selected) + " $newCount"
                    bottomMenuEnable(newCount > 0)

                    var isAnchor = false
                    for (adapterItemModel in list) {
                        if (dataModel.getCheckedId().contains(adapterItemModel.note.id)) {
                            if (!adapterItemModel.note.isTop) isAnchor = true
                        }
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

    private fun showPopupMenu(v: View) {
        val popupMenu = PopupMenu(context, v)
        popupMenu.inflate(R.menu.folders_popup_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.trash_can ->
                    (activity as OpenFragment).openTrashCanFragment()
                R.id.personal_folder -> (activity as OpenFragment).openPasswordFragment(false)
            }
            true
        }
        popupMenu.show()
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

    private fun changeIcon(isAnchor: Boolean) {
        val menuItem = binding?.btMenuNotes?.menu?.findItem(R.id.pinToTopOfList)
        if (isAnchor) {
            menuItem?.setIcon(R.drawable.ic_pin)
            menuItem?.setTitle(R.string.anchor)
        } else {
            menuItem?.setIcon(R.drawable.ic_unfasten)
            menuItem?.setTitle(R.string.unfasten)
        }
    }

    private fun initSearchView() {
        binding?.svNotes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                dataModel.getAdapterItemList(newText!!, type)
                return true
            }
        })
    }

    private fun initButton() {
        binding!!.fbAdd.setOnClickListener {
            val newNote = Note(Type.IS_NORMAL.name)
            (activity as OpenFragment).openDetailFragment(newNote, true)
        }
    }

    private fun initBottomNavigationView() {

        binding?.btMenuNotes?.setOnItemSelectedListener {
            val checkedId: HashSet<Int> = dataModel.getCheckedId()
            when (it.itemId) {
                R.id.hide -> {
                    for (itemModel in list) {
                        if (checkedId.contains(itemModel.note.id))
                            moveToPersonalFolder(itemModel.note)
                        adapter.notifyItemRemoved(itemModel.note.id)
                    }

                    val noteMoved = this.resources.getQuantityString(
                        R.plurals.plurals_note_moved,
                        checkedId.size
                    )
                    Toast.makeText(
                        context,
                        "$noteMoved ${resources.getString(R.string.to_personal_folder)}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    goToNormalView()
                    dataModel.getAdapterItemList("", type)
                }

                R.id.pinToTopOfList -> {
                    if (it.title.equals(resources.getString(R.string.anchor)))
                        for (itemModel in list) {
                            if (checkedId.contains(itemModel.note.id))
                                moveTop(itemModel.note)
                            adapter.notifyItemRemoved(itemModel.note.id)
                        }
                    else {
                        for (itemModel in list) {
                            if (checkedId.contains(itemModel.note.id))
                                removeTop(itemModel.note)
                            adapter.notifyItemRemoved(itemModel.note.id)
                        }

                        it.setIcon(R.drawable.ic_pin)
                        it.setTitle(R.string.anchor)
                    }
                    dataModel.getAdapterItemList("", type)
                    goToNormalView()
                }
                R.id.deleteNotes -> {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                        this.resources.getQuantityString(
                            R.plurals.plurals_note_count,
                            checkedId!!.size,
                            checkedId.size
                        )
                    val message = "${resources.getString(R.string.delete)} $noteString?"
                    alertDialog.setMessage(message)
                    alertDialog.setNegativeButton(
                        R.string.undo
                    ) { dialog, _ ->
                        dialog.dismiss()
                        goToNormalView()
                    }
                    alertDialog.setPositiveButton(
                        R.string.ok
                    ) { dialog, _ ->
                        for (itemModel in list) {
                            if (checkedId.contains(itemModel.note.id))
                                moveToTrash(itemModel.note)
                            adapter.notifyItemRemoved(itemModel.note.id)
                        }
                        dataModel.getAdapterItemList("", type)
                        dialog.dismiss()
                        goToNormalView()
                    }

                    val alert = alertDialog.create()
                    alert.show()
                }
            }; true
        }
    }


    private fun moveToPersonalFolder(note: Note) {
        note.typeName = Type.IS_HIDDEN.name
        dataModel.updateNote(note)
    }


    private fun moveTop(note: Note) {
        note.isTop = true
        dataModel.updateNote(note)
    }

    private fun removeTop(note: Note) {
        note.isTop = false
        dataModel.updateNote(note)
    }

    private fun moveToTrash(note: Note) {
        note.typeName = Type.IS_TRASHED.name
        note.removalTime = System.currentTimeMillis()
        dataModel.updateNote(note)
    }

    private fun goToNormalView() {
        binding?.btMenuNotes?.visibility = View.GONE
        binding?.fbAdd?.visibility = View.VISIBLE
        binding?.imFolders?.visibility = View.VISIBLE
        binding?.tvTitle?.text = ""
        binding?.tbNotes?.menu?.clear()
        binding?.tbNotes?.inflateMenu(R.menu.list_or_grid_toolbar_menu)
        dataModel.allChecked(false)
        adapter.isShowCheckBox(false)
        dataModel.allChecked(false)
    }

    override fun onClickItem(note: Note?) {
        if (binding!!.fbAdd.visibility == View.VISIBLE) {
            (activity as OpenFragment).openDetailFragment(note!!, false)
        } else {
            dataModel.updateCheckedList(note!!.id)
            val count = dataModel.getCheckedId().size
            binding?.tvTitle?.text = resources.getString(R.string.selected) + " $count"
            if (count != 0) {
                bottomMenuEnable(true)
                var isAnchor = false
                val checkedId = dataModel.getCheckedId()
                for (adapterItemModel in list) {
                    if (checkedId.contains(adapterItemModel.note.id))
                        if (!adapterItemModel.note.isTop) isAnchor = true
                }
                changeIcon(isAnchor)
            } else bottomMenuEnable(false)
        }
    }


    override fun onLongClickItem() {
        binding?.btMenuNotes?.visibility = View.VISIBLE
        binding?.fbAdd?.visibility = View.GONE
        binding?.imFolders?.visibility = View.GONE
        binding?.tvTitle?.text = resources.getString(R.string.select_objects)
        binding?.tbNotes?.menu?.clear()
        binding?.tbNotes?.inflateMenu(R.menu.choose_all_toolbar_menu)
        adapter.isShowCheckBox(true)
        if (dataModel.getCheckedId().isEmpty()) {
            bottomMenuEnable(false)
        }
    }

    private fun bottomMenuEnable(isEnabled: Boolean) {
        if (isEnabled) {
            binding?.btMenuNotes?.menu?.findItem(R.id.pinToTopOfList)?.isEnabled = true
            binding?.btMenuNotes?.menu?.findItem(R.id.deleteNotes)?.isEnabled = true
            binding?.btMenuNotes?.menu?.findItem(R.id.hide)?.isEnabled = true
        } else {
            changeIcon(true)
            binding?.btMenuNotes?.menu?.findItem(R.id.pinToTopOfList)?.isEnabled = false
            binding?.btMenuNotes?.menu?.findItem(R.id.deleteNotes)?.isEnabled = false
            binding?.btMenuNotes?.menu?.findItem(R.id.hide)?.isEnabled = false
            binding!!.btMenuNotes.menu.findItem(R.id.plug).isChecked = true
            binding!!.btMenuNotes.menu.setGroupCheckable(0, false, true)
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
        binding?.rcList?.adapter = null
        callback.remove()
    }

    override fun onDestroy() {
        binding = null
        dataModel.closeBd()
        super.onDestroy()
    }

}
