package jp.ac.tokushima_u.is.ll.io;

import java.io.IOException;
import jp.ac.tokushima_u.is.ll.io.JsonHandler.HandlerException;
import android.content.ContentResolver;

public class RemoteExecutor {
    private final ContentResolver mResolver;

    public RemoteExecutor(ContentResolver resolver) {
        mResolver = resolver;
    }

    public void execute(JsonHandler handler) throws HandlerException {
        try {
        	handler.parseAndApply(mResolver);
        } catch (HandlerException e) {
            throw e;
        } catch (IOException e) {
        }
    }
}
