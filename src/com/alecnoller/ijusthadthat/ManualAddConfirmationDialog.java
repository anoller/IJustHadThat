package com.alecnoller.ijusthadthat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ManualAddConfirmationDialog extends DialogFragment {
	String userInput;
	static ManualAddConfirmationDialog newInstance(String title) {
		ManualAddConfirmationDialog add = new ManualAddConfirmationDialog();
		Bundle args = new Bundle();
		args.putString("title", title);
		add.setArguments(args);
		return add;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		String title = getArguments().getString("title");
		final EditText input = new EditText(getActivity()); 
		
		//capitalize each word in the EditText
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		
		return new AlertDialog.Builder(getActivity())
	    .setView(input)
		.setIcon(R.drawable.ic_menu_add)
		.setTitle(title)
		.setPositiveButton("Done", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					userInput = input.getText().toString();
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					((IJustHadThatActivity)getActivity()).doPositiveAddDialog(userInput);
				}
			})
		.setNegativeButton("Never mind.", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
				}
			}).create();
	} 
}

