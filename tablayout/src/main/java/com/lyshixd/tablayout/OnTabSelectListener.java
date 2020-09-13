package com.lyshixd.tablayout;

/**
 *@Author: liyang
 *@Time: 2020/9/12 8:16 AM
 *@Description: tab点击回调
 */
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
