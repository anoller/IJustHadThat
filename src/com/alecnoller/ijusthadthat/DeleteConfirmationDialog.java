package com.alecnoller.ijusthadthat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DeleteConfirmationDialog extends DialogFragment {
	 static DeleteConfirmationDialog newInstance(String title) {
		DeleteConfirmationDialog del = new DeleteConfirmationDialog();
		Bundle args = new Bundle();
		args.putString("title", title);
		del.setArguments(args);
		return del;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");
		return new AlertDialog.Builder(getActivity())
		.setIcon(R.drawable.ic_menu_delete)
		.setTitle(title)
		.setPositiveButton("OK", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					((IJustHadThatActivity)getActivity()).doPositiveDeleteDialog();
				}
			})
		.setNegativeButton("No, don't!", null).create();
	}
}