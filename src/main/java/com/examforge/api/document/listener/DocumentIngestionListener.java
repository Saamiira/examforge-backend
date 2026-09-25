package com.examforge.api.document.listener;

import com.examforge.api.ai.client.EmbeddingClient;
import com.examforge.api.document.entity.Document;
import com.examforge.api.document.entity.DocumentChunk;
import com.examforge.api.document.entity.DocumentStatus;
import com.examforge.api.document.event.DocumentUploadedEvent;
import com.examforge.api.document.repository.DocumentChunkRepository;
import com.examforge.api.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentIngestionListener {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingClient embeddingClient;

    private static final int CHUNK_SIZE = 1000;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDocumentUploadedEvent(DocumentUploadedEvent event) {
        log.info("Starting ingestion for document id: {}", event.getDocumentId());
        
        Document document = documentRepository.findById(event.getDocumentId()).orElse(null);
        if (document == null) return;

        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            List<String> chunks = extractAndChunkPdf(document.getFileUrl());
            if (chunks.isEmpty()) {
                throw new RuntimeException("No text could be extracted from the PDF");
            }

            log.info("Extracted {} chunks. Calling embedding API...", chunks.size());
            List<float[]> embeddings = embeddingClient.embed(chunks);

            List<DocumentChunk> documentChunks = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunkEntity = new DocumentChunk();
                chunkEntity.setDocument(document);
                chunkEntity.setContent(chunks.get(i));
                chunkEntity.setEmbedding(embeddings.get(i));
                chunkEntity.setChunkIndex(i);
                documentChunks.add(chunkEntity);
            }
            documentChunkRepository.saveAll(documentChunks);

            document.setStatus(DocumentStatus.READY);
            documentRepository.save(document);
            log.info("Ingestion completed for document id: {}", document.getId());
            
        } catch (Exception e) {
            log.error("Failed to ingest document id: {}", document.getId(), e);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Unknown error");
            documentRepository.save(document);
        }
    }

    private List<String> extractAndChunkPdf(String filePath) throws IOException {
        List<String> chunks = new ArrayList<>();
        try (PDDocument pdDocument = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(pdDocument);
            
            // Basic chunking strategy: fixed length
            if (fullText != null && !fullText.isBlank()) {
                fullText = fullText.replaceAll("\\s+", " ").trim();
                for (int i = 0; i < fullText.length(); i += CHUNK_SIZE) {
                    chunks.add(fullText.substring(i, Math.min(fullText.length(), i + CHUNK_SIZE)));
                }
            }
        }
        return chunks;
    }
}
