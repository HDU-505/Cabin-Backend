package com.hdu.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SearchForm implements Serializable {
    private String id;
    private String name;
    private String gender;
    private Integer age;
    private List<Date> date;
}
