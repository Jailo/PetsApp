package com.example.android.pets.data;

import android.provider.BaseColumns;

public final class PetsContract {

    public PetsContract() {
    }

    public static abstract class petsEntry implements BaseColumns {

        public static final String TABLE_NAME = "pets";

        // Constants for the database column headers
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BREED = "breed";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_WEIGHT = "weight";

        // Gender value constants
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

    }

}
