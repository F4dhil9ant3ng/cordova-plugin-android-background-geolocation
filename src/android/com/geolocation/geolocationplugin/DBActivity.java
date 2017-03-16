package com.geolocation.geolocationplugin;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;


public class DBActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<LocModel> locModelList;
    SQLiteDatabase sqLiteDatabase;
    LocationsDbHelper locationsDbHelper;
    Toolbar myToolbar;
    String package_name;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        package_name = getApplication().getPackageName();
        context = getApplication();
        setContentView(getApplication().getResources().getIdentifier("activity_database", "layout", package_name));

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        recyclerView = (RecyclerView) findViewById(getApplication().getResources().getIdentifier("rv_database", "id", package_name));
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        locationsDbHelper = new LocationsDbHelper(this);
        sqLiteDatabase = locationsDbHelper.getReadableDatabase();
        String[] projection = {
                LocationsDbContract.Locations._ID,
                LocationsDbContract.Locations.COLUMN_NAME_LONGITUDE,
                LocationsDbContract.Locations.COLUMN_NAME_LATITUDE,
                LocationsDbContract.Locations.COLUMN_NAME_DISTANCE
        };
        Cursor cursor = sqLiteDatabase.query(LocationsDbContract.Locations.TABLE_NAME,projection, null,null,null,null,null);
        locModelList = new ArrayList<LocModel>();
        while(cursor.moveToNext()) {
            locModelList.add(new LocModel(cursor.getString(1), cursor.getString(2),cursor.getString(3)));
        }
        cursor.close();

        LocRVAdapter locRVAdapter = new LocRVAdapter(locModelList, getApplication());
        recyclerView.setAdapter(locRVAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(getApplication().getResources().getIdentifier("menu_main", "menu", package_name), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == context.getResources().getIdentifier("action_refresh", "id", package_name)){
            Intent intent = this.getIntent();
                finish();
                startActivity(intent);
                return true;
        }
        else if(item.getItemId() == context.getResources().getIdentifier("action_clear", "id", package_name)){
            locationsDbHelper.onClear(sqLiteDatabase);
                GeoLocService.setNewRowId(0);
                Intent intent1 = this.getIntent();
                finish();
                startActivity(intent1);
                return true;
        }
        else if(item.getItemId() == context.getResources().getIdentifier("action_settings", "id", package_name)){
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }
}
