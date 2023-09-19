package com.bojlerapp;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Iterator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SecondFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SecondFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    MyListAdapter adapter;

    ListView list;

    String[] subtitle=new String[]{
            "32°C    Gretje traja 37 min",
            "40°C    Gretje traja 62 min",
            "48°C    Gretje traja 86 min",
            "48°C    Gretje traja 86 min",
            "Vklopi takoj",
            "Izklopi takoj",

            "Funkcija še ne deluje",
            "Funkcija še ne deluje",
            "Funkcija še ne deluje"
    };

    String[] maintitle ={
            "Tuširanje 1 oseba",
            "Tuširanje 2 osebi",
            "Tuširanje 2 osebi in lasje",
            "Ročni vklop",
            "Ročni izklop",

            "Vikend",
            "Poceni elektrika",
            "Maximalno"
    };



    Integer[] imgid={
            R.drawable.enaoseba__1_,
            R.drawable.dveosebi,
            R.drawable.triosebe,
            R.drawable.power_on,
            R.drawable.power_off,
            R.drawable.electricity_20x20,
            R.drawable.money_20x20,
            R.drawable.money_20x20
    };


    public SecondFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SecondFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SecondFragment newInstance(String param1, String param2) {
        SecondFragment fragment = new SecondFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        adapter = new MyListAdapter(getActivity(), maintitle, subtitle, imgid);
        list = (ListView) getView().findViewById(R.id.list_second);
        list.setAdapter(adapter);

        updateProgramPageOnce();


        /*Handler b = new Handler();

        Runnable a = new Runnable() {
            @Override
            public void run() {
                colorAllBackgroundsWhite();
            }
        };
        b.postDelayed(a,0);*/

        try {
            updateStatusPageLoop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }







        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //ta del z GREEN naredi da se celotna vrstica obarva zeleno ko se pritisne na določen gumb
                /*if((((ColorDrawable)view.getBackground()).getColor()==Color.GREEN)){
                    colorAllBackgroundsWhite();

                }
                else{
                    colorAllBackgroundsWhite();
                    view.setBackgroundColor(Color.GREEN);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            colorAllBackgroundsWhite();
                        }
                    }, 100);
                }
*/
                //ROČNI VKLOP
                if (position == 3) {
                    Parametri newParameter = null;
                    try {
                        newParameter = new Parametri(99,"");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    MainActivity.sendSMS(MainActivity.phoneNo,newParameter.createBasicSMSText());
                    updateFirebase("Ročni vklop",newParameter.secondsFromNowToFinish,newParameter.secondsFromNowToStart, newParameter.finalT);


                    //ROČNI IZKLOP
                } else if (position == 4) {
                    Parametri newParameter = null;
                    try {
                        newParameter = new Parametri(100,"");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    MainActivity.sendSMS(MainActivity.phoneNo,newParameter.createBasicSMSText());
                    updateFirebase("Ročni izklop",newParameter.secondsFromNowToFinish,newParameter.secondsFromNowToStart, newParameter.finalT);

                }




                else {


                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                            //TUŠIRANJE 1 OSEBA
                            if (position == 0) {


                                Parametri newParameter = null;
                                try {
                                    newParameter = new Parametri(Parametri.finalTempZaEnoOsebo(), hourOfDay + ":" + minute);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                MainActivity.sendSMS(MainActivity.phoneNo, newParameter.createBasicSMSText());

                                updateFirebase("Tuširanje 1 oseba",newParameter.secondsFromNowToFinish,newParameter.secondsFromNowToStart, newParameter.finalT);

                                Toast.makeText(getContext(),"Z gretjem pričnem čez "+newParameter.secondsFromNowToStart/60+" min",Toast.LENGTH_LONG).show();


                                //TUŠIRANJE 2 OSEBI
                            } else if (position == 1) {
                                Parametri newParameter = null;
                                try {
                                    newParameter = new Parametri(Parametri.finalTempZaDveOsebi(), hourOfDay + ":" + minute);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                MainActivity.sendSMS(MainActivity.phoneNo, newParameter.createBasicSMSText());

                                updateFirebase("Tuširanje 2 osebi",newParameter.secondsFromNowToFinish,newParameter.secondsFromNowToStart, newParameter.finalT);

                                System.out.println(Parametri.getHoursOfStringDate(hourOfDay + ":" + minute) + "blaaa");

                                Toast.makeText(getContext(),"Z gretjem pričnem čez "+newParameter.secondsFromNowToStart/60+" min",Toast.LENGTH_LONG).show();
                                //TUŠIRANJE 2 OSEBI + LASJE
                            } else if (position == 2) {
                                Parametri newParameter = null;
                                try {
                                    newParameter = new Parametri(Parametri.finalTempZaDveOsebiInLasje(), hourOfDay + ":" + minute);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                MainActivity.sendSMS(MainActivity.phoneNo, newParameter.createBasicSMSText());

                                updateFirebase("Tuširanje 2 osebi in lasje",newParameter.secondsFromNowToFinish,newParameter.secondsFromNowToStart,newParameter.finalT);


                                System.out.println(Parametri.getHoursOfStringDate(hourOfDay + ":" + minute) + "blaaa");

                                Toast.makeText(getContext(),"Z gretjem pričnem čez "+newParameter.secondsFromNowToStart/60+" min",Toast.LENGTH_LONG).show();
                            }


                        }

                    } ,12, 0, true);

                    timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            System.out.println("PRITISNIL SI CANCELL");
                        }
                    });
                    timePickerDialog.show();
                }
            }
        });
    }

   /* public void colorAllBackgroundsWhite(){
        for(int i=0; i<list.getChildCount();i++){
            list.getChildAt(i).setBackgroundColor(Color.WHITE);
        }
    }*/


    public void updateFirebase(String imePrograma, long casDoKoncaPrograma, long casDoPricetkaGretja, int zeljenaTemperatura){
        FirebaseDatabase database = FirebaseDatabase.getInstance("firebase URL...");
        DatabaseReference myRef = database.getReference("Reference...");

        LocalDate today = LocalDate.now(ZoneId.of("ECT"));
        long nowSecondsOfEpoch= LocalDateTime.of(today, LocalTime.now()).toEpochSecond(ZoneOffset.ofTotalSeconds(0));


        //nastavljanje vrednosti
        myRef.child("StatusPage").child("CasSpremembe").setValue(nowSecondsOfEpoch);
        myRef.child("StatusPage").child("TrenutniProgram").setValue(imePrograma);
        myRef.child("StatusPage").child("CasDoKoncaPrograma").setValue(casDoKoncaPrograma);
        myRef.child("StatusPage").child("CasDoPricetkaGretja").setValue(casDoPricetkaGretja);
        myRef.child("StatusPage").child("ZeljenaTemperatura").setValue(zeljenaTemperatura);


    }


//posodablja stran na 10s
    public void updateStatusPageLoop() throws IOException, InterruptedException {
        //Handler je zato da se osvežuje besedilo na vsakih 1s
        Handler handler = new Handler();
        int delay = 30000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {

                //branje vrednosti
                updateProgramPageOnce();

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    //Posodobi prikazno stran z novimi podatki iz firebas-a
    public void updateProgramPageOnce() {

        FirebaseDatabase database = FirebaseDatabase.getInstance("firebase URL...");
        DatabaseReference myRef = database.getReference("Reference...");

        //branje vrednosti
        myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()){
                    DataSnapshot snapshot = task.getResult().child("Referencee....");


                    Parametri.FINALTEMP1OS =((Long) snapshot.child("FinalT1").getValue()).intValue();
                    Parametri.FINALTEMP2OS =((Long) snapshot.child("FinalT2").getValue()).intValue();
                    Parametri.FINALTEMP2OSLASJE =((Long) snapshot.child("FinalT3").getValue()).intValue();

                    try {
                        subtitle[0] = Parametri.finalTempZaEnoOsebo() + "°C    Gretje traja " + Parametri.calculateHeatingTime(Parametri.finalTempZaEnoOsebo(), Parametri.getPresentT()) + " min";
                        subtitle[1] = Parametri.finalTempZaDveOsebi() + "°C    Gretje traja " + Parametri.calculateHeatingTime(Parametri.finalTempZaDveOsebi(), Parametri.getPresentT()) + " min";
                        subtitle[2] = Parametri.finalTempZaDveOsebiInLasje() + "°C    Gretje traja " + Parametri.calculateHeatingTime(Parametri.finalTempZaDveOsebiInLasje(), Parametri.getPresentT()) + " min";
                        subtitle[3] = "Vklopi takoj";
                        subtitle[4] = "Izklopi takoj";
                        subtitle[5] = "Funkcija še ne deluje";
                        subtitle[6] = "Funkcija še ne deluje";
                        subtitle[7] = "Funkcija še ne deluje";

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                    //posodobi izgled StatusPaga
                    adapter.notifyDataSetChanged();


                }
            };

        });

    }











    public void brbr(){
        //inicializacija
        FirebaseDatabase database = FirebaseDatabase.getInstance("firebase URL....");
        DatabaseReference myRef = database.getReference("Reference...");
        //nastavljanje vrednosti
        myRef.child("Reference....").child("Reference...").setValue("12345678");

        //branje vrednosti
        myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()){
                    for (Iterator<DataSnapshot> it = task.getResult().getChildren().iterator(); it.hasNext();) {
                        DataSnapshot snapshot = it.next();
                        System.out.println("REFERENCA: "+snapshot.getKey()+" "+snapshot.getValue());

                    }

                }
            };

        });

    }




    }