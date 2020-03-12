package com.epam.http.requests;

import com.epam.http.requests.updaters.CookieUpdater;
import com.epam.jdi.tools.DataClass;
import com.epam.jdi.tools.map.MapArray;
import com.epam.jdi.tools.map.MultiMap;
import com.epam.jdi.tools.pairs.Pair;
import io.restassured.authentication.AuthenticationScheme;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.MultiPartSpecification;
import io.restassured.specification.ProxySpecification;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents all HTTP request data.
 *
 * @author <a href="mailto:roman.iovlev.jdi@gmail.com">Roman_Iovlev</a>
 */
public class RequestData extends DataClass<RequestData> {
    public boolean empty = true;
    public String uri = null;
    public String path = null;
    public Object body = null;
    public String contentType = null;
    public Headers headers = new Headers();
    public Cookies cookies = new Cookies();
    public MultiMap<String, String> pathParams = new MultiMap<>();
    public MultiMap<String, String> queryParams = new MultiMap<>();
    public MultiMap<String, String> formParams = new MultiMap<>();
    public ArrayList<MultiPartSpecification> multiPartSpecifications = new ArrayList<>();
    public ProxySpecification proxySpecification = null;
    public AuthenticationScheme authenticationScheme = null;
    public Pair<String, String> trustStore = null;
//            new Pair<>(null,null);

    public CookieUpdater addCookies() { return new CookieUpdater(() -> this); }
    /**
     * Set content type to request data.
     *
     * @param contentType  content type as string
     */
    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    /**
     * Set content type to request data.
     *
     * @param contentType  content type as ContentType
     */
    public void setContentType(ContentType contentType){
        this.contentType = contentType.toString();
    }

    /**
     * Set multipart parameters to request data.
     *
     * @param multiPartSpecBuilder  MultiPartSpecBuilder
     */
    public void setMultiPart(MultiPartSpecBuilder multiPartSpecBuilder) {
        multiPartSpecifications.add(multiPartSpecBuilder.build());
    }

    /**
     * Set authentication scheme to request data
     * This allows authentcation for requests
     * @param authScheme authentication scheme: from restassured or custom
     */

    public void setAuth(AuthenticationScheme authScheme) {
        authenticationScheme = authScheme;
    }

    /**
     * Set multipart parameters to request data.
     *
     * @param file  File parameter
     */
    public void setMultiPart(File file) {
        multiPartSpecifications.add(new MultiPartSpecBuilder(file).build());
    }

    /**
     * Set proxy parameters to request data.
     *
     * @param scheme scheme
     * @param host host
     * @param port port
     */
    public void setProxySpecification(String scheme, String host, int port) {
        this.proxySpecification = ProxySpecification.host(host).and().withPort(port).and().withScheme(scheme);
    }

    /**
     * Set trustStore parameters to request data.
     *
     * @param pathToJks pathToJks
     * @param password password
     */
    public RequestData requestTrustStore(String pathToJks, String password){
        this.trustStore = new Pair<>(pathToJks, password);
        return this;
    }

    /**
     * Clean Custom user Request data to avoid using old data in new requests
     */
    public void clear() {
        headers = new Headers();
        pathParams.clear();
        queryParams.clear();
        formParams.clear();
        cookies = new Cookies();
        body = null;
        path = null;
        uri = null;
        contentType = null;
        empty = true;
        multiPartSpecifications.clear();
        proxySpecification = null;
        authenticationScheme = null;
        trustStore = null;
    }

    /**
     * Adds headers to HTTP request
     *
     * @param objects pairs of headers name and value
     * @return generated request data with provided headers
     */
    public RequestData addHeaders(Object[][] objects) {
        List<Header> headerList = new ArrayList<>();
        for (Object[] header : objects) {
            headerList.add(new Header(header[0].toString(), header[1].toString()));
        }
        headers = new Headers(headerList);
        return this;
    }

    /**
     * Adds header without value to HTTP request
     *
     * @param name of header
     * @return generated request data with provided header
     */
    public RequestData addHeader(String name) {
        return addHeader(name, "");
    }

    /**
     * Adds header with multiple values to HTTP request
     *
     * @param name             of header
     * @param value            of header
     * @param additionalValues of header
     * @return generated request data with provided header
     */
    public RequestData addHeader(String name, String value, String... additionalValues) {
        List<Header> headerList = new ArrayList<>(headers.asList());
        headerList.add(new Header(name, value));
        for (String headerValue : additionalValues) {
            headerList.add(new Header(name, headerValue));
        }
        headers = new Headers(headerList);
        return this;
    }

    /**
     * Adds header from Map to HTTP request
     *
     * @param map of header
     * @return generated request data with provided headers
     */
    public RequestData addHeaders(Map map) {
        List<Header> headerList = new ArrayList<>(headers.asList());
        for (Object header : map.keySet()) {
            headerList.add(new Header(header.toString(), map.get(header).toString()));
        }
        headers = new Headers(headerList);
        return this;
    }

    /**
     * Adds headers from List to HTTP request
     *
     * @param list of headers
     * @return generated request data with provided headers
     */
    public RequestData addHeaders(List<Header> list) {
        List<Header> headerList = new ArrayList<>(headers.asList());
        headerList.addAll(list);
        headers = new Headers(headerList);
        return this;
    }

    /**
     * Adds headers from number of Header objects to HTTP request
     *
     * @param headerObjects number of header objects to create Headers
     * @return generated request data with provided headers
     */
    public RequestData addHeaders(Header... headerObjects) {
        List<Header> headerList = new ArrayList<>(headers.asList());
        Collections.addAll(headerList, headerObjects);
        headers = new Headers(headerList);
        return this;
    }

    /**
     * Adds headers from MapArray to HTTP request
     *
     * @param mapArray of headers
     * @return generated request data with provided headers
     */
    public RequestData addHeaders(MapArray mapArray) {
        return addHeaders(mapArray.toMap());
    }
}
