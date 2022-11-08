package com.example.demo.db;

import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "SmartChoice")
@Data
public class SmartChoiceEntity {
  
  @Id
  private String id;
  private Integer num;
  private String Image;
  private String device_nm;
  private String device_info;
  private String high_price;
  private String middle_price;
  private String row_price;

}
