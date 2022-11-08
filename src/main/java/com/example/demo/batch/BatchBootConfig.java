package com.example.demo.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchBootConfig {

  public final JobBuilderFactory jobBuilderFactory;
  public final StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job processJob() {
    return jobBuilderFactory.get("processJob").start(orderStep1(null)).build();
  }

  @Bean
  @JobScope
  public Step orderStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
    return stepBuilderFactory.get("orderStep1").tasklet((contribution, chunkContext) -> {
      log.info("this is Step1");
      log.info(">>>>>>> requestDate = {} ", requestDate);
      return RepeatStatus.FINISHED;
    }).build();
  }

  @Bean
  public JobExecutionListener listener() {
    return new JobCompletionListener();
  }

}
