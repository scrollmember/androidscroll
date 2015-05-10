package jp.ac.tokushima_u.is.ll.io;

import org.xmlpull.v1.XmlPullParser;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Abstract class that handles reading and parsing an {@link XmlPullParser} into
 * a set of {@link ContentProviderOperation}. It catches recoverable network
 * exceptions and rethrows them as {@link HandlerException}. Any local
 * {@link ContentProvider} exceptions are considered unrecoverable.
 * <p>
 * This class is only designed to handle simple one-way synchronization.
 */
public abstract class JsonHandler {
    private final String mAuthority;

    public JsonHandler(String authority) {
        mAuthority = authority;
    }

    /**
     * Parse the given {@link XmlPullParser}, turning into a series of
     * {@link ContentProviderOperation} that are immediately applied using the
     * given {@link ContentResolver}.
     */
    public void parseAndApply(ContentResolver resolver)
            throws HandlerException {
        try {
            final ArrayList<ContentProviderOperation> batch = parse(resolver);
            if(batch!=null)
            	resolver.applyBatch(mAuthority, batch);
        } catch (HandlerException e) {
            throw e;
        } catch (IOException e) {
            throw new HandlerException("Problem reading response", e);
        } catch (RemoteException e) {
            // Failed binder transactions aren't recoverable
            throw new RuntimeException("Problem applying batch operation", e);
        } catch (OperationApplicationException e) {
            // Failures like constraint violation aren't recoverable
            // TODO: write unit tests to exercise full provider
            // TODO: consider catching version checking asserts here, and then
            // wrapping around to retry parsing again.
            throw new RuntimeException("Problem applying batch operation", e);
        }
    }

    /**
     * Parse the given {@link XmlPullParser}, returning a set of
     * {@link ContentProviderOperation} that will bring the
     * {@link ContentProvider} into sync with the parsed data.
     */
    public abstract ArrayList<ContentProviderOperation> parse(ContentResolver resolver) throws IOException;

    /**
     * General {@link IOException} that indicates a problem occured while
     * parsing or applying an {@link XmlPullParser}.
     */
    public static class HandlerException extends IOException {
        public HandlerException(String message) {
            super(message);
        }

        public HandlerException(String message, Throwable cause) {
            super(message);
            initCause(cause);
        }

        @Override
        public String toString() {
            if (getCause() != null) {
                return getLocalizedMessage() + ": " + getCause();
            } else {
                return getLocalizedMessage();
            }
        }
    }
}
