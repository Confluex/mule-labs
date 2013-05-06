package com.confluex.mule.test.http.expectations

import com.confluex.mule.test.http.ClientRequest
import groovy.transform.ToString

@ToString(includeNames = true)
class MethodExpectation implements Expectation {


    public static final GET = new MethodExpectation("GET")
    public static final PUT = new MethodExpectation("PUT")
    public static final DELETE = new MethodExpectation("DELETE")
    public static final POST = new MethodExpectation("POST")

    String method


    MethodExpectation(String method) {
        this.method = method.toUpperCase()
    }

    Boolean verify(ClientRequest request) {
        return request.method?.toUpperCase() == method
    }

}
