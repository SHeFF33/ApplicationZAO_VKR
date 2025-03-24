package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.order.DocumentType;
import ru.zaomurom.applicationzao.repositories.DocumentTypeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentTypeService {
    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    public DocumentType save(DocumentType documentType) {
        return documentTypeRepository.save(documentType);
    }

    public List<DocumentType> findAll() {
        return documentTypeRepository.findAll();
    }
    public Optional<DocumentType> findById(Long id) {
        return documentTypeRepository.findById(id);
    }
}
