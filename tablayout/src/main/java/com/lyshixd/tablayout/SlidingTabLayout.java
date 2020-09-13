package com.lyshixd.tablayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *@Author: liyang
 *@Time: 2020/9/12 7:52 AM
 *@Description: 带有滑动条的Tab控件
 *
 * 实现要求
 * 1.滑动条宽、高、圆角、颜色(渐变)、位置可自定，多种滑动样式 1.平移 2.先增再减
 * 2.文字大小、加粗可自定，可设置 dp,px类型
 * 3.文字跟随滑动进度逐渐变大和缩小，自定选中文字大小颜色和未选中文字大小颜色
 * 4.每一项tab可设置WRAP_CONTENT或自定义宽度、padding、margin
 * 5.每一项tab可自定未读数量，style、大小、位置
 * 6.每一项tab可自定后方跟随view,例如倒三角，设置大小、位置、显示时机
 */
public class SlidingTabLayout extends HorizontalScrollView implements ViewPager.OnPageChangeListener {

	private Context mContext;
	private ViewPager mViewPager;
	//tab的容器
	private LinearLayout mTabsContainer;
	//tab此类维护的title列表
	private ArrayList<String> mTitles;
	//当前tab数量
	private int mTabCount;
	//当前选中的tab
	private int mCurrentTab;
	private float mCurrentPositionOffset;

	/** 用于绘制显示器 */
	private Rect mIndicatorRect = new Rect();
	/** 用于实现滚动居中 */
	private Rect mTabRect = new Rect();

	//viewpager是否平滑的移动到目标位置。当中间跨度过大时，设置false
	private boolean mIsViewPagerSmoothScroll = false;

	//整个tab容器的高度
	private int mHeight;

	//tab的宽度给定
	private float mTabWidth;
	private float mTabPadding;
	//tab的宽度是否占满随容器宽度
	private boolean mIsTabWidthEqual = false;


	//标题字体大小模式，PX不跟随系统字体大小，DIP跟随系统字体大小，默认不跟随
	private int mTextSizeType = TypedValue.COMPLEX_UNIT_PX;
	private float mTextSelectSize;
	private float mTextUnSelectSize;
	private int mTextSelectColor;
	private int mTextUnSelectColor;
	private int mTextBold;
	public static final int TEXT_BOLD_SELCET = 0;
	public static final int TEXT_BOLD_BOTH = 1;
	public static final int TEXT_BOLD_NONE = 2;

	/** 指示器 */
	private float mIndicatorWidth;
	private float mIndicatorHeight;
	private float mIndicatorCornerRadius;
	private float mIndicatorMarginLeft;
	private float mIndicatorMarginTop;
	private float mIndicatorMarginRight;
	private float mIndicatorMarginBottom;
	private boolean mIsIndicatorWidthEqualTitle;
	private int mIndicatorColor;
	private int mIndicatorStartColor;
	private int mIndicatorCenterColor;
	private int mIndicatorEndColor;
	private GradientDrawable mIndicatorDrawable = new GradientDrawable();
	private int mIndicatorGravity;

	private OnTabSelectListener selectListener;


	public SlidingTabLayout(Context context) {
		super(context);
	}

	public SlidingTabLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public SlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setHorizontalScrollBarEnabled(false);
		setFillViewport(true);//设置滚动视图是否可以伸缩其内容以填充视口
		setWillNotDraw(false);//重写onDraw方法,需要调用这个方法来清除flag
		setClipChildren(false);
		setClipToPadding(false);

		this.mContext = context;
		mTabsContainer = new LinearLayout(mContext);
		addView(mTabsContainer);

		obtainAttributes(context, attrs);

		//获取布局设置的高度
		String height  = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height");
		//如果view高度是确定值
		if (!height.equals(ViewGroup.LayoutParams.MATCH_PARENT + "") && !height.equals(ViewGroup.LayoutParams.WRAP_CONTENT + "")) {
			int[] systemAttrs = {android.R.attr.height};
			TypedArray typedArray = mContext.obtainStyledAttributes(attrs, systemAttrs);
			mHeight = typedArray.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			typedArray.recycle();
		}

	}

	private void obtainAttributes(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);

		mTabWidth = ta.getDimension(R.styleable.SlidingTabLayout_sl_tab_width, dp2px(-1));
		mIsTabWidthEqual = ta.getBoolean(R.styleable.SlidingTabLayout_sl_tab_width, false);
		mTabPadding = ta.getDimension(R.styleable.SlidingTabLayout_sl_tab_padding, mIsTabWidthEqual || mTabWidth > 0 ? dp2px(0) : dp2px(10));


		mTextBold = ta.getInt(R.styleable.SlidingTabLayout_sl_text_bold, TEXT_BOLD_NONE);
		mTextSelectSize = ta.getInt(R.styleable.SlidingTabLayout_sl_text_select_size, 16);
		mTextUnSelectSize = ta.getInt(R.styleable.SlidingTabLayout_sl_text_unselect_size, 12);
		mTextSelectColor = ta.getColor(R.styleable.SlidingTabLayout_sl_text_select_color, Color.parseColor("#000000"));
		mTextUnSelectColor = ta.getColor(R.styleable.SlidingTabLayout_sl_text_unselect_color, Color.parseColor("#888888"));

		mIndicatorWidth = ta.getDimension(R.styleable.SlidingTabLayout_sl_indicator_width, -1);
		mIndicatorHeight = ta.getDimension(R.styleable.SlidingTabLayout_sl_indicator_height, 0);
		mIndicatorCornerRadius = ta.getDimension(R.styleable.SlidingTabLayout_sl_indicator_radius, 0);
		mIndicatorMarginLeft = ta.getDimension(R.styleable.SlidingTabLayout_sl_indicator_margin_left, 0);
		mIndicatorMarginTop = ta.getDimension(R.styleable.SlidingTabLayout_sl_indicator_margin_top, 0);
		mIndicatorMarginRight = ta.getDimension(R.styleable.SlidingTabLayout_sl_indicator_margin_right, 0);
		mIndicatorMarginBottom = ta.getDimension(R.styleable.SlidingTabLayout_sl_indicator_margin_bottom, 0);
		mIsIndicatorWidthEqualTitle = ta.getBoolean(R.styleable.SlidingTabLayout_sl_indicator_width_equal_tisle, mIndicatorWidth > 0 ? false : true);

		mIndicatorColor = ta.getColor(R.styleable.SlidingTabLayout_sl_indicator_color, Color.parseColor("#000000"));
		mIndicatorStartColor = ta.getColor(R.styleable.SlidingTabLayout_sl_indicator_start_color, 0);
		mIndicatorCenterColor = ta.getColor(R.styleable.SlidingTabLayout_sl_indicator_center_color, 0);
		mIndicatorEndColor = ta.getColor(R.styleable.SlidingTabLayout_sl_indicator_end_color, 0);

		mIndicatorGravity = ta.getInt(R.styleable.SlidingTabLayout_sl_indicator_gravity, 1) == 0 ? Gravity.TOP : Gravity.BOTTOM;

		mTextSizeType = ta.getInt(R.styleable.SlidingTabLayout_sl_text_size_type, TypedValue.COMPLEX_UNIT_DIP);

		ta.recycle();
	}

	/**
	 * 关联ViewPager
	 */
	public void setViewPager(@NonNull ViewPager vp) {
		this.mViewPager = vp;
		mViewPager.removeOnPageChangeListener(this);
		mViewPager.addOnPageChangeListener(this);
		notifyDataSetChanged();
	}

	/**
	 * 更新数据
	 */
	private void notifyDataSetChanged() {
		mTabsContainer.removeAllViews();
		if (mViewPager == null || mViewPager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager or ViewPager Adapter is null");
		}
		mTabCount = mTitles == null ? mViewPager.getAdapter().getCount() : mTitles.size();
		View tabView;
		for (int i = 0; i < mTabCount; i++) {
			tabView = View.inflate(mContext, R.layout.layout_tab, null);
			CharSequence pageTitle = mTitles == null ? mViewPager.getAdapter().getPageTitle(i) : mTitles.get(i);
			if (pageTitle != null)
				addTab(i, pageTitle.toString(), tabView);
		}

		updateTabStyles();
	}

	/**
	 * 添加tabview
	 * @param position 位置
	 * @param title 标题
	 * @param tabView 自定义view
	 */
	private void addTab(int position, String title, View tabView) {
		TextView tv_tab_text = tabView.findViewById(R.id.tv_tab_title);
		if (tv_tab_text == null || TextUtils.isEmpty(title)) {
			return;
		}
		tv_tab_text.setText(title);
		tabView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = mTabsContainer.indexOfChild(v);
				if (position >= 0) {
					// 当前viewpager未选中此项
					if (mViewPager.getCurrentItem() != position) {
						mViewPager.setCurrentItem(position, Math.abs(mCurrentTab - position) == 1);
						if (selectListener != null) {
							selectListener.onTabFirstChose(position);
						}
					} else {
						if (selectListener != null) {
							selectListener.onTabReselctChose(position);
						}
					}
				}
			}
		});

		//设置样式
		LinearLayout.LayoutParams params;
		if (mTabWidth > 0) {
			params = new LinearLayout.LayoutParams(mTabCount, LinearLayout.LayoutParams.MATCH_PARENT);
		} else {
			params = mIsTabWidthEqual ?
					new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f) :
					new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		}
		mTabsContainer.addView(tabView, position, params);
	}

	/**
	 * 设置tab样式
	 */
	private void updateTabStyles() {
		for (int i = 0; i < mTabCount; i++) {
			boolean isSelect = i == mCurrentTab;
			View tabView = mTabsContainer.getChildAt(i);
			TextView title = tabView.findViewById(R.id.tv_tab_title);
			if (title == null) {
				break;
			}
			title.setTextColor(isSelect ? mTextSelectColor : mTextUnSelectColor);
			title.setTextSize(mTextSizeType, isSelect ? mTextSelectSize : mTextUnSelectSize);
			title.setPadding((int)mTabPadding, 0, (int)mTabPadding, 0);
			if (mTextBold == TEXT_BOLD_BOTH) {
				title.getPaint().setFakeBoldText(true);
			}
			else if (mTextBold == TEXT_BOLD_SELCET) {
				title.getPaint().setFakeBoldText(isSelect);
			}
			else {
				title.getPaint().setFakeBoldText(false);
			}
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//		Log.d("mLog", "positionOffsetPixels********: " + positionOffsetPixels);
//		Log.d("mLog", "positionOffsetPixels: " + positionOffsetPixels + "-----" + i++);
		this.mCurrentTab = position;
		this.mCurrentPositionOffset = positionOffset;
		scrollToCurrentTab();
		getTargetPosition(position, positionOffsetPixels);

		invalidate();
	}

	private boolean mIsScrollSelected = false;
	@Override
	public void onPageSelected(int position) {
		updateTabSelection(position);
		mIsScrollSelected = true;
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	private boolean isNext;
	private void getTargetPosition(int position, int positionOffsetPixels) {
		if (positionOffsetPixels == 0) {
			mIsScrollSelected = false;
			return;
		}
		if (mIsScrollSelected) {
			// 滑动的像素最大值就是vp的宽度 - 1，再滑动就执行onPageSelected方法了
			if (isNext && positionOffsetPixels == mViewPager.getWidth() - 1) {
				mIsScrollSelected = false;
			}
			if (!isNext && positionOffsetPixels == 1) {
				mIsScrollSelected = false;
			}
			return;
		}
		if (mViewPager.getCurrentItem() == position) {
			Log.d("mLog", "下一个: " + position);
			isNext = true;
			changeTextSize(isNext, mViewPager.getCurrentItem() + 1);
		}
		if (mViewPager.getCurrentItem() - 1 == position) {
			Log.d("mLog", "上一个: " + position);
			isNext = false;
			changeTextSize(isNext, mViewPager.getCurrentItem() - 1);
		}

	}


	private int mLastOffsetPixels;
//	private void cal(int position, int positionOffsetPixels) {
//		if (positionOffsetPixels == 0) {
//			mLastOffsetPixels = 0;
//			return;
//		}
//
//		// 下一个显示
//		if (mViewPager.getCurrentItem() == position) {
//			if (positionOffsetPixels > mLastOffsetPixels) {
//				//TODO 开始处理
//				changeTextSize(position, positionOffsetPixels);
//			}
//			Log.d("mLog", "下一个显示: " + positionOffsetPixels + "-----");
//
//
//		} else if (mViewPager.getCurrentItem() - 1 == position) {
//			if (positionOffsetPixels > mLastOffsetPixels) {
//				//TODO 开始处理
//				changeTextSize(position, positionOffsetPixels);
//			}
//		}
//
////		if (positionOffsetPixels > mLastOffsetPixels) {
////
////			if (mViewPager.getCurrentItem() == position + 1) {
////				Log.d("mLog", "向右滑动时  滑动过程未结束，但已选中");
////				mLastOffsetPixels = 0;
////				return;
////			}
////			//TODO 开始处理
////			changeTextSize(position, positionOffsetPixels);
////		}
////		else if (positionOffsetPixels < mLastOffsetPixels) {
////			Log.d("mLog", "上一个显示: " + positionOffsetPixels + "-----");
////			// 向右滑动时  滑动过程未结束，但已选中
////			if (mViewPager.getCurrentItem() == position) {
////				Log.d("mLog", "向右滑动时  滑动过程未结束，但已选中");
////				mLastOffsetPixels = 0;
////				return;
////			}
////			//TODO 开始处理
////			changeTextSize(position, positionOffsetPixels);
////		}
////		else {
////			Log.d("mLog", "手指停留: " + positionOffsetPixels + "-----");
////			mLastOffsetPixels = 0;
////			return;
////		}
//
//		mLastOffsetPixels = positionOffsetPixels;
//	}

	private int i = 0;
	private void changeTextSize(boolean isNext, int nextPosition) {

		TextView currentText = mTabsContainer.getChildAt(mViewPager.getCurrentItem()).findViewById(R.id.tv_tab_title);
		TextView nextText = mTabsContainer.getChildAt(nextPosition).findViewById(R.id.tv_tab_title);

		if (mTextUnSelectSize != mTextSelectSize) {
			//文字大小渐变
			float disSize = Math.abs(mTextUnSelectSize - mTextSelectSize) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset));
			currentText.setTextSize(mTextSizeType, mTextSelectSize - disSize);
			nextText.setTextSize(mTextSizeType, mTextUnSelectSize + disSize);
		}

		//文字颜色渐变
		int[] currentColor = new int[]{Color.alpha(mTextSelectColor), Color.red(mTextSelectColor), Color.green(mTextSelectColor), Color.blue(mTextSelectColor)};
		int[] nextColor = new int[]{Color.alpha(mTextUnSelectColor), Color.red(mTextUnSelectColor), Color.green(mTextUnSelectColor), Color.blue(mTextUnSelectColor)};

		int[] disToNextColor = new int[]{
				(int) ((nextColor[0] - currentColor[0]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
				(int) ((nextColor[1] - currentColor[1]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
				(int) ((nextColor[2] - currentColor[2]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
				(int) ((nextColor[3] - currentColor[3]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
		};
		int[] disToCurrentColor = new int[]{
				(int) ((currentColor[0] - nextColor[0]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
				(int) ((currentColor[1] - nextColor[1]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
				(int) ((currentColor[2] - nextColor[2]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
				(int) ((currentColor[3] - nextColor[3]) * (isNext ? mCurrentPositionOffset : (1 - mCurrentPositionOffset))),
		};

		currentText.setTextColor(Color.argb(
				currentColor[0] - disToCurrentColor[0],
				currentColor[1] - disToCurrentColor[1],
				currentColor[2] - disToCurrentColor[2],
				currentColor[3] - disToCurrentColor[3]));
		nextText.setTextColor(Color.argb(
				nextColor[0] - disToNextColor[0],
				nextColor[1] - disToNextColor[1],
				nextColor[2] - disToNextColor[2],
				nextColor[3] - disToNextColor[3]));
	}

	private int mLastScrollX;

	/**
	 * 滚动到当前位置并且居中(如果可以)
	 */
	private void scrollToCurrentTab() {
		if (mTabCount <= 0) {
			return;
		}
		// 平移距离
		int offsetdistance = (int) (mCurrentPositionOffset * mTabsContainer.getChildAt(mCurrentTab).getWidth());
		//当前Tab的left+当前Tab的Width
		int newScrollX = mTabsContainer.getChildAt(mCurrentTab).getLeft() + offsetdistance;
		if (mCurrentTab > 0 || offsetdistance > 0) {
			//HorizontalScrollView移动到当前tab,并居中
			newScrollX -= getWidth() / 2 - getPaddingLeft();
			calcIndicatorRect();
			newScrollX += ((mTabRect.right - mTabRect.left) / 2);
		}

		if (newScrollX != mLastScrollX) {
			mLastScrollX = newScrollX;
			/** scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
			 *  x:表示离起始位置的x水平方向的偏移量
			 *  y:表示离起始位置的y垂直方向的偏移量
			 */
			scrollTo(newScrollX, 0);
		}

	}


	/**
	 * 更新目标tab样式
	 */
	private void updateTabSelection(int position) {
		for (int i = 0; i < mTabCount; ++i) {
			View tabView = mTabsContainer.getChildAt(i);
			boolean isSelect = i == position;
			TextView title = tabView.findViewById(R.id.tv_tab_title);
			if (title != null) {
				title.setTextColor(isSelect ? mTextSelectColor : mTextUnSelectColor);
				title.setTextSize(mTextSizeType, isSelect ? mTextSelectSize : mTextUnSelectSize);
				if (mTextBold == TEXT_BOLD_SELCET) {
					title.getPaint().setFakeBoldText(isSelect);
				}
			}
		}
	}

	private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private float margin;

	/**
	 * 配置指示器位置
	 */
	private void calcIndicatorRect() {
		View currentTabView = mTabsContainer.getChildAt(mCurrentTab);
		float left = currentTabView.getLeft();
		float right = currentTabView.getRight();

		if (mIsIndicatorWidthEqualTitle) {
			TextView title = currentTabView.findViewById(R.id.tv_tab_title);
			//TODO 不一定是这个大小
			mTextPaint.setTextSize(mTextUnSelectSize);
			float textWidth = mTextPaint.measureText(title.getText().toString());
			margin = (right - left - textWidth) / 2;
		}

		// 如果不到最后一个,配置下一个tab
		if (mCurrentTab < mTabCount - 1) {
			View nextTabView = mTabsContainer.getChildAt(mCurrentTab + 1);
			float nextTabLeft = nextTabView.getLeft();
			float nextTabRight = nextTabView.getRight();

			left = left + mCurrentPositionOffset * (nextTabLeft - left);
			right = right + mCurrentPositionOffset * (nextTabRight - right);

			if (mIsIndicatorWidthEqualTitle) {
				TextView nextTitle = nextTabView.findViewById(R.id.tv_tab_title);
				//TODO 不一定是这个大小
				mTextPaint.setTextSize(mTextUnSelectSize);
				float nextTextWidth = mTextPaint.measureText(nextTitle.getText().toString());
				margin = margin + mCurrentPositionOffset * (nextTextWidth - margin);
			}
		}

		mIndicatorRect.left = (int) left;
		mIndicatorRect.right = (int) right;
		if (mIsIndicatorWidthEqualTitle) {
			mIndicatorRect.left = (int) (left + margin - 1);
			mIndicatorRect.right = (int) (right - margin - 1);
		}

		mTabRect.left = (int) left;
		mTabRect.right = (int) right;

		if (mIndicatorWidth >= 0) {
			float indicatorLeft = currentTabView.getLeft() + (currentTabView.getWidth() - mIndicatorWidth) / 2;
			if (mCurrentTab < mTabCount - 1) {
				View nextTab = mTabsContainer.getChildAt(mCurrentTab + 1);
				indicatorLeft = indicatorLeft
						+ mCurrentPositionOffset
						* ((float) currentTabView.getWidth() / 2 + (float) nextTab.getWidth() / 2);
			}
			mIndicatorRect.left = (int) indicatorLeft;
			mIndicatorRect.right = (int) (mIndicatorRect.left + mIndicatorWidth);
		}
	}

	private boolean mIsJump = false;

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isInEditMode() || mTabCount <= 0) {
			return;
		}
		int height = getHeight();
		int paddingLeft = getPaddingLeft();

		// 绘制指示器
		calcIndicatorRect();
		if (mIndicatorWidth >= 0){
			// 设置渐变色
			if (mIndicatorStartColor != 0 && mIndicatorEndColor != 0) {
				mIndicatorDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
				mIndicatorDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
				if (mIndicatorCenterColor != 0) {
					mIndicatorDrawable.setColors(new int[]{mIndicatorStartColor, mIndicatorCenterColor, mIndicatorEndColor});
				} else {
					mIndicatorDrawable.setColors(new int[]{mIndicatorStartColor, mIndicatorEndColor});
				}
			} else {
				mIndicatorDrawable.setColor(mIndicatorColor);
			}

			if (mIndicatorGravity == Gravity.BOTTOM) {
				mIndicatorDrawable.setBounds(
						paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
						height - (int) mIndicatorHeight - mIndicatorRect.bottom,
						paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
						height - (int) mIndicatorMarginBottom);

			} else {
				mIndicatorDrawable.setBounds(
						paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
						(int) mIndicatorMarginTop,
						paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
						height - (int) mIndicatorMarginBottom);
			}
			mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
			mIndicatorDrawable.draw(canvas);
		}
	}

	protected int dp2px(float dp) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	protected int sp2px(float sp) {
		final float scale = this.mContext.getResources().getDisplayMetrics().scaledDensity;
		return (int) (sp * scale + 0.5f);
	}
}
