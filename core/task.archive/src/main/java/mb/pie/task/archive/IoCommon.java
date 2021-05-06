package mb.pie.task.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class IoCommon {
    static void copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        int n;
        while(-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
