package umaya.edu.checador.Notifications;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import umaya.edu.checador.R;
import umaya.edu.checador.models.Notificaciones;

/**
 * Created by OSORIO on 29/10/2016.
 */

public class PrincipalAdapter extends RecyclerView.Adapter<PrincipalAdapter.ViewHolder>{
    ArrayList<Notificaciones> notificacionesList = new ArrayList<>();
    public PrincipalAdapter(ArrayList<Notificaciones> notificacionesList){
        this.notificacionesList = notificacionesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_list_notification,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Notificaciones notificaciones = notificacionesList.get(position);
        holder.titulo.setText(notificaciones.getTitulo());
        holder.contenido.setText(notificaciones.getContenido());
        holder.fecha.setText(notificaciones.getFecha());
        holder.extra.setText(notificaciones.getExtra());
    }

    @Override
    public int getItemCount() {
        return notificacionesList.size();
    }

    public void replaceData(ArrayList<Notificaciones> items){
        setList(items);
        notifyDataSetChanged();
    }
    private void setList(ArrayList<Notificaciones> list) {
        this.notificacionesList = list;
    }

    public void addItem(Notificaciones pushMessage) {
        notificacionesList.add(0, pushMessage);
        notifyItemInserted(0);
    }

    public void clear() {
        int size = this.notificacionesList.size();
        this.notificacionesList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fecha;
        public TextView titulo;
        public TextView contenido;
        public TextView extra;
        public ViewHolder(View itemView) {
            super(itemView);
            fecha = (TextView) itemView.findViewById(R.id.fecha);
            titulo = (TextView) itemView.findViewById(R.id.titulo);
            contenido = (TextView) itemView.findViewById(R.id.subtitulo);
            extra = (TextView) itemView.findViewById(R.id.date);
        }
    }
}
