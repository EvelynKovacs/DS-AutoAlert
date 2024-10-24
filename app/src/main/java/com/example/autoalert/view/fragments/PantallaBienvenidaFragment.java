package com.example.autoalert.view.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.autoalert.R;
import com.example.autoalert.view.activities.MenuInicioActivity;

public class PantallaBienvenidaFragment extends Fragment {

    public interface OnCompleteListener {
        void onComplete();
    }

    private PantallaBienvenidaFragment.OnCompleteListener listener;

    public void setOnCompleteListener(PantallaBienvenidaFragment.OnCompleteListener listener) {
        this.listener = listener;
    }

    public PantallaBienvenidaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_pantalla_bienvenida, container, false);

//        // Obtener referencia del VideoView
//        VideoView videoBackground = view.findViewById(R.id.videoBackground);
//
//        // Establecer la ruta del video (puede ser un archivo en raw o en assets)
//        Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.video1);
//        videoBackground.setVideoURI(videoUri);
//        videoBackground.setZOrderOnTop(true); // Asegúrate que el VideoView esté detrás del TextView
//
//        // Iniciar el video
//        videoBackground.start();
//
//        // Configurar el listener para que el video se reproduzca en un bucle
//        videoBackground.setOnCompletionListener(mp -> {
//            mp.start(); // Reproduce el video nuevamente al completarse
//        });

        // Obtener referencia del botón de usuario
        TextView btnContinue = view.findViewById(R.id.btnContinue);

        // Configurar el clic en el botón
        btnContinue.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.button_scale);
            v.startAnimation(anim);
            // Iniciar transacción para cambiar a los pasos a seguir
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new PasosASeguirFragment());
            transaction.addToBackStack(null);
            transaction.commit();

            // Marcar que ya no es la primera vez que ingresa
            ((MenuInicioActivity) getActivity()).markFirstTimeCompleted();

            if (listener != null) {
                listener.onComplete();
            }
        });

        return view;
    }
}
