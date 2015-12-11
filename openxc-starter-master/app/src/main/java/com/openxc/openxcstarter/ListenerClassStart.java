package com.openxc.openxcstarter;

import android.view.View;
import android.widget.Toast;

/**
 * Listener class for the "start" button
 */


public class ListenerClassStart implements View.OnClickListener {

    /**
     * gets called when user clicks on the "start" button
     * @param v  -> view
     */
    @Override
    public void onClick(View v) {


        if(StarterActivity.getEnabled() == false){

            //check if valid Trip Id is set

            if(StarterActivity.getmTripId().getText().toString().isEmpty() == false) {
                StarterActivity.setEnabled(true);
                Toast.makeText(StarterActivity.getStarterActivcity().getBaseContext(), "Started Service", Toast.LENGTH_SHORT).show();
                StarterActivity.showNotification();
            }
            else {
                Toast.makeText(StarterActivity.getStarterActivcity().getBaseContext(), "Please insert trip name", Toast.LENGTH_SHORT).show();
            }
        } else {
            StarterActivity.setEnabled(false);
            Toast.makeText(StarterActivity.getStarterActivcity().getBaseContext(), "Stopped Service", Toast.LENGTH_SHORT).show();
            StarterActivity.cancelNotification();
        }

    }
}
