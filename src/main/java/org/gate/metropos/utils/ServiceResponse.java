package org.gate.metropos.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ServiceResponse<T> {
    boolean success;
    int code; // just in case
    String message;
    T data;
}
