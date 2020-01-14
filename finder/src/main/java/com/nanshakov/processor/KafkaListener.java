package com.nanshakov.processor;

import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.repo.FileUploader;
import com.nanshakov.common.repo.PostMetaRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

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
            = Metrics.counter("process.total", "process", "total");
    private Counter duplicates
            = Metrics.counter("process.duplicates", "process", "duplicates");
    private Counter processingErrors
            = Metrics.counter("process.error", "process", "error");

    @org.springframework.kafka.annotation.KafkaListener(topics = "${spring.kafka.topic}")
    //Реализуем логику двойной проверки
    //1 по url, на случай если воркер получил то что уже есть в метаданных. Это легкая проверка, без скачивания
    //2 по хешу контента
    public void consume(ConsumerRecord<String, PostDto> rawMessage) {
        log.info("=> consumed {}", rawMessage.value());
        String hash = rawMessage.key();
        PostDto post = rawMessage.value();
        //simple hash by url
        if (!postMetaRepository.containsByUrl(hash)) {
            log.info("Downloading...{}", post.getImgUrl());
            try {
                byte[] img = Utils.copyURLToByteArray(post.getImgUrl());
                String contentHash = Utils.calculateHashSha256(img);
                if (!postMetaRepository.containsByContent(contentHash)) {
                    String fname = contentHash + Utils.getExtension(post.getImgUrl());
                    fileUploader.putObject(bucket, fname, img);
                    post.setUrlHash(hash);
                    post.setImg(Utils.copyURLToByteArray(post.getImgUrl()));
                    post.setContentHash(Utils.calculateHashSha256(img));
                    post.setPathToContent(constructUrl(fname));
                    totalProcessed.increment();
                    postMetaRepository.add(post);
                } else {
                    duplicates.increment();
                }
            } catch (Exception e) {
                processingErrors.increment();
                log.error(e);
            }
        } else {
            duplicates.increment();
            log.info("{} found in clickhouse, do nothing", rawMessage.key());
        }

    }

    private String constructUrl(String fname) {
        return endpoint + ":" + port + "/" + bucket + "/" + fname;
    }

}