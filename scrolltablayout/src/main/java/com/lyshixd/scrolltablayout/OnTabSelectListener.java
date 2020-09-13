package com.lyshixd.scrolltablayout;


public interface OnTabSelectListener {

	/**
	 * 第一次点击选中
	 */
	void onTabFirstChose(int position);

	/**
	 * 选中时再次点击
	 */
	void onTabReselctChose(int position);
}
