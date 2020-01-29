package com.epam.http.response;

import com.epam.jdi.tools.func.JAction1;
import com.epam.jdi.tools.map.MapArray;
import com.epam.jdi.tools.pairs.Pair;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.function.Function;

import static com.epam.http.ExceptionHandler.exception;
import static com.epam.http.JdiHttpSettigns.logger;
import static com.epam.http.response.ResponseStatusType.ERROR;
import static com.epam.http.response.ResponseStatusType.OK;
import static com.epam.jdi.tools.StringUtils.LINE_BREAK;
import static java.lang.String.format;

/**
 * Represents full HTTP response.
 *
 * @author <a href="mailto:roman.iovlev.jdi@gmail.com">Roman_Iovlev</a>
 */
public class RestResponse{
    private final Response raResponse;
    private final long responseTimeMSec;
    public String body = null;
    public ResponseStatus status = null;
    public String contenType = "";

    public RestResponse() {
        this.raResponse = null;
        responseTimeMSec = 0;
    }
    public static RestResponse Response() {
        return new RestResponse();
    }
    public RestResponse(Response raResponse) {
        this(raResponse, 0);
    }
    public RestResponse(Response raResponse, long time) {
        this.raResponse = raResponse;
        responseTimeMSec = time;
        body = raResponse.body().asString();
        status = new ResponseStatus(raResponse);
        contenType = raResponse.contentType();
        logger.info(toString());
    }
    public RestResponse set(JAction1<RestResponse> valueFunc) {
        RestResponse thisObj = this;
        valueFunc.execute(thisObj);
        return thisObj;
    }
    public boolean verify(Function<RestResponse, Boolean> validator) {
        return validator.apply(this);
    }

    /**
     * Check the validity of the response.
     * @param validator     function to validate assuming the result would be boolean
     * @return              Rest Assured validatable response
     */
    public ValidatableResponse validate(Function<RestResponse, Boolean> validator) {
        if (!verify(validator))
            throw exception("Bad raResponse: " + toString());
        return assertThat();
    }

    /**
     * Check if response status is OK(code starts with 2).
     * @return          result of assertion
     */
    public ValidatableResponse isOk() {
        return isStatus(OK);
    }

    /**
     * Check if response status has any errors.
     * @return          result of assertion
     */
    public ValidatableResponse hasErrors() {
        return isStatus(ERROR);
    }

    /**
     * Validate the status.
     * @param type      of status as enumeration value
     * @return          result of assertion
     */
    public ValidatableResponse isStatus(ResponseStatusType type) {
        return validate(r -> status.type == type);
    }
    public ValidatableResponse isEmpty() {
        return validate(r -> body.equals(""));
    }

    /**
     * Check response body according to expected values.
     * @param params    key name and matcher with expected value for that key
     * @return          Rest Assured response
     */
    public ValidatableResponse assertBody(MapArray<String, Matcher<?>> params) {
        ValidatableResponse vr = assertThat();
        try {
            for (Pair<String, Matcher<?>> pair : params)
                vr.body(pair.key, pair.value);
            return vr;
        } catch (Exception ex) { throw new RuntimeException("Only <String, Matcher> pairs available for assertBody"); }
    }

    /**
     * Check response body according to expected values.
     * @param params    key name and matcher with expected value for that key
     * @return          Rest Assured response
     */
    public ValidatableResponse assertBody(Object[][] params) {
        return assertBody(new MapArray<>(params));
    }

    /**
     * Get text/html media type content by path.
     * @param path      the HTML path
     * @return          string matching the provided HTML path
     */
    public String getFromHtml(String path) {
        return raResponse.body().htmlPath().getString(path);
    }

    /**
     * Get response headers.
     *
     * @return      response headers list
     */
    public List<Header> headers() { return raResponse.getHeaders().asList(); }

    /**
     * Get response cookie associated by the given name.
     * @param name      cookie key name
     * @return          cookie value
     */
    public String cookie(String name) { return raResponse.getCookie(name); }

    /**
     * Get Rest Assured response.
     * @return          Rest Assured response
     */
    public Response raResponse() { return raResponse; }

    /**
     * Time taken to perform HTTP request.
     *
     * @return      time
     */
    public long responseTime() { return responseTimeMSec; }

    /**
     * Returns validatable response.
     * @return          validatable Rest Assured response
     */
    public ValidatableResponse assertThat() { return raResponse.then(); }

    /**
     * Verify the status of response.
     * @param rs        expected response status containing code, type and text message
     * @return          response
     */
    public RestResponse assertStatus(ResponseStatus rs) {
        String errors = "";
        if (status.code != rs.code)
            errors += format("Wrong status code %s. Expected: %s", status.code, rs.code) + LINE_BREAK;
        if (!status.type.equals(rs.type))
            errors += format("Wrong status type %s. Expected: %s", status.type, rs.type) + LINE_BREAK;
        if (!status.text.equals(rs.text))
            errors += format("Wrong status text %s. Expected: %s", status.text, rs.text);
        if (!errors.equals(""))
            throw exception(errors);
        return this;
    }
    @Override
    public String toString() {
        return format("Response status: %s %s (%s)", status.code, status.text, status.type) + LINE_BREAK +
               "Response body: " + body;
    }
}