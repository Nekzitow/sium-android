package umaya.edu.checador.models;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import umaya.edu.checador.R;

/**
 * Created by OSORIO on 25/10/2016.
 */

public class CustomDialog {

    public void showDialogo(final Context context, int response, final Activity asdf){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.a);
        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        switch (response) {
            case 1:
                imageView.setBackgroundColor(Color.rgb(0,166,90));
                imageView.setImageResource(R.mipmap.check);
                text.setText("A TIEMPO");
                break;
            case 2:
                imageView.setBackgroundColor(Color.rgb(0,115,183));
                imageView.setImageResource(R.mipmap.warning);
                text.setText("RETARDO");
                break;
            case 3:
                imageView.setBackgroundColor(Color.rgb(221,75,57));
                imageView.setImageResource(R.mipmap.error);
                text.setText("FUERA DE TIEMPO");
                break;
            case 6:
                imageView.setBackgroundColor(Color.rgb(221,75,57));
                imageView.setImageResource(R.mipmap.error);
                text.setText("ESTE DISPOSITIVO NO ESTÁ LIGADO CON ESTA CUENTA");
                break;
            case 4:
                imageView.setBackgroundColor(Color.rgb(255,119,1));
                imageView.setImageResource(R.mipmap.doublecheck);
                text.setText("YA REGISTRADO");
                break;
            case 7:
                imageView.setBackgroundColor(Color.rgb(221,75,57));
                imageView.setImageResource(R.mipmap.warning);
                text.setText("SALON NO VÁLIDO");
            default:
                imageView.setBackgroundColor(Color.rgb(221,75,57));
                imageView.setImageResource(R.mipmap.warning);
                text.setText("NO CUENTA CON UN HORARIO ASIGNADO");
                break;
        }
        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                asdf.finish();
            }
        });

        dialog.show();
    }


}
