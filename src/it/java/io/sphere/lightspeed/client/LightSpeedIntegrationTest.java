package io.sphere.lightspeed.client;

import com.ning.http.client.cookie.Cookie;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.sphere.lightspeed.client.LightSpeedHttpClient.SESSION_COOKIE;

public abstract class LightSpeedIntegrationTest {
    private static final String LIGHTSPEED_IT_APP_URL = "LIGHTSPEED_IT_APP_URL";
    private static final String LIGHTSPEED_IT_APP_ID = "LIGHTSPEED_IT_APP_ID";
    private static final String LIGHTSPEED_IT_APP_PRIVATE_ID = "LIGHTSPEED_IT_APP_PRIVATE_ID";
    private static final String LIGHTSPEED_IT_USERNAME = "LIGHTSPEED_IT_USERNAME";
    private static final String LIGHTSPEED_IT_PASSWORD = "LIGHTSPEED_IT_PASSWORD";
    private static final String SESSION_ID = "2wxFJQIUp5rT-pCRdniHJO5GJWxSRU1PVEVfU0VTU0lPTj00YWM2YjZiMS04ZWZiLTQzMDktODM0Ny02OTI3ZGI5NzEzNTg=";
    private static volatile int threadCountAtStart;
    private static volatile NingAsyncHttpClientAdapter client;

    protected synchronized static LightSpeedClient client() {
        return LightSpeedClientFactory.of().createClient(config(), ningClient());
    }

    protected synchronized static NingAsyncHttpClientAdapter ningClient() {
        if (client == null) {
            final Cookie sessionCookie = Cookie.newValidCookie(SESSION_COOKIE, SESSION_ID, null, SESSION_ID, "/", -1, -1, true, false);
            client = NingAsyncHttpClientAdapter.of(username(), password(), Optional.of(sessionCookie));
        }
        return client;
    }

    protected static LightSpeedConfig config() {
        return LightSpeedConfig.of(appUrl(), appId(), appPrivateId(), username(), password());
    }

    private static String getValueForEnvVar(final String key) {
        return Optional.ofNullable(System.getenv(key))
                .orElseThrow(() -> new RuntimeException(
                        "Missing environment variable " + key + ", please provide the following environment variables:\n" +
                                "export " + LIGHTSPEED_IT_APP_URL + "=\"https://localhost:9630/api\"\n" +
                                "export " + LIGHTSPEED_IT_USERNAME + "=\"YOUR username\"\n" +
                                "export " + LIGHTSPEED_IT_PASSWORD + "=\"YOUR password\"\n" +
                                "export " + LIGHTSPEED_IT_APP_ID + "=\"YOUR app ID\"\n" +
                                "export " + LIGHTSPEED_IT_APP_PRIVATE_ID + "=\"YOUR app private ID\""));
    }

    protected static String appUrl() {
        return getValueForEnvVar(LIGHTSPEED_IT_APP_URL);
    }

    protected static String appId() {
        return getValueForEnvVar(LIGHTSPEED_IT_APP_ID);
    }

    protected static String appPrivateId() {
        return getValueForEnvVar(LIGHTSPEED_IT_APP_PRIVATE_ID);
    }

    protected static String username() {
        return getValueForEnvVar(LIGHTSPEED_IT_USERNAME);
    }

    protected static String password() {
        return getValueForEnvVar(LIGHTSPEED_IT_PASSWORD);
    }

    protected static <T> T execute(final EndpointRequest<T> request) {
        return handleExecute(client().execute(request));
    }

    protected static <T> T execute(final ResourceRequest<T> request) {
        return handleExecute(client().execute(request));
    }

    private static <T> T handleExecute(final CompletableFuture<T> result) {
        try {
            return result.get();
        } catch (final Exception e) {
            if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new ClientException(e);
            }
        }
    }

    @BeforeClass
    public synchronized static void setup() {
        threadCountAtStart = countThreads();
    }

    @AfterClass
    public synchronized static void shutdownClient() {
        if (client != null) {
            client.close();
            client = null;
            final int threadsNow = countThreads();
            final long bufferForGcThreadAndSbt = 1;
            final long allowedThreadCount = threadCountAtStart + bufferForGcThreadAndSbt;
            if (threadsNow > allowedThreadCount) {
                throw new RuntimeException("Thread leak! After client shutdown created threads are still alive. Threads now: " + threadsNow + " Threads before: " + threadCountAtStart);
            }
        }
    }

    protected static int countThreads() {
        return Thread.activeCount();
    }
}

