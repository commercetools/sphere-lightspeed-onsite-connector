package io.sphere.lightspeed.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import com.ning.http.client.*;
import com.ning.http.client.cookie.Cookie;
import io.sphere.sdk.http.HttpException;
import io.sphere.sdk.http.HttpRequest;
import io.sphere.sdk.http.HttpResponse;
import io.sphere.sdk.http.StringHttpRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import static io.sphere.lightspeed.client.LightSpeedHttpClient.SESSION_COOKIE;
import static java.util.Arrays.asList;

class NingAsyncHttpClientAdapterActor extends UntypedActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NingAsyncHttpClientAdapterActor.class);
    private final AsyncHttpClient asyncHttpClient;
    private Optional<Cookie> sessionCookieOpt;
    private List<RequestMessage> requestMsgList = new LinkedList<>();
    private boolean isFirstTime;

    private NingAsyncHttpClientAdapterActor(final AsyncHttpClient asyncHttpClient, final Optional<Cookie> sessionCookie) {
        this.asyncHttpClient = asyncHttpClient;
        this.sessionCookieOpt = sessionCookie;
        this.isFirstTime = !sessionCookieOpt.isPresent();
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof RequestMessage) {
            handleRequestMessage((RequestMessage) message);
        } else if (message instanceof SessionAvailableMessage) {
            handleSessionAvailable((SessionAvailableMessage) message);
        } else {
            unhandled(message);
        }
    }

    /**
     * Create Props for a NingAsyncHttpClientAdapterActor.
     * @return a Props for creating this actor.
     */
    public static Props props(final AsyncHttpClient asyncHttpClient, final Optional<Cookie> sessionCookie) {
        return Props.create(new Creator<NingAsyncHttpClientAdapterActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public NingAsyncHttpClientAdapterActor create() throws Exception {
                return new NingAsyncHttpClientAdapterActor(asyncHttpClient, sessionCookie);
            }
        });
    }

    private void handleRequestMessage(final RequestMessage requestMsg) {
        if (sessionCookieOpt.isPresent() || isFirstTime) {
            final CompletableFuture<Response> response = execute(requestMsg.httpRequest, sessionCookieOpt);
            transferFutureResult(response, requestMsg);
            if (!sessionCookieOpt.isPresent()) {
                transferSession(response);
            }
            isFirstTime = false;
        } else {
            requestMsgList.add(requestMsg);
        }
    }

    private void handleSessionAvailable(final SessionAvailableMessage sessionMsg) {
        this.sessionCookieOpt = Optional.of(sessionMsg.sessionCookie);
        this.requestMsgList.forEach(requestMsg -> self().tell(requestMsg, self()));
        this.requestMsgList.clear();
    }

    private void transferSession(final CompletableFuture<Response> responseFuture) {
        responseFuture.whenCompleteAsync((response, throwable) -> {
            // TODO Handle error when session is absent
            final Cookie sessionCookie = parseSession(response).get();
            final SessionAvailableMessage msg = new SessionAvailableMessage(sessionCookie);
            self().tell(msg, self());
        });
    }

    private void transferFutureResult(final CompletableFuture<Response> responseFuture, final RequestMessage msg) {
        responseFuture.whenCompleteAsync((response, throwable) -> {
            final HttpResponse httpResponse = parseResponse(msg.httpRequest, response);
            LOGGER.debug("Response " + httpResponse);
            if (throwable == null) {
                msg.httpResponseFuture.complete(httpResponse);
            } else {
                msg.httpResponseFuture.completeExceptionally(throwable);
            }
        });
    }

    private CompletableFuture<Response> execute(final HttpRequest httpRequest, final Optional<Cookie> sessionCookie) {
        final Request request = asNingRequest(httpRequest, sessionCookie);
        return wrap(asyncHttpClient.executeRequest(request));
    }

    private Request asNingRequest(final HttpRequest request, final Optional<Cookie> cookies) {
        final RequestBuilder builder = new RequestBuilder()
                .setUrl(request.getUrl())
                .setMethod(request.getHttpMethod().toString())
                .setCookies(cookies.map(Arrays::asList).orElse(asList()));

        request.getHeaders().getHeadersAsMap().forEach(builder::setHeader);

        request.getBody().ifPresent(body -> {
            if (body instanceof StringHttpRequestBody) {
                final String bodyAsString = ((StringHttpRequestBody) body).getString();
                builder.setBodyEncoding(StandardCharsets.UTF_8.name()).setBody(bodyAsString);
            }
        });
        return builder.build();
    }

    /**
     * Creates a {@link java.util.concurrent.CompletableFuture} from a {@link com.ning.http.client.ListenableFuture}.
     * @param listenableFuture the future of the ning library
     * @return the Java 8 future implementation
     */
    private static CompletableFuture<Response> wrap(final ListenableFuture<Response> listenableFuture) {
        final CompletableFuture<Response> result = new CompletableFuture<>();
        final Runnable listener = () -> {
            try {
                result.complete(listenableFuture.get());
            } catch (final InterruptedException | ExecutionException e) {
                result.completeExceptionally(e.getCause());
            }
        };
        listenableFuture.addListener(listener, ForkJoinPool.commonPool());
        return result;
    }

    private Optional<Cookie> parseSession(final Response response) {
        final Predicate<Cookie> onlySessionCookies = c -> c.getName().equals(SESSION_COOKIE);
        return response.getCookies().stream().filter(onlySessionCookies).findFirst();
    }

    private HttpResponse parseResponse(final HttpRequest httpRequest, final Response response) {
        final byte[] responseBodyAsBytes = getResponseBodyAsBytes(response);
        final Optional<byte[]> body = responseBodyAsBytes.length > 0 ? Optional.of(responseBodyAsBytes) : Optional.empty();
        return HttpResponse.of(response.getStatusCode(), body, Optional.of(httpRequest));
    }

    private byte[] getResponseBodyAsBytes(final Response response) {
        try {
            return response.getResponseBodyAsBytes();
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    static final class RequestMessage {
        private final CompletableFuture<HttpResponse> httpResponseFuture;
        private final HttpRequest httpRequest;

        RequestMessage(final CompletableFuture<HttpResponse> httpResponseFuture, final HttpRequest httpRequest) {
            this.httpResponseFuture = httpResponseFuture;
            this.httpRequest = httpRequest;
        }

        @Override
        public String toString() {
            return "RequestMessage{" +
                    "httpResponseFuture=" + httpResponseFuture +
                    ", httpRequest=" + httpRequest +
                    '}';
        }
    }

    private static final class SessionAvailableMessage {
        private final Cookie sessionCookie;

        SessionAvailableMessage(final Cookie sessionCookie) {
            this.sessionCookie = sessionCookie;
        }

        @Override
        public String toString() {
            return "SessionAvailableMessage{" +
                    "sessionCookie=" + sessionCookie +
                    '}';
        }
    }
}
