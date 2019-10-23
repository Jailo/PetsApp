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

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetCursorAdapter;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    // ID for the Cursor loader
    private static final int PET_LOADER = 0;

    // Pet cursor adaptor, that will be initialized on create
    private static PetCursorAdapter petAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find List view resource
        ListView petListView = (ListView) findViewById(R.id.list_view);

        // Find empty view and set to pet list view
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Create new Pet adapter
        // Cursor is null for now, will update within the Cursor loader
        petAdapter = new PetCursorAdapter(this, null);

        // Set pet adapter to list view
        petListView.setAdapter(petAdapter);

        // Set on item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent that will go to the edit pet activity
                Intent editPetIntent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Create a Uri with the current pet's Id, set that data to the intent
                Uri currentPetUri = Uri.withAppendedPath(PetEntry.CONTENT_URI, String.valueOf(id));
                editPetIntent.setData(currentPetUri);

                // Start the intent
                startActivity(editPetIntent);
            }
        });

        // Prepare the loader by either re-connecting with an existing one or creating a new one
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:

                // Delete all pets in database
                int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);

                //If no pets have been deleted, show an error message
                if (rowsDeleted == 0) {
                    Toast.makeText(this, getString(R.string.delete_all_pets_failed), Toast.LENGTH_SHORT).show();
                } else {
                    // else show a message saying deleting all pets has been successful
                    Toast.makeText(this, getString(R.string._delete_all_pets_successful), Toast.LENGTH_SHORT).show();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void insertPet() {

        // Create Content values for a new row in database
        ContentValues values = new ContentValues();

        // Add in pet data like its name, breed, gender, and weight
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert new "dummy" placeholder pet into the database
        // By calling ContentResolver Insert method, which will then call Pet Providers insertPet method
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Projection string array for reading from database only from colums we care about
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };


        return new CursorLoader(
                this,
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        petAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Reset loader by setting Pet Adaptor's cursor to null
        petAdapter.swapCursor(null);
    }
}