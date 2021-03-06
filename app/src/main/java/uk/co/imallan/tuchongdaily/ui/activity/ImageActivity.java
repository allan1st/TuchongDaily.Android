package uk.co.imallan.tuchongdaily.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.imallan.tuchongdaily.R;
import uk.co.imallan.tuchongdaily.utils.ImageUtils;

/**
 * Created by allan on 15/2/27.
 */
public class ImageActivity extends AbstractActivity implements View.OnClickListener {

	private static final String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";

	private static final String EXTRA_IMAGE_CAMERA_INFO = "EXTRA_IMAGE_CAMERA_INFO";

	private static final String EXTRA_IMAGE_LENS_INFO = "EXTRA_IMAGE_LENS_INFO";

	private ImageView mImageView;

	private TextView mCameraInfo;

	private TextView mLensInfo;

	private View mRootView;

	public static void startActivity(Context context, String imageURL, String cameraInfo, String lensInfo, View sharedView) {
		Intent intent = new Intent(context, ImageActivity.class);
		intent.putExtra(EXTRA_IMAGE_URL, imageURL);
		intent.putExtra(EXTRA_IMAGE_CAMERA_INFO, cameraInfo);
		intent.putExtra(EXTRA_IMAGE_LENS_INFO, lensInfo);
		ActivityOptionsCompat options;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, sharedView, context.getString(R.string.share_hero));
		} else {
			options = ActivityOptionsCompat.makeScaleUpAnimation(sharedView, 0, 0, sharedView.getWidth(), sharedView.getHeight());
		}
		if (!TextUtils.isEmpty(imageURL)) {
			intent.putExtra(EXTRA_IMAGE_URL, imageURL);
		} else {
			throw new RuntimeException("You must provide an image url");
		}
		ActivityCompat.startActivity((Activity) context, intent, options.toBundle());
	}

	@Override
	boolean prepareServiceReceiver() {
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		initTransitions();
		initElements();
		String imageURL = getIntent().getStringExtra(EXTRA_IMAGE_URL);
		String cameraInfoText = getIntent().getStringExtra(EXTRA_IMAGE_CAMERA_INFO);
		String lensInfoText = getIntent().getStringExtra(EXTRA_IMAGE_LENS_INFO);
		if (!TextUtils.isEmpty(cameraInfoText)) {
			mCameraInfo.setText(cameraInfoText);
		}
		if (!TextUtils.isEmpty(lensInfoText)) {
			mLensInfo.setText(lensInfoText);
		}
		Picasso.with(this).load(imageURL).noPlaceholder().noFade()
				.transform(new ImageUtils.LimitImageSizeTransformation(ImageUtils.LimitImageSizeTransformation.QUALITY.QUALITY_1080P))
				.into(mImageView, new Callback() {
					@Override
					public void onSuccess() {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							startPostponedEnterTransition();
						}
					}

					@Override
					public void onError() {

					}
				});
	}

	private void initElements() {
		mRootView = findViewById(R.id.root_view_activity_image);
		mImageView = (ImageView) findViewById(R.id.image_view_image);
		mCameraInfo = (TextView) findViewById(R.id.text_image_camera_info);
		mLensInfo = (TextView) findViewById(R.id.text_image_lens_info);

		mImageView.setOnClickListener(this);
		mRootView.setOnClickListener(this);
	}

	private void initTransitions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Slide slideInBottom = new Slide();
			slideInBottom.addTarget(R.id.text_image_camera_info);
			slideInBottom.addTarget(R.id.text_image_lens_info);
			slideInBottom.setStartDelay(300);
			getWindow().setEnterTransition(slideInBottom);
			Slide slide = new Slide();
			slide.addTarget(R.id.text_image_camera_info);
			slide.addTarget(R.id.text_image_lens_info);
			getWindow().setReturnTransition(slide);
			postponeEnterTransition();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.root_view_activity_image:
			case R.id.image_view_image:
				onBackPressed();
				break;
		}
	}
}
