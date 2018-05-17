package mb.pie.vfs.access;

import mb.pie.vfs.path.PPath;

public interface FileAccess {
    void readFile(PPath file);

    void writeFile(PPath file);
}
