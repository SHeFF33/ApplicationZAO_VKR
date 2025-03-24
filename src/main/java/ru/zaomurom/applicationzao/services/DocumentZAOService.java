package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.DocumentZAO;
import ru.zaomurom.applicationzao.repositories.DocumentZAORepository;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentZAOService {

    @Autowired
    private DocumentZAORepository documentZAORepository;

    public List<DocumentZAO> findAll() {
        return documentZAORepository.findAll();
    }

    public DocumentZAO save(DocumentZAO documentZAO) {
        return documentZAORepository.save(documentZAO);
    }

    public void deleteById(Long id) {
        documentZAORepository.deleteById(id);
    }

    public Optional<DocumentZAO> findById(Long id) {
        return documentZAORepository.findById(id);
    }
}
