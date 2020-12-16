package com.lyshixd.scrolltablayout;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.lyshixd.tablayout.SlidingTabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

	private SlidingTabLayout tabLayout;
	private ViewPager viewPager;
	private MyPagerAdapter mAdapter;

	private ArrayList<Fragment> mFragments = new ArrayList<>();

	private final String[] mTitles = {
			"热门", "iOS", "Android"
			, "前端", "后端", "设计", "工具资源", "后端", "设计", "工具资源"
	};

//	private final String[] mTitles = {
//		"热门", "工具资源"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tabLayout = findViewById(R.id.tablayout);
		viewPager = findViewById(R.id.viewPager);

		init();
	}


	private void init() {

		for (String title : mTitles) {
			mFragments.add(SimpleCardFragment.getInstance(title));
		}

		mAdapter = new MyPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mAdapter);

		tabLayout.setViewPager(viewPager);

	}


	private class MyPagerAdapter extends FragmentPagerAdapter {
		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTitles[position];
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}
	}
}