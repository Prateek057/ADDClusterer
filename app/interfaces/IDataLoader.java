package interfaces;

import java.io.IOException;

public abstract interface IDataLoader<E> {
    E loadData(String path) throws IOException;
}
