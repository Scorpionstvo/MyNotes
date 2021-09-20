package com.example.myproject.project

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myproject.project.util.OnBackPressedListener
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentHiddenNotesBinding
import com.example.myproject.dummy.dataBase.DbManager
import com.example.myproject.project.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class HiddenNotesFragment : Fragment(), HiddenNoteAdapter.ShowDetailListener,
    OnBackPressedListener {
    private var binding: FragmentHiddenNotesBinding? = null
    private val adapter = HiddenNoteAdapter(this)
    private lateinit var dbManager: DbManager
    private var isListView = false
    private var nameIconGrid: String? = null
    private var job: Job? = null
    private var isChecked = true
    private var hiddenList = ArrayList<Note>()
    lateinit var alertDialog: AlertDialog.Builder

    interface OpenFragment {
        fun openDetailFragment(note: Note, isNew: Boolean, callerFragment: String)

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
        dbManager = context?.let { DbManager(it) }!!
        binding!!.rcHiddenList.adapter = adapter
        recyclerViewStateCreated()
        initToolbar()
        initSearchView()
        initBottomNavigationView()
    }

    private fun recyclerViewStateCreated() {
        if (isListView) {
            binding?.rcHiddenList?.layoutManager = GridLayoutManager(context, 1)
            nameIconGrid = resources.getString(R.string.grid)
        } else {
            binding!!.rcHiddenList.layoutManager = GridLayoutManager(context, 2)
            nameIconGrid = resources.getString(R.string.list)
        }
    }


    private fun changeStateRecyclerView(isListView: Boolean): String {
        return if (isListView) {
            binding!!.rcHiddenList.layoutManager = GridLayoutManager(context, 2)
            resources.getString(R.string.list)
        } else {
            binding!!.rcHiddenList.layoutManager = GridLayoutManager(context, 1)
            resources.getString(R.string.grid)
        }
    }

    private fun initToolbar() {
        binding!!.tbHiddenNotes.menu.findItem(R.id.list).title = nameIconGrid
        binding!!.tbHiddenNotes.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.list -> {
                    it.title = changeStateRecyclerView(isListView)
                    isListView = !isListView
                }
                R.id.chooseAll -> {
                    isChecked = adapter.getCheckedId().size < hiddenList.size
                    adapter.allChecked(isChecked)

                    binding!!.tvHiddenNotesTitle.text =
                        resources.getString(R.string.selected) + " ${adapter.getCheckedId().size}"

                    val isEnabled = adapter.getCheckedId().size > 0
                    bottomMenuEnable(isEnabled)

                }
                R.id.change_password -> {
                    (activity as OpenFragment).openPasswordFragment(true)
                }
            }
            true
        }
    }


    private fun initSearchView() {
        binding?.svHiddenNotes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                fillAdapter(newText!!)
                return true
            }
        })
    }


    private fun initBottomNavigationView() {
        binding!!.btMenuHiddenNotes.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.declassify -> {
                    val checkedItems = adapter.getCheckedId()
                    for (i in checkedItems.indices) {
                        val indexForDelete = checkedItems[i]
                        val note = hiddenList[indexForDelete]
                        dbManager.insertToTable(note)
                        dbManager.removeItemFromHiddenFragment(note)
                    }
                    fillAdapter("")
                    goToNormalView()
                }
                R.id.deleteNotes -> {
                    val checkedItems = adapter.getCheckedId()
                    alertDialog = AlertDialog.Builder(activity)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                        this.resources.getQuantityString(
                            R.plurals.plurals_note_count,
                            checkedItems.size
                        )
                    alertDialog.setMessage(resources.getString(R.string.delete) + " ${checkedItems.size}  $noteString?")
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
                            val note = hiddenList[indexForDelete]
                            dbManager.removeItemFromHiddenFragment(
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
        binding?.btMenuHiddenNotes?.visibility = View.GONE
        binding?.tvHiddenNotesTitle?.text = resources.getString(R.string.title_toolbar_hidden_notes)
        binding?.tbHiddenNotes?.menu?.clear()
        binding?.tbHiddenNotes?.inflateMenu(R.menu.hidden_toolbar_menu)
        adapter.isShowCheckBox(false)
    }

    override fun onResume() {
        super.onResume()
        dbManager.openDb()
        fillAdapter("")

    }

    override fun onClickElement(note: Note?, position: Int) {
        if (binding?.btMenuHiddenNotes?.visibility == View.GONE) {
            (activity as OpenFragment).openDetailFragment(note!!, false, Constants.HIDDEN_FRAGMENT)
        } else {
            val count = adapter.getCheckedId().size
            binding?.tvHiddenNotesTitle?.text = resources.getString(R.string.selected) + " $count"
            if (count > 0) {
                bottomMenuEnable(true)
                var isAncor = false
                for (i in 0 until count) {
                    val index = adapter.getCheckedId()[i]
                    if (!hiddenList[index].isTop) isAncor = true
                }

            } else bottomMenuEnable(false)
        }
    }


    override fun onLongClickElement() {
        binding?.btMenuHiddenNotes?.visibility = View.VISIBLE
        binding?.tvHiddenNotesTitle?.text = resources.getString(R.string.select_objects)
        binding?.tbHiddenNotes?.menu?.clear()
        binding?.tbHiddenNotes?.inflateMenu(R.menu.choose_all_toolbar_menu)
        adapter.isShowCheckBox(true)
        if (adapter.getCheckedId().isEmpty()) {
            bottomMenuEnable(false)
        }
    }

    private fun bottomMenuEnable(isEnabled: Boolean) {
        if (isEnabled) {
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.declassify)?.isEnabled = true
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.delete)?.isEnabled = true
        } else {
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.declassify)?.isEnabled = false
            binding?.btMenuHiddenNotes?.menu?.findItem(R.id.delete)?.isEnabled = false
        }
    }


    override fun onDestroy() {
        dbManager.closeDb()
        binding = null
        super.onDestroy()
    }

    private fun fillAdapter(text: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            hiddenList = dbManager.readDataFromHiddenTable(text)
            hiddenList.sort()
            adapter.updateAdapter(hiddenList)
            if (hiddenList.isNotEmpty()) {
                binding?.tvEmpty?.visibility = View.GONE
            } else {
                binding?.tvEmpty?.visibility = View.VISIBLE
            }
        }

    }

    override fun onBackPressed(): Boolean {
        return if (binding?.btMenuHiddenNotes?.visibility == View.VISIBLE) {
            binding?.btMenuHiddenNotes?.visibility = View.GONE
            binding?.tvHiddenNotesTitle?.text =
                resources.getString(R.string.title_toolbar_hidden_notes)
            adapter.isShowCheckBox(false)
            binding?.tbHiddenNotes?.menu?.clear()
            binding?.tbHiddenNotes?.inflateMenu(R.menu.hidden_toolbar_menu)
            false
        } else true
    }

}


