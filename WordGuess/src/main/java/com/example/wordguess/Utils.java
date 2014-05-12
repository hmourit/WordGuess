package com.example.wordguess;

import android.content.res.TypedArray;

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
}
