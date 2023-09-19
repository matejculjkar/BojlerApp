package com.bojlerapp;

import androidx.annotation.NonNull;

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
import java.util.Calendar;
import java.util.Date;

public class Parametri {
    /*VSE TEMPERATURE SO V °C, ČASOVNE NEZNANKE SO V MINUTAH, DATUMI SO V UTC ČASU V MS*/
    int finalT;//v°C
    String endDate;//v formatu HH:MM
    int presentT; /*default vrednost je 20C - če imamo internetno povezavo in vremenska stran
                    deluje izračunamo to temperaturo s pomočjo funkcije getPresentT*/
    public static final float HEATING_FACTOR=3.083333333f; //min/°C

    int heatingTime;//min



    String stringSecondsFromNowToStart;//zapisano v lepi obliki: HH:MM:SS
    long secondsFromNowToStart;

    long secondsFromNowToFinish;

    //TO SO DEFAULT VREDNOSTI TEMPERATUR DO KATERIH MORA BOJLER ZAGRETI VODO;
    //DEJANSKE VREDNOSTI SE NASTAVLJAJO V FIREBASU - FINALT1, FINALT2 in FINALT3
    static int FINALTEMP1OS=32;
    static int FINALTEMP2OS=40;
    static int FINALTEMP2OSLASJE=48;



    public Parametri(int finalT, String endDate) throws InterruptedException {
        this.finalT=finalT;
        this.endDate=endDate;
        this.presentT=getPresentT();




        System.out.println("KONČNA TEMP: "+finalT+" ČAS KO BO VODA PRIPRAVLJENA: "+endDate+" TEMPERATURA STANOVANJA: "+presentT);
        System.out.println("SMS: "+createBasicSMSText());

    }

     static int finalTempZaEnoOsebo() throws InterruptedException {
        return FINALTEMP1OS;//32
    }
    static int finalTempZaDveOsebi(){
        return FINALTEMP2OS;//40
    }
    static int finalTempZaDveOsebiInLasje(){
        return FINALTEMP2OSLASJE;//48
    }




    /*ko je bojler hladen se smatra da bo imel spodaj izračunano T (T okolice) - izračuna se tako,
    da vzamemo max in min dnevno T, in od razlike med njima vzamemo 1/3 in jo dodamo min dnevni T,
     če je dobljena T manjša od 20C, potem vzamemo vrednost 20C, prav tako uzamemo 20C če nimamo
     internetne povezave oz. gre kaj narobe pri dobivanju vremena iz spleta
     PRIMER:jutranja T je 20C, dnevna pa 30C, iz tega predvidevamo, da se bo stanovanje segrelo na
     cca 23C, in to temperaturo nam ta funkcija vrne
     */

    public static int getPresentT() throws InterruptedException {

        int[] dejanskaZacetnaTint = {20};

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    URL vremeLink = new URL("get your own weather api link");
                    BufferedReader in = new BufferedReader(new InputStreamReader(vremeLink.openStream()));

                    //Tukaj dobimo celotno besedilo vremena
                    String inputLine= in.readLine();

                    in.close();

                    int  minTindex=inputLine.indexOf("\"temperature_2m_min\":[");
                    int maxTindex= inputLine.indexOf("\"temperature_2m_max\":[");


                    String minT=inputLine.substring(minTindex+22,minTindex+26);
                    String maxT=inputLine.substring(maxTindex+22,maxTindex+26);

                    double maxTdouble= Double.valueOf(maxT);
                    double minTdouble=Double.valueOf(minT);
                    double razlikaT= (maxTdouble-minTdouble)/3;



                    double dejanskaZacetnaT=razlikaT+minTdouble;

                    dejanskaZacetnaTint[0] =(int) dejanskaZacetnaT;

                    if(dejanskaZacetnaTint[0] <20){
                        dejanskaZacetnaTint[0] =20;
                    }

                    System.out.println("Min Temp je "+minT+" Izračunana začetna T je "+ dejanskaZacetnaTint[0] +" Max Temp je"+maxT);
                    System.out.println("BESEDILO"+inputLine);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        thread.join();

        System.out.println("KONEC JE:"+dejanskaZacetnaTint[0]);

        return dejanskaZacetnaTint[0];

        //TODO:DODAJ KODO IZ FIREBASA
    }



    public void calculateHeatingTime(int presentT){
        heatingTime=Math.round((finalT-presentT)*HEATING_FACTOR);
        System.out.println("Čas segrevanja je"+heatingTime+" min");
    }

    static int calculateHeatingTime (int finalT, int presentT){
        int heatingTime=Math.round((finalT-presentT)*HEATING_FACTOR);
        System.out.println("Čas segrevanja je"+heatingTime+" min");

        return heatingTime;

    }

    public String createBasicSMSText(){
        String basicText="";
        if(finalT!=99&&finalT!=100){
            calculateHeatingTime(presentT);
            secondsFromNowToStart=this.calculateSecondsFromNowToStart(endDate);

            secondsFromNowToFinish=heatingTime*60+secondsFromNowToStart;

            basicText="OFF:"+secondsFromNowToStart+";ON:"+(heatingTime*60+secondsFromNowToStart)+";";
            System.out.println("Čas gretja:"+" "+secondsFromNowToStart);

        }

        else if(finalT==99){
            basicText="OFF:0;ON:99999";
        }
        else if(finalT==100){
            basicText="OFF:0;ON:1";
        }

        return basicText;
    }

    public long calculateSecondsFromNowToStart(String endDate){


        long secondsFromMidnightToEnd=Parametri.getHoursOfStringDate(endDate)*3600+Parametri.getMinutesOfStringDate(endDate)*60;
        long secondsFromMidnightToStart=secondsFromMidnightToEnd-heatingTime*60;

        LocalDate today = LocalDate.now(ZoneId.of("ECT"));

        LocalDateTime todayMidnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);

        long secondsFromMidnightToNow = (LocalDateTime.of(today,LocalTime.now()).toEpochSecond(ZoneOffset.ofTotalSeconds(0))-todayMidnight.toEpochSecond(ZoneOffset.ofTotalSeconds(0)));

        long secondsFromNowToStart= secondsFromMidnightToStart-secondsFromMidnightToNow;


        if(secondsFromNowToStart<0){
            secondsFromNowToStart=secondsFromNowToStart+24*3600;
        }

        System.out.println("Z OGREVANJEM PRIČNI ČEZ:"+secondsFromNowToStart/60+"min");



        //to služi za test todayTimeInSeconds
       /* System.out.println("URA JE: "+todayTimeInSeconds/3600+":"+(todayTimeInSeconds-todayTimeInSeconds/3600*3600)/60+":"+
                ((todayTimeInSeconds-todayTimeInSeconds/3600*3600)-(todayTimeInSeconds-todayTimeInSeconds/3600*3600)/60*60));
*/


        return secondsFromNowToStart;
    }


    //TI DVE FUKNCIJI SLUŽITA ZATO DA DOBIŠ URE IN MINUTE IZ STRINGA KI
    // GA PRIDOBIŠ V EDITTEXTU NA STRANI OD PROGRAMA ZA ČAS PRIPRAVE VODE
    public static int getHoursOfStringDate(String hoursAndMinutes){
        int hourInt=Integer.parseInt(hoursAndMinutes.substring(0,hoursAndMinutes.indexOf(":")));
        return hourInt;
    }
    public static int getMinutesOfStringDate(String hoursAndMinutes){
        int minutesInt=Integer.parseInt(hoursAndMinutes.substring(hoursAndMinutes.indexOf(":")+1,hoursAndMinutes.length()));
        return minutesInt;
    }

    public static long convertEndDateToSecondsOfToday(String endDate){
        long secondsToEndDateFromMidnight=Parametri.getHoursOfStringDate(endDate)*3600+Parametri.getMinutesOfStringDate(endDate)*60;
        return secondsToEndDateFromMidnight;
    }

    public static long getSecondsFromNowToEnd(long secondsToEndDateFromMidnight, long secondsToNowFromMidnight){
        long secondsFromNowToEnd=secondsToEndDateFromMidnight-secondsToNowFromMidnight;
        return secondsFromNowToEnd;
    }
    //Converts seconds to HH:MM:SS format, only for testing
    public static String convertSecondsToString(long seconds){
        String timeFormat= seconds/3600+"h: "+(seconds-seconds/3600*3600)/60+"min: "+
                ((seconds-seconds/3600*3600)-(seconds-seconds/3600*3600)/60*60)+"s";
        return timeFormat;
    }





}
