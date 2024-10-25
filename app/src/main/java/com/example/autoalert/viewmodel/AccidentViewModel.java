package com.example.autoalert.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AccidentViewModel extends ViewModel {
    private MutableLiveData<Boolean> accidenteDetectado = new MutableLiveData<>();

    public LiveData<Boolean> getAccidenteDetectado() {
        return accidenteDetectado;
    }

    // Método para notificar que hubo un accidente
    public void notificarAccidente() {
        accidenteDetectado.setValue(true); // Notifica que hubo un accidente
    }

    // Método para resetear el estado del accidente
    public void resetAccidente() {
        accidenteDetectado.setValue(false); // Resetea el estado de accidente
    }
}


