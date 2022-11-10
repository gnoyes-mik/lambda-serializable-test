package me.gnoyes;

import java.io.IOException;
import java.io.Serializable;

import static me.gnoyes.SerializationUtils.deserialize;
import static me.gnoyes.SerializationUtils.serialize;

public class SerializableLambdaExpression implements Serializable {
    public final Runnable runnable = (Runnable & Serializable) () -> System.out.println("please serialize this message");

    public Runnable getRunnable() {
        return runnable;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        SerializableLambdaExpression object = new SerializableLambdaExpression();
        Runnable fn = object.getRunnable();

        System.out.print("직렬화 하기 전 Runnable 실행 > ");
        fn.run();

        // 직렬화
        String path = "./seri.txt";
        serialize(object, path);
        System.out.println("직렬화 완료 파일명:" + path);

        // 역직렬화
        SerializableLambdaExpression deserializedObject = (SerializableLambdaExpression) deserialize(path);

        System.out.print("역직렬화 후 Runnable 실행 > ");
        Runnable fn2 = deserializedObject.getRunnable();
        fn2.run();
    }
}


