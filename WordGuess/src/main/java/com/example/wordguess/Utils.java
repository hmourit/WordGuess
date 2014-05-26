package com.example.wordguess;

import android.content.res.TypedArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

/**
 * Created by hmourit on 5/12/14.
 */
public class Utils {
    public static Iterator<Integer> getCycleIterator(final TypedArray typedArray) {
        return new Iterator<Integer>() {

            private TypedArray ta = typedArray;
            private int currentIndex = 0;
            private int lastIndex = ta.length() - 1;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                Integer resourceId = ta.getResourceId(currentIndex, 0);
                if (currentIndex == lastIndex) currentIndex = 0;
                else currentIndex++;
                return resourceId;
            }

            @Override
            public void remove() {

            }
        };
    }


    /**
     * read a file and converting it to String using StringWriter
     */
    public static String fromInputStreamToString(InputStream inputStream) throws IOException {

        char[] buff = new char[1024];
        Writer stringWriter = new StringWriter();

        try {
            Reader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int n;
            while ((n = bReader.read(buff)) != -1) {
                stringWriter.write(buff, 0, n);
            }
        } finally {
            stringWriter.close();
            inputStream.close();
        }
        return stringWriter.toString();
    }
}
