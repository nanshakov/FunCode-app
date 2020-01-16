package com.nanshakov.processor;

import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.repo.FileUploader;
import com.nanshakov.common.repo.PostMetaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class KafkaListener {

    @Autowired
    private PostMetaRepository postMetaRepository;
    @Autowired
    private FileUploader fileUploader;
    @Value("${s3.endpoint}")
    private String endpoint;
    @Value("${s3.port}")
    private int port;
    @Value("${s3.bucket}")
    private String bucket;

    private Counter totalProcessed
            = Metrics.counter("processing.total", "processing", "total");
    private Counter duplicates
            = Metrics.counter("processing.duplicates", "processing", "duplicates");
    private Counter processingErrors
            = Metrics.counter("processing.error", "processing", "error");

    @org.springframework.kafka.annotation.KafkaListener(topics = "${spring.kafka.topic}")
    //Реализуем логику двойной проверки
    //1 по url, на случай если воркер получил то что уже есть в метаданных. Это легкая проверка, без скачивания
    //2 по хешу контента
    public void consume(ConsumerRecord<String, PostDto> rawMessage) {
        log.trace("=> consumed {}", rawMessage.value());
        String hash = rawMessage.key();
        PostDto post = rawMessage.value();
        //быстрый поиск по хешу url
        if (postMetaRepository.containsByUrl(hash)) {
            duplicates.increment();
            log.trace("{} found in clickhouse, do nothing", rawMessage.key());
            return;
        }
        try {
            log.trace("Downloading...{}", post.getImgUrl());
            byte[] img = Utils.copyUrlToByteArray(post.getImgUrl());
            String contentHash = Utils.calculateHashSha256(img);
            //долгий поиск (за счет загрузки) по хешу кнтента
            if (postMetaRepository.containsByContent(contentHash)) {
                duplicates.increment();
                return;
            }
            String name = contentHash + Utils.getExtension(post.getImgUrl());
            fileUploader.putObject(bucket, name, img);
            post.setUrlHash(hash);
            post.setImg(Utils.copyUrlToByteArray(post.getImgUrl()));
            post.setContentHash(Utils.calculateHashSha256(img));
            post.setPathToContent(constructUrl(name));
            postMetaRepository.add(post);
            totalProcessed.increment();
        } catch (Exception e) {
            processingErrors.increment();
            log.error(e);
        }

    }

    private String constructUrl(String fname) {
        return endpoint + ":" + port + "/" + bucket + "/" + fname;
    }

}