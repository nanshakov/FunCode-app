package com.nanshakov.processor;

import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.Post;
import com.nanshakov.common.dto.PostWithMeta;
import com.nanshakov.common.repo.PostMetaRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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

    @org.springframework.kafka.annotation.KafkaListener(topics = "${spring.kafka.topic}")
    public void consume(ConsumerRecord<String, Post> rawMessage) {
        log.info("=> consumed {}", rawMessage.value());
        String key = rawMessage.key();
        Post post = rawMessage.value();
        //simple hash by url
        if (!postMetaRepository.contains(rawMessage.key())) {
            log.info("downloading...");
            PostWithMeta p = new PostWithMeta(post);
            try {
                p.setImg(Utils.copyURLToByteArray(post.getUrl()));
                p.setHash(Utils.calculateHashSha256(p.getImg()));
                //TODO go to Clickhous
            } catch (IOException e) {
                log.error(e);
            }
        } else {
            log.trace("{} found in clickhouse, do nothing", rawMessage.key());
        }

    }

}