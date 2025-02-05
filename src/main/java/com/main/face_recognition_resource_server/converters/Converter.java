package com.main.face_recognition_resource_server.converters;

public interface Converter<T, S> {
  T convert(S source);



}
