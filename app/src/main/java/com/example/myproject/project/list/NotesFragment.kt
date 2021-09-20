package com.example.myproject.project.list

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myproject.project.util.OnBackPressedListener
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentNotesBinding
import com.example.myproject.data.db.DbManager
import com.example.myproject.project.note.Note
import com.example.myproject.project.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Collections.sort
import kotlin.collections.ArrayList

class NotesFragment : Fragment(), NoteAdapter.ShowDetail, OnBackPressedListener {
    private var binding: FragmentNotesBinding? = null
    private val adapter = NoteAdapter(this)
    private lateinit var dbManager: DbManager
    private var list = ArrayList<Note>()
    private var isListView = false
    lateinit var nameIconGrid: String
    private var isChecked = false
    lateinit var alertDialog: AlertDialog.Builder
    private var job: Job? = null

    interface OpenFragment {

        fun openDetailFragment(note: Note, isNew: Boolean, callerFragment: String)

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
        dbManager = context?.let { DbManager(it) }!!
        binding!!.rcList.adapter = adapter
        recyclerViewStateCreated()
        initToolbar()
        initSearchView()
        initButton()
        initBottomNavigationView()
    }


    private fun recyclerViewStateCreated() {
        val share =
                activity?.getSharedPreferences(
                        Constants.SHARED_PREF_NAME_NOTES_FRAGMENT,
                        Context.MODE_PRIVATE
                )
        isListView = share!!.getBoolean(Constants.SHARED_PREF_KEY_NOTES_FRAGMENT, false)
        if (isListView) {
            binding!!.rcList.layoutManager = GridLayoutManager(context, 1)
            nameIconGrid = resources.getString(R.string.grid)
        } else {
            binding!!.rcList.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            nameIconGrid = resources.getString(R.string.list)
        }
    }

    private fun changeStateRecyclerView(isListView: Boolean): String {
        return if (isListView) {
            binding!!.rcList.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            resources.getString(R.string.list)
        } else {
            binding!!.rcList.layoutManager = GridLayoutManager(context, 1)
            resources.getString(R.string.grid)
        }
    }

    private fun initToolbar() {
        binding!!.tbNotes.menu.findItem(R.id.list).title = nameIconGrid
        binding!!.tbNotes.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.list -> {
                    it.title = changeStateRecyclerView(isListView)
                    isListView = !isListView

                }
                R.id.trash_can -> {
                    (activity as OpenFragment).openTrashCanFragment()
                }
                R.id.personal_folder -> {
                    (activity as OpenFragment).openPasswordFragment(false)
                }
                R.id.chooseAll -> {
                    isChecked = adapter.getCheckedId().size < list.size
                    adapter.allChecked(isChecked)

                    binding!!.tvTitle.text =
                            resources.getString(R.string.selected) + " ${adapter.getCheckedId().size}"

                    val isEnabled = adapter.getCheckedId().size > 0
                    bottomMenuEnable(isEnabled)

                    var isAnchor = false
                    for (i in 0 until adapter.getCheckedId().size) {
                        if (!list[adapter.getCheckedId()[i]].isTop) {
                            isAnchor = true
                        }
                    }

                    if (isAnchor) {
                        changeIcon(true)
                    } else {
                        if (adapter.getCheckedId().size != 0) {
                            changeIcon(false)
                        }
                    }
                }
            }
            true
        }
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
            val newNote = Note()
            (activity as OpenFragment).openDetailFragment(newNote, true, Constants.NOTES_FRAGMENT)

        }
    }

    private fun initBottomNavigationView() {

        binding!!.btMenuNotes.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.hide -> {
                    val checkedItems = adapter.getCheckedId()
                    for (i in checkedItems.indices) {
                        val indexForDelete = checkedItems[i]
                        val note = list[indexForDelete]
                        dbManager.moveToPersonalFolder(
                                note
                        )
                    }

                    val noteMoved = this.resources.getQuantityString(R.plurals.plurals_note_moved, checkedItems.size)
                    Toast.makeText(context, "$noteMoved ${resources.getString(R.string.to_personal_folder)}", Toast.LENGTH_LONG)
                            .show()
                    fillAdapter("")
                    goToNormalView()
                }
                R.id.pinToTopOfList -> {
                    val checkedItems = adapter.getCheckedId()
                    if (it.title.equals(resources.getString(R.string.anchor))) {
                        for (i in checkedItems.indices) {
                            val indexForMoveTop = checkedItems[i]
                            list[indexForMoveTop].isTop = true
                            dbManager.updateItem(list[indexForMoveTop])
                        }
                    } else
                        for (i in checkedItems.indices) {
                            val indexForMoveTop = checkedItems[i]
                            list[indexForMoveTop].isTop = false
                            dbManager.updateItem(list[indexForMoveTop])
                        }
                    fillAdapter("")
                    goToNormalView()
                    it.setIcon(R.drawable.ic_pin)
                    it.setTitle(R.string.anchor)
                }
                R.id.deleteNotes -> {
                    val checkedItems = adapter.getCheckedId()
                    alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                            this.resources.getQuantityString(R.plurals.plurals_note_count, checkedItems.size, checkedItems.size)
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
                        for (i in checkedItems.indices) {
                            val indexForDelete = checkedItems[i]
                            val note = list[indexForDelete]
                            dbManager.removeItem(
                                    note
                            )
                            fillAdapter("")
                        }
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


    private fun goToNormalView() {
        binding?.btMenuNotes?.visibility = View.GONE
        binding?.fbAdd?.visibility = View.VISIBLE
        binding?.tvTitle?.text = ""
        binding?.tbNotes?.menu?.clear()
        binding?.tbNotes?.inflateMenu(R.menu.notes_toolbar_menu)
        adapter.isShowCheckBox(false)
    }

    override fun onResume() {
        super.onResume()
        dbManager.openDb()
        fillAdapter("")

    }

    override fun onClickElement(note: Note?, position: Int) {
        if (binding!!.fbAdd.visibility == View.VISIBLE) {
            (activity as OpenFragment).openDetailFragment(note!!, false, Constants.NOTES_FRAGMENT)
        } else {
            val count = adapter.getCheckedId().size
            binding!!.tvTitle.text = resources.getString(R.string.selected) + " $count"
            if (count > 0) {
                bottomMenuEnable(true)
                var isAnchor = false
                for (i in 0 until count) {
                    val index = adapter.getCheckedId()[i]
                    if (!list[index].isTop) isAnchor = true
                }
                changeIcon(isAnchor)
            } else bottomMenuEnable(false)
        }
    }


    override fun onLongClickElement() {
        binding!!.btMenuNotes.visibility = View.VISIBLE
        binding!!.fbAdd.visibility = View.GONE
        binding!!.tvTitle.text = resources.getString(R.string.select_objects)
        binding!!.tbNotes.menu?.clear()
        binding!!.tbNotes.inflateMenu(R.menu.choose_all_toolbar_menu)
        adapter.isShowCheckBox(true)
        if (adapter.getCheckedId().isEmpty()) {
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


    override fun onDestroy() {
        val savedVariant =
                activity?.getSharedPreferences(
                        Constants.SHARED_PREF_NAME_NOTES_FRAGMENT,
                        Context.MODE_PRIVATE
                )
                        ?.edit()
        savedVariant?.putBoolean(Constants.SHARED_PREF_KEY_NOTES_FRAGMENT, isListView)
        savedVariant?.apply()
        dbManager.closeDb()
        binding = null
        super.onDestroy()
    }

    private fun fillAdapter(text: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            list = dbManager.readDataFromNotesTable(text)
            sort(list)
            adapter.updateAdapter(list)
            if (list.isNotEmpty()) {
                binding!!.tvGreeting.visibility = View.GONE
            } else {
                binding!!.tvGreeting.visibility = View.VISIBLE
            }
        }

    }

    override fun onBackPressed(): Boolean {
        return if (binding!!.fbAdd.visibility == View.GONE) {
            binding!!.fbAdd.visibility = View.VISIBLE
            binding!!.btMenuNotes.visibility = View.GONE
            binding!!.tvTitle.text = ""
            adapter.isShowCheckBox(false)
            binding!!.tbNotes.menu?.clear()
            binding!!.tbNotes.inflateMenu(R.menu.notes_toolbar_menu)
            false
        } else true
    }


}


