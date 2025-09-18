package com.hdu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Label implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String experiment_id;

    private String timeStamp;

    private String label;

    private String num;
}
