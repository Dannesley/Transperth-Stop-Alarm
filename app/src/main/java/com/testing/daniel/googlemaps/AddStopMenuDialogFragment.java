package com.testing.daniel.googlemaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;


public class AddStopMenuDialogFragment extends DialogFragment
{
    //Used to allow access to a callback from hitting the positive dialog option
    public interface AddStopMenuDialogListener
    {
        public void onStopMenuDialogPositiveClick(String editText, int checkedButtonId);
    }

    AddStopMenuDialogListener mListener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        //Verify the host activity implements the interface
        try
        {
            //Instantiate the listener so we can send events to the host
            mListener = (AddStopMenuDialogListener)activity;
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
        //setupRadioGroupListener();
        //Use builder class to construct custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Get the layout inflater to use custom layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //Inflate and set the layout for the dialog
        //Pass null as the parent view since its going into the dialog layout
        /******************VERY FUCKING IMPORTANT********************/
        //MAKE SURE is to use the exact same view for accessing the UI elements
        //of the dialog. Otherwise listeners will not work on Input Controls
        final View view = inflater.inflate(R.layout.add_stop_dialog, null);//
        /***********************************************************/
        builder.setView(view)
                .setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        EditText text = (EditText)view.findViewById(R.id.edit_stop_number);
                        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radio_add_group);
                        mListener.onStopMenuDialogPositiveClick(text.getText().toString(),
                                                                radioGroup.getCheckedRadioButtonId());
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                });

        //make sure the radio group is listening
        setupRadioGroupListener(view);

        return builder.create();
    }

    //Used to listen to the radio group inside the dialog
    public void setupRadioGroupListener(View view)
    {
        final EditText text = (EditText)view.findViewById(R.id.edit_stop_number);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radio_add_group);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(checkedId == R.id.radio_add_number)
                {
                    text.setEnabled(true);
                }
                else
                {
                    text.setEnabled(false);
                }
            }
        });
    }
}
