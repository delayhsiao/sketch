/*******************************************************************************
 * Copyright 2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package me.xiaoapn.easy.imageloader.task;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import me.xiaoapn.easy.imageloader.util.ViewScaleType;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Wrapper for Android {@link android.widget.ImageView ImageView}. Keeps weak reference of ImageView to prevent memory
 * leaks.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.9.0
 */
public class ImageViewAware{
	protected Reference<ImageView> imageViewRef;
	protected boolean checkActualViewSize;
	private BitmapLoadTask bitmapLoadTask;

	/**
	 * Constructor.
	 * References {@link #ImageViewAware(android.widget.ImageView, boolean) ImageViewAware(imageView, true)}.
	 *
	 * @param imageView {@link android.widget.ImageView ImageView} to work with
	 */
	public ImageViewAware(ImageView imageView, BitmapLoadTask bitmapLoadTask) {
		this(imageView, bitmapLoadTask, true);
	}

	/**
	 * Constructor
	 *
	 * @param imageView           {@link android.widget.ImageView ImageView} to work with
	 * @param checkActualViewSize <b>true</b> - then {@link #getWidth()} and {@link #getHeight()} will check actual
	 *                            size of ImageView. It can cause known issues like
	 *                            <a href="https://github.com/nostra13/Android-Universal-Image-Loader/issues/376">this</a>.
	 *                            But it helps to save memory because memory cache keeps bitmaps of actual (less in
	 *                            general) size.
	 *                            <p/>
	 *                            <b>false</b> - then {@link #getWidth()} and {@link #getHeight()} will <b>NOT</b>
	 *                            consider actual size of ImageView, just layout parameters.
	 */
	public ImageViewAware(ImageView imageView, BitmapLoadTask bitmapLoadTask, boolean checkActualViewSize) {
		this.imageViewRef = new WeakReference<ImageView>(imageView);
		this.checkActualViewSize = checkActualViewSize;
		this.bitmapLoadTask = bitmapLoadTask;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Width is defined by target {@link ImageView view} parameters, configuration
	 * parameters or device display dimensions.<br />
	 * Size computing algorithm:<br />
	 * 1) Get the actual drawn <b>getWidth()</b> of the View. If view haven't drawn yet then go
	 * to step #2.<br />
	 * 2) Get <b>layout_width</b>. If it hasn't exact value then go to step #3.<br />
	 * 3) Get <b>maxWidth</b>.
	 */
	public int getWidth() {
		ImageView imageView = imageViewRef.get();
		if (imageView != null) {
			final ViewGroup.LayoutParams params = imageView.getLayoutParams();
			int width = 0;
			if (checkActualViewSize && params != null && params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
				width = imageView.getWidth(); // Get actual image width
			}
			if (width <= 0 && params != null) width = params.width; // Get layout width parameter
			if (width <= 0) width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check maxWidth parameter
			return width;
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Height is defined by target {@link ImageView view} parameters, configuration
	 * parameters or device display dimensions.<br />
	 * Size computing algorithm:<br />
	 * 1) Get the actual drawn <b>getHeight()</b> of the View. If view haven't drawn yet then go
	 * to step #2.<br />
	 * 2) Get <b>layout_height</b>. If it hasn't exact value then go to step #3.<br />
	 * 3) Get <b>maxHeight</b>.
	 */
	public int getHeight() {
		ImageView imageView = imageViewRef.get();
		if (imageView != null) {
			final ViewGroup.LayoutParams params = imageView.getLayoutParams();
			int height = 0;
			if (checkActualViewSize && params != null && params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
				height = imageView.getHeight(); // Get actual image height
			}
			if (height <= 0 && params != null) height = params.height; // Get layout height parameter
			if (height <= 0) height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check maxHeight parameter
			return height;
		}
		return 0;
	}

	public ViewScaleType getScaleType() {
		ImageView imageView = imageViewRef.get();
		if (imageView != null) {
			return ViewScaleType.fromImageView(imageView);
		}
		return null;
	}

	public ImageView getImageView() {
		final ImageView imageView = imageViewRef.get();
		BitmapLoadTask newBitmapLoadTask = BitmapLoadTask.getBitmapLoadTask(imageView);
        if (newBitmapLoadTask != null && newBitmapLoadTask == bitmapLoadTask) {
            return imageView;
        }else{
        	return null;
        }
	}

	public boolean isCollected() {
		return getImageView() == null;
	}

	private static int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = (Integer) field.get(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
}
