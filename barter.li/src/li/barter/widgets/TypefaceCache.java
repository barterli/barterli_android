package li.barter.widgets;

import java.util.Hashtable;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * @author Vinay S Shenoy Typeface cache to cache the typefaces
 */
public class TypefaceCache {

	private static final Hashtable<String, Typeface> CACHE = new Hashtable<String, Typeface>();

	public static final String ALEGREYA_BLACK_ITALIC = "fonts/Alegreya-BlackItalic.otf";
	public static final String ALEGREYA_BOLD = "fonts/Alegreya-Bold.otf";
	public static final String ALEGREYA_ITALIC = "fonts/Alegreya-Italic.otf";
	public static final String ALEGREYA_REGULAR = "fonts/Alegreya-Regular.otf";

	public static Typeface get(AssetManager manager, int typefaceCode) {
		synchronized (CACHE) {

			String typefaceName = getTypefaceName(typefaceCode);

			if (!CACHE.containsKey(typefaceName)) {
				Typeface t = Typeface.createFromAsset(manager, typefaceName);
				CACHE.put(typefaceName, t);
			}
			return CACHE.get(typefaceName);
		}
	}

	public static Typeface get(AssetManager manager, String typefaceName) {
		return get(manager, getCodeForTypefaceName(typefaceName));
	}

	private static int getCodeForTypefaceName(String typefaceName) {

		if (typefaceName.equals(ALEGREYA_BLACK_ITALIC)) {
			return 0;
		} else if (typefaceName.equals(ALEGREYA_BOLD)) {
			return 1;
		} else if (typefaceName.equals(ALEGREYA_ITALIC)) {
			return 2;
		} else if (typefaceName.equals(ALEGREYA_REGULAR)) {
			return 3;
		} else {
			return 3;
		}
	}

	private static String getTypefaceName(int typefaceCode) {
		switch (typefaceCode) {
		case 0:
			return ALEGREYA_BLACK_ITALIC;

		case 1:
			return ALEGREYA_BOLD;

		case 2:
			return ALEGREYA_ITALIC;

		case 3:
			return ALEGREYA_REGULAR;

		default:
			return ALEGREYA_REGULAR;
		}
	}

}
