package com.example.demo.config;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {
    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);
    @Value("${httpConnection.max:100}")
    private int maxHttpConnection;
    @Value("${httpConnection.perRoute:30}")
    private int maxPerRouteConnection;
    @Value("${httpConnection.validateInactivity:3000}")
    private int validateInactivity;
    @Value("${httpConnection.ssl.trustAllCertificates:false}")
    private boolean trustAllCertificates;
    @Value("${httpConnection.timeout.socket:5000}")
    private int socketTimeout;
    @Value("${httpConnection.timeout.connect:1000}")
    private int connectTimeout;
    @Value("${httpConnection.ttlSec:600}")
    private long maxTtl;
    @Value("${httpConnection.timeout.request:1000}")
    private int requestTimeout;
    @Value("${httpConnection.monitor.enabled:false}")
    private boolean connectionManagerMonitor;
    @Value("${httpConnection.monitor.intervalSec:30}")
    private int monitorIntervalSec;
    @Value("${httpConnection.5XX.retry.count:3}")
    private int serviceErrorRetryCount;
    @Value("${httpConnection.5XX.retry.interval:5}")
    private int retryInterval;
    @Value("${httpConnection.connect.retry.count:3}")
    private int connectionRetryCount;

    public RestTemplateConfig() {
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        log.info("HttpConnectionPool max({}) perRoute({}) validateInactivity({}) timeout.socket({}) timeout.connect({}) timeout.request({}) ttlSec({}) monitor.enabled({}) monitor.intervalSec({}) 5XX.retry.count({}), 5XX.retry.interval({}) connect.retry.count({}) ssl.trustAllCertificates({})", new Object[]{this.maxHttpConnection, this.maxPerRouteConnection, this.validateInactivity, this.socketTimeout, this.connectTimeout, this.requestTimeout, this.maxTtl, this.connectionManagerMonitor, this.monitorIntervalSec, this.serviceErrorRetryCount, this.retryInterval, this.connectionRetryCount, this.trustAllCertificates});
        return new HttpComponentsClientHttpRequestFactory(this.httpClient());
    }

    private CloseableHttpClient httpClient() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        this.createConnectionPool(httpClientBuilder);
        this.attachRetryHandlers(httpClientBuilder);
        this.setTimeouts(httpClientBuilder);
        return httpClientBuilder.build();
    }

    private void attachRetryHandlers(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder.setRetryHandler(this.retryHandler()).setServiceUnavailableRetryStrategy(this.serviceUnAvailableRetryStrategy());
    }

    private void setTimeouts(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(this.connectTimeout).setSocketTimeout(this.socketTimeout).setConnectionRequestTimeout(this.requestTimeout).build());
    }

    private SSLConnectionSocketFactory sslSocketFactory() {
        try {
            SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial((KeyStore)null, (certificate, authType) -> {
                return true;
            }).build();
            return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (Exception var2) {
            log.info("failed to create socket connection factory", var2);
            return null;
        }
    }

    private PoolingHttpClientConnectionManager connectionManager() {
        PoolingHttpClientConnectionManager connectionManager;
        if (this.trustAllCertificates) {
            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
            registryBuilder.register("http", new PlainConnectionSocketFactory());
            SSLConnectionSocketFactory sslConnectionSocketFactory = this.sslSocketFactory();
            if (sslConnectionSocketFactory != null) {
                registryBuilder.register("https", sslConnectionSocketFactory);
            }

            connectionManager = new PoolingHttpClientConnectionManager(registryBuilder.build(), (HttpConnectionFactory)null, (SchemePortResolver)null, (DnsResolver)null, this.maxTtl, TimeUnit.SECONDS);
        } else {
            connectionManager = new PoolingHttpClientConnectionManager(this.maxTtl, TimeUnit.SECONDS);
        }

        connectionManager.setMaxTotal(this.maxHttpConnection);
        connectionManager.setDefaultMaxPerRoute(this.maxPerRouteConnection);
        connectionManager.setValidateAfterInactivity(this.validateInactivity);
        return connectionManager;
    }

    private void createConnectionPool(HttpClientBuilder httpClientBuilder) {
        PoolingHttpClientConnectionManager connectionManager = this.connectionManager();
        httpClientBuilder.setConnectionManager(connectionManager);
        if (this.connectionManagerMonitor) {
            this.watchConnectionManager(connectionManager);
        }

    }

    private void watchConnectionManager(PoolingHttpClientConnectionManager connectionManager) {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            log.info("HttpConnectionPool({})", connectionManager.getTotalStats().toString());
            connectionManager.getRoutes().forEach((route) -> {
                log.info("HttpConnectionPool {} for ({})", connectionManager.getMaxPerRoute(route), route.toString());
            });
        }, (long)this.monitorIntervalSec, (long)this.monitorIntervalSec, TimeUnit.SECONDS);
    }

    private ServiceUnavailableRetryStrategy serviceUnAvailableRetryStrategy() {
        return new ServiceUnavailableRetryStrategy() {
            public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
                boolean isRetry = false;

                try {
                    if (HttpStatus.Series.SERVER_ERROR.equals(HttpStatus.Series.valueOf(response.getStatusLine().getStatusCode()))) {
                        isRetry = true;
                    }
                } catch (Throwable var6) {
                    RestTemplateConfig.log.error("Exception occurred while determining the serviceUnAvailableRetryStrategy, current executionCount({})", executionCount, var6);
                }

                isRetry = isRetry && executionCount <= RestTemplateConfig.this.serviceErrorRetryCount;
                if (executionCount > 1) {
                    RestTemplateConfig.log.trace("service unavailable isRetry({}) count({}) ", isRetry, executionCount);
                }

                return isRetry;
            }

            public long getRetryInterval() {
                return (long)RestTemplateConfig.this.retryInterval;
            }
        };
    }

    private HttpRequestRetryHandler retryHandler() {
        return new DefaultHttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                RestTemplateConfig.log.trace("retry executionCount({})", executionCount);
                return executionCount <= RestTemplateConfig.this.connectionRetryCount;
            }
        };
    }

    public static void setErrorHandler(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                HttpStatus.Series series = clientHttpResponse.getStatusCode().series();
                return HttpStatus.Series.CLIENT_ERROR.equals(series) || HttpStatus.Series.SERVER_ERROR.equals(series);
            }

            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
                BufferedReader br = new BufferedReader(new InputStreamReader(clientHttpResponse.getBody()));
                StringBuilder sb = new StringBuilder();
                br.lines().forEach(sb::append);
                sb.append(" HEADER ");
                clientHttpResponse.getHeaders().keySet().forEach((key) -> {
                    sb.append(key).append("=").append(clientHttpResponse.getHeaders().get(key)).append(";");
                });
                RestTemplateConfig.log.warn(sb.toString());
                throw new IOException(sb.toString());
            }
        });
    }
}
