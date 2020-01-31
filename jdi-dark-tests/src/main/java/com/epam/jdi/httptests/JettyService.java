package com.epam.jdi.httptests;

import com.epam.http.annotations.ContentType;
import com.epam.http.annotations.DELETE;
import com.epam.http.annotations.GET;
import com.epam.http.annotations.Header;
import com.epam.http.annotations.Headers;
import com.epam.http.annotations.POST;
import com.epam.http.annotations.ServiceDomain;
import com.epam.http.requests.RestMethod;

import static io.restassured.http.ContentType.TEXT;

@ServiceDomain("http://localhost:8080/")
public class JettyService {

    @GET("/multiCookie")
    static RestMethod getMultiCookie;

    @GET("/setCookies")
    static RestMethod setCookies;

    @GET("/cookie")
    static RestMethod getCookie;

    @POST("/reflect")
    static RestMethod postReflect;

    @GET("/setCommonIdCookies")
    static RestMethod getCommonIdCookies;

    @GET("/header")
    @Header(name = "HeaderTestName", value = "HeaderTestValue")
    static RestMethod getWithSingleHeaderInRequest;

    @GET("/header")
    @Headers({
            @Header(name = "Header1", value = "Value1"),
            @Header(name = "Header2", value = "Value2")
    })
    static RestMethod getWithMultipleHeadersInRequest;

    @DELETE("/cookie")
    static RestMethod deleteCookie;

    @DELETE("/greet")
    static RestMethod deleteGreet;

    @DELETE("/body")
    @ContentType(TEXT)
    static RestMethod deleteBody;
}
