package mb.pie.vfs.access;

import mb.pie.vfs.path.PPath;

public interface DirAccess {
    void readDir(PPath dir);

    void writeDir(PPath dir);
}
