package com.bsejawal.service;

import com.bsejawal.entity.Customer1;
import com.bsejawal.repository.Customer1Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithoutSpringBatch {
    private final Customer1Repository customerRepository;
    private static final String path = "src/main/resources/customers.csv";


    public void loaCSV(){

        Path path = Path.of(WithoutSpringBatch.path);

        try(BufferedReader reader = Files.newBufferedReader(path)){
            String line;
            long lineNumber = 0;
            List<Customer1> customers = new ArrayList<>();
            while((line = reader.readLine()) != null){
                if(lineNumber != 0) {
                    String[] data = line.split(",");
                    Customer1 customer = Customer1.builder()
                            .id(Integer.parseInt(data[0]))
                            .firstName(data[1])
                            .lastName(data[2])
                            .email(data[3])
                            .gender(data[4])
                            .contactNo(data[5])
                            .country(data[6])
                            .dob(data[7])
                            .build();
                    customers.add(customer);
                }
                lineNumber++;
            }
            customerRepository.saveAll(customers);

        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

}
