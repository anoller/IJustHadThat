package com.alecnoller.ijusthadthat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gmarz.googleplaces.GooglePlaces;
import org.gmarz.googleplaces.models.Place;
import org.gmarz.googleplaces.models.PlacesResult;
import org.gmarz.googleplaces.models.Result.StatusCode;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.color;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AutoAddConfirmationDialog extends DialogFragment {
	//TODO Figure out Android API_KEY - why doesn't this work?
	//public final String API_KEY = "AIzaSyDW5GoQNHriETgp7SfdbzDR20UqvXhf_1A";
	public final String API_KEY = "AIzaSyA1gYh-ibMwdJ-kaXx941-qwAP1PJGK-lU";
	List<Place> places;
	Button showResult;
	LinearLayout layout;
	int pressCount = 0;
	
	// Create confirmation dialog for location-based additions
	static AutoAddConfirmationDialog newInstance(
			String title, double latitude, double longitude) {
		AutoAddConfirmationDialog add = new AutoAddConfirmationDialog();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putDouble("latitude", latitude);
		args.putDouble("longitude", longitude);
		add.setArguments(args);
		return add;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		buildLayout();
		String title = getArguments().getString("title"); 
		double latitude = getArguments().getDouble("latitude");
		double longitude = getArguments().getDouble("longitude");
		
		places = new ArrayList<Place>();
		
		// Execute AsyncTask to download the Places API JSON data
		new DownloadPlacesTask().execute(latitude, longitude);
	
		// Build the confirmation dialog
		return new AlertDialog.Builder(getActivity())
		.setIcon(R.drawable.ic_menu_mylocation)
		.setView(layout)
		.setTitle(title)
		.setPositiveButton("Yes, add it", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					((IJustHadThatActivity)getActivity())
					.doPositiveAutoAddDialog(showResult.getText().toString());
				}
			})
		.setNegativeButton("Never mind", null).create();
	}
	
	private void buildLayout() {
		//create the button that displays the Places found
		showResult = new Button(getActivity()); 
		showResult.setText("Loading...");
		showResult.setGravity(Gravity.CENTER_HORIZONTAL);
		showResult.setPadding(0, 30, 0, 30);
		showResult.setBackgroundColor(color.transparent);
		showResult.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				pressCount += 1;
				if (pressCount >= places.size()) {
					showResult.setText("There's no more!");
				} else {
					showResult.setText(places.get(pressCount).getName());
				}
			}
		});
		
		//create the "Powered by Google" logo
		ImageView google = new ImageView(getActivity());
		google.setImageResource(R.drawable.powered_by_google);
		
		layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(showResult);
		//TODO add attributions?
		layout.addView(google);		
	}
	
	private class DownloadPlacesTask extends AsyncTask<Double, Void, List<Place>> {
		
		protected List<Place> doInBackground(Double... params) {
			double latitude = params[0];
			double longitude = params[1];			
			
			GooglePlaces googlePlaces = new GooglePlaces(API_KEY);
						
			List<Place> placeData = null;
			PlacesResult result;
			try {
				//result = googlePlaces.getPlaces("restaurant", 100, latitude, longitude);
				//NearbySearchQuery query = new NearbySearchQuery(latitude, longitude);
				//query.setRanking(NearbySearchQuery.Ranking.Distance);
				//query.addType("restaurant");
				
				//result = googlePlaces.getPlaces(query);
				
				String query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
						+ "location=" + latitude + "," + longitude + "&rankby=distance"
						+ "&types=restaurant&key=" + API_KEY;
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(query);
				ResponseHandler<String> handler = new BasicResponseHandler();
				String response = client.execute(request, handler);
				JSONObject jsonResponse = new JSONObject(response);
				result = new PlacesResult(jsonResponse);
				
				if (result.getStatusCode() == StatusCode.OK) {
					placeData = result.getPlaces();
				} 
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return placeData;
		}

		protected void onPostExecute(List<Place> placeData) {
			updatePlace(placeData);
		}
	}
	
	// Update the result shown in the dialog
	public void updatePlace(List<Place> placeData) {
		if (placeData == null) {
			showResult.setText("No places found");
		} else {
			places.addAll(placeData);
			showResult.setText(places.get(pressCount).getName());
		}
	}
}
