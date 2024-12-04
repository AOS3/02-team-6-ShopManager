package com.lion.a02_team_6_shoppingmall.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lion.a02_team_6_shoppingmall.MainActivity
import com.lion.a02_team_6_shoppingmall.R
import com.lion.a02_team_6_shoppingmall.databinding.FragmentBookCategorySubBinding
import com.lion.a02_team_6_shoppingmall.databinding.RowMainBinding
import com.lion.a02_team_6_shoppingmall.repository.BookRepository
import com.lion.a02_team_6_shoppingmall.util.BookType
import com.lion.a02_team_6_shoppingmall.util.FragmentName
import com.lion.a02_team_6_shoppingmall.viewmodel.BookViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BookCategorySubFragment(val bookCategoryType: BookCategoryType) : Fragment() {

    lateinit var fragmentBookCategorySubBinding: FragmentBookCategorySubBinding
    lateinit var mainActivity: MainActivity
    var bookList = mutableListOf<BookViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentBookCategorySubBinding = FragmentBookCategorySubBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        settingRecyclerView()
        gettingBookCategoryData()

        return fragmentBookCategorySubBinding.root
    }

    //  데이터를 가져오는 메서드
    fun gettingBookCategoryData(){
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                BookRepository.selectBookDataAll(mainActivity)
            }
            bookList = work1.await()

            // 카테고리에 따라 데이터를 필터링
            bookList = when (bookCategoryType) {
                BookCategoryType.BOOK_CATEGORY_TYPE_ALL -> bookList
                BookCategoryType.BOOK_CATEGORY_LITERATURE -> bookList.filter { it.bookType == BookType.BOOK_LITERATURE }.toMutableList()
                BookCategoryType.BOOK_CATEGORY_TYPE_NATURE -> bookList.filter { it.bookType == BookType.BOOK_NATURE }.toMutableList()
                BookCategoryType.BOOK_CATEGORY_TYPE_HUMANITIES -> bookList.filter { it.bookType == BookType.BOOK_HUMANITIES }.toMutableList()
                BookCategoryType.BOOK_CATEGORY_TYPE_ETC -> bookList.filter { it.bookType == BookType.BOOK_ETC }.toMutableList()
            }
            // RecyclerView 갱신
            fragmentBookCategorySubBinding.recyclerViewBookCategory.adapter?.notifyDataSetChanged()
        }
    }

    // RecyclerView의 어뎁터
    inner class RecyclerViewAdapterMain(): RecyclerView.Adapter<RecyclerViewAdapterMain.ViewHolderMain>(){
        inner class ViewHolderMain(var rowMainBinding: RowMainBinding) : RecyclerView.ViewHolder(rowMainBinding.root),
            OnClickListener {

            override fun onClick(v: View?) {
                val databundle = Bundle()
                databundle.putInt("bookIdx", bookList[adapterPosition].bookIdx)
                mainActivity.replaceFragment(FragmentName.SHOW_FRAGMENT, true, true, databundle)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMain {
            val rowMainBinding = RowMainBinding.inflate(layoutInflater, parent, false)
            val viewHolderMain = ViewHolderMain(rowMainBinding)

            rowMainBinding.root.setOnClickListener(viewHolderMain)

            return viewHolderMain
        }

        override fun getItemCount(): Int {
            return bookList.size
        }

        override fun onBindViewHolder(holder: ViewHolderMain, position: Int) {
            if (bookList.isEmpty()) {
                // 데이터가 없는 경우 기본 메시지를 설정
                holder.rowMainBinding.textViewRowType.text = ""
                holder.rowMainBinding.textViewRowTitle.text = "등록된 책이 없습니다."
                holder.rowMainBinding.textViewRowCount.text = ""
                holder.rowMainBinding.textViewRowNeed.visibility = View.GONE
                // 이미지 아이콘 초기화
                holder.rowMainBinding.textViewRowTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                val currentBook = bookList[position]

                // 데이터가 있는 경우 정보를 설정
                holder.rowMainBinding.textViewRowType.text = currentBook.bookType.str
                holder.rowMainBinding.textViewRowTitle.text = currentBook.bookTitle
                holder.rowMainBinding.textViewRowCount.text = "${currentBook.bookCount}권"

                // 이미지가 있는 경우 CompoundDrawable 설정
                if (currentBook.bookImage.isNotEmpty()) {
                    holder.rowMainBinding.textViewRowTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.imagesmode_24px, 0)
                } else {
                    // 이미지가 없는 경우 CompoundDrawable 초기화
                    holder.rowMainBinding.textViewRowTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }

                // 책 수량에 따라 가시성 설정
                if (currentBook.bookCount <= 10) {
                    holder.rowMainBinding.textViewRowNeed.visibility = View.VISIBLE
                } else {
                    holder.rowMainBinding.textViewRowNeed.visibility = View.GONE
                }
            }
        }
    }

    fun settingRecyclerView(){
        fragmentBookCategorySubBinding.apply {
            recyclerViewBookCategory.adapter = RecyclerViewAdapterMain()
            recyclerViewBookCategory.layoutManager = LinearLayoutManager(mainActivity)
            val deco = DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL)
            recyclerViewBookCategory.addItemDecoration(deco)
        }
    }
}

// 프래그먼트를 통해 보고자 하는 정보 이름
enum class BookCategoryType(val number:Int, val str:String){
    BOOK_CATEGORY_TYPE_ALL(1, "전체"),
    BOOK_CATEGORY_LITERATURE(2, "문학"),
    BOOK_CATEGORY_TYPE_NATURE(3, "자연"),
    BOOK_CATEGORY_TYPE_HUMANITIES(4, "인문"),
    BOOK_CATEGORY_TYPE_ETC(5, "기타")
}