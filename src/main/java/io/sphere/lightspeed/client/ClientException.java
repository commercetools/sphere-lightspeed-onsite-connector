package io.sphere.lightspeed.client;

public class ClientException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ClientException() {
    }

    public ClientException(final String message) {
        super(message);
    }

    public ClientException(final Throwable cause) {
        super(cause);
    }

    public ClientException(final HttpResponse httpResponse) {
        super(httpResponse.getStatusCode() + ": " + httpResponse.getResponseBody().map(String::new).orElse("Empty body"));
    }
}
