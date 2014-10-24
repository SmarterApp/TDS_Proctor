/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package TDS.Proctor.Web.client;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

/**
 * @author mpatel
 *
 */
@Component
public class TestRegServiceClient
{
  /**
   * Base URI to use to talk to TestReg REST API
   */
//  @Value("${tib.baseUri:\"\"}")
  private String baseUri = "https://tr-dev.opentestsystem.org:8443/rest/studentpackage?ssid=S99423&stateabbreviation=WI";
  
  public String getBaseUri () {
    return baseUri;
  }

  public void setBaseUri (String baseUri) {
    this.baseUri = baseUri;
  }

  @Resource
  protected OAuth2RestTemplate testRegClientRestTemplate;
  
  public <T> ResponseEntity<T> exchange(HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
    return testRegClientRestTemplate.exchange (this.baseUri , method, requestEntity, responseType);
  }
  
  public <T> ResponseEntity<T> exchange(final Map<String, String[]> requestParamMap,HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
    return testRegClientRestTemplate.exchange (this.baseUri + buildRequestParamQuery(requestParamMap) , method, requestEntity, responseType);
  }
  
  private String buildRequestParamQuery(final Map<String, String[]> requestParamMap) {
    final StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append('?');
    boolean first = true;
    final Iterator<Entry<String, String[]>> it = requestParamMap.entrySet().iterator();

    while (it.hasNext()) {
        final Map.Entry<String, String[]> pairs = it.next();

        if (pairs != null) {
            for (final String val : pairs.getValue()) {
                if (!first) {
                    queryBuilder.append('&');
                }
                first = false;

                queryBuilder.append(pairs.getKey()).append("=").append(val);
            }
        }
    }
    return queryBuilder.toString();
}
  
  
}
