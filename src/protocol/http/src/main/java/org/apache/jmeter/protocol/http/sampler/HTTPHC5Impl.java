/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.HttpSSLProtocolSocketFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Sampler using Apache HttpClient 5.x classic API.
 * <p>
 * Supports common REST JSON/form workloads: GET/POST/PUT/DELETE/PATCH/HEAD,
 * headers, request body, basic multipart file upload, proxy, HTTPS,
 * per-thread connection pooling, CookieManager string cookies, and basic
 * {@link HTTPSampleResult} metrics.
 * </p>
 * <p>
 * <b>Known gaps vs {@link HTTPHC4Impl}</b> (intentional for a working classic path):
 * </p>
 * <ul>
 * <li>HC5 5.3 uses {@link DefaultRedirectStrategy} (no LaxRedirectStrategy until 5.4+)</li>
 * <li>Digest / NTLM / Kerberos auth are not fully ported; Basic auth uses
 * {@link AuthManager#getAuthHeaderForURL(URL)} preemptively when configured</li>
 * <li>Precise connect-time instrumentation (HC4 connection operator) is approximate</li>
 * <li>Wire-level sent/received byte metrics are estimated from headers/body sizes</li>
 * <li>CacheManager uses JMeter header arrays / a thin HC4 adapter for save/set</li>
 * <li>WebDAV methods, OPTIONS/TRACE body edge-cases, and slow CPS socket factories
 * are not fully mirrored</li>
 * <li>Brotli/gzip relax-mode interceptors from HC4 are not duplicated; HC5 default
 * content decompression applies</li>
 * </ul>
 * <p>
 * Properties: {@code httpclient5.*} mirror key {@code httpclient4.*} settings
 * (idle timeout, validate after inactivity, time-to-live, retry count, default UA)
 * with fallback to {@code httpclient4.*} then {@code httpclient.*} where applicable.
 * </p>
 */
/**
 * HTTP Sampler using Apache HttpClient 5.x (classic API).
 * <p>Select via HTTP Request Implementation = {@code HttpClient5}, or
 * {@code jmeter.httpsampler=HttpClient5}. Default remains HttpClient4.</p>
 */
@SuppressWarnings("deprecation") // HC5 classic execute / AuthManager.BASIC_DIGEST still used transitionaly
public class HTTPHC5Impl extends HTTPHCAbstractImpl {

    private static final Logger log = LoggerFactory.getLogger(HTTPHC5Impl.class);

    private static final String CONTEXT_ATTRIBUTE_PARENT_SAMPLE_CLIENT_STATE = "__jmeter.HC5.H_T__";
    private static final String CONTEXT_LOCAL_ADDRESS = "__jmeter.HC5.localAddress__";

    private static final boolean DISABLE_DEFAULT_UA = getPropBool(
            "httpclient5.default_user_agent_disabled",
            "httpclient4.default_user_agent_disabled",
            false);

    private static final int RETRY_COUNT = getPropInt(
            "httpclient5.retrycount",
            "httpclient4.retrycount",
            null,
            0);

    private static final int IDLE_TIMEOUT = getPropInt(
            "httpclient5.idletimeout",
            "httpclient4.idletimeout",
            null,
            0);

    private static final int VALIDITY_AFTER_INACTIVITY_TIMEOUT = getPropInt(
            "httpclient5.validate_after_inactivity",
            "httpclient4.validate_after_inactivity",
            null,
            4900);

    private static final int TIME_TO_LIVE = getPropInt(
            "httpclient5.time_to_live",
            "httpclient4.time_to_live",
            null,
            60000);

    private static final boolean BASIC_AUTH_PREEMPTIVE = getPropBool(
            "httpclient5.auth.preemptive",
            "httpclient4.auth.preemptive",
            true);

    private static final Pattern PORT_PATTERN = Pattern.compile("\\d+");

    private static final ThreadLocal<Map<HttpClientKey, ClientState>>
            HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY =
            InheritableThreadLocal.withInitial(() -> new HashMap<>(5));

    private volatile HttpUriRequestBase currentRequest;

    protected HTTPHC5Impl(HTTPSamplerBase testElement) {
        super(testElement);
    }

    private static int getPropInt(String primary, String fallback, String tertiary, int def) {
        String v = JMeterUtils.getProperty(primary);
        if (!JOrphanUtils.isBlank(v)) {
            return Integer.parseInt(v.trim());
        }
        v = JMeterUtils.getProperty(fallback);
        if (!JOrphanUtils.isBlank(v)) {
            return Integer.parseInt(v.trim());
        }
        if (tertiary != null) {
            v = JMeterUtils.getProperty(tertiary);
            if (!JOrphanUtils.isBlank(v)) {
                return Integer.parseInt(v.trim());
            }
        }
        return def;
    }

    private static boolean getPropBool(String primary, String fallback, boolean def) {
        String v = JMeterUtils.getProperty(primary);
        if (!JOrphanUtils.isBlank(v)) {
            return Boolean.parseBoolean(v.trim());
        }
        return JMeterUtils.getPropDefault(fallback, def);
    }

    @Override
    protected HTTPSampleResult sample(URL url, String method,
            boolean areFollowingRedirect, int frameDepth) {

        if (log.isDebugEnabled()) {
            log.debug("Start : sample {} method {} followingRedirect {} depth {}",
                    url, method, areFollowingRedirect, frameDepth);
        }

        HTTPSampleResult res = createSampleResult(url, method);
        CloseableHttpClient httpClient;
        HttpUriRequestBase httpRequest;
        HttpClientContext clientContext = HttpClientContext.create();
        HttpClientKey key = createHttpClientKey(url);
        ClientState clientState;

        try {
            clientState = setupClient(key);
            httpClient = clientState.httpClient;
            httpRequest = createHttpRequest(url.toURI(), method);
            setupRequest(url, httpRequest, res, clientContext);
        } catch (Exception e) {
            res.sampleStart();
            res.sampleEnd();
            errorResult(e, res);
            return res;
        }

        res.sampleStart();

        final CacheManager cacheManager = getCacheManager();
        if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method)
                && cacheManager.inCache(url, toJMeterHeaders(httpRequest.getHeaders()))) {
            return updateSampleResultForResourceInCache(res);
        }

        CloseableHttpResponse httpResponse = null;
        try {
            currentRequest = httpRequest;
            handleMethod(method, res, httpRequest);
            applyBasicAuth(url, httpRequest);

            httpResponse = httpClient.execute(httpRequest, clientContext);

            if (localAddress != null) {
                httpRequest.addHeader(HEADER_LOCAL_ADDRESS, localAddress.toString());
            }
            res.setRequestHeaders(getAllHeadersExceptCookie(httpRequest));

            Header contentType = httpResponse.getLastHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            if (contentType != null) {
                String ct = contentType.getValue();
                res.setContentType(ct);
                res.setEncodingAndType(ct);
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                res.setResponseData(readResponse(res, entity.getContent(), entity.getContentLength()));
            }

            res.sampleEnd();
            currentRequest = null;

            int statusCode = httpResponse.getCode();
            res.setResponseCode(Integer.toString(statusCode));
            res.setResponseMessage(httpResponse.getReasonPhrase());
            res.setSuccessful(isSuccessCode(statusCode));
            res.setResponseHeaders(getResponseHeaders(httpResponse));
            if (res.isRedirect()) {
                final Header headerLocation = httpResponse.getLastHeader(HTTPConstants.HEADER_LOCATION);
                if (headerLocation == null) {
                    throw new IllegalArgumentException("Missing location header in redirect for "
                            + httpRequest.getMethod() + " " + httpRequest.getRequestUri());
                }
                res.setRedirectLocation(headerLocation.getValue());
            }

            long headerBytes = (long) res.getResponseHeaders().length()
                    + (long) httpResponse.getHeaders().length
                    + 1L + 2L;
            long bodyBytes = res.getBodySizeAsLong();
            if (bodyBytes <= 0 && entity != null && entity.getContentLength() >= 0) {
                bodyBytes = entity.getContentLength();
            }
            res.setHeadersSize((int) headerBytes);
            res.setBodySize(bodyBytes);
            // Estimated sent bytes (HC4 uses connection metrics)
            res.setSentBytes((long) res.getRequestHeaders().length()
                    + (res.getQueryString() != null ? res.getQueryString().length() : 0L));

            if (getAutoRedirects()) {
                updateResultUrlAfterRedirect(clientContext, res);
            }

            saveConnectionCookies(httpResponse, res.getURL(), getCookieManager());

            if (cacheManager != null) {
                saveCacheDetails(cacheManager, httpResponse, res);
            }

            res = resultProcessing(areFollowingRedirect, frameDepth, res);
            if (!isSuccessCode(statusCode)) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        } catch (IOException e) {
            log.debug("IOException", e);
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
            res.setRequestHeaders(getAllHeadersExceptCookie(httpRequest));
            errorResult(e, res);
            return res;
        } catch (RuntimeException e) {
            log.debug("RuntimeException", e);
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
            errorResult(e, res);
            return res;
        } finally {
            JOrphanUtils.closeQuietly(httpResponse);
            currentRequest = null;
            JMeterContextService.getContext().getSamplerContext().remove(CONTEXT_ATTRIBUTE_PARENT_SAMPLE_CLIENT_STATE);
        }
        return res;
    }

    private static void updateResultUrlAfterRedirect(HttpClientContext clientContext, HTTPSampleResult res) {
        try {
            if (clientContext.getHttpRoute() == null) {
                return;
            }
            HttpHost target = clientContext.getHttpRoute().getTargetHost();
            if (target != null) {
                res.setURL(new URL(target.toURI()));
            }
        } catch (Exception e) {
            log.debug("Could not update URL after redirect", e);
        }
    }

    private void applyBasicAuth(URL url, HttpUriRequestBase httpRequest) {
        AuthManager authManager = getAuthManager();
        if (authManager == null || !BASIC_AUTH_PREEMPTIVE) {
            return;
        }
        Authorization authorization = authManager.getAuthForURL(url);
        if (authorization == null) {
            return;
        }
        Mechanism mechanism = authorization.getMechanism();
        if (mechanism == Mechanism.BASIC) {
            String header = authorization.toBasicHeader();
            if (header != null) {
                httpRequest.setHeader(HTTPConstants.HEADER_AUTHORIZATION, header);
            }
        }
    }

    private static HttpUriRequestBase createHttpRequest(URI uri, String method) {
        if (method.equals(HTTPConstants.POST)) {
            return new HttpPost(uri);
        } else if (method.equals(HTTPConstants.GET)) {
            return new HttpGet(uri);
        } else if (method.equals(HTTPConstants.PUT)) {
            return new HttpPut(uri);
        } else if (method.equals(HTTPConstants.HEAD)) {
            return new HttpHead(uri);
        } else if (method.equals(HTTPConstants.TRACE)) {
            return new HttpTrace(uri);
        } else if (method.equals(HTTPConstants.OPTIONS)) {
            return new HttpOptions(uri);
        } else if (method.equals(HTTPConstants.DELETE)) {
            return new HttpDelete(uri);
        } else if (method.equals(HTTPConstants.PATCH)) {
            return new HttpPatch(uri);
        } else {
            // Generic method (including possible WebDAV) via HttpUriRequestBase
            return new HttpUriRequestBase(method, uri);
        }
    }

    protected void handleMethod(String method, HTTPSampleResult result,
            HttpUriRequestBase httpRequest) throws IOException {
        if (HTTPConstants.POST.equals(method)
                || HTTPConstants.PUT.equals(method)
                || HTTPConstants.PATCH.equals(method)
                || HTTPConstants.DELETE.equals(method)
                || (HTTPConstants.GET.equals(method)
                    && ((!hasArguments() && getSendFileAsPostBody())
                        || getSendParameterValuesAsPostBody()))) {
            String entityBody = setupHttpEntityEnclosingRequestData(httpRequest);
            result.setQueryString(entityBody);
        }
    }

    protected HTTPSampleResult createSampleResult(URL url, String method) {
        HTTPSampleResult res = new HTTPSampleResult();
        configureSampleLabel(res, url);
        res.setHTTPMethod(method);
        res.setURL(url);
        return res;
    }

    private ClientState setupClient(HttpClientKey key) {
        Map<HttpClientKey, ClientState> map = HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY.get();
        boolean concurrentDwn = this.testElement.isConcurrentDwn();
        Map<String, Object> samplerContext = JMeterContextService.getContext().getSamplerContext();
        ClientState state = null;
        if (concurrentDwn) {
            state = (ClientState) samplerContext.get(CONTEXT_ATTRIBUTE_PARENT_SAMPLE_CLIENT_STATE);
        }
        if (state == null) {
            state = map.get(key);
        }

        boolean reset = Boolean.TRUE.equals(resetStateOnThreadGroupIteration.get());
        resetStateIfNeeded(map);
        if (reset) {
            state = null;
        } else if (state == null) {
            state = map.get(key);
        }

        if (state == null) {
            state = createClientState(key);
            map.put(key, state);
            if (log.isDebugEnabled()) {
                log.debug("Created new HttpClient5: @{} {}", System.identityHashCode(state.httpClient), key);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Reusing HttpClient5: @{} {}", System.identityHashCode(state.httpClient), key);
        }

        if (concurrentDwn) {
            samplerContext.put(CONTEXT_ATTRIBUTE_PARENT_SAMPLE_CLIENT_STATE, state);
        }
        return state;
    }

    private ClientState createClientState(HttpClientKey key) {
        DnsResolver resolver = createDnsResolver();

        // Use JMeter SSL socket factory so per-thread SSL context resets are honored
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                new HttpSSLProtocolSocketFactory(JsseSSLManager.CPS),
                NoopHostnameVerifier.INSTANCE);

        ConnectionConfig.Builder connCfg = ConnectionConfig.custom()
                .setValidateAfterInactivity(TimeValue.ofMilliseconds(VALIDITY_AFTER_INACTIVITY_TIMEOUT))
                .setTimeToLive(TimeValue.ofMilliseconds(TIME_TO_LIVE));
        int cto = getConnectTimeout();
        if (cto > 0) {
            connCfg.setConnectTimeout(Timeout.ofMilliseconds(cto));
        }
        int rto = getResponseTimeout();
        if (rto > 0) {
            connCfg.setSocketTimeout(Timeout.ofMilliseconds(rto));
        } else if (SO_TIMEOUT > 0) {
            connCfg.setSocketTimeout(Timeout.ofMilliseconds(SO_TIMEOUT));
        }

        PoolingHttpClientConnectionManagerBuilder cmBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setDnsResolver(resolver)
                .setDefaultConnectionConfig(connCfg.build());

        if (this.testElement.isConcurrentDwn()) {
            try {
                int maxConcurrentDownloads = Integer.parseInt(this.testElement.getConcurrentPool());
                cmBuilder.setMaxConnPerRoute(Math.max(maxConcurrentDownloads, 2));
            } catch (NumberFormatException nfe) {
                // sampler will log
            }
        }

        PoolingHttpClientConnectionManager connManager = cmBuilder.build();

        HttpClientBuilder builder = HttpClients.custom()
                .setConnectionManager(connManager)
                .setConnectionManagerShared(false)
                .disableCookieManagement()
                .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(RETRY_COUNT, TimeValue.ofSeconds(1L)))
                .setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE)
                .setRoutePlanner(new DefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE) {
                    @Override
                    protected InetAddress determineLocalAddress(HttpHost firstHop, HttpContext context)
                            throws HttpException {
                        InetAddress addr = (InetAddress) context.getAttribute(CONTEXT_LOCAL_ADDRESS);
                        if (addr != null) {
                            return addr;
                        }
                        return localAddress;
                    }
                });

        if (DISABLE_DEFAULT_UA) {
            builder.disableDefaultUserAgent();
        }

        if (IDLE_TIMEOUT > 0) {
            builder.setKeepAliveStrategy((response, context) -> {
                TimeValue duration = DefaultConnectionKeepAliveStrategy.INSTANCE
                        .getKeepAliveDuration(response, context);
                if (duration == null || duration.toMilliseconds() <= 0) {
                    return TimeValue.ofMilliseconds(IDLE_TIMEOUT);
                }
                return duration;
            });
        }

        if (key.hasProxy) {
            HttpHost proxy = new HttpHost(key.proxyScheme, key.proxyHost, key.proxyPort);
            builder.setProxy(proxy);
            if (!key.proxyUser.isEmpty()) {
                BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(key.proxyHost, key.proxyPort),
                        new UsernamePasswordCredentials(key.proxyUser, key.proxyPass.toCharArray()));
                builder.setDefaultCredentialsProvider(credsProvider);
            }
        }

        return new ClientState(builder.build(), connManager);
    }

    private DnsResolver createDnsResolver() {
        final DNSCacheManager dnsCacheManager = this.testElement.getDNSResolver();
        if (dnsCacheManager == null) {
            return SystemDefaultDnsResolver.INSTANCE;
        }
        return new DnsResolver() {
            @Override
            public InetAddress[] resolve(String host) throws UnknownHostException {
                return dnsCacheManager.resolve(host);
            }

            @Override
            public String resolveCanonicalHostname(String host) throws UnknownHostException {
                final InetAddress[] addresses = resolve(host);
                if (addresses != null && addresses.length > 0) {
                    return addresses[0].getCanonicalHostName();
                }
                return SystemDefaultDnsResolver.INSTANCE.resolveCanonicalHostname(host);
            }
        };
    }

    private HttpClientKey createHttpClientKey(URL url) {
        final String host = url.getHost();
        String proxyScheme = getProxyScheme();
        String proxyHost = getProxyHost();
        int proxyPort = getProxyPortInt();
        String proxyPass = getProxyPass();
        String proxyUser = getProxyUser();

        boolean useStaticProxy = isStaticProxy(host);
        boolean useDynamicProxy = isDynamicProxy(proxyHost, proxyPort);
        boolean useProxy = useStaticProxy || useDynamicProxy;

        if (!useDynamicProxy) {
            proxyScheme = PROXY_SCHEME;
            proxyHost = PROXY_HOST;
            proxyPort = PROXY_PORT;
            proxyUser = PROXY_USER;
            proxyPass = PROXY_PASS;
        }
        return new HttpClientKey(url, useProxy, proxyScheme, proxyHost, proxyPort, proxyUser, proxyPass);
    }

    private static void resetStateIfNeeded(Map<HttpClientKey, ClientState> map) {
        if (Boolean.TRUE.equals(resetStateOnThreadGroupIteration.get())) {
            closeAndClear(map);
            ((JsseSSLManager) SSLManager.getInstance()).resetContext();
            resetStateOnThreadGroupIteration.set(Boolean.FALSE);
        }
    }

    private static void closeAndClear(Map<HttpClientKey, ClientState> map) {
        for (ClientState state : map.values()) {
            JOrphanUtils.closeQuietly(state.httpClient);
            JOrphanUtils.closeQuietly(state.connectionManager);
        }
        map.clear();
    }

    protected void setupRequest(URL url, HttpUriRequestBase httpRequest, HTTPSampleResult res,
            HttpClientContext clientContext) throws IOException {
        RequestConfig.Builder rCB = RequestConfig.custom();
        rCB.setRedirectsEnabled(getAutoRedirects());
        rCB.setMaxRedirects(HTTPSamplerBase.MAX_REDIRECTS);

        int rto = getResponseTimeout();
        if (rto > 0) {
            rCB.setResponseTimeout(Timeout.ofMilliseconds(rto));
        }

        httpRequest.setConfig(rCB.build());

        final InetAddress inetAddr = getIpSourceAddress();
        if (inetAddr != null) {
            clientContext.setAttribute(CONTEXT_LOCAL_ADDRESS, inetAddr);
        } else if (localAddress != null) {
            clientContext.setAttribute(CONTEXT_LOCAL_ADDRESS, localAddress);
        }

        if (getUseKeepAlive()) {
            httpRequest.setHeader(HTTPConstants.HEADER_CONNECTION, HTTPConstants.KEEP_ALIVE);
        } else {
            httpRequest.setHeader(HTTPConstants.HEADER_CONNECTION, HTTPConstants.CONNECTION_CLOSE);
        }

        setConnectionHeaders(httpRequest, url, getHeaderManager(), getCacheManager());
        String cookies = setConnectionCookie(httpRequest, url, getCookieManager());

        if (res != null) {
            if (cookies != null && !cookies.isEmpty()) {
                res.setCookies(cookies);
            } else {
                res.setCookies(getOnlyCookieFromHeaders(httpRequest));
            }
        }
    }

    protected String setConnectionCookie(HttpRequest request, URL url, CookieManager cookieManager) {
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(url);
            if (cookieHeader != null) {
                request.setHeader(HTTPConstants.HEADER_COOKIE, cookieHeader);
            }
        }
        return cookieHeader;
    }

    protected static void setConnectionHeaders(HttpUriRequestBase request, URL url,
            HeaderManager headerManager, CacheManager cacheManager) {
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                for (JMeterProperty jMeterProperty : headers) {
                    org.apache.jmeter.protocol.http.control.Header header =
                            (org.apache.jmeter.protocol.http.control.Header) jMeterProperty.getObjectValue();
                    String headerName = header.getName();
                    if (!HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(headerName)) {
                        String headerValue = header.getValue();
                        if (HTTPConstants.HEADER_HOST.equalsIgnoreCase(headerName)) {
                            int port = getPortFromHostHeader(headerValue, url.getPort());
                            headerValue = headerValue.replaceFirst(":\\d+$", "");
                            if (port != -1 && port == url.getDefaultPort()) {
                                port = -1;
                            }
                            if (port == -1) {
                                request.addHeader(HEADER_HOST, headerValue);
                            } else {
                                request.addHeader(HEADER_HOST, headerValue + ":" + port);
                            }
                        } else {
                            request.addHeader(headerName, headerValue);
                        }
                    }
                }
            }
        }
        if (cacheManager != null) {
            applyCacheConditionalHeaders(request, url, cacheManager);
        }
    }

    /**
     * CacheManager HC4 setHeaders overload requires HC4 request types; adapt temporarily.
     */
    private static void applyCacheConditionalHeaders(HttpUriRequestBase request, URL url,
            CacheManager cacheManager) {
        org.apache.http.client.methods.HttpGet tmp =
                new org.apache.http.client.methods.HttpGet(url.toString());
        for (Header h : request.getHeaders()) {
            tmp.addHeader(h.getName(), h.getValue());
        }
        cacheManager.setHeaders(url, tmp);
        org.apache.http.Header ims = tmp.getFirstHeader(HTTPConstants.IF_MODIFIED_SINCE);
        if (ims != null) {
            request.setHeader(ims.getName(), ims.getValue());
        }
        org.apache.http.Header inm = tmp.getFirstHeader(HTTPConstants.IF_NONE_MATCH);
        if (inm != null) {
            request.setHeader(inm.getName(), inm.getValue());
        }
    }

    private static void saveCacheDetails(CacheManager cacheManager, ClassicHttpResponse response,
            HTTPSampleResult res) {
        org.apache.http.message.BasicHttpResponse tmp =
                new org.apache.http.message.BasicHttpResponse(
                        org.apache.http.HttpVersion.HTTP_1_1,
                        response.getCode(),
                        response.getReasonPhrase());
        for (Header h : response.getHeaders()) {
            tmp.addHeader(h.getName(), h.getValue());
        }
        cacheManager.saveDetails(tmp, res);
    }

    private static int getPortFromHostHeader(String hostHeaderValue, int defaultValue) {
        String[] hostParts = hostHeaderValue.split(":");
        if (hostParts.length > 1) {
            String portString = hostParts[hostParts.length - 1];
            if (PORT_PATTERN.matcher(portString).matches()) {
                return Integer.parseInt(portString);
            }
        }
        return defaultValue;
    }

    private static String getResponseHeaders(ClassicHttpResponse response) {
        Header[] rh = response.getHeaders();
        StringBuilder headerBuf = new StringBuilder(40 * (rh.length + 1));
        headerBuf.append(new StatusLine(response));
        headerBuf.append('\n');
        for (Header responseHeader : rh) {
            headerBuf.append(responseHeader.getName())
                    .append(": ")
                    .append(responseHeader.getValue())
                    .append('\n');
        }
        return headerBuf.toString();
    }

    private static String getAllHeadersExceptCookie(HttpRequest method) {
        return getFromHeadersMatchingPredicate(method, ALL_EXCEPT_COOKIE);
    }

    private static String getOnlyCookieFromHeaders(HttpRequest method) {
        String cookieHeader = getFromHeadersMatchingPredicate(method, ONLY_COOKIE).trim();
        if (!cookieHeader.isEmpty()) {
            return cookieHeader.substring(HTTPConstants.HEADER_COOKIE_IN_REQUEST.length()).trim();
        }
        return "";
    }

    private static String getFromHeadersMatchingPredicate(HttpRequest method,
            Predicate<? super String> predicate) {
        if (method != null) {
            StringBuilder hdrs = new StringBuilder(150);
            Header[] requestHeaders = method.getHeaders();
            for (Header requestHeader : requestHeaders) {
                if (predicate.test(requestHeader.getName())) {
                    hdrs.append(requestHeader.getName())
                            .append(": ")
                            .append(requestHeader.getValue())
                            .append('\n');
                }
            }
            return hdrs.toString();
        }
        return "";
    }

    private static org.apache.jmeter.protocol.http.control.Header[] toJMeterHeaders(Header[] headers) {
        if (headers == null || headers.length == 0) {
            return new org.apache.jmeter.protocol.http.control.Header[0];
        }
        org.apache.jmeter.protocol.http.control.Header[] result =
                new org.apache.jmeter.protocol.http.control.Header[headers.length];
        for (int i = 0; i < headers.length; i++) {
            result[i] = new org.apache.jmeter.protocol.http.control.Header(
                    headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    protected String setupHttpEntityEnclosingRequestData(HttpUriRequestBase entityEnclosingRequest)
            throws IOException {
        StringBuilder postedBody = new StringBuilder(1000);
        HTTPFileArg[] files = getHTTPFiles();
        final String contentEncoding = getContentEncoding();
        Charset charset = Charset.forName(contentEncoding);

        if (getUseMultipart()) {
            if (entityEnclosingRequest.containsHeader(HTTPConstants.HEADER_CONTENT_TYPE)) {
                entityEnclosingRequest.removeHeaders(HTTPConstants.HEADER_CONTENT_TYPE);
            }
            boolean doBrowserCompatibleMultipart = getDoBrowserCompatibleMultipart();
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.setCharset(charset);
            if (doBrowserCompatibleMultipart) {
                multipartEntityBuilder.setMode(HttpMultipartMode.LEGACY);
            } else {
                multipartEntityBuilder.setMode(HttpMultipartMode.EXTENDED);
            }
            for (JMeterProperty jMeterProperty : getArguments()) {
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                String parameterName = arg.getName();
                if (arg.isSkippable(parameterName)) {
                    continue;
                }
                ContentType contentType;
                if (arg.getContentType().indexOf(';') >= 0) {
                    contentType = ContentType.parse(arg.getContentType());
                } else {
                    contentType = ContentType.create(arg.getContentType(), charset);
                }
                multipartEntityBuilder.addTextBody(parameterName, arg.getValue(), contentType);
            }
            for (HTTPFileArg file : files) {
                File reservedFile = FileServer.getFileServer().getResolvedFile(file.getPath());
                multipartEntityBuilder.addBinaryBody(
                        file.getParamName(),
                        reservedFile,
                        ContentType.parse(file.getMimeType()),
                        reservedFile.getName());
            }
            HttpEntity entity = multipartEntityBuilder.build();
            entityEnclosingRequest.setEntity(entity);
            writeEntityToSB(postedBody, entity, contentEncoding);
        } else {
            Header contentTypeHeader = entityEnclosingRequest.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            boolean hasContentTypeHeader = contentTypeHeader != null
                    && contentTypeHeader.getValue() != null
                    && !contentTypeHeader.getValue().isEmpty();

            if (!hasArguments() && getSendFileAsPostBody()) {
                HTTPFileArg file = files[0];
                if (!hasContentTypeHeader) {
                    if (file.getMimeType() != null && !file.getMimeType().isEmpty()) {
                        entityEnclosingRequest.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
                    } else if (ADD_CONTENT_TYPE_TO_POST_IF_MISSING) {
                        entityEnclosingRequest.setHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                                HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }
                FileEntity fileRequestEntity = new FileEntity(
                        FileServer.getFileServer().getResolvedFile(file.getPath()),
                        ContentType.DEFAULT_BINARY);
                entityEnclosingRequest.setEntity(fileRequestEntity);
                postedBody.append("<actual file content, not shown here>");
            } else if (getSendParameterValuesAsPostBody()) {
                if (!hasContentTypeHeader) {
                    HTTPFileArg file = files.length > 0 ? files[0] : null;
                    if (file != null && file.getMimeType() != null && !file.getMimeType().isEmpty()) {
                        entityEnclosingRequest.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
                    } else if (ADD_CONTENT_TYPE_TO_POST_IF_MISSING) {
                        entityEnclosingRequest.setHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                                HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }
                StringBuilder postBody = new StringBuilder();
                for (JMeterProperty jMeterProperty : getArguments()) {
                    HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                    postBody.append(arg.getEncodedValue(contentEncoding));
                }
                StringEntity requestEntity = new StringEntity(postBody.toString(),
                        ContentType.TEXT_PLAIN.withCharset(charset));
                entityEnclosingRequest.setEntity(requestEntity);
                postedBody.append(postBody);
            } else {
                if (!hasContentTypeHeader && ADD_CONTENT_TYPE_TO_POST_IF_MISSING) {
                    entityEnclosingRequest.setHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                            HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                }
                UrlEncodedFormEntity entity = createUrlEncodedFormEntity(contentEncoding);
                entityEnclosingRequest.setEntity(entity);
                writeEntityToSB(postedBody, entity, contentEncoding);
            }
        }
        return postedBody.toString();
    }

    private UrlEncodedFormEntity createUrlEncodedFormEntity(final String urlContentEncoding)
            throws UnsupportedEncodingException {
        PropertyIterator args = getArguments().iterator();
        List<NameValuePair> nvps = new ArrayList<>();
        while (args.hasNext()) {
            HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
            String parameterName = arg.getName();
            if (arg.isSkippable(parameterName)) {
                continue;
            }
            String parameterValue = arg.getValue();
            if (!arg.isAlwaysEncoded()) {
                parameterName = URLDecoder.decode(parameterName, urlContentEncoding);
                parameterValue = URLDecoder.decode(parameterValue, urlContentEncoding);
            }
            nvps.add(new BasicNameValuePair(parameterName, parameterValue));
        }
        return new UrlEncodedFormEntity(nvps, Charset.forName(urlContentEncoding));
    }

    private static void writeEntityToSB(final StringBuilder postedBody, final HttpEntity entity,
            final String contentEncoding) throws IOException {
        if (entity.isRepeatable()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            entity.writeTo(bos);
            bos.flush();
            postedBody.append(bos.toString(contentEncoding != null
                    ? contentEncoding
                    : SampleResult.DEFAULT_HTTP_ENCODING));
            bos.close();
        } else {
            postedBody.append("<Entity was not repeatable, cannot view what was sent>");
        }
    }

    private static void saveConnectionCookies(ClassicHttpResponse method, URL u,
            CookieManager cookieManager) {
        if (cookieManager != null) {
            Header[] hdrs = method.getHeaders(HTTPConstants.HEADER_SET_COOKIE);
            for (Header hdr : hdrs) {
                cookieManager.addCookieFromHeader(hdr.getValue(), u);
            }
        }
    }

    @Override
    protected void notifyFirstSampleAfterLoopRestart() {
        log.debug("notifyFirstSampleAfterLoopRestart called "
                + "with config(httpclient.reset_state_on_thread_group_iteration={})",
                RESET_STATE_ON_THREAD_GROUP_ITERATION);
        JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
        if (jMeterVariables.isSameUserOnNextIteration()) {
            resetStateOnThreadGroupIteration.set(false);
        } else {
            resetStateOnThreadGroupIteration.set(RESET_STATE_ON_THREAD_GROUP_ITERATION);
        }
    }

    @Override
    protected void threadFinished() {
        log.debug("Thread Finished");
        closeThreadLocalConnections();
    }

    private static void closeThreadLocalConnections() {
        Map<HttpClientKey, ClientState> map = HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY.get();
        if (map != null) {
            closeAndClear(map);
        }
    }

    @Override
    public boolean interrupt() {
        HttpUriRequestBase request = currentRequest;
        if (request != null) {
            currentRequest = null;
            try {
                request.cancel();
            } catch (UnsupportedOperationException e) {
                log.warn("Could not abort pending request", e);
            }
        }
        return request != null;
    }

    private static final class ClientState {
        private final CloseableHttpClient httpClient;
        private final PoolingHttpClientConnectionManager connectionManager;

        private ClientState(CloseableHttpClient httpClient,
                PoolingHttpClientConnectionManager connectionManager) {
            this.httpClient = httpClient;
            this.connectionManager = connectionManager;
        }
    }

    private static final class HttpClientKey {
        private final String protocol;
        private final String authority;
        private final boolean hasProxy;
        private final String proxyScheme;
        private final String proxyHost;
        private final int proxyPort;
        private final String proxyUser;
        private final String proxyPass;
        private final int hashCode;

        public HttpClientKey(URL url, boolean hasProxy, String proxyScheme, String proxyHost,
                int proxyPort, String proxyUser, String proxyPass) {
            this.protocol = url.getProtocol();
            this.authority = url.getAuthority();
            this.hasProxy = hasProxy;
            this.proxyScheme = proxyScheme;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUser = proxyUser;
            this.proxyPass = proxyPass;
            this.hashCode = computeHash();
        }

        private int computeHash() {
            int hash = 17;
            hash = hash * 31 + (hasProxy ? 1 : 0);
            if (hasProxy) {
                hash = hash * 31 + Objects.hashCode(proxyScheme);
                hash = hash * 31 + Objects.hashCode(proxyHost);
                hash = hash * 31 + proxyPort;
                hash = hash * 31 + Objects.hashCode(proxyUser);
                hash = hash * 31 + Objects.hashCode(proxyPass);
            }
            hash = hash * 31 + Objects.hashCode(protocol);
            hash = hash * 31 + Objects.hashCode(authority);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof HttpClientKey)) {
                return false;
            }
            HttpClientKey other = (HttpClientKey) obj;
            if (!Objects.equals(authority, other.authority)
                    || !Objects.equals(protocol, other.protocol)
                    || hasProxy != other.hasProxy) {
                return false;
            }
            if (!hasProxy) {
                return true;
            }
            return this.proxyPort == other.proxyPort
                    && Objects.equals(proxyScheme, other.proxyScheme)
                    && Objects.equals(proxyHost, other.proxyHost)
                    && Objects.equals(proxyUser, other.proxyUser)
                    && Objects.equals(proxyPass, other.proxyPass);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(protocol).append("://").append(authority);
            if (hasProxy) {
                sb.append(" via ").append(proxyUser).append('@')
                        .append(proxyScheme).append("://")
                        .append(proxyHost).append(':').append(proxyPort);
            }
            return sb.toString();
        }
    }
}
