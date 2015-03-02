package uk.co.imallan.tuchongdaily.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import uk.co.imallan.tuchongdaily.R;
import uk.co.imallan.tuchongdaily.db.Table;
import uk.co.imallan.tuchongdaily.provider.ImageProvider;
import uk.co.imallan.tuchongdaily.provider.PostProvider;
import uk.co.imallan.tuchongdaily.ui.activity.ImageActivity;
import uk.co.imallan.tuchongdaily.ui.adapter.PostImagesRecyclerViewAdapter;
import uk.co.imallan.tuchongdaily.utils.ImageUtils;

/**
 * Created by allan on 15/2/25.
 */
public class PostFragment extends AbstractFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String POST_SERVER_ID = "POST_SERVER_ID";

	private static final int LOADER_POST = 1;

	private static final int LOADER_POST_IMAGES = 2;

	private String mServerId;

	private TextView mTitle;

	private ImageView mImage;

	public RecyclerView mRecyclerView;

	public PostImagesRecyclerViewAdapter mAdapter;

	private View mImageContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		mServerId = arguments.getString(POST_SERVER_ID);
	}

	@Override
	protected boolean prepareServiceReceiver() {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_post, container, false);
		mTitle = (TextView) rootView.findViewById(R.id.text_post_title);
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_post_images);
		mImage = (ImageView) rootView.findViewById(R.id.image_post);
		mImageContainer = rootView.findViewById(R.id.view_poster_container);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		mAdapter = new PostImagesRecyclerViewAdapter(getActivity());
		mRecyclerView.setAdapter(mAdapter);
		getLoaderManager().initLoader(LOADER_POST_IMAGES, null, this);
		getLoaderManager().initLoader(LOADER_POST, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_POST_IMAGES:
				return new CursorLoader(getActivity(), ImageProvider.uriPostImages(mServerId), null, null, null, null);
			case LOADER_POST:
				return new CursorLoader(getActivity(), PostProvider.uriPost(mServerId), null, null, null, null);
			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case LOADER_POST_IMAGES:
				boolean isImageSet = false;
				for (int i = 0; i < data.getCount(); i++) {
					if (data.moveToPosition(i)) {
						final int width = data.getInt(data.getColumnIndex(Table.Image.COLUMN_WIDTH));
						final int height = data.getInt(data.getColumnIndex(Table.Image.COLUMN_HEIGHT));
						if (width < height) {
							//portrait mode
							setPosterImage(data, i);
							isImageSet = true;
							break;
						}
					}
				}
				if (!isImageSet) {
					setPosterImage(data, 0);
				}
				mAdapter.swapCursor(data);
				if (data.getCount() <= 1) {
					mRecyclerView.setVisibility(View.INVISIBLE);
				} else {
					mRecyclerView.setVisibility(View.VISIBLE);
				}
				break;
			case LOADER_POST:
				if (data.moveToPosition(0)) {
					String title = data.getString(data.getColumnIndex(Table.Post.COLUMN_TITLE));
					mTitle.setText(title);
				}
				break;
		}
	}

	private void setPosterImage(Cursor data, int position) {
		if (data != null && data.moveToPosition(position)) {
			final String url = data.getString(data.getColumnIndex(Table.Image.COLUMN_URL_FULL));
			Picasso.with(getActivity()).load(url)
					.transform(new ImageUtils.LimitImageSizeTransformation(
									ImageUtils.LimitImageSizeTransformation.QUALITY.QUALITY_1080P)
					).fit().centerCrop()
					.into(mImage);
			final String cameraInfo = data.getString(data.getColumnIndex(Table.Image.COLUMN_CAMERA));
			final String lensInfo = data.getString(data.getColumnIndex(Table.Image.COLUMN_LENS));
			mImageContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ImageActivity.startActivity(getActivity(), url, cameraInfo, lensInfo, mImage);
				}
			});
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	public void onPageScrollFrom(float positionOffset, int positionOffsetPixels) {
		mImage.setTranslationX(positionOffsetPixels / 2);
	}

	public void onPageScrollto(float positionOffset, int positionOffsetPixels) {
		mImage.setTranslationX((positionOffsetPixels - mImage.getWidth()) / 2);
	}

	public void onPageSelected() {
		mImage.setTranslationX(0);
	}
}
