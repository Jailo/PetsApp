package com.example.android.pets.data;

import android.provider.BaseColumns;

public final class PetContract {

    public PetContract() {
    }

    public static abstract class PetEntry implements BaseColumns {

        public static final String TABLE_NAME = "pets";

        // Constants for the database column headers
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        // Gender value constants
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

    }

}
