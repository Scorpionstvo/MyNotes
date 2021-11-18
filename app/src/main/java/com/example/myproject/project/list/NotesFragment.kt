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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentNotesBinding
import com.example.myproject.project.adapter.NoteAdapter
import com.example.myproject.project.type.Type
import com.example.myproject.project.application.MyApplication
import com.example.myproject.project.note.Note
import com.example.myproject.project.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class NotesFragment : Fragment(), NoteAdapter.ItemClickListener {
    private var binding: FragmentNotesBinding? = null
    private val adapter = NoteAdapter(this)
    private val dbManager = MyApplication.dbManager
    private var list = ArrayList<Note>()
    private var isListView = false
    private var isChecked = false
    lateinit var alertDialog: AlertDialog.Builder
    private var job: Job? = null

    private val type = Type.IS_NORMAL.name

    interface OpenFragment {

        fun openDetailFragment(note: Note, isNew: Boolean)

        fun openTrashCanFragment()

        fun openPasswordFragment(isDataChange: Boolean)

    }

    companion object {
        fun newInstance() = NotesFragment()
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
        recyclerViewStateCreated()
        initToolbar()
        initSearchView()
        initButton()
        initBottomNavigationView()
        initOnBackPressedListener()
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
                    val count = adapter.getCheckedCount()
                    isChecked = count < list.size
                    adapter.allChecked(isChecked)
                    val newCount = adapter.getCheckedCount()
                    binding?.tvTitle?.text =
                        resources.getString(R.string.selected) + " $newCount"

                    val isEnabled = newCount > 0
                    bottomMenuEnable(isEnabled)

                    var isAnchor = false
                    for (i in 0 until newCount) {
                        if (!adapter.getCheckedNotes()[i].isTop) {
                            isAnchor = true
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
                R.id.trash_can -> (activity as OpenFragment).openTrashCanFragment()
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
                fillAdapter(newText!!)
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
            val checkedItems = adapter.getCheckedNotes()
            when (it.itemId) {
                R.id.hide -> {
                    for (i in checkedItems) {
                        moveToPersonalFolder(i)
                    }
                    val noteMoved = this.resources.getQuantityString(
                        R.plurals.plurals_note_moved,
                        checkedItems.size
                    )
                    Toast.makeText(
                        context,
                        "$noteMoved ${resources.getString(R.string.to_personal_folder)}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    fillAdapter("")
                    goToNormalView()
                }
                R.id.pinToTopOfList -> {
                    if (it.title.equals(resources.getString(R.string.anchor))) {
                        for (i in checkedItems) {
                            moveTop(i)
                        }
                    } else
                        for (i in checkedItems) {
                            removeTop(i)
                        }
                    fillAdapter("")
                    goToNormalView()
                    it.setIcon(R.drawable.ic_pin)
                    it.setTitle(R.string.anchor)
                }
                R.id.deleteNotes -> {
                    alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                        this.resources.getQuantityString(
                            R.plurals.plurals_note_count,
                            checkedItems.size,
                            checkedItems.size
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
                        for (i in checkedItems) {
                            moveToTrash(i)
                        }
                        fillAdapter("")
                        dialog.dismiss()
                    }
                    goToNormalView()
                    val alert = alertDialog.create()
                    alert.show()
                }
            }
            true
        }
    }

    private fun moveToPersonalFolder(note: Note) {
        note.typeName = Type.IS_HIDDEN.name
        dbManager.updateItem(note)
    }

    private fun moveTop(note: Note) {
        note.isTop = true
        dbManager.updateItem(note)
    }

    private fun removeTop(note: Note) {
        note.isTop = false
        dbManager.updateItem(note)
    }

    private fun moveToTrash(note: Note) {
        note.typeName = Type.IS_TRASHED.name
        note.removalTime = System.currentTimeMillis()
        dbManager.updateItem(note)
    }

    private fun goToNormalView() {
        binding?.btMenuNotes?.visibility = View.GONE
        binding?.fbAdd?.visibility = View.VISIBLE
        binding?.imFolders?.visibility = View.VISIBLE
        binding?.tvTitle?.text = ""
        binding?.tbNotes?.menu?.clear()
        binding?.tbNotes?.inflateMenu(R.menu.list_or_grid_toolbar_menu)
        adapter.isShowCheckBox(false)
    }

    override fun onResume() {
        super.onResume()
        dbManager.openDb()
        fillAdapter("")
    }

    override fun onClickItem(note: Note?) {
        if (binding!!.fbAdd.visibility == View.VISIBLE) {
            (activity as OpenFragment).openDetailFragment(note!!, false)
        } else {
            val count = adapter.getCheckedCount()
            binding?.tvTitle?.text = resources.getString(R.string.selected) + " $count"
            if (count > 0) {
                bottomMenuEnable(true)
                var isAnchor = false
                val checkedItems = adapter.getCheckedNotes()
                for (i in checkedItems) {
                    if (!i.isTop) isAnchor = true
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
        if (adapter.getCheckedNotes().isEmpty()) {
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

    private fun fillAdapter(text: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            list = dbManager.readDataFromTable(text, type)
            adapter.updateAdapter(list)
            if (list.isNotEmpty()) {
                binding!!.tvGreeting.visibility = View.GONE
            } else {
                binding!!.tvGreeting.visibility = View.VISIBLE
            }
        }
    }

    private fun initOnBackPressedListener() {
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding?.fbAdd?.visibility == View.GONE) {
                    goToNormalView()
                } else {
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.rcList?.adapter = null
    }

    override fun onDestroy() {
        dbManager.closeDb()
        binding = null
        super.onDestroy()
    }

}
