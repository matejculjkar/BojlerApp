package com.bojlerapp;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ListView list;

    String[] maintitle ={
            "Trenutna temperatura",
            "Željena temperatura",
            "Status bojlerja",
            "Trenutni program",
            "Čas do konca programa",
            "Mesečna poraba elektrike",
            "Mesečna cena elektrike"
    };

    String[] subtitle ={
            "Ni internetne povezave",
            "Ni internetne povezave",
            "Ni internetne povezave",
            "Ni internetne povezave",
            "Ni internetne povezave",
            "Ni internetne povezave",
            "Ni internetne povezave"
    };

    Integer[] imgid={
            R.drawable.temperature_now_20x20,
            R.drawable.temperature_req_20x20,
            R.drawable.running_20x20,
            R.drawable.program_20x20,
            R.drawable.time_20x20,
            R.drawable.electricity_20x20,
            R.drawable.money_20x20
    };

    MyListAdapter adapter;

    public FirstFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FirstFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstFragment newInstance(String param1, String param2) {

        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateStatusPageOnce();


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {







        adapter=new MyListAdapter(getActivity(), maintitle, subtitle,imgid);
        list = (ListView) getView().findViewById(R.id.list_first);
        list.setAdapter(adapter);

        try {
            updateStatusPageLoop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                // TODO Auto-generated method stub
                if(position == 0) {
                    //code specific to first list item
                    Toast.makeText(getActivity(),"Place Your First Option Code",Toast.LENGTH_SHORT).show();
                }

                else if(position == 1) {
                    //code specific to 2nd list item
                    Toast.makeText(getActivity(),"Place Your Second Option Code",Toast.LENGTH_SHORT).show();
                }

                else if(position == 2) {

                    Toast.makeText(getActivity(),"Place Your Third Option Code",Toast.LENGTH_SHORT).show();
                }
                else if(position == 3) {

                    Toast.makeText(getActivity(),"Place Your Forth Option Code",Toast.LENGTH_SHORT).show();
                }
                else if(position == 4) {

                    Toast.makeText(getActivity(),"Place Your Fifth Option Code",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

//Posodobi subtitle String[] da ima prave vnose iz Firebas-a, posodablja 1x na sekundo
    public void updateStatusPageLoop() throws IOException, InterruptedException {
        //Handler je zato da se osvežuje besedilo na vsakih 1s
        Handler handler = new Handler();
        int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {

                //branje vrednosti
                updateStatusPageOnce();

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

//Posodobi prikazno stran z novimi podatki iz firebas-a
    public void updateStatusPageOnce() {

        FirebaseDatabase database = FirebaseDatabase.getInstance("Firebase URL...");
        DatabaseReference myRef = database.getReference("Reference...");

        System.out.println("DELAM");



        //branje vrednosti
        myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()){
                    DataSnapshot snapshot = task.getResult().child("Reference...");



                    if(casDoKoncaProgramaIzracun(snapshot)==0){
                        subtitle[0]="Funkcija še ne deluje";//snapshot.child("TrenutnaTemperatura").getValue().toString();
                        subtitle[1]="";
                        subtitle[2]=statusBojlerja(snapshot);
                        subtitle[3]="";
                        subtitle[4]="";
                        subtitle[5]="Funkcija še ne deluje";//snapshot.child("MesecnaPorabaElektrike").getValue().toString();
                        subtitle[6]="Funkcija še ne deluje";//snapshot.child("MesecnaCenaPorabljeneElektrike").getValue().toString();


                        //V primeru ročnega vklopa
                        if("Ročni vklop".equals(snapshot.child("TrenutniProgram").getValue().toString())){
                            subtitle[0]="Funkcija še ne deluje";//snapshot.child("TrenutnaTemperatura").getValue().toString();
                            subtitle[1]="";
                            subtitle[2]="Bojler deluje";
                            subtitle[3]="Ročni vklop";
                            subtitle[4]="";
                            subtitle[5]="Funkcija še ne deluje";//snapshot.child("MesecnaPorabaElektrike").getValue().toString();
                            subtitle[6]="Funkcija še ne deluje";//snapshot.child("MesecnaCenaPorabljeneElektrike").getValue().toString();
                            imgid[2]=R.drawable.running_20x20;
                        }
                        if("Ročni izklop".equals(snapshot.child("TrenutniProgram").getValue().toString())){
                            subtitle[0]="Funkcija še ne deluje";//snapshot.child("TrenutnaTemperatura").getValue().toString();
                            subtitle[1]="";
                            subtitle[2]="Bojler ne deluje";
                            subtitle[3]="";
                            subtitle[4]="";
                            subtitle[5]="Funkcija še ne deluje";//snapshot.child("MesecnaPorabaElektrike").getValue().toString();
                            subtitle[6]="Funkcija še ne deluje";//snapshot.child("MesecnaCenaPorabljeneElektrike").getValue().toString();
                            imgid[2]=R.drawable.not_running_20x20;
                        }


                    }
                    else{
                        subtitle[0]="Funkcija še ne deluje";//snapshot.child("TrenutnaTemperatura").getValue().toString()+" °C";
                        subtitle[1]=snapshot.child("ZeljenaTemperatura").getValue().toString()+" °C";
                        subtitle[2]=statusBojlerja(snapshot);//deluje
                        subtitle[3]=snapshot.child("TrenutniProgram").getValue().toString();
                        subtitle[4]=Parametri.convertSecondsToString(casDoKoncaProgramaIzracun(snapshot));
                        subtitle[5]="Funkcija še ne deluje";//snapshot.child("MesecnaPorabaElektrike").getValue().toString();
                        subtitle[6]="Funkcija še ne deluje";//snapshot.child("MesecnaCenaPorabljeneElektrike").getValue().toString();

                    }


                    //posodobi izgled StatusPaga
                    adapter.notifyDataSetChanged();
                    //System.out.println("REFERENCA: " + snapshot.child("CasDoPricetkaGretja").getValue() + " ");
                }
            };

        });

    }


    public long casDoKoncaProgramaIzracun(DataSnapshot snapshot){
        String casDoKoncaProgramaString=snapshot.child("CasDoKoncaPrograma").getValue().toString();
        long casDoKoncaProgramaLong=Long.parseLong(casDoKoncaProgramaString);

        String casSpremembeEpochString=snapshot.child("CasSpremembe").getValue().toString();
        long casSpremembeEpochLong=Long.parseLong(casSpremembeEpochString);

        LocalDate today = LocalDate.now(ZoneId.of("ECT"));
        long nowSecondsOfEpoch= LocalDateTime.of(today, LocalTime.now()).toEpochSecond(ZoneOffset.ofTotalSeconds(0));

        long konecProgramaEpoch =casSpremembeEpochLong+casDoKoncaProgramaLong;

        long casDoKoncaProgramaRealLong=konecProgramaEpoch-nowSecondsOfEpoch;

        if(casDoKoncaProgramaRealLong<0){
            casDoKoncaProgramaRealLong=0;
        }

        return casDoKoncaProgramaRealLong;
    }

    public String statusBojlerja(DataSnapshot snapshot){
        String casDoKoncaProgramaString=snapshot.child("CasDoKoncaPrograma").getValue().toString();
        String casDoPricetkaGretjaString=snapshot.child("CasDoPricetkaGretja").getValue().toString();
        String casSpremembeString=snapshot.child("CasSpremembe").getValue().toString();

        long casDoKoncaProgramaLong=Long.parseLong(casDoKoncaProgramaString);
        long casDoPricetkaGretjaLong=Long.parseLong(casDoPricetkaGretjaString);
        long casSpremembeLong=Long.parseLong(casSpremembeString);

        LocalDate today = LocalDate.now(ZoneId.of("ECT"));
        long nowSecondsOfEpoch= LocalDateTime.of(today, LocalTime.now()).toEpochSecond(ZoneOffset.ofTotalSeconds(0));

        //če je false bojler ne deluje
        String delovanjeBojlerja;

        if((nowSecondsOfEpoch<=(casSpremembeLong+casDoPricetkaGretjaLong))||(nowSecondsOfEpoch>=(casSpremembeLong+casDoKoncaProgramaLong))){
            delovanjeBojlerja="Bojler ne deluje";
            imgid[2]=R.drawable.not_running_20x20;
        }
        else {
            delovanjeBojlerja="Bojler deluje";
            imgid[2]=R.drawable.running_20x20;
        }

        return delovanjeBojlerja;

    }
}



