package com.hdu.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HR implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String experiment_id;

    private String timeStamp;

    private Double data;

    private String num;
}
