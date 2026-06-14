package com.banco.banco_api.modules.shared.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
}
