package com.saltynote.service.domain.converter;

import java.util.List;

public interface BaseConverter<D, E> {
    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDto(List<E> entityList);

    List<E> toEntity(List<D> dtoList);
}