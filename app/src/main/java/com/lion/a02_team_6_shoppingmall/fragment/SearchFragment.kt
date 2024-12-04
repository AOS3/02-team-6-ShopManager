package com.lion.a02_team_6_shoppingmall.fragment

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lion.a02_team_6_shoppingmall.MainActivity
import com.lion.a02_team_6_shoppingmall.R
import com.lion.a02_team_6_shoppingmall.databinding.FragmentSearchBinding
import com.lion.a02_team_6_shoppingmall.databinding.RowMainBinding
import com.lion.a02_team_6_shoppingmall.repository.BookRepository
import com.lion.a02_team_6_shoppingmall.util.BookType
import com.lion.a02_team_6_shoppingmall.util.FragmentName
import com.lion.a02_team_6_shoppingmall.viewmodel.BookViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    lateinit var fragmentSearchBinding: FragmentSearchBinding
    lateinit var mainActivity: MainActivity
    var searchBookList = mutableListOf<BookViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentSearchBinding = FragmentSearchBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity
        settingToolbar()
        settingTextField()
        settingRecyclerView()
        return fragmentSearchBinding.root
    }

    fun settingToolbar() {
        fragmentSearchBinding.materialToolbarSearch.apply {
            title = "책 제목 검색"
            isTitleCentered = true
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                mainActivity.removeFragment(FragmentName.SEARCH_FRAGMENT)
            }
        }
    }

    fun settingRecyclerView() {
        fragmentSearchBinding.apply {
            recyclerSearch.adapter = RecyclerViewAdapterSearch()
            recyclerSearch.layoutManager = LinearLayoutManager(mainActivity)
            val deco = DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL)
            recyclerSearch.addItemDecoration(deco)
        }
    }

    inner class RecyclerViewAdapterSearch() : RecyclerView.Adapter<RecyclerViewAdapterSearch.ViewHolderSearch>() {
        inner class ViewHolderSearch(var rowMainBinding: RowMainBinding) : RecyclerView.ViewHolder(rowMainBinding.root), OnClickListener {
            var isLongClick = false

            override fun onClick(v: View?) {
                isLongClick = false
                val databundle = Bundle()
                databundle.putInt("bookIdx", searchBookList[adapterPosition].bookIdx)
                mainActivity.replaceFragment(FragmentName.SHOW_FRAGMENT, true, true, databundle)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSearch {
            val rowMainBinding = RowMainBinding.inflate(layoutInflater, parent, false)
            val viewHolderMain = ViewHolderSearch(rowMainBinding)

            rowMainBinding.root.setOnClickListener(viewHolderMain)

            return viewHolderMain
        }

        override fun getItemCount(): Int {
            return searchBookList.size
        }

        override fun onBindViewHolder(holder: ViewHolderSearch, position: Int) {
            holder.rowMainBinding.textViewRowType.text = searchBookList[position].bookType.str
            holder.rowMainBinding.textViewRowTitle.text = searchBookList[position].bookTitle
            holder.rowMainBinding.textViewRowCount.text = "${searchBookList[position].bookCount}권"
            if (searchBookList[position].bookCount <= 10){
                holder.rowMainBinding.textViewRowNeed.visibility = View.VISIBLE
            } else {
                holder.rowMainBinding.textViewRowNeed.visibility = View.GONE
            }
        }
    }

    fun settingTextField() {
        fragmentSearchBinding.apply {
            // 검색창에 포커스를 준다.
            mainActivity.showSoftInput(textFieldSearch.editText!!)

            // 키보드의 엔터를 누르면 동작하는 리스너
            textFieldSearch.editText?.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val work1 = async(Dispatchers.IO) {
                            val searchBookTitle = textFieldSearch.editText?.text.toString()
                            BookRepository.selectBookDataByTitle(mainActivity, searchBookTitle)
                        }
                        searchBookList = work1.await()
                        recyclerSearch.adapter?.notifyDataSetChanged()
                    }
                    mainActivity.hideSoftInput()
                    true
                } else {
                    false
                }
            }
        }
    }
}