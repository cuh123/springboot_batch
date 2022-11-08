package com.example.demo.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.example.demo.db.SmartChoiceEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableBatchProcessing
public class MongoTestJobParallelConfig {
  public static final String JOB_NAME = "multiThreadPagingBatch";

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final MongoTemplate mongoTemplate;

  private int chunkSize;

  @Value("${chunkSize:50000}")
  public void setChunkSize(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  private int poolSize;

  @Value("${poolSize:4}") // (1)
  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  @Bean(name = JOB_NAME + "taskPool")
  public TaskExecutor executor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); // (2)
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setThreadNamePrefix("multi-thread-");
    executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
    executor.initialize();
    return executor;
  }

  @Bean(name = JOB_NAME)
  public Job job() {
//    return jobBuilderFactory.get(JOB_NAME).start(step()).preventRestart().build();
    return jobBuilderFactory.get(JOB_NAME).start(step()).build();
  }

  @Bean(name = JOB_NAME + "_step")
  @JobScope
  public Step step() {
    return stepBuilderFactory.get(JOB_NAME + "_step")
        .<SmartChoiceEntity, SmartChoiceEntity>chunk(chunkSize).reader(reader(null))
        .writer(writer()).taskExecutor(executor()) // (2)
        .throttleLimit(poolSize) // (3)
        .build();
  }


  @Bean(name = JOB_NAME + "_reader")
  @StepScope
  public MongoItemReader<SmartChoiceEntity> reader(
      @Value("#{jobParameters[createDate]}") String createDate) {
//    Map<String, Object> params = new HashMap<>();
//    params.put("createDate",
//        LocalDate.parse("2019-01-10", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    List<Object> params = new ArrayList<Object>();
    params.add("ReaderVersion=7");

    MongoItemReader<SmartChoiceEntity> mongoItemReader = new MongoItemReader<>();
    mongoItemReader.setTemplate(mongoTemplate);
    mongoItemReader.setCollection("SmartChoiceDummy");
    mongoItemReader.setQuery("{}");
    Map<String, Sort.Direction> sort = new HashMap<>();
    sort.put("_id", Sort.Direction.ASC);
    mongoItemReader.setSort(sort);
    mongoItemReader.setPageSize(50000);
    mongoItemReader.setMaxItemCount(100000);
    mongoItemReader.setTargetType(SmartChoiceEntity.class);
    mongoItemReader.setParameterValues(params);

    return mongoItemReader;
  }

  @Bean(name = JOB_NAME + "_writer")
  @StepScope
  public MongoItemWriter<SmartChoiceEntity> writer() {
    MongoItemWriter<SmartChoiceEntity> mongoItemWriter = new MongoItemWriter<>();
    mongoItemWriter.setTemplate(mongoTemplate);
    mongoItemWriter.setCollection("batchTest");
    return mongoItemWriter;
  }
}
