package me.mattlogan.library;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Stack;

/**
 * It's a normal Stack of ints.  Only Parcelable!
 */
class ParcelableIntStack extends Stack<Integer> implements Parcelable {

    ParcelableIntStack() {
    }

    private ParcelableIntStack(Parcel in) {
        int[] elements = in.createIntArray();
        for (int element : elements) {
            add(element);
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
        for (int i = 0; i < size(); i++) {
            elements[i] = get(i);
        }
        dest.writeIntArray(elements);
    }
}
