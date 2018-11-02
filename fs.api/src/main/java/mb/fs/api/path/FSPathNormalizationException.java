package mb.fs.api.path;

import java.util.Collection;

public class FSPathNormalizationException extends Exception {
    public FSPathNormalizationException(Collection<String> segments) {
        super(createMessage(segments));
    }

    private static String createMessage(Collection<String> segments) {
        // TODO: create better message.
        return "Failed to normalize; '..' segment was found without a preceding segment";
    }
}
