package com.example.nac02;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class ServiceAlarm extends Service {

    ArrayList<Worker> threads = new ArrayList<>();

    public ServiceAlarm() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i("ServiceAlarme", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ServiceAlarme", "onStartComand");

        int hora = intent.getExtras().getInt("hora");
        int min = intent.getExtras().getInt("min");
        Context context = this;
        Worker w = new Worker(startId, hora, min, context);
        w.start();
        this.threads.add(w);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("ServiceAlarme", "onDestroy");
        super.onDestroy();

        for (Worker t : threads) {
            t.ativo = false;
        }
    }

    class Worker extends Thread{
        private int cont;
        private int _startId;
        private int hora=0;
        private int min=0;
        private Context context;
        public boolean ativo;

        public Worker(int _startId, int hora, int min, Context context) {
            this.cont = 0;
            this._startId = _startId;
            this.hora = hora;
            this.min = min;
            this.context = context;
            this.ativo = true;
        }

        @Override
        public void run() {
            Log.i("ServiceAlarme","Start: " + this._startId);
            //verifica alarmes dentro de um intervalo de 20 minutos
            while (this.ativo && this.cont < 1200){
                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
                this.cont++;
                Calendar cAtual = Calendar.getInstance();
                Calendar cUser = Calendar.getInstance();
                cUser.set(cAtual.get(Calendar.YEAR), cAtual.get(Calendar.MONTH), cAtual.get(Calendar.DAY_OF_MONTH), this.hora, this.min);
//                Log.i("ServiceAlarme","Data: " + cUser.getTime());
                if(cUser.get(Calendar.HOUR_OF_DAY)==cAtual.get(Calendar.HOUR_OF_DAY) && cUser.get(Calendar.MINUTE)==cAtual.get(Calendar.MINUTE)) {
//                    Log.i("ServiceAlarme", "Tá na hora!!");
                    //=========================================================================================
                    //Gerar notificação
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, "appnotification")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Alarme")
                            .setContentText(Integer.toString(hora)+":"+Integer.toString(min))
                            .setPriority(NotificationCompat.PRIORITY_HIGH);
                    //Geração do canal de envio de notificações
                    NotificationChannel canal = new NotificationChannel("appnotification", "Meu Canal", NotificationManager.IMPORTANCE_HIGH);
                    canal.setDescription("Notificação de alarmes");

                    NotificationManager notMag = getSystemService(NotificationManager.class);
                    notMag.createNotificationChannel(canal);

                    NotificationManagerCompat notMagComp = NotificationManagerCompat.from(this.context);
                    notMagComp.notify(1, builder.build());
                    //=========================================================================================
                    this.ativo = false;
                }


            }
            Log.i("ServiceAlarme","Stop: " + this._startId);
        }
    }
}