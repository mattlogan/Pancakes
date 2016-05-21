package me.mattlogan.library;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Stack;

/**
 * It's a normal Stack of ints.  Only Parcelable!
 */
public class ParcelableIntStack extends Stack<Integer> implements Parcelable {

    public ParcelableIntStack() {
        super();
    }

    protected ParcelableIntStack(Parcel in) {
        int[] elements = in.createIntArray();
        for (int i = elements.length; i >= 0; i--) {
            push(elements[i]);
        }
    }

    public static final Creator<ParcelableIntStack> CREATOR = new Creator<ParcelableIntStack>() {
        @Override
        public ParcelableIntStack createFromParcel(Parcel in) {
            return new ParcelableIntStack(in);
        }

        @Override
        public ParcelableIntStack[] newArray(int size) {
            return new ParcelableIntStack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int[] elements = new int[size()];
        int index = 0;
        while (!isEmpty()) {
            elements[index] = pop();
            index++;
        }

        dest.writeIntArray(elements);
    }
}
