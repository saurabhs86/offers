package io.uslab.offers.controller;

import io.uslab.offers.config.IAConfig;
import io.uslab.offers.config.WFACookieConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@RestController
public class IAController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IAConfig iaConfig;

    @Autowired
    private WFACookieConfig wfaCookieConfig;

    @CrossOrigin

    @RequestMapping(path = "/offers", method = RequestMethod.GET)
    public ResponseEntity<?> getOffers(HttpServletRequest request) {

        String url = iaConfig.getUrl();
        String cookies = addCookiesToHeader(request);
        String responseBody = "";

        if (cookies != null && !cookies.isEmpty()) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Cookie", cookies);

            HttpEntity<MultiValueMap<String, String>> httpEntity =
                    new HttpEntity<MultiValueMap<String, String>>(
                        parse(request.getQueryString()) ,
                        httpHeaders
            );

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            responseBody = removeComments(responseEntity.getBody());

        }
        //return ResponseEntity.ok(responseBody);
         return ResponseEntity.status(HttpStatus.OK).body(responseBody);

    }

    public String addCookiesToHeader(HttpServletRequest request) {
        String wfaCookieValue = "";
        boolean wfaCookieFound = false;

        if (request.getCookies() != null) {
            Cookie[] cookies = request.getCookies();
            for(Cookie cookie: cookies) {
                if (wfaCookieConfig.getName().equals(cookie.getName())) {
                    wfaCookieValue = cookie.getValue();
                    wfaCookieFound = true;
                }
            }
        }

        if (wfaCookieFound == true) {
            return wfaCookieConfig.getName() + "=" + wfaCookieValue + ";";
        } else {
            return null;
        }
    }

    private String removeComments(String input) {
        return  input.substring(2, input.length() - 2);
    }

    public static MultiValueMap<String, String> parse(final String input) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (String pair : input.split("&")) {
            int index = pair.indexOf("=");
            if (index == -1) {
                map.add(pair, "");
                continue;
            }
            String key = pair.substring(0, index);
            String value = pair.substring(index + 1);
            map.add(key, value);
        }
        return map;
    }
}
