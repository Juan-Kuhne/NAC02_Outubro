package com.example.nac02;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public final String PreferenceKey = "regAlarmes";
    public String[] alarmes = new String[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //recupera alrmes salvos no sharedPreferences
        TextView[] tvAlm = {findViewById(R.id.alm1), findViewById(R.id.alm2), findViewById(R.id.alm3), findViewById(R.id.alm4), findViewById(R.id.alm5)};
        String chave = "alm";
        for (int i=1; i<=5; i++){
            String alm = getPreference(chave+i); //consulta valores salvos
            alarmes[i-1] = alm;
            if (!(alm=="vazio")) { //valor encontrado
                String[] data = alm.split(":"); //separa hora, minuto e estado
                tvAlm[i-1].setText(data[0]+":"+data[1]); //seta hora e minuto do alarme salvo
            }
        }
        for(int i=0; i<=4;i++) {
            Log.i("ServiceAlarme", "i="+i);
            Log.i("ServiceAlarme", alarmes[i]);
        }

        Button btStart = findViewById(R.id.btStart);
        Button btStop = findViewById(R.id.btStop);
        EditText txtHora = findViewById(R.id.txtHora);
        EditText txtMin = findViewById(R.id.txtMin);

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(txtHora.getText().length()==0 || txtMin.getText().length()==0) {
                    Toast.makeText(MainActivity.this, "Hora inválida", Toast.LENGTH_SHORT).show();
                }else {
                    //pega informações inseridas
                    int hora = Integer.parseInt(txtHora.getText().toString());
                    int min = Integer.parseInt((txtMin.getText().toString()));
                    Calendar cAtual = Calendar.getInstance();
                    int hAtual = cAtual.get(Calendar.HOUR_OF_DAY);
                    int mAtual = cAtual.get(Calendar.MINUTE);
                    Calendar cUser = Calendar.getInstance();
                    cUser.set(cAtual.get(Calendar.YEAR), cAtual.get(Calendar.MONTH), cAtual.get(Calendar.DAY_OF_MONTH), hora, min);

                    if(cUser.before(cAtual)) { //valida informações
                        Toast.makeText(MainActivity.this, "Hora inválida", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Dados válidos!", Toast.LENGTH_SHORT).show();
                        //adiciona alarme na tela
                        for(int i=0; i<=4; i++) {
                            if(alarmes[i] == "vazio") { //se não há um alarme registrado, realiza o registro na tela
                                tvAlm[i].setText(hora+":"+min);
                                startMyService(hora, min);
                                addPreferences("alm"+(i+1), hora+":"+min);
                                alarmes[i] = hora+":"+min;
                                Log.i("ServiceAlarme", alarmes[i]);
                                Log.i("ServiceAlarme", "Hora Atual:"+hAtual+":"+mAtual);
                                break;
                            } else {
                                String[] horario = alarmes[i].split(":");
                                if(Integer.parseInt(horario[0])<hAtual) { //caso a hora registrada já passou, é atualizada pelo novo alarme inserido
                                    tvAlm[i].setText(hora+":"+min);
                                    startMyService(hora, min);
                                    addPreferences("alm"+(i+1), hora+":"+min);
                                    alarmes[i] = hora+":"+min;
                                    Log.i("ServiceAlarme", "Alarme:"+horario[0]+":"+horario[1]);
                                    Log.i("ServiceAlarme", "Hora Atual:"+hAtual+":"+mAtual);
                                    break;
                                } else if(Integer.parseInt(horario[0])==hAtual){
                                    if(Integer.parseInt(horario[1])<=mAtual) {
                                        tvAlm[i].setText(hora+":"+min);
                                        startMyService(hora, min);
                                        addPreferences("alm"+(i+1), hora+":"+min);
                                        alarmes[i] = hora+":"+min;
                                        Log.i("ServiceAlarme", "Alarme:"+horario[0]+":"+horario[1]);
                                        Log.i("ServiceAlarme", "Hora Atual:"+hAtual+":"+mAtual);
                                        break;
                                    }
                                }
                            }
                        }
                    }
//                    Log.i("ServiceAlarme", cAtual.getTime().toString());
//                    Log.i("ServiceAlarme", cUser.getTime().toString());
                }
            }
        });

        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMyService();
            }
        });
    }

    public void startMyService(int hora, int min) {
        Intent intent = new Intent(this, ServiceAlarm.class);
        intent.putExtra("hora", hora);
        intent.putExtra("min", min);
        startService(intent);
    }

    public void stopMyService() {
        Intent intent = new Intent(this, ServiceAlarm.class);
        stopService(intent);
    }

    public void addPreferences(String chave, String valor) {
        SharedPreferences sh = getSharedPreferences(PreferenceKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sh.edit();

        ed.remove(chave);
        ed.putString(chave, valor).apply();

    }

    public String getPreference(String chave) {
        SharedPreferences sh = getSharedPreferences(PreferenceKey, Context.MODE_PRIVATE);
        return sh.getString(chave, "vazio");
    }

}