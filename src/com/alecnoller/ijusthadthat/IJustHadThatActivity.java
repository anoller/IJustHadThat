package com.alecnoller.ijusthadthat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class IJustHadThatActivity extends Activity {

	CoordinateFinder location;
	ArrayList<String> checkedPlaces = new ArrayList<String>();
	ArrayAdapter<String> checkedPlacesAdapter;
	ListView placeCheckList;
	DBManager db;
	Button chooseButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		chooseButton = (Button)	findViewById(R.id.choosePlace);

		//create the coordinate finder to prepare latitude and longitude
		location = new CoordinateFinder(this);
		
		//create the database and copy the default data, if none is present
		db = new DBManager(this);
		try {
			String destPath = "/data/data/" + getPackageName() + "/databases";
			File f = new File(destPath);
			if (!f.exists()) {
				f.mkdirs();
				f.createNewFile();
				
				//copy the db from the assets folder into the databases folder
				CopyDB(getBaseContext().getAssets().open("placesdb"),
						new FileOutputStream(destPath + "/PlacesDB"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//create the checklist of places
		createPlaceCheckList();
			
		//retrieve all the places from the database
		db.open();
		Cursor c = db.getAllPlaces();
		if (c.moveToFirst()) {
			do {
				checkedPlacesAdapter.add(c.getString(1));
			} while (c.moveToNext());
		}
		db.close();
		
		//update the checklist of places
		checkedPlacesAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		createMenu(menu);
		return true;
	}
	
	// Create the action bar menu items
	private void createMenu(Menu menu) {
		//create action bar icon to delete selected entries
		MenuItem deletePlace = menu.add(0, 0, 0, "Delete");
		deletePlace.setIcon(R.drawable.ic_menu_delete);
		deletePlace.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		//create action bar icon to add new entries
		MenuItem addPlace = menu.add(0, 1, 1, "Add");
		addPlace.setIcon(R.drawable.ic_menu_add);
		addPlace.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuChoice(item);
	}
	
	// Create the functions of the action bar menu items
	private boolean menuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			DeleteConfirmationDialog delConfirm = DeleteConfirmationDialog.newInstance(
					"You've selected " + checkedPlaces.size() + " place(s) to delete."
					+ " Are you sure?");
			delConfirm.show(getFragmentManager(), "dialog");
			return true;
		case 1:
			addPlaceManually();
			return true;
		}
		return false;
	}
	
	// The positive selection for the "confirm delete" dialog
	public void doPositiveDeleteDialog() {
		db.open();
		for (String place : checkedPlaces) {
			db.deletePlace(place);
			checkedPlacesAdapter.remove(place);
		}
		db.close();
		checkedPlaces.clear();
		checkedPlacesAdapter.notifyDataSetChanged(); 
		correctCheckList();
		correctChooseFromButton();
	}
	
	// Create a new dialog to manually enter a place name
	public void addPlaceManually() {
		ManualAddConfirmationDialog addConfirm = ManualAddConfirmationDialog.newInstance(
				"Add a new place: ");
		addConfirm.show(getFragmentManager(), "dialog");
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	
	// The positive selection for the manual addition dialog
	public void doPositiveAddDialog(String place) {
		boolean foundDuplicate = false;
		
		// Prevent duplicate entries
		for (int i = 0; i < checkedPlacesAdapter.getCount(); i++) {
			if (checkedPlacesAdapter.getItem(i).equalsIgnoreCase(place)) {
				foundDuplicate = true;
				break;
			}
		}
		
		if (foundDuplicate) {
			Toast toast = Toast.makeText(this, "You already added this!", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} else {
			if (!place.isEmpty()) {
				db.open();
				db.insertPlace(place);
				checkedPlacesAdapter.add(place);
				checkedPlacesAdapter.notifyDataSetChanged();
				db.close();
			}
			correctCheckList();
			correctChooseFromButton();
		}
	}
	
	// Copy the pre-made database data into the new database
	public void CopyDB(InputStream inputStream, 
			OutputStream outputStream) throws IOException {
		//copy 1K bytes at a time
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, length);
		}
		inputStream.close();
		outputStream.close();
	}
	
	// Create the central checklist ListView to hold the user's places
	public void createPlaceCheckList() {
		//populate checklist
		placeCheckList = (ListView) this.findViewById(R.id.placeList);
		placeCheckList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		placeCheckList.setTextFilterEnabled(true);
		checkedPlacesAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_checked);
		placeCheckList.setAdapter(checkedPlacesAdapter);
		
		//set click listener		
		placeCheckList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String place = checkedPlacesAdapter.getItem(position);
				//check for duplicates - if none, add to the list of selections
				if (checkedPlaces.contains(place)) {
					checkedPlaces.remove(place);
				} else {
					checkedPlaces.add(place);
				}
				
				//update the count on the "choose" button
				correctChooseFromButton();
			}
		});
	}
	
	// Select all items from the checklist
	public void selectAllCheckList(View view) {
		for (int i = 0; i < checkedPlacesAdapter.getCount(); i++) {
			placeCheckList.setItemChecked(i, true);
			String place = checkedPlacesAdapter.getItem(i);
			if (!checkedPlaces.contains(place)) {
				checkedPlaces.add(place);
			}
			correctChooseFromButton();
		}
	}
	
	// Clear all selections from the checklist
	public void clearCheckList(View view) {
		for (int i = 0; i < checkedPlacesAdapter.getCount(); i++) {
			placeCheckList.setItemChecked(i, false);
		}
		checkedPlaces.clear();
		correctChooseFromButton();
	}
	
	// Correct the "checked" or "unchecked" status of list items
	public void correctCheckList() {
		for (int i = 0; i < checkedPlacesAdapter.getCount(); i++) {
			if (checkedPlaces.contains(checkedPlacesAdapter.getItem(i))) {
				placeCheckList.setItemChecked(i, true);
			} else {
				placeCheckList.setItemChecked(i, false);
			}
		}
	}
	
	// Update the text displayed on the "Choose" button
	public void correctChooseFromButton() {
		if (checkedPlacesAdapter.isEmpty()) {
			chooseButton.setText("YOU DON'T LIKE ANYTHING");
		} else if (checkedPlaces.size() == 0 || 
				checkedPlaces.size() == checkedPlacesAdapter.getCount()) {
			chooseButton.setText("CHOOSE FROM ALL");
		} else {
			chooseButton.setText("CHOOSE FROM " + checkedPlaces.size());
		}
	}
	
	// Randomly select a place from the available choices
	public void choosePlace(View view) {
		String choice;
		if (!checkedPlacesAdapter.isEmpty()) {
			if (checkedPlaces.isEmpty()) {
				//choose randomly from all of the places
				choice = checkedPlacesAdapter.getItem(new Random().nextInt(checkedPlacesAdapter.getCount()));
			} else {
				//choose randomly from the selected places
				choice = checkedPlaces.get(new Random().nextInt(checkedPlaces.size()));
			}
			Toast toast = Toast.makeText(this, "You should probably eat at " + 
					choice, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} else {
			Toast toast = Toast.makeText(this, "You don't like anything!", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
	
	// Send the device's current location to the "Add My Location" dialog
	public void addLoc(View view) {
		Double latitude = location.getCurrentLatitude();
		Double longitude = location.getCurrentLongitude();
		
		// Don't open the confirmation dialog unless lat and long have been received
		if (latitude != null && longitude != null) {
			AutoAddConfirmationDialog addConfirm = AutoAddConfirmationDialog.newInstance(
					"Tap for new results:", latitude, longitude);
			addConfirm.show(getFragmentManager(), "dialog");	
		} else {
			Toast toast = Toast.makeText(this, "Searching for location... try again...", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
	
	// The positive selection for the "Add My Location" dialog
	public void doPositiveAutoAddDialog(String place) {
		boolean foundDuplicate = false;
		
		// Prevent duplicate entries
		for (int i = 0; i < checkedPlacesAdapter.getCount(); i++) {
			if (checkedPlacesAdapter.getItem(i).equalsIgnoreCase(place)) {
				Log.d("Checking item: ", "1");
				foundDuplicate = true;
				break;
			}
		}
		
		if (foundDuplicate) {
			Toast toast = Toast.makeText(this, "You already added this!", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} else {
			db.open();
			db.insertPlace(place);
			checkedPlacesAdapter.add(place);
			checkedPlacesAdapter.notifyDataSetChanged();
			db.close();
			correctCheckList();
			correctChooseFromButton();
		}
	}
}

