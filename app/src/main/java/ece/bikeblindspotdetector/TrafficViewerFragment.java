package ece.bikeblindspotdetector;


import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *
 */
public class TrafficViewerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    Button alert_Left;
    Button alert_Center;
    Button alert_Right;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_traffic_viewer, container, false);

        alert_Left = (Button) view.findViewById(R.id.alert_left);
        alert_Center = (Button) view.findViewById(R.id.alert_center);
        alert_Right = (Button) view.findViewById(R.id.alert_right);

        return view;
    }

    /*
    The following 9 methods set each individual buttons to the respective color stated in the method
    name. These are used to change the screen output so that the user can see where traffic is in
    relation to themselves
     */

    /*
    These 3 methods set the left button to a color
     */
    public void setLeftGreen()
    {
        alert_Left.setBackgroundColor(Color.GREEN);
    }
    public void setLeftYellow()
    {
        alert_Left.setBackgroundColor(Color.YELLOW);
    }
    public void setLeftRed()
    {
        alert_Left.setBackgroundColor(Color.RED);
    }

    /*
    These 3 methods set the center button to a color
     */
    public void setCenterGreen()
    {
        alert_Center.setBackgroundColor(Color.GREEN);
    }
    public void setCenterYellow()
    {
        alert_Center.setBackgroundColor(Color.YELLOW);
    }
    public void setCenterRed()
    {
        alert_Center.setBackgroundColor(Color.RED);
    }

    /*
    These 3 methods set the right button to a color
     */
    public void setRightGreen()
    {
        alert_Right.setBackgroundColor(Color.GREEN);
    }
    public void setRightYellow()
    {
        alert_Right.setBackgroundColor(Color.YELLOW);
    }
    public void setRightRed()
    {
        alert_Right.setBackgroundColor(Color.RED);
    }
}
