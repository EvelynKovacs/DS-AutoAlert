package com.example.autoalert.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressFetcher {
    private Context context;

    public AddressFetcher(Context context) {
        this.context = context;
    }

    public void fetchAddressFromLocation(Location location, MutableLiveData<String> address) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressString = addresses.get(0).getAddressLine(0);
                    address.postValue(addressString);  // Actualizar la dirección
                } else {
                    address.postValue("Dirección no encontrada");
                }
            } catch (IOException e) {
                e.printStackTrace();
                address.postValue("Error al obtener la dirección");
            }
        }).start();
    }
}
