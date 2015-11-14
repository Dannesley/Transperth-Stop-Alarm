package com.testing.daniel.googlemaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class AddStopDialogFragment extends DialogFragment
{
    //Used to allow access to a callback from hitting the positive dialog option
    public interface AddStopDialogListener
    {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    AddStopDialogListener mListener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        //Verify the host activity implements the interface
        try
        {
            //Instantiate the listener so we can send events to the host
            mListener = (AddStopDialogListener)activity;
        }
        catch(ClassCastException e)
        {
            //The activity doesn't implement the interface
            throw new ClassCastException(activity.toString()
                                         +" must implement AddStopDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle onSavedInstance)
    {
        //Use builder class to construct custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_title_add_stop)
               .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener()
               {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        mListener.onDialogPositiveClick(AddStopDialogFragment.this);
                    }
               })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }

                });

        return builder.create();
    }
}
