package me.gnoyes;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

public class SerializationUtils {
    public static void serialize(Object obj, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(outputFile.toPath()))) {
            outputStream.writeObject(obj);
        }
    }

    public static Object deserialize(String inputPath) throws IOException, ClassNotFoundException {
        File inputFile = new File(inputPath);
        try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(inputFile.toPath()))) {
            return inputStream.readObject();
        }
    }
}
