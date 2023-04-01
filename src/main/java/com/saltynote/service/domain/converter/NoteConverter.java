package com.saltynote.service.domain.converter;

import com.saltynote.service.domain.transfer.NoteDto;
import com.saltynote.service.entity.Note;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoteConverter extends BaseConverter<NoteDto, Note> {

}
