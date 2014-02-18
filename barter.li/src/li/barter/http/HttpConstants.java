package li.barter.http;

/**
 * @author Vinay S Shenoy Interface that holds all constants related to Http
 *         Requests
 */
public class HttpConstants {

	/**
	 * Enum to switch between servers
	 */
	private enum Server {

		LOCAL("http://162.243.198.171/api/v", API_VERSION), DEV(
				"http://162.243.198.171/api/v", API_VERSION), PRODUCTION(
				"http://162.243.198.171/api/v", API_VERSION);

		public final String mUrl;

		Server(String url, int version) {
			mUrl = url + version;
		}
	}

	private static final int API_VERSION = 1;

	private static Server SERVER = Server.LOCAL;

	public static String getApiBaseUrl() {
		return SERVER.mUrl;
	}

	/**
	 * Empty interface to remember all API endpoints
	 */
	public static interface ApiEndpoints {
		public static final String BOOK_SUGGESTIONS = "/book_suggestions.json";
		public static final String BOOK_INFO = "/book_info.json";
		public static final String BOOKS = "/books.json";
		public static final String CREATE_USER = "/create_user.json";
		public static final String HANGOUTS = "/hangouts.json";
		public static final String USER_PREFERRED_LOCATION = "/user_preferred_location.json";
	}

	public static final String Q = "q";
	public static final String T = "t";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String AUTHORS = "authors";
	public static final String AUTHOR = "author";
	public static final String NAME = "name";
	public static final String PUBLICATION_YEAR = "publication_year";

}
