package club.revived.commons.serialization;

import java.io.*;

/**
 * FileSerializer
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class FileSerializer {

    /**
     * Serializes a Serializable object to the specified file path.
     *
     * @param object   the object to serialize
     * @param filePath the destination file path
     * @throws IOException if an I/O error occurs
     */
    public static void serialize(final Serializable object, final String filePath) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("Object to serialize must not be null");
        }

        try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(object);
            oos.flush();
        }
    }

    /**
     * Deserializes an object from the specified file path.
     *
     * @param filePath the source file path
     * @param <T>      the expected object type
     * @return the deserialized object
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class definition is not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(final String filePath)
            throws IOException, ClassNotFoundException {

        try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
            return (T) ois.readObject();
        }
    }
}
