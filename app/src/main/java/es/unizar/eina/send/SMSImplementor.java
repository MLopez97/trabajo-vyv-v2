package es.unizar.eina.send;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by Miguel on 18/12/2017.
 */

public class SMSImplementor implements SendImplementor{

    /** actividad desde la cual se abrira la actividad de gestión de correo */
    private Activity sourceActivity;

    /** Constructor
     * @param source actividad desde la cual se abrira la actividad de gestion de SMS
     */
    public SMSImplementor(Activity source){
        setSourceActivity(source);
    }

    /**  Actualiza la actividad desde la cual se abrira la actividad de gestion de SMS */
    public void setSourceActivity(Activity source) {
        sourceActivity = source;
    }

    /**  Recupera la actividad desde la cual se abrira la actividad de gestion de SMS */
    public Activity getSourceActivity(){
        return sourceActivity;
    }

    /**
     * Implementacion del metodo send utilizando la aplicacion de gestion de SMS de Android
     * Solo se copia el asunto y el cuerpo
     * @param subject asunto
     * @param body cuerpo del mensaje
     */
    public void send (String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("sms_body", subject+": "+body);
        intent.setType("vnd.android-dir/mms-sms");
        getSourceActivity().startActivity(intent);
    }

}
