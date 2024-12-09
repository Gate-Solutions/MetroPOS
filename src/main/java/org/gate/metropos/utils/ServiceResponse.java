package org.gate.metropos.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@ToString
public class ServiceResponse<T> {
    boolean success;
    int code;
    String message;
    T data;
}
