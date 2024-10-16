package com.example.autoalert.model;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {

    @TypeConverter
    public static List<String> fromString(String value) {
        return value == null ? null : Arrays.asList(value.split(","));
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        return list == null ? null : list.stream().collect(Collectors.joining(","));
    }
}
