package com.example.myproject.project

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myproject.project.util.OnBackPressedListener
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentTrashBinding
import com.example.myproject.dummy.dataBase.DbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import java.util.Collections.sort
import kotlin.collections.ArrayList

class TrashFragment : Fragment(), TrashAdapter.TransferChoice, OnBackPressedListener {
    private var binding: FragmentTrashBinding? = null
    private var trashList = ArrayList<Note>()
    private var adapter = TrashAdapter(this)
    private lateinit var dbManager: DbManager
    private var isListView = false
    private var nameIconGrid: String? = null
    private var isChecked = true
    lateinit var alertDialog: AlertDialog.Builder
    private var job: Job? = null

    companion object {
        fun newInstance() = TrashFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrashBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbManager = context?.let { DbManager(it) }!!
        binding!!.rcDeletedList.adapter = adapter
        recyclerViewStateCreated()
        initToolbar()
        initSearchView()
        initBottomNavigationView()
        dbManager.openDb()
        fillAdapter("")
    }


    private fun recyclerViewStateCreated() {
        if (isListView) {
            binding?.rcDeletedList?.layoutManager = GridLayoutManager(context, 1)
            nameIconGrid = resources.getString(R.string.grid)
        } else {
            binding!!.rcDeletedList.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            nameIconGrid = resources.getString(R.string.list)
        }
    }


    private fun changeStateRecyclerView(isListView: Boolean): String {
        return if (isListView) {
            binding!!.rcDeletedList.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            resources.getString(R.string.list)
        } else {
            binding!!.rcDeletedList.layoutManager = GridLayoutManager(context, 1)
            resources.getString(R.string.grid)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initToolbar() {
        binding!!.tbTrashCan.menu.findItem(R.id.list).title = nameIconGrid
        binding!!.tbTrashCan.setNavigationIcon(R.drawable.ic_back)
        binding!!.tbTrashCan.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding!!.tbTrashCan.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.list -> {
                    it.title = changeStateRecyclerView(isListView)
                    isListView = !isListView
                }
                R.id.empty_trash -> {
                    dbManager.emptyTrash()
                    fillAdapter("")
                }
                R.id.chooseAll -> {

                    isChecked = adapter.getCheckedId().size < trashList.size
                    adapter.allChecked(isChecked)
                    binding?.tvTrashTitle?.text =
                        resources.getString(R.string.selected) + " ${adapter.getCheckedId().size}"
                    val isEnabled = adapter.getCheckedId().size > 0
                    bottomMenuEnable(isEnabled)
                }
            }
            true
        }
    }


    private fun initSearchView() {
        binding?.svDeletedNotes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
               fillAdapter(newText!!)
                return true
            }
        })
    }

    private fun restoreNote(note: Note) {
        dbManager.insertToTable(note)
        dbManager.removeItemFromTrashCan(note.id.toString())
    }


    private fun initBottomNavigationView() {
        binding!!.btMenuTrash.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.restore -> {
                    val checkedItems = adapter.getCheckedId()
                    for (i in checkedItems.indices) {
                        val indexForDelete = checkedItems[i]
                        val note = trashList[indexForDelete]
                        restoreNote(note)
                    }
                    fillAdapter("")
                    goToNormalView()
                }
                R.id.delete_permanently -> {
                    val checkedItems = adapter.getCheckedId()
                    alertDialog = AlertDialog.Builder(activity)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                        this.resources.getQuantityString(
                            R.plurals.plurals_note_count,
                            checkedItems.size
                        )
                    val message = "${resources.getString(R.string.delete_permanently)} $noteString?"
                    alertDialog.setMessage(message)
                    alertDialog.setNegativeButton(
                        R.string.undo
                    )
                    { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialog.setPositiveButton(
                        R.string.ok
                    )
                    { dialog, _ ->
                        for (i in checkedItems.indices) {
                            val indexForDelete = checkedItems[i]
                            val note = trashList[indexForDelete]
                            dbManager.removeItemFromTrashCan(
                                note.id.toString()
                            )

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


    private fun goToNormalView() {
        binding?.btMenuTrash?.visibility = View.GONE
        binding?.tvTrashTitle?.text = ""
        binding?.tbTrashCan?.menu?.clear()
        binding?.tbTrashCan?.inflateMenu(R.menu.trash_toolbar_menu)
        adapter.isShowCheckBox(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        dbManager.closeDb()
        binding = null
    }


    private fun deleteTimer(note: Note): Boolean {
        val calendar = Calendar.getInstance()
        val date = Date(note.removalTime)
        calendar.time = date
        val now = Calendar.getInstance()
        now.add(Calendar.DAY_OF_YEAR, -30)
        return calendar.get(Calendar.DAY_OF_YEAR) < now.get(Calendar.DAY_OF_YEAR) || calendar.get(
            Calendar.YEAR
        ) < now.get(Calendar.YEAR)
    }

    private fun fillAdapter(text: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            trashList = dbManager.readDataFromTrashTable(text)
            for (it in trashList) {
                if (deleteTimer(it)) dbManager.removeItemFromTrashCan(it.id.toString())
            }
            sort(trashList)
            adapter.updateAdapter(trashList)
            if (trashList.size > 0) {
                binding?.tvTrashListEmpty?.visibility = View.GONE
            } else {
                binding?.tvTrashListEmpty?.visibility = View.VISIBLE
            }
        }
    }

    override fun clickCheck() {
        val count = adapter.getCheckedId().size
        binding?.tvTrashTitle?.text = resources.getString(R.string.selected) + " $count"
        if (count > 0) bottomMenuEnable(true) else bottomMenuEnable(false)
    }

    override fun onLongClickElement() {
        binding?.btMenuTrash?.visibility = View.VISIBLE
        binding?.tvTrashTitle?.text = resources.getString(R.string.select_objects)
        binding?.tbTrashCan?.menu?.clear()
        binding?.tbTrashCan?.inflateMenu(R.menu.choose_all_toolbar_menu)
        adapter.isShowCheckBox(true)
        if (adapter.getCheckedId().isEmpty()) bottomMenuEnable(false)

    }

    private fun bottomMenuEnable(isEnabled: Boolean) {
        if (isEnabled) {
            binding!!.btMenuTrash.menu.findItem(R.id.deleteNotes)?.isEnabled = true
            binding!!.btMenuTrash.menu.findItem(R.id.restore)?.isEnabled = true
        } else {
            binding!!.btMenuTrash.menu.findItem(R.id.deleteNotes)?.isEnabled = false
            binding!!.btMenuTrash.menu.findItem(R.id.restore)?.isEnabled = false
            binding!!.btMenuTrash.menu.findItem(R.id.plug).isChecked = true
            binding!!.btMenuTrash.menu.setGroupCheckable(0, false, true)
        }
    }

    override fun onResume() {
        super.onResume()
        dbManager.openDb()
        fillAdapter("")
    }

    override fun onBackPressed(): Boolean {
        return if (binding?.btMenuTrash?.visibility == View.VISIBLE) {
            binding?.btMenuTrash?.visibility = View.GONE
            binding?.tvTrashTitle?.text = resources.getString(R.string.title_toolbar_trash_fragment)
            adapter.isShowCheckBox(false)
            binding?.tbTrashCan?.menu?.clear()
            binding?.tbTrashCan?.inflateMenu(R.menu.trash_toolbar_menu)
          false
        } else true
    }


}
