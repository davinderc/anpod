package com.blork.anpod.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {
	
	/** The Constant TAG. */
	public static final String TAG = "Astronomy Picture of the Day";

	/**
	 * Gets the jSON.
	 *
	 * @param url the url
	 * @return the jSON
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static String getJSON(URL url) throws IOException, URISyntaxException {
		//InputStream instream = getStream(url);
		URLConnection connection = url.openConnection();
		InputStream instream = connection.getInputStream();
		return streamToString(instream);
	}

	/**
	 * Stream to string.
	 *
	 * @param is the is
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static String streamToString(InputStream is) throws IOException {
		try {
			BufferedReader buf = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			StringBuilder sb = new StringBuilder();
			String s;
			while(true) {
				s = buf.readLine();
				if(s==null || s.length()==0)
					break;
				sb.append(s);
			}
			buf.close();
			is.close();
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Gets the stream.
	 *
	 * @param url the url
	 * @return the stream
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws OutOfMemoryError the out of memory error
	 */
	public static InputStream getStream(URL url) throws ClientProtocolException, IOException, OutOfMemoryError{
		HttpGet httpRequest = null;

		try {
			httpRequest = new HttpGet(url.toURI());
			httpRequest.removeHeaders("User-Agent");

			httpRequest.setHeader(	"Accept-Encoding", 	"gzip"		);
			httpRequest.setHeader( 	"Pragma", 			"no-cache" 	);
			httpRequest.setHeader(	"Cache-Control", 	"no-cache" 	);
			httpRequest.setHeader( 	"Expires", 			"0" 		);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

		HttpEntity entity = response.getEntity();
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity); 
		InputStream instream = bufHttpEntity.getContent();

		Header contentEncoding = response.getFirstHeader("Content-Encoding");

		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			instream = new GZIPInputStream(instream);
		}

		return instream;
	}

	/**
	 * Gets the bitmap.
	 *
	 * @param imageUrl the image url
	 * @param wallpaperWidth the wallpaper width
	 * @param wallpaperHeight the wallpaper height
	 * @return the bitmap
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Bitmap getBitmap(URL imageUrl, int wallpaperWidth, int wallpaperHeight) throws IOException {

		InputStream instream = Utils.getStream(imageUrl);
		return streamToBitmap(instream, wallpaperWidth, wallpaperHeight);
	}

	/**
	 * Stream to bitmap.
	 *
	 * @param instream the instream
	 * @param width the width
	 * @param height the height
	 * @return the bitmap
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Bitmap streamToBitmap(InputStream instream, int width, int height) throws IOException {
		instream.mark(Integer.MAX_VALUE);

		//Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(instream, null, o);

		//Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while(true){
			if(width_tmp / 2 < width || height_tmp / 2 < height)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
		//Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;

		instream.reset();

		Bitmap image = BitmapFactory.decodeStream(instream, null, o2);
		return image;
	}

	/**
	 * Checks if is network connected.
	 *
	 * @param context the context
	 * @return true, if successful
	 */
	public static boolean IsNetworkConnected(Context context) {
		boolean result = false;
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			result = false;
		} else {
			if (!info.isAvailable()) {
				result = false;
			} else {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if is wi fi connected.
	 *
	 * @param context the context
	 * @return true, if successful
	 */
	public static boolean IsWiFiConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();

		int netType = info.getType();
		if (netType == ConnectivityManager.TYPE_WIFI) {
			return info.isConnected();
		} else {
			return false;
		}
	}



	/**
	 * Prepend to list.
	 *
	 * @param <E> the element type
	 * @param list the list
	 * @param additions the additions
	 * @return the list
	 */
	public static <E> List<E> prependToList(List<E> list, List<E> additions) {
		Collections.reverse(additions);
		for (E element: additions) {
			if (!list.contains(element)) {
				list.add(0, element);
			}
		}
		return list;
	}
}