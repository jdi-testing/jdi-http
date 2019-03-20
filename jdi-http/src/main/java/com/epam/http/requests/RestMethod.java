package com.epam.http.requests;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.http.annotations.Cookie;
import com.epam.http.annotations.QueryParameter;
import com.epam.http.response.ResponseStatusType;
import com.epam.http.response.RestResponse;
import com.epam.jdi.tools.func.JAction1;
import com.epam.jdi.tools.map.MapArray;
import com.google.gson.Gson;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.time.StopWatch;

import static com.epam.http.ExceptionHandler.exception;
import static com.epam.http.JdiHttpSettigns.logger;
import static com.epam.http.requests.RestRequest.doRequest;
import static com.epam.http.response.ResponseStatusType.OK;
import static com.epam.jdi.tools.PrintUtils.formatParams;
import static com.epam.jdi.tools.PrintUtils.print;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.apache.commons.lang3.time.StopWatch.createStarted;

public class RestMethod<T> {
    public RequestSpecification spec = given().filter(new AllureRestAssured());;
    private RequestData data;
    private RestMethodTypes type;
    private Gson gson = new Gson();
    private ResponseStatusType expectedStatus = OK;

    public RestMethod() {}
    public RestMethod(JAction1<RequestSpecification> specFunc, RestMethodTypes type) {
        specFunc.execute(spec);
        this.type = type;
    }
    public RestMethod(RestMethodTypes type, String url) {
        this(type, new RequestData().set(d -> d.url = url));
    }
    public RestMethod(RestMethodTypes type, RequestData data) {
        this.data = data;
        this.type = type;
    }
    public RestMethod(RestMethodTypes type, String url, RequestSpecification requestSpecification) {
        this(type, url);
        this.spec = spec.spec(requestSpecification);
    }
    public void addHeader(String name, String value) {
        data.headers.add(name, value);
    }
    public void addHeader(com.epam.http.annotations.Header header) {
        addHeader(header.name(), header.value());
    }
    public void addHeader(Header header) {
        addHeader(header.getName(), header.getValue());
    }
    public void addHeaders(com.epam.http.annotations.Header... headers) {
        for(com.epam.http.annotations.Header header : headers)
            addHeader(header);
    }
    public void setContentType(ContentType ct) {
        data.contentType = ct;
    }
    public void addHeaders(Header[] headers) {
        for(Header header : headers)
            addHeader(header);
    }
    public void addCookie(String name, String value) {
        data.cookies.add(name, value);
    }
    public void addCookie(Cookie cookie) {
        addCookie(cookie.name(), cookie.value());
    }
    public void addCookies(Cookie... cookies) {
        for (Cookie cookie : cookies) {
            addCookie(cookie);
        }
    }
    public RestMethod expectStatus(ResponseStatusType status) {
        expectedStatus = status; return this;
    }

    void addQueryParameters(QueryParameter... params) {
        data.queryParams.addAll(new MapArray<>(params,
            QueryParameter::name, QueryParameter::value));
    }

    public RestResponse call() {
        if (type == null)
            throw exception("HttpMethodType not specified");
        RequestSpecification spec = getSpec().log().all();
        logger.info(format("Do %s request %s", type, data.url));
        return doRequest(type, spec, expectedStatus);
    }
    public T callAsData(Class<T> c) {
        try {
            return call().raResponse().body().as(c);
        } catch (Exception ex) {
            throw new RuntimeException("Can't convert response in " + c.getSimpleName());
        }
    }
    public T asData(Class<T> c) {
        return callAsData(c);
    }
    public RestResponse postData(T data) {
        this.data.body = gson.toJson(data);
        getSpec().body(this.data.body);
        return call();
    }

    public RestResponse call(String... params) {
        if (data.url.contains("%s") && params.length > 0)
            data.url = format(data.url, params);
        return call();
    }
    public RestResponse post(String body) {
        return call(new RequestData().set(rd -> rd.body = body));
    }

    public RestResponse call(RequestData requestData) {
        if (!requestData.pathParams.isEmpty())
            data.pathParams.addAll(requestData.pathParams);
        if (!requestData.queryParams.isEmpty())
            data.queryParams.addAll(requestData.queryParams);
        if (requestData.body != null)
            data.body = requestData.body;
        if (!requestData.headers.isEmpty()) {
            data.headers.addAll(requestData.headers);
        }
        if (!requestData.cookies.isEmpty()) {
            data.cookies.addAll(requestData.cookies);
        }
       return call();
    }
    public RestResponse call(RequestSpecification requestSpecification) {
        this.spec = spec.spec(requestSpecification);
        return call();
    }
    public RequestSpecification getSpec() {
        if (data == null)
            return spec;
        if (data.pathParams.any() && data.url.contains("{"))
            data.url = formatParams(data.url, data.pathParams);
        spec.contentType(data.contentType);
        spec.baseUri(data.url);
        if (data.queryParams.any()) {
            spec.queryParams(data.queryParams.toMap());
            data.url += "?" + print(data.queryParams.toMap(), "&", "{0}={1}");
        }
        if (data.body != null)
            spec.body(data.body);
        if (data.headers.any())
            spec.headers(data.headers.toMap());
        if (data.cookies.any())
            spec.cookies(data.cookies.toMap());
        return spec;
    }
    public void isAlive() {
        isAlive(2000);
    }
    public void isAlive(int liveTimeMSec) {
        StopWatch watch = createStarted();
        ResponseStatusType status;
        do { status = call().status.type;
        } while (status != OK && watch.getTime() < liveTimeMSec);
        call().isOk();
    }
}
