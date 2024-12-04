package com.lion.a02_team_6_shoppingmall.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lion.a02_team_6_shoppingmall.MainActivity
import com.lion.a02_team_6_shoppingmall.R
import com.lion.a02_team_6_shoppingmall.databinding.FragmentMainBinding
import com.lion.a02_team_6_shoppingmall.util.FragmentName


class MainFragment : Fragment() {
    lateinit var fragmentMainBinding: FragmentMainBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentMainBinding = FragmentMainBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity
        settingToolbar()
        settingTab()
        settingFabMain()
        return fragmentMainBinding.root
    }

    fun settingFabMain(){
        fragmentMainBinding.fabMain.apply {
            setOnClickListener {
                mainActivity.replaceFragment(FragmentName.INPUT_FRAGMENT, true, true, null)
            }
        }
    }

    // 탭을 구성하는 메서드
    fun settingTab(){
        fragmentMainBinding.apply {
            // ViewPager2의 어뎁터를 설정한다.
            viewPagerBookCategory.adapter = ViewPagerAdapter(childFragmentManager, lifecycle)

            // TabLayout과 ViewPager2가 상호 작용을 할 수 있도록 연동시켜준다.
            val tabLayoutMediator = TabLayoutMediator(tabLayoutBookCategory, viewPagerBookCategory) { tab, position ->
                // 각 탭에 보여줄 문자열을 새롭게 구성해줘야 한다.
                when(position){
                    0 -> tab.text = "전체"
                    1 -> tab.text = "문학"
                    2 -> tab.text = "자연"
                    3 -> tab.text = "인문"
                    4 -> tab.text = "기타"
                }
            }
            tabLayoutMediator.attach()
        }
    }

    // ViewPager2의 어뎁터
    inner class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle){
        // ViewPager2를 통해 보여줄 프래그먼트의 개수
        override fun getItemCount(): Int {
            return 5
        }

        // position번째에서 사용할 Fragment 객체를 생성해 반환하는 메서드
        override fun createFragment(position: Int): Fragment {
            val newFragment = when(position){
                0 -> BookCategorySubFragment(BookCategoryType.BOOK_CATEGORY_TYPE_ALL)
                1 -> BookCategorySubFragment(BookCategoryType.BOOK_CATEGORY_LITERATURE)
                2 -> BookCategorySubFragment(BookCategoryType.BOOK_CATEGORY_TYPE_NATURE)
                3 -> BookCategorySubFragment(BookCategoryType.BOOK_CATEGORY_TYPE_HUMANITIES)
                else -> BookCategorySubFragment(BookCategoryType.BOOK_CATEGORY_TYPE_ETC)
            }
            return newFragment
        }
    }

    fun settingToolbar(){
        fragmentMainBinding.materialToolbar.apply {
            title = "책 정보 목록"
            isTitleCentered = true

            setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menuLocation -> {
                        mainActivity.replaceFragment(FragmentName.LOCATION_FRAGMENT, true, true, null)
                    }

                    R.id.menuSearch -> {
                        mainActivity.replaceFragment(FragmentName.SEARCH_FRAGMENT, true, true, null)
                    }
                }
                true
            }
        }
    }
}