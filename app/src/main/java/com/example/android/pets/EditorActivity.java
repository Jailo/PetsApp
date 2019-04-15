/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Log Tag
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * Existing loader ID
     */
    private static final int EXISTING_PET_LOADER = 0;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    /**
     * Uri for the current pet, null if creating a new pet
     */
    private Uri mCurrentPetUri;

    private boolean mPetHasChanged = false;
    // Listens for when a user presses something on the view,
    // which would mean some input has been changed, so then set mPetHasChanged from false to true
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            Log.v(LOG_TAG, "its going down");
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();

        mCurrentPetUri = intent.getData();

        // If the intents data is null, then we must add a new pet into the database
        if (mCurrentPetUri == null) {
            // set title to "Add a Pet"
            setTitle(R.string.editor_activity_title_add_a_pet);
        } else {
            // If data is NOT null, then we are editing an existing pet
            // Set the activity's title "Edit Pet"
            setTitle(R.string.editor_activity_title_edit_pet);

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER,null, this);

        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Set on touch listeners on each field
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE;
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    /**
     * Inserts new pet data into the database
     */
    private void savePet() {

        // Get pet data from edit text and spinners
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weight = 0;
        // If the weight text field is NOT empty, update the weight integer for the pet
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        // If the text fields are empty just finish activity instead of saving an incomplete pet
        if (mCurrentPetUri == null && TextUtils.isEmpty(mNameEditText.getText())
                && TextUtils.isEmpty(mBreedEditText.getText())
                && TextUtils.isEmpty(mWeightEditText.getText()) && mGender == PetEntry.GENDER_UNKNOWN) {
            return;
        }

        // Create Content values for a new row in database
        ContentValues values = new ContentValues();

        // Add in pet data like its name, breed, gender, and weight
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        //Create a brand new pet if the current pet uri is null
        if (mCurrentPetUri == null) {

            Log.i(LOG_TAG, "Creating new pet");

            // Insert new pet into the database
            // By calling ContentResolver Insert method, which will then call PetProvider's insertPet method
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            // If there was an error saving new row, display an error toast message
            if (newUri == null) {
                // Toast message as an error message
                Toast.makeText(this, R.string.editor_insert_pet_unsuccessful, Toast.LENGTH_SHORT).show();

            } else {
                // Else if new pet was added succesfully, display a success toast message
                Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();
            }

        } else {
            // update current pet
            Log.v(LOG_TAG, "Updating a pet");

            int rowsUpdated = getContentResolver().update(mCurrentPetUri, values, null, null);

            Log.v(LOG_TAG, "Updated rows: " + String.valueOf(rowsUpdated));

            Log.v(LOG_TAG, "mGender is: " + String.valueOf(mGender));

            // If there was an error saving new row, display an error toast message
            if (rowsUpdated == 0) {
                // Toast message as an error message
                Toast.makeText(this, R.string.editor_insert_pet_unsuccessful, Toast.LENGTH_SHORT).show();

            } else {
                // Else if new pet was added succesfully, display a success toast message
                Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();
            }

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // call savePet to save new pet and exit the activity
                savePet();
                finish();

                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mPetHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Projection string array for reading from database only from colums we care about
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if curser is null, or has less than 1 row in database
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Move cursor to the first row, and read from it
        //this should be the only row in the cursor
        if (cursor.moveToFirst()) {

            // Find the index for each column in the database
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // Get the values for the current pet from the database columns
            String currentPetName = cursor.getString(nameColumnIndex);
            String currentPetBreed = cursor.getString(breedColumnIndex);
            int currentPetGender = cursor.getInt(genderColumnIndex);
            int currentPetWeight = cursor.getInt(weightColumnIndex);

            // Update the editor text fields with the current pet's data
            mNameEditText.setText(currentPetName);
            mBreedEditText.setText(currentPetBreed);
            mWeightEditText.setText(String.valueOf(currentPetWeight));


            // Check whether the pet gender is male, female, or unknown
            // and set it to the gender spinner
            switch (currentPetGender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(PetEntry.GENDER_MALE);
                    mGender = 1;
                    break;

                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(PetEntry.GENDER_FEMALE);
                    mGender = 2;
                    break;

                default:
                    mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
                    mGender = 0;
                    break;
            }

            // set global Gender variable to pet gender so the current pet's gender will be saved
            mGender = currentPetGender;

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Remove everything from the input fields
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0); // Set gender to Unknown
    }
}