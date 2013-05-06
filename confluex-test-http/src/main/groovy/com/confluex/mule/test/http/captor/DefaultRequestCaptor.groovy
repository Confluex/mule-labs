package com.confluex.mule.test.http.captor

import com.confluex.mule.test.http.ClientRequest
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.springframework.core.io.Resource

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@EqualsAndHashCode
@ToString(includeNames = true)
class DefaultRequestCaptor implements RequestCaptor {
    Map<String, String> headers = [:]
    List<ClientRequest> requests = []

    Resource resource
    Integer status = 200
    String text = "ok"


    void render(HttpServletRequest request, HttpServletResponse response) {
        response.status = status
        headers.each { k, v ->
            response.addHeader(k, v)
        }

        // Not sure why this isn't working..
        //        response.outputStream << resource?.inputStream ?: text
        if (resource) {
            response.outputStream << resource.inputStream
        } else {
            response.outputStream << text
        }

        requests << new ClientRequest(request)
    }

}
