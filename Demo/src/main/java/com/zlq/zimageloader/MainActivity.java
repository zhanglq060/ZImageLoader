package com.zlq.zimageloader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.zlq.imageloader.library.ZImageLoader;

public class MainActivity extends Activity {

	int screenWidth;
	int screenHeight;

	GridView mGridView;

	static String[] imageurls = new String[]{
			"http://a.hiphotos.baidu.com/image/pic/item/e7cd7b899e510fb3a78c787fdd33c895d0430c44.jpg",
			"http://c.hiphotos.baidu.com/image/pic/item/6a600c338744ebf8a05ade3bdbf9d72a6059a78f.jpg",
			"http://a.hiphotos.baidu.com/image/pic/item/a2cc7cd98d1001e989b2e809ba0e7bec54e7972e.jpg",
			"http://e.hiphotos.baidu.com/image/pic/item/622762d0f703918ff453d0ce533d269759eec430.jpg",
			"http://e.hiphotos.baidu.com/image/pic/item/1ad5ad6eddc451da95aff9cab2fd5266d01632b1.jpg",
			"http://b.hiphotos.baidu.com/image/pic/item/91529822720e0cf366e3f1bd0f46f21fbe09aa64.jpg",
			"http://g.hiphotos.baidu.com/image/pic/item/060828381f30e9244e3f894a49086e061d95f736.jpg",
			"http://f.hiphotos.baidu.com/image/pic/item/4a36acaf2edda3cce9d46c2403e93901213f9286.jpg",
			"http://h.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697c6e7ac5d57fbb2fb4316d8fe.jpg",
			"http://a.hiphotos.baidu.com/image/pic/item/f11f3a292df5e0fea17be0d7586034a85fdf7280.jpg",
			"http://f.hiphotos.baidu.com/image/pic/item/cefc1e178a82b90147fd0234718da9773912ef61.jpg",
			"http://f.hiphotos.baidu.com/image/pic/item/023b5bb5c9ea15cef3565b4fb3003af33a87b238.jpg",
			"http://g.hiphotos.baidu.com/image/pic/item/342ac65c1038534335e53d679613b07eca80884a.jpg",
			"http://g.hiphotos.baidu.com/image/pic/item/0824ab18972bd407db4108497e899e510eb30984.jpg",
			"http://c.hiphotos.baidu.com/image/pic/item/b8014a90f603738ddb6bb664b61bb051f819ec57.jpg",
			"http://a.hiphotos.baidu.com/image/pic/item/7e3e6709c93d70cf0b55154bfddcd100bba12bcc.jpg",
			"http://c.hiphotos.baidu.com/image/pic/item/e824b899a9014c08e41c92e30f7b02087bf4f455.jpg",
			"http://pic22.nipic.com/20120703/8097124_063922121000_2.jpg",
			"http://s1.sinaimg.cn/bmiddle/004eJ1QZzy6IBQUpTcQ70&690",
			"http://f.hiphotos.baidu.com/image/pic/item/91529822720e0cf34a6bdafb0846f21fbe09aa16.jpg",
			"http://e.hiphotos.baidu.com/image/pic/item/32fa828ba61ea8d339faad04950a304e241f58d2.jpg",
			"http://e.hiphotos.baidu.com/image/pic/item/d53f8794a4c27d1e8ea27a4c19d5ad6edcc438b5.jpg",
			"http://pic1.win4000.com/pic/5/32/6d8d1128064.jpg",
			"http://pic1.win4000.com/pic/5/32/6d8d1128073.jpg",
			"http://e.hiphotos.baidu.com/image/pic/item/14ce36d3d539b6002e357fbaeb50352ac75cb7c7.jpg",
			"http://a.hiphotos.baidu.com/image/pic/item/0d338744ebf81a4ce917568fd22a6059252da64b.jpg",
			"http://f.hiphotos.baidu.com/image/pic/item/fcfaaf51f3deb48f39764d06f41f3a292cf578cf.jpg",
			"http://c.hiphotos.baidu.com/image/pic/item/5bafa40f4bfbfbed81fec7cb7df0f736aec31fda.jpg",
			"http://d.hiphotos.baidu.com/image/h%3D200/sign=207b48fab54543a9ea1bfdcc2e168a7b/54fbb2fb43166d22dc28839a442309f79052d265.jpg",
			"http://h.hiphotos.baidu.com/image/pic/item/d0c8a786c9177f3ef0f2e97675cf3bc79e3d5682.jpg",
			"http://a.hiphotos.baidu.com/image/pic/item/f2deb48f8c5494ee48afe2c129f5e0fe98257e5d.jpg",
			"http://e.hiphotos.baidu.com/image/pic/item/f603918fa0ec08fa058cfe7f5dee3d6d54fbda5e.jpg",
			"http://a.hiphotos.baidu.com/image/pic/item/b7fd5266d0160924933e331bd60735fae6cd3492.jpg",
			"http://b.hiphotos.baidu.com/image/pic/item/3b87e950352ac65c59904622f9f2b21192138acd.jpg",
			"http://d.hiphotos.baidu.com/image/pic/item/c8177f3e6709c93d5b13b5b19a3df8dcd0005496.jpg",
			"http://f.hiphotos.baidu.com/image/pic/item/0824ab18972bd40778fc694b7e899e510fb30948.jpg"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		setContentView(R.layout.main);

		mGridView = (GridView) findViewById(R.id.gridview);
		Adapter adapter = new Adapter();
		mGridView.setAdapter(adapter);

		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//do some thing...
			}
		});

	}

	class Adapter extends BaseAdapter {

		@Override
		public int getCount() {
			return imageurls.length;
		}

		@Override
		public Object getItem(int position) {
			return imageurls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = null;
			if (convertView == null){
				imageView = new ImageView(MainActivity.this);
			}else{
				imageView = (ImageView) convertView;
			}
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			ZImageLoader.with(MainActivity.this)
					.setImageUrl(imageurls[position])
					.setTargetImageView(imageView)
					.load();
			imageView.setLayoutParams(new GridView.LayoutParams(screenWidth/3, screenWidth/3));
			return imageView;
		}
	}
}
