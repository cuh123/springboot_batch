package com.example.demo.batch;

import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.example.demo.db.SmartChoiceEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class MongoInsertTestJobConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final MongoTemplate mongoTemplate;

  private int chunkSize = 500000;

  @Bean
  public Job mongoInsertTestJob() {
    return jobBuilderFactory.get("mongoInsertTestJob").start(mongoInsertTestStep()).build();
  }

  @Bean
  public Step mongoInsertTestStep() {
    return stepBuilderFactory.get("mongoInsertTestStep")
        .<SmartChoiceEntity, SmartChoiceEntity>chunk(chunkSize)
        .reader(mongoItemReader())
        .writer(mongoItemWriter()).build();
  }

  @Bean
  public MongoItemReader<SmartChoiceEntity> mongoItemReader(){
    MongoItemReader<SmartChoiceEntity> mongoItemReader = new MongoItemReader<>();
    mongoItemReader.setTemplate(mongoTemplate);
    mongoItemReader.setCollection("SmartChoiceDummy");
    mongoItemReader.setQuery("{}");
    Map<String, Sort.Direction> sort = new HashMap<>();
    sort.put("_id", Sort.Direction.ASC);
    mongoItemReader.setSort(sort);
    mongoItemReader.setPageSize(500000);
    mongoItemReader.setTargetType(SmartChoiceEntity.class);
    
    return mongoItemReader;
  }
  
  
  @Bean 
  public MongoItemWriter<SmartChoiceEntity> mongoItemWriter(){
    MongoItemWriter<SmartChoiceEntity> mongoItemWriter = new MongoItemWriter<>();
    mongoItemWriter.setTemplate(mongoTemplate);
    mongoItemWriter.setCollection("batchTest");
    return mongoItemWriter;
  }
}
