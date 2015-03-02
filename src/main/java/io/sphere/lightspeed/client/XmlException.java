package io.sphere.lightspeed.client;

public class XmlException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    public XmlException() {
    }

    public XmlException(final Throwable cause) {
        super(cause);
    }

    public XmlException(final String message) {
        super(message);
    }

    public XmlException(final byte[] input, final Throwable cause) {
        super("Cannot parse: " + new String(input), cause);
    }

    public XmlException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public XmlException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
