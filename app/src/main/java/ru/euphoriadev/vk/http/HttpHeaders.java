package ru.euphoriadev.vk.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by Igor on 25.02.16.
 * <p/>
 * HTTP Header - This line in the HTTP message that contains a colon-separated
 * pair name-value. The header format conforms to the common header format network
 * text messages.
 * <p/>
 * Powered by Wikipedia.
 */
public class HttpHeaders {

    /**
     * The list of valid formats resource, e.g. text/plain
     */
    public static final String ACCEPT = "Accept";

    /**
     * The list of supported encodings for presentation to the user.
     * e.g. UTF-8
     */
    public static final String ACCEPT_CHARSET = "Accept-Charset";

    /**
     * The list of supported encoding content entity in the transmission.
     * e.g. <compress | gzip | deflate | sdch | identity>
     */
    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * The list of supported languages.
     * e.g. ru
     */
    public static final String ACCEPT_LANGUAGE = "Accept-Charset";

    /**
     * A list of units of measurement ranges.
     * e.g. bytes
     */
    public static final String ACCEPT_RANGES = "Accept-Ranges";

    /**
     * The number of seconds since the modification of the resource.
     */
    public static final String AGE = "Age";

    /**
     * The list of supported methods.
     * e.g. OPTIONS, GET, HEAD, POST
     */
    public static final String ALLOW = "Age";

    /**
     * Indication of alternative ways of resource representation
     */
    public static final String ALTERNATES = "Alternates";

    /**
     * Auth data
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * The main directives to control caching
     * <p/>
     * Cache-Control: no-cache
     * Cache-Control: no-store
     * Cache-Control: max-age=3600
     * Cache-Control: max-stale=0
     * Cache-Control: min-fresh=0
     * Cache-Control: no-transform
     * Cache-Control: only-if-cached
     * Cache-Control: cache-extension
     */
    public static final String CACHE_CONTROL = "Cache-Control";

    /**
     * Information on conducting connection.
     * e.g. close
     */
    public static final String CONNECTION = "Cache-Connection";

    /**
     * Information on the permanent location of the resource.
     * Removed in HTTP/1.1v2.
     */
    @Deprecated
    public static final String CONTENT_BASE = "Content-Base";

    /**
     * Method of distribution entities in the message when
     * transferring several fragments.
     * e.g. form-data; name="AttachedFile1"; filename="photo-1.jpg"
     */
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * The method of coding the content entities to the transmission
     */
    public static final String CONTENT_ENCODING = "Content-Encoding";

    /**
     * One or more natural languages content entity
     * e.g. en, ase, ru
     */
    public static final String CONTENT_LANGUAGE = "Content-Language";

    /**
     * The size of the content entity in bytes
     * e.g. 1348
     */
    public static final String CONTENT_LENGTH = "Content-Length";

    /**
     * The alternate content location of the entity
     */
    public static final String CONTENT_LOCATION = "Content-Location";

    /**
     * Base64 MD5 hash of the entity to check the integrity
     * e.g. Q2hlY2sgSW50ZWdyaXR5IQ==
     */
    public static final String CONTENT_MD5 = "Content-MD5";

    /**
     * Byte ranges of the transferred entity if it returns a fragment
     * e.g. bytes 88080384-160993791/160993792
     */
    public static final String CONTENT_RANGE = "Content-Range";

    /**
     * The format and method of presenting entities
     * e.g. text/html;charset=utf-8
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * Information about the current version of the entity
     */
    public static final String CONTENT_VERSION = "Content-Version";

    /**
     * Date of generation of the response
     */
    public static final String DATE = "Date";

    /**
     * Tag (unique identifier) version of the entity, used when caching
     * e.g. "56d-9989200-1132c580"
     */
    public static final String ETAG = "ETag";

    /**
     * Tells the server that the client expects it for more action
     * e.g. 100-continue
     */
    public static final String EXPECT = "Expect";

    /**
     * Date of expiry of the relevance of the entity
     * e.g 31 Jan 2012 15:02:53 GMT
     */
    public static final String EXPIRES = "Expires";

    /**
     * The e-mail address of the responsible person from the client
     * e.g. user@example.com
     */
    public static final String FROM = "From";

    /**
     * Domain name and host port of the requested resource.
     * Required to support virtual hosting on the servers.
     * e.g. ru.wikipedia.org
     */
    public static final String HOST = "Host";

    /**
     * The list of tag versions of the entity. To execute the method, if they exist
     * e.g. "737060cd8c284d8af7ad3082f209582d"
     */
    public static final String IF_MATCH = "If-Match";

    /**
     * Date. To execute the method if the entity has changed since the specified moment.
     * e.g. Sat, 29 Oct 1994 19:43:31 GMT
     */
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * The list of tag versions of the entity. To execute the method if none exists
     * e.g. "737060cd8c284d8af7ad3082f209582d"
     */
    public static final String IF_NONE_MATCH = "If-None-Match";

    /**
     * The list of tag versions of the entity or date for a particular piece entity
     */
    public static final String IF_RANGE = "If-Range";

    /**
     * Date. To execute the method if the entity
     * has not changed since the specified date.
     */
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    /**
     * Date of last modification of entity
     */
    public static final String LAST_MODIFIED = "Last-Modified";

    /**
     * Indicates logically connected with the essence of
     * the resource is similar to the <LINK> tag in HTML.
     */
    public static final String LINK = "Link";

    /**
     * URI on which the client should go or URI of the created resource.
     * e.g. http://example.com/about.html#contacts
     */
    public static final String LOCATION = "Location";

    /**
     * The maximum allowable number of hops through a proxy.
     * e.g. 10
     */
    public static final String MAX_FORWARDS = "Location";

    /**
     * Version of the MIME Protocol that generated the message
     */
    public static final String MIME_VERSION = "MIME-Version";

    /**
     * Special selecting operation.
     * e.g. no-cache
     */
    public static final String PRAGMA = "Pragma";

    /**
     * The authentication parameters at the proxy server
     */
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /**
     * Information for authorizing on the proxy server.
     * e.g. Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * The list of available methods similarly Allow, but for the entire server
     */
    public static final String PUBLIC = "Public";

    /**
     * Byte ranges for fragments of the request resource.
     * e.g. bytes=50000-99999,250000-399999,500000
     */
    public static final String RANGE = "Range";

    /**
     * The URI of the resource, after which the client has made the current request.
     * e.g. http://en.wikipedia.org/wiki/Main_Page
     */
    public static final String REFERER = "Referer";

    /**
     * The date or the time in seconds after which to retry the request
     */
    public static final String RETRY_AFTER = "Retry-After";

    /**
     * A list of names and versions of the web server and its components with comments.
     * For proxy server the Via field.
     * e.g. Apache/2.2.17 (Win32) PHP/5.3.5
     */
    public static final String SERVER = "Server";

    /**
     * The title of the entity
     */
    public static final String TITLE = "Title";

    /**
     * List of advanced coding techniques in the transmission.
     * e.g. trailers, deflate
     */
    public static final String TE = "TE";

    /**
     * The list of fields relevant to the encoding of the message
     */
    public static final String TRAILER = "Trailer";

    /**
     * List of methods of encoding that have been
     * applied to the message for transmission.
     * e.g. chunked
     */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * A list of names and versions of the client and its components with comments
     * e.g. Mozilla/5.0 (X11; Linux i686; rv:2.0.1)
     * Gecko/20100101 Firefox/4.0.1
     */
    public static final String USER_AGENT = "User-Agent";


    /*
     * ----------- Annotations for Headers -----------
     */

    /**
     * Used in requests and responses
     */
    @Retention(CLASS)
    @Target({PARAMETER, METHOD, LOCAL_VARIABLE, FIELD})
    public @interface GeneralHeader {
    }

    /**
     * Used only in requests
     */
    @Retention(CLASS)
    @Target({PARAMETER, METHOD, LOCAL_VARIABLE, FIELD})
    public @interface RequestHeader {
    }

    /**
     * Used only in responses
     */
    @Retention(CLASS)
    @Target({METHOD})
    public @interface ResponseHeader {
    }

    /**
     * Accompany each entity of the message. Used in requests and responses
     */
    @Retention(CLASS)
    @Target({METHOD})
    public @interface EntityHeader {
    }

}
