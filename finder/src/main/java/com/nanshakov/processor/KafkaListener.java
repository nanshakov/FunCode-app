package com.nanshakov.processor;

import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.Post;
import com.nanshakov.common.repo.FileUploader;
import com.nanshakov.common.repo.PostMetaRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class KafkaListener {

    //    @Value("${spring.kafka.consumer.group.id}")
//    private String kafkaGroupId;
    @Autowired
    private PostMetaRepository postMetaRepository;
    @Autowired
    private FileUploader fileUploader;
    @Value("${endpoint}")
    private String endpoint;
    @Value("${port}")
    private int port;
    @Value("${bucket}")
    private String bucket;

    @org.springframework.kafka.annotation.KafkaListener(topics = "${spring.kafka.topic}")
    //Реализуем логику двойной проверки
    //1 по url, на случай если воркер получил то что уже есть в метаданных. Это легкая проверка, без скачивания
    //2 по хешу контента
    public void consume(ConsumerRecord<String, Post> rawMessage) {
        log.info("=> consumed {}", rawMessage.value());
        String hash = rawMessage.key();
        Post post = rawMessage.value();
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
                    post.setDateTime(LocalDateTime.now());
                    postMetaRepository.add(post);
                }
            } catch (Exception e) {
                log.error(e);
            }
        } else {
            log.info("{} found in clickhouse, do nothing", rawMessage.key());
        }

    }

    private String constructUrl(String fname) {
        return endpoint + ":" + port + "/" + bucket + "/" + fname;
    }

}